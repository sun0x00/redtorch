package xyz.redtorch.common.web.socket

import org.slf4j.LoggerFactory
import org.springframework.web.socket.*
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.constant.Constant.KEY_TRUE
import xyz.redtorch.common.sync.dto.Action
import xyz.redtorch.common.sync.dto.Auth
import xyz.redtorch.common.sync.enumeration.ActionEnum
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.common.utils.Lz4Utils
import java.io.IOException
import java.net.URI
import java.nio.ByteBuffer
import java.util.*

class WebSocketClient(private val websocketUri: URI, private val authId: String, private val authToken: String, private val callBack: WebSocketClientCallBack) {

    private val logger = LoggerFactory.getLogger(WebSocketClient::class.java)

    private val clientId: String = UUID.randomUUID().toString()
    private val webSocketClient: StandardWebSocketClient = StandardWebSocketClient()
    private var webSocketHandler: WebSocketHandler
    private var webSocketSession: WebSocketSession? = null

    var isConnected = false
        private set
    var isConnecting = false
        private set

    init {
        // 封装一层处理
        webSocketHandler = object : AbstractWebSocketHandler() {

            // 再次尝试关闭
            fun tryClose(session: WebSocketSession) {
                try {
                    if (session.isOpen) {
                        session.close()
                    }
                } catch (e: Exception) {
                    logger.error("关闭异常,clientId={},uri={}", clientId, websocketUri.toString(), e)
                }
                webSocketSession = null
                isConnecting = false
                isConnected = false
            }

            override fun afterConnectionEstablished(session: WebSocketSession) {
                logger.info("连接已建立,准备发起验证,clientId={},uri={},sessionId={}", clientId, websocketUri.toString(), session.id)
                webSocketSession = ConcurrentWebSocketSessionDecorator(session, 15 * 1000, Integer.MAX_VALUE)
                try {
                    val auth = Auth().apply {
                        id = authId
                        token = authToken
                    }
                    val action = Action().apply {
                        actionEnum = ActionEnum.Auth
                        data = JsonUtils.writeToJsonString(auth)
                    }
                    callBack.afterConnectionEstablished()
                    session.sendMessage(TextMessage(JsonUtils.writeToJsonString(action)))
                } catch (e: Exception) {
                    logger.error("发送验证信息异常,clientId={},uri={}", clientId, websocketUri.toString(), e)
                    tryClose(session)
                }
            }

            override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                try {
                    if (!isConnected) {

                        val action = JsonUtils.readToObject(message.payload, Action::class.java)

                        if (action.data == KEY_TRUE) {
                            logger.info("客户端验证成功,clientId={},uri={}", clientId, websocketUri.toString())
                            isConnected = true
                            isConnecting = false
                            callBack.handleAuthSucceeded()
                            return
                        } else {
                            callBack.handleAuthFailed()
                            logger.error("客户端验证失败,clientId={},uri={}", clientId, websocketUri.toString())
                            isConnected = false
                            isConnecting = false
                            return
                        }
                    }
                    val payload = message.payload
                    // 根据头部字符串判断消息是否被压缩过了,如果压缩则解压
                    if (payload.startsWith(Constant.LZ4FRAME_HEADER)) {
                        val decompressed = Lz4Utils.frameDecompress(payload.slice(Constant.LZ4FRAME_HEADER.length until payload.length))!!
                        callBack.handleTextMessage(decompressed)
                    } else {
                        callBack.handleTextMessage(payload)
                    }
                } catch (e: Exception) {
                    logger.error("处理文本消息数据发生错误,clientId={},uri={},sessionId={}", clientId, websocketUri.toString(), session.id, e)
                    tryClose(session)
                }
            }

            @Throws(Exception::class)
            override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
                callBack.handleBinaryMessage(message.payload)
            }

            @Throws(Exception::class)
            override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
                val pingTimestamp = message.payload.asLongBuffer().get()
                val delay = System.currentTimeMillis() - pingTimestamp
                logger.info("收到Pong回报,clientId={},延迟{}ms", clientId, delay)
                callBack.handlePongMessage(delay)
            }

            @Throws(Exception::class)
            override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
                logger.error("传输错误,clientId={},uri={},sessionId={}", clientId, websocketUri.toString(), session.id, exception)
                tryClose(session)
            }

            @Throws(Exception::class)
            override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
                logger.error("连接已关闭,clientId={},uri={},session={}", clientId, websocketUri.toString(), session.id)
                webSocketSession = null
                isConnected = false
                isConnecting = false
            }
        }
    }

    fun getClientId(): String {
        return clientId
    }

    fun ping(): Long {
        // 将当前时间戳作为Ping的内容,收到Pong后可计算两个时间戳的时间差,即为双向通信延迟
        val pingStartTimestamp = System.currentTimeMillis()
        if (isConnecting || !isConnected || webSocketSession == null) {
            logger.error("发送Ping错误,会话不存在,clientId={},uri={}", clientId, websocketUri.toString())
        } else if (!webSocketSession!!.isOpen) {
            logger.error("发送Ping失败,会话处于关闭状态,clientId={},uri={}", clientId, websocketUri.toString())
        }
        try {
            val byteBuffer = ByteBuffer.allocate(java.lang.Long.BYTES).putLong(pingStartTimestamp).flip()
            val message = PingMessage(byteBuffer)
            webSocketSession!!.sendMessage(message)
        } catch (e: IOException) {
            logger.error("发送Ping异常,clientId={},uri={}", clientId, websocketUri.toString(), e)
        }
        return pingStartTimestamp
    }

    fun connect() {
        try {
            close()
            if (!isConnecting) {
                logger.info("开始连接,clientId={},uri={}", clientId, websocketUri.toString())
                isConnecting = true
                webSocketClient.doHandshake(webSocketHandler, WebSocketHttpHeaders(), websocketUri).get()
            } else {
                logger.warn("拒绝发起新连接,仍在验证中,clientId={},uri={}", clientId, websocketUri.toString())
            }
        } catch (e: Exception) {
            logger.error("发起连接异常,clientId={},uri={}", clientId, websocketUri.toString(), e)
            isConnecting = false
            isConnected = false
        }
    }

    fun close() {
        isConnected = false
        try {
            webSocketSession?.close()
        } catch (e: Exception) {
            logger.error("关闭连接异常,clientId={},uri={}", clientId, websocketUri.toString(), e)
        }
        webSocketSession = null
        isConnecting = false
    }

    @Synchronized
    fun sendTextMessage(data: String): Boolean {
        if (isConnecting || !isConnected || webSocketSession == null) {
            logger.error("发送数据失败,未建立连接,clientId={},uri={}", clientId, websocketUri.toString())
            return false
        }
        if (!webSocketSession!!.isOpen) {
            logger.error("发送数据失败,连接已关闭,clientId={},uri={}", clientId, websocketUri.toString())
            return false
        }
        return try {
            // 对于较长的字符串,采用压缩的形式发送
            if (data.length > 100 * 1024) {
                val compressedData = Lz4Utils.frameCompress(data)
                // 对于压缩的消息,在文本头部加入标记
                webSocketSession!!.sendMessage(TextMessage(Constant.LZ4FRAME_HEADER + compressedData))
            } else {
                webSocketSession!!.sendMessage(TextMessage(data))
            }
            true
        } catch (e: IOException) {
            logger.error("发送数据异常,clientId={},uri={}", clientId, websocketUri.toString(), e)
            close()
            false
        }
    }
}