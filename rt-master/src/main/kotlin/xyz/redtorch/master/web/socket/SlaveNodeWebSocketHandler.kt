package xyz.redtorch.master.web.socket

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import xyz.redtorch.common.cache.CacheService
import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.trade.dto.*
import xyz.redtorch.master.service.SlaveNodeService
import xyz.redtorch.master.sync.SlaveNodeTransceiver
import java.util.concurrent.locks.ReentrantLock

@Component
class SlaveNodeWebSocketHandler : AbstractWebSocketHandler(), InitializingBean {

    private val logger = LoggerFactory.getLogger(SlaveNodeWebSocketHandler::class.java)

    @Autowired
    lateinit var slaveNodeService: SlaveNodeService

    @Autowired
    lateinit var cacheService: CacheService

    @Autowired
    private lateinit var tradeClientWebSocketHandler: TradeClientWebSocketHandler

    private val sessionIdToTransceiverMap = HashMap<String, SlaveNodeTransceiver>()
    private val sessionIdToTransceiverMapLock = ReentrantLock()

    private val slaveNodeIdToTransceiverMap = HashMap<String, SlaveNodeTransceiver>()
    private val slaveNodeIdToTransceiverMapLock = ReentrantLock()

    override fun afterPropertiesSet() {

        Thread {

            Thread.currentThread().name = "RT-SlaveNodeWebSocketHandler-UpdateCaches"

            var count = 0
            while (!Thread.currentThread().isInterrupted) {

                try {
                    val transceiverList = getTransceiverList()

                    val tickMap = HashMap<String, Tick>()
                    val contractMap = HashMap<String, Contract>()
                    val accountMap = HashMap<String, Account>()
                    val positionMap = HashMap<String, Position>()
                    val orderMap = HashMap<String, Order>()
                    val tradeMap = HashMap<String, Trade>()

                    val pingStartTimestamp = if (count % 100 == 0) {
                        System.currentTimeMillis()
                    } else {
                        0
                    }

                    for (transceiver in transceiverList) {
                        if (transceiver.isOpen && transceiver.isAuthed) {
                            if (count % 100 == 0) {
                                // 100*200ms=20s Ping一次
                                transceiver.ping(pingStartTimestamp)
                            }
                            tickMap.putAll(transceiver.quoteMirror.tickMap)
                            contractMap.putAll(transceiver.baseMirror.contractMap)
                            accountMap.putAll(transceiver.portfolioMirror.accountMap)
                            positionMap.putAll(transceiver.portfolioMirror.positionMap)
                            orderMap.putAll(transceiver.transactionMirror.orderMap)
                            tradeMap.putAll(transceiver.transactionMirror.tradeMap)
                        }else{
                            if (pingStartTimestamp-transceiver.establishedTimestamp>30_000){
                                logger.error("客户端超时未认证断开,目标sessionId={}", transceiver.id)
                                transceiver.close()
                            }
                        }
                    }

                    cacheService.updateTickMap(tickMap)
                    cacheService.updateAccountMap(accountMap)
                    cacheService.updatePositionMap(positionMap)
                    cacheService.updateContractMap(contractMap)
                    cacheService.updateOrderMap(orderMap)
                    cacheService.updateTradeMap(tradeMap)

                    // 200毫秒同步一次缓存数据
                    Thread.sleep(200)

                    count++
                    // 重置为0
                    if (count == Int.MAX_VALUE) {
                        count = 0
                    }

                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("定时同步缓存线程异常", e)
                }
            }

        }.start()

    }

    private fun getTransceiverList(): ArrayList<SlaveNodeTransceiver> {
        val transceiverList = ArrayList<SlaveNodeTransceiver>()
        sessionIdToTransceiverMapLock.lock()
        try {
            transceiverList.addAll(sessionIdToTransceiverMap.values)
        } finally {
            sessionIdToTransceiverMapLock.unlock()
        }
        return transceiverList
    }

    fun sessionIdToTransceiverMapPut(sessionId: String, slaveNodeTransceiver: SlaveNodeTransceiver) {
        sessionIdToTransceiverMapLock.lock()
        try {
            sessionIdToTransceiverMap[sessionId] = slaveNodeTransceiver
        } finally {
            sessionIdToTransceiverMapLock.unlock()
        }
    }

    fun sessionIdToTransceiverMapRemove(sessionId: String): SlaveNodeTransceiver? {
        sessionIdToTransceiverMapLock.lock()
        try {
            return sessionIdToTransceiverMap.remove(sessionId)
        } finally {
            sessionIdToTransceiverMapLock.unlock()
        }
    }

    fun slaveNodeIdToTransceiverMapPut(slaveNodeId: String, slaveNodeTransceiver: SlaveNodeTransceiver) {
        slaveNodeIdToTransceiverMapLock.lock()
        try {
            slaveNodeIdToTransceiverMap[slaveNodeId] = slaveNodeTransceiver
        } finally {
            slaveNodeIdToTransceiverMapLock.unlock()
        }
    }

    fun slaveNodeIdToTransceiverMapRemove(slaveNodeId: String): SlaveNodeTransceiver? {
        slaveNodeIdToTransceiverMapLock.lock()
        try {
            return slaveNodeIdToTransceiverMap.remove(slaveNodeId)
        } finally {
            slaveNodeIdToTransceiverMapLock.unlock()
        }
    }

    fun slaveNodeIdToTransceiverMapGet(slaveNodeId: String): SlaveNodeTransceiver? {
        slaveNodeIdToTransceiverMapLock.lock()
        try {
            return slaveNodeIdToTransceiverMap[slaveNodeId]
        } finally {
            slaveNodeIdToTransceiverMapLock.unlock()
        }
    }

    fun closeBySlaveNodeId(slaveNodeId: String) {
        val transceiver = slaveNodeIdToTransceiverMapRemove(slaveNodeId)
        if (transceiver != null) {
            transceiver.close()
            sessionIdToTransceiverMapRemove(transceiver.id)
        }
    }

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        // 理论上SessionID是很难重复的，但判断比较安全
        if (sessionIdToTransceiverMap.containsKey(session.id)) {
            logger.error("发现重复sessionId={}", session.id)
            sessionIdToTransceiverMap[session.id]?.close()
        }
        // 对于建立连接的会话,完全初始化镜像收发关系
        val transceiver = SlaveNodeTransceiver(this, session)
        sessionIdToTransceiverMapPut(session.id, transceiver)
    }

    @Throws(Exception::class)
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        sessionIdToTransceiverMap[session.id]?.handleTextMessage(message)
    }

    @Throws(Exception::class)
    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        logger.error("未启用对BinaryMessage的支持,sessionId={}", session.id)
    }

    @Throws(Exception::class)
    override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
        sessionIdToTransceiverMap[session.id]?.handlePoneMessage(message)
    }

    @Throws(Exception::class)
    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.warn("传输异常,尝试关闭,sessionId={}", session.id, exception)
        // 此处必须调用关闭,以防止被动关闭导致部分逻辑未触发
        val transceiver = sessionIdToTransceiverMapRemove(session.id)
        if (transceiver != null) {
            transceiver.close()
            transceiver.slaveNodeId?.let {
                slaveNodeIdToTransceiverMapRemove(it)
            }
        }
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.warn("连接已关闭,sessionId={}", session.id)
        // 此处必须调用关闭,以防止被动关闭导致部分逻辑未触发
        val transceiver = sessionIdToTransceiverMapRemove(session.id)
        if (transceiver != null) {
            transceiver.close()
            transceiver.slaveNodeId?.let {
                slaveNodeIdToTransceiverMapRemove(it)
            }
        }
    }

    fun process(tick: Tick) {
        tradeClientWebSocketHandler.addTick(tick)
    }

    fun process(order: Order) {
        tradeClientWebSocketHandler.addOrder(order)
    }

    fun process(trade: Trade) {
        tradeClientWebSocketHandler.addTrade(trade)
    }

    fun process(notice: Notice) {
        tradeClientWebSocketHandler.addNotice(notice)
    }
}