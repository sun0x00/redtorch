package xyz.redtorch.master.sync

import org.slf4j.LoggerFactory
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.constant.Constant.KEY_FALSE
import xyz.redtorch.common.constant.Constant.KEY_TRUE
import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.common.sync.dto.*
import xyz.redtorch.common.sync.enumeration.ActionEnum
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.trade.dto.Order
import xyz.redtorch.common.trade.dto.Tick
import xyz.redtorch.common.trade.dto.Trade
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.common.utils.Lz4Utils
import xyz.redtorch.master.web.socket.SlaveNodeWebSocketHandler
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class SlaveNodeTransceiver(
    private val slaveNodeWebSocketHandler: SlaveNodeWebSocketHandler,
    session: WebSocketSession
) : ConcurrentWebSocketSessionDecorator(session, 30 * 1000, Integer.MAX_VALUE) {

    companion object {
        private val logger = LoggerFactory.getLogger(SlaveNodeTransceiver::class.java)
    }

    var slaveNodeId: String? = null
        private set

    var isAuthed = false
        private set

    val establishedTimestamp = System.currentTimeMillis()

    private var pingStartTimestamp = 0L
    private var delay = 0L

    var slaveNodeSettingMirror: SlaveNodeSettingMirror = SlaveNodeSettingMirror()
        private set

    var portfolioMirror: PortfolioMirror = PortfolioMirror()
        private set

    var baseMirror: BaseMirror = BaseMirror()
        private set

    var transactionMirror: TransactionMirror = TransactionMirror()
        private set

    var quoteMirror: QuoteMirror = QuoteMirror()
        private set

    var slaveNodeReportMirror: SlaveNodeReportMirror = SlaveNodeReportMirror()
        private set

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    fun handleTextMessage(message: TextMessage) {
        singleThreadExecutor.execute {
            try {
                var payload = message.payload

                // 如果是LZ4Frame压缩的，则先解压
                if (payload.startsWith(Constant.LZ4FRAME_HEADER)) {
                    payload = Lz4Utils.frameDecompress(payload.slice(Constant.LZ4FRAME_HEADER.length until payload.length))!!
                }

                // 解析功能
                val action = JsonUtils.mapper.readValue(payload, Action::class.java)

                // 判断会话是否已经通过认证
                if (!isAuthed) {
                    // 如果未认证.当前应为认证请求
                    if (action.actionEnum == ActionEnum.Auth) {
                        handleAuth(action)
                    } else {
                        // 如果不是认证请求,则关闭会话
                        logger.warn("会话未认证,收到非认证Action,断开,sessionId={}", id)
                        close()
                    }
                } else {
                    if (Constant.isDebugEnable) {
                        if (action.data != null
                            && action.actionEnum != ActionEnum.BaseMirrorPatch // 合约数量过大,只打印Action类型
                        ) {
                            logger.info(
                                "处理Action,sessionId={},slaveNodeId={},ActionEnum={},Data={}",
                                id,
                                slaveNodeId,
                                action.actionEnum,
                                JsonUtils.mapper.writeValueAsString(JsonUtils.readToJsonNode(action.data!!))
                            )
                        } else {
                            logger.info("处理Action,sessionId={},slaveNodeId={},处理ActionEnum={}", id, slaveNodeId, action.actionEnum)
                        }
                    }

                    // 处理其他功能
                    when (action.actionEnum) {
                        ActionEnum.TransactionMirrorPatch -> {
                            handleTransactionMirrorPatch(action)
                        }
                        ActionEnum.QuoteMirrorPatch -> {
                            handleQuoteMirrorPatch(action)
                        }
                        ActionEnum.TradeRtnPatch -> {
                            handleTradeRtnPatch(action)
                        }
                        ActionEnum.OrderRtnPatch -> {
                            handleOrderRtnPatch(action)
                        }
                        ActionEnum.PortfolioMirrorPatch -> {
                            handlePortfolioMirrorPatch(action)
                        }
                        ActionEnum.BaseMirrorPatch -> {
                            handleBaseMirrorPatch(action)
                        }
                        ActionEnum.SlaveNodeStatusMirrorPatch -> {
                            handleSlaveNodeReportMirrorPatch(action)
                        }
                        ActionEnum.TickRtnPatch -> {
                            handleTickRtnPatch(action)
                        }
                        ActionEnum.NoticeRtn -> {
                            handleNoticeRtn(action)
                        }
                        else -> logger.error("未能识别的Action,sessionId={},slaveNodeId={},Action={}", id, slaveNodeId, payload)
                    }

                }
            } catch (e: Exception) {
                logger.error("处理报文异常,sessionId={},slaveNodeId={}", id, slaveNodeId, e)
                close()
            }
        }
    }

    fun ping(pingStartTimestamp: Long) {
        if (isAuthed) {
            if (this.pingStartTimestamp != 0L) {
                if (pingStartTimestamp - this.pingStartTimestamp > 20 * 1000) {
                    logger.error("Ping超时,sessionId={}", id)
                    close()
                }
            } else {
                this.pingStartTimestamp = pingStartTimestamp
                try {
                    val byteBuffer = ByteBuffer.allocate(java.lang.Long.BYTES).putLong(this.pingStartTimestamp).flip()
                    val message = PingMessage(byteBuffer)
                    sendMessage(message)
                } catch (e: IOException) {
                    logger.error("发送Ping异常,sessionId={}", id, e)
                }
            }
        } else {
            logger.error("发送Ping错误,会话未认证,sessionId={}", id)
        }
    }

    fun getDelay(): Long {
        return delay
    }

    fun handlePoneMessage(message: PongMessage) {
        this.pingStartTimestamp = 0L
        val pingStartTimestamp = message.payload.asLongBuffer().get()
        this.delay = System.currentTimeMillis() - pingStartTimestamp
        logger.info("收到Pong,sessionId={},slaveNodeId={},延时{}ms", id, slaveNodeId, delay)
    }

    override fun close() {
        logger.info("关闭会话,sessionId={},slaveNodeId={}", id, slaveNodeId)
        isAuthed = false
        slaveNodeWebSocketHandler.sessionIdToTransceiverMapRemove(id)
        slaveNodeId?.let {
            slaveNodeWebSocketHandler.slaveNodeIdToTransceiverMapRemove(it)
        }


        try {
            if (!singleThreadExecutor.isShutdown) {
                singleThreadExecutor.shutdownNow()
            }
        } catch (e: Exception) {
            logger.error("关闭线程池异常,sessionId={},slaveNodeId={}", id, slaveNodeId, e)
        }

        if (isOpen) {
            try {
                super.close()
            } catch (e: Exception) {
                logger.warn("尝试再次关闭异常,sessionId={},slaveNodeId={}", id, slaveNodeId, e)
            }
        }
    }

    private fun sendTextMessage(data: String) {
        if (isOpen) {
            if (data.length > 100 * 1024) {
                val compressedData = Lz4Utils.frameCompress(data)
                sendMessage(TextMessage(Constant.LZ4FRAME_HEADER + compressedData))
            } else {
                sendMessage(TextMessage(data))
            }
        }
    }

    private fun handleAuth(action: Action) {
        // 解析认证数据
        val auth = JsonUtils.readToObject(action.data!!, Auth::class.java)

        slaveNodeId = auth.id!!

        // 从数据库中查询
        val slaveNode = slaveNodeWebSocketHandler.slaveNodeService.slaveNodeAuth(auth.id!!, auth.token!!)
        if (slaveNode != null) {
            slaveNodeWebSocketHandler.slaveNodeIdToTransceiverMapPut(slaveNodeId!!, this)
            // 认证通过
            isAuthed = true
            // 发送认证请求通过回报
            sendTextMessage(JsonUtils.writeToJsonString(Action().apply {
                actionEnum = ActionEnum.AuthResult
                data = KEY_TRUE
            }))
        } else {
            // 发送认证请求失败回报
            sendTextMessage(JsonUtils.writeToJsonString(Action().apply {
                actionEnum = ActionEnum.AuthResult
                data = KEY_FALSE
            }))
            logger.warn("节点认证失败,关闭会话,sessionId={},authId={}", id, auth.id)
            close()
        }
    }

    private fun handleTransactionMirrorPatch(action: Action) {
        transactionMirror = JsonUtils.applyJsonPatch(transactionMirror, action.data!!)
    }

    private fun handleTradeRtnPatch(action: Action) {
        val tradeRtnPatch = JsonUtils.readToObject(action.data!!, TradeRtnPatch::class.java)

        val trade = if (tradeRtnPatch.uniformSymbol.isNullOrBlank()) {
            tradeRtnPatch.trade!!
        } else {
            val oldTrade = Trade()
            baseMirror.contractMap[tradeRtnPatch.uniformSymbol]?.let {
                oldTrade.contract = it
            }
            JsonUtils.applyJsonPatch(oldTrade, tradeRtnPatch.jsonPath!!)
        }

        transactionMirror.tradeMap[trade.tradeId] = trade

        slaveNodeWebSocketHandler.process(trade)

    }

    private fun handleOrderRtnPatch(action: Action) {
        val orderRtnPatch = JsonUtils.readToObject(action.data!!, OrderRtnPatch::class.java)

        val order = if (orderRtnPatch.orderId.isNullOrBlank()) {
            orderRtnPatch.order!!
        } else {
            val orderId = orderRtnPatch.orderId!!

            val oldOrder = if (transactionMirror.orderMap.containsKey(orderId)) {
                transactionMirror.orderMap[orderId]!!
            } else {
                Order()
            }
            JsonUtils.applyJsonPatch(oldOrder, orderRtnPatch.jsonPath!!)
        }

        transactionMirror.orderMap[order.orderId] = order

        slaveNodeWebSocketHandler.process(order)
    }

    private fun handleQuoteMirrorPatch(action: Action) {
        quoteMirror = JsonUtils.applyJsonPatch(quoteMirror, action.data!!)
    }

    private fun handleTickRtnPatch(action: Action) {
        val tickRtnPatch = JsonUtils.readToObject(action.data!!, TickRtnPatch::class.java)

        val tick = if (tickRtnPatch.uniformSymbol.isNullOrBlank()) {
            tickRtnPatch.tick!!
        } else {
            val uniformSymbol = tickRtnPatch.uniformSymbol!!

            val oldTick = if (quoteMirror.tickMap.containsKey(uniformSymbol)) {
                quoteMirror.tickMap[uniformSymbol]!!
            } else {
                Tick()
            }
            JsonUtils.applyJsonPatch(oldTick, tickRtnPatch.jsonPath!!)
        }

        quoteMirror.tickMap[tick.contract.uniformSymbol] = tick

        slaveNodeWebSocketHandler.process(tick)
    }

    private fun handlePortfolioMirrorPatch(action: Action) {
        portfolioMirror = JsonUtils.applyJsonPatch(portfolioMirror, action.data!!)
    }

    private fun handleBaseMirrorPatch(action: Action) {
        baseMirror = JsonUtils.applyJsonPatch(baseMirror, action.data!!)
    }

    private fun handleSlaveNodeReportMirrorPatch(action: Action) {
        slaveNodeReportMirror = JsonUtils.applyJsonPatch(slaveNodeReportMirror, action.data!!)
    }

    private fun handleNoticeRtn(action: Action) {
        val notice = JsonUtils.readToObject(action.data!!, Notice::class.java)
        slaveNodeWebSocketHandler.process(notice)
    }

    /**
     * 更新镜像数据
     */
    @Synchronized
    fun updateSlaveNodeSettingMirror(subscribedList: List<Contract>, gatewaySettingList: List<GatewaySetting>) {
        val targetSlaveNodeSettingMirror = SlaveNodeSettingMirror()

        for (contract in subscribedList) {
            targetSlaveNodeSettingMirror.subscribedMap[contract.uniformSymbol] = contract
        }
        for (gatewaySetting in gatewaySettingList) {
            targetSlaveNodeSettingMirror.gatewaySettingMap[gatewaySetting.id!!] = gatewaySetting
        }

        val jsonPatch = JsonUtils.diffAsJsonPatch(slaveNodeSettingMirror, targetSlaveNodeSettingMirror)

        if (!jsonPatch.isEmpty) {
            val action = Action().apply {
                actionEnum = ActionEnum.SlaveNodeSettingMirrorPatch
                data = jsonPatch.toString()
            }
            sendTextMessage(JsonUtils.writeToJsonString(action))
            slaveNodeSettingMirror = targetSlaveNodeSettingMirror
        }
    }

    fun cancelOrder(cancelOrder: CancelOrder) {
        val action = Action().apply {
            actionEnum = ActionEnum.CancelOrder
            data = JsonUtils.writeToJsonString(cancelOrder)
        }
        sendTextMessage(JsonUtils.writeToJsonString(action))
    }

    fun submitOrder(insertOrder: InsertOrder) {
        val action = Action().apply {
            actionEnum = ActionEnum.InsertOrder
            data = JsonUtils.writeToJsonString(insertOrder)
        }
        sendTextMessage(JsonUtils.writeToJsonString(action))
    }

}