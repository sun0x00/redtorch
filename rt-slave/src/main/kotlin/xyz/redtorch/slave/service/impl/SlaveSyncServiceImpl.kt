package xyz.redtorch.slave.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.sync.dto.*
import xyz.redtorch.common.sync.enumeration.ActionEnum
import xyz.redtorch.common.trade.dto.*
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.common.web.socket.WebSocketClient
import xyz.redtorch.common.web.socket.WebSocketClientCallBack
import xyz.redtorch.slave.service.GatewayMonitorService
import xyz.redtorch.slave.service.SlaveCacheService
import xyz.redtorch.slave.service.SlaveSyncService
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Service
class SlaveSyncServiceImpl : SlaveSyncService, InitializingBean, WebSocketClientCallBack {

    private val logger = LoggerFactory.getLogger(SlaveSyncServiceImpl::class.java)

    @Value("\${rt.node-id}")
    private lateinit var nodeId: String

    @Value("\${rt.auth-token}")
    private lateinit var authToken: String

    @Value("\${rt.web-socket-url}")
    private lateinit var webSocketUrl: String

    private lateinit var webSocketClient: WebSocketClient

    private var slaveNodeSettingMirror: SlaveNodeSettingMirror? = null

    private var slaveNodeReportMirror: SlaveNodeReportMirror? = null
    private var quoteMirror: QuoteMirror? = null
    private var transactionMirror: TransactionMirror? = null
    private var portfolioMirror: PortfolioMirror? = null
    private var baseMirror: BaseMirror? = null


    private var isAuthed = false

    // 用于提交定单并行处理加速
    private val submitOrderExecutor: ExecutorService = Executors.newCachedThreadPool()

    @Autowired
    private lateinit var slaveCacheService: SlaveCacheService

    @Autowired
    private lateinit var gatewayMonitorService: GatewayMonitorService

    override fun afterPropertiesSet() {
        webSocketClient = WebSocketClient(URI.create(webSocketUrl), nodeId, authToken, this)

        Thread {
            Thread.currentThread().name = "RT-SlaveSyncServiceImpl-SyncMirror"
            var count = 0
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(2000)

                    if (webSocketClient.isConnected) {
                        if (count % 2 == 0) {
                            // 4秒同步一次
                            slaveCacheService.syncBaseMirror()
                        }
                        slaveCacheService.syncPortfolioMirror()
                        slaveCacheService.syncQuoteMirror()
                        slaveCacheService.syncTransactionMirror()

                        if (count % 10 == 0) {
                            // 2000ms*10=20s  Ping一次
                            webSocketClient.ping()
                        }

                    } else {
                        webSocketClient.close()
                        webSocketClient.connect()
                    }

                    count++
                    // 重置为0
                    if (count == Int.MAX_VALUE) {
                        count = 0
                    }

                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("同步服务检查线程异常", e)
                }

            }
        }.start()

    }

    override fun afterConnectionEstablished() {
    }

    override fun handleTextMessage(message: String) {
        val action = JsonUtils.readToObject(message, Action::class.java)

        if (Constant.isDebugEnable) {
            if (action.data != null) {
                // 合约数量过大,只打印Action类型
                if (action.actionEnum == ActionEnum.BaseMirrorPatch) {
                    logger.info("处理ActionEnum={}", action.actionEnum)
                } else {
                    logger.info("处理ActionEnum={},Data={}", action.actionEnum, JsonUtils.mapper.writeValueAsString(JsonUtils.readToJsonNode(action.data!!)))
                }
            } else {
                logger.debug("处理ActionEnum={}", action.actionEnum)
            }
        }

        when (action.actionEnum) {
            ActionEnum.SlaveNodeSettingMirrorPatch -> {
                handleSlaveNodeSettingMirrorPatch(action)
            }
            ActionEnum.InsertOrder -> {
                val insertOrder = JsonUtils.readToObject(action.data!!, InsertOrder::class.java)
                handleInsertOrder(insertOrder)
            }
            ActionEnum.CancelOrder -> {
                val cancelOrder = JsonUtils.readToObject(action.data!!, CancelOrder::class.java)
                handleCancelOrder(cancelOrder)
            }
            else -> {
                logger.error("未能识别的Action={}", message)
            }
        }
    }

    override fun handleBinaryMessage(message: ByteBuffer) {
    }

    override fun handlePongMessage(delay: Long) {
    }

    override fun handleTransportError(exception: Throwable) {
        isAuthed = false
        logger.error("WS传输错误,清空同步镜像")
        slaveNodeReportMirror = null
        quoteMirror = null
        transactionMirror = null
        portfolioMirror = null
        baseMirror = null
    }

    override fun afterConnectionClosed(status: CloseStatus) {
        isAuthed = false
        logger.error("WS传输断开,清空同步镜像")
        slaveNodeSettingMirror = null

    }

    override fun handleAuthFailed() {
    }

    override fun handleAuthSucceeded() {

        slaveNodeReportMirror = SlaveNodeReportMirror()

        quoteMirror = QuoteMirror()
        transactionMirror = TransactionMirror()
        portfolioMirror = PortfolioMirror()
        baseMirror = BaseMirror()

        isAuthed = true

        slaveCacheService.syncBaseMirror()
        slaveCacheService.syncPortfolioMirror()
        slaveCacheService.syncQuoteMirror()
        slaveCacheService.syncTransactionMirror()
    }

    private fun handleInsertOrder(insertOrder: InsertOrder) {
        submitOrderExecutor.execute {
            try {
                val gateway = gatewayMonitorService.getGateway(insertOrder.gatewayId)
                if (gateway == null) {
                    logger.error("发送定单错误,网关不存在,InsertOrder={}", JsonUtils.writeToJsonString(insertOrder))
                } else if (!gateway.isConnected()) {
                    logger.error("发送定单错误,网关已断开,InsertOrder={}", JsonUtils.writeToJsonString(insertOrder))
                } else {
                    val orderId = gateway.submitOrder(insertOrder)
                    if (!orderId.isNullOrBlank()) {
                        logger.info("发送定单成功,orderId={},originOrderId={}", orderId, insertOrder.originOrderId)
                    } else {
                        logger.error("发送定单失败,网关返回空定单Id,originOrderId={}", insertOrder.originOrderId)
                    }
                }
            } catch (e: Exception) {
                logger.error("处理发送定单异常", e)
            }
        }
    }

    private fun handleSlaveNodeSettingMirrorPatch(action: Action) {
        slaveNodeSettingMirror = if (slaveNodeSettingMirror == null) {
            JsonUtils.applyJsonPatch(SlaveNodeSettingMirror(), action.data!!)
        } else {
            JsonUtils.applyJsonPatch(slaveNodeSettingMirror, action.data!!)
        }
        gatewayMonitorService.updateGatewaySetting(slaveNodeSettingMirror!!.gatewaySettingMap)
        gatewayMonitorService.updateSubscribedContract(slaveNodeSettingMirror!!.subscribedMap)
    }

    private fun handleCancelOrder(cancelOrder: CancelOrder) {
        val gateway = gatewayMonitorService.getGateway(cancelOrder.gatewayId)
        if (gateway == null) {
            logger.error("撤销定单错误,网关不存在,CancelOrder={}", JsonUtils.writeToJsonString(cancelOrder))
        } else if (!gateway.isConnected()) {
            logger.error("撤销定单错误,网关已断开,CancelOrder={}", JsonUtils.writeToJsonString(cancelOrder))
        } else {
            val res = gateway.cancelOrder(cancelOrder)
            if (res) {
                logger.info("撤销定单发送成功,CancelOrder={},", JsonUtils.writeToJsonString(cancelOrder))
            } else {
                logger.error("撤销定单发送失败,网关返回错误,CancelOrder={},", JsonUtils.writeToJsonString(cancelOrder))
            }
        }
    }

    override fun updateTransactionMirror(orderMap: MutableMap<String, Order>, tradeMap: MutableMap<String, Trade>) {

        if (isAuthed) {
            val transactionMirror = TransactionMirror()
            transactionMirror.tradeMap.putAll(tradeMap)
            transactionMirror.orderMap.putAll(orderMap)

            val tradesMirrorJsonPatch = JsonUtils.diffAsJsonPatch(this.transactionMirror!!, transactionMirror)

            if (!tradesMirrorJsonPatch.isEmpty) {
                this.transactionMirror = transactionMirror

                val action = Action().apply {
                    actionEnum = ActionEnum.TransactionMirrorPatch
                    data = tradesMirrorJsonPatch.toString()
                }
                webSocketClient.sendTextMessage(JsonUtils.writeToJsonString(action))
            }
        }


    }

    override fun updateQuoteMirror(tickMap: MutableMap<String, Tick>) {

        if (isAuthed) {
            val quoteMirror = QuoteMirror()
            quoteMirror.tickMap.putAll(tickMap)
            quoteMirror.subscribedMap = this.quoteMirror!!.subscribedMap

            val quotesMirrorJsonPatch = JsonUtils.diffAsJsonPatch(this.quoteMirror!!, quoteMirror)

            if (!quotesMirrorJsonPatch.isEmpty) {
                this.quoteMirror = quoteMirror

                val action = Action().apply {
                    actionEnum = ActionEnum.QuoteMirrorPatch
                    data = quotesMirrorJsonPatch.toString()
                }
                webSocketClient.sendTextMessage(JsonUtils.writeToJsonString(action))
            }
        }

    }

    override fun updateBaseMirror(contractMap: MutableMap<String, Contract>) {
        if (isAuthed) {
            val baseMirror = BaseMirror()
            baseMirror.contractMap.putAll(contractMap)
            val baseMirrorJsonPatch = JsonUtils.diffAsJsonPatch(this.baseMirror!!, baseMirror)

            if (!baseMirrorJsonPatch.isEmpty) {
                this.baseMirror = baseMirror

                val action = Action().apply {
                    actionEnum = ActionEnum.BaseMirrorPatch
                    data = baseMirrorJsonPatch.toString()
                }
                webSocketClient.sendTextMessage(JsonUtils.writeToJsonString(action))
            }
        }
    }

    override fun updatePortfolioMirror(accountMap: MutableMap<String, Account>, positionMap: MutableMap<String, Position>) {

        if (isAuthed) {
            val portfolioMirror = PortfolioMirror()
            portfolioMirror.accountMap.putAll(accountMap)
            portfolioMirror.positionMap.putAll(positionMap)

            val baseMirrorJsonPatch = JsonUtils.diffAsJsonPatch(this.portfolioMirror!!, portfolioMirror)

            if (!baseMirrorJsonPatch.isEmpty) {
                this.portfolioMirror = portfolioMirror

                val action = Action().apply {
                    actionEnum = ActionEnum.PortfolioMirrorPatch
                    data = baseMirrorJsonPatch.toString()
                }
                webSocketClient.sendTextMessage(JsonUtils.writeToJsonString(action))
            }
        }
    }

    override fun updateSlaveNodeReportMirror(gatewayStatusList: ArrayList<GatewayStatus>) {

        if (isAuthed) {
            val slaveNodeReportMirror = SlaveNodeReportMirror()
            for (gatewayStatus in gatewayStatusList) {
                slaveNodeReportMirror.gatewayStatusMap[gatewayStatus.gatewayId] = gatewayStatus
            }

            val slaveNodeStatusMirrorJsonPatch = JsonUtils.diffAsJsonPatch(this.slaveNodeReportMirror!!, slaveNodeReportMirror)

            if (!slaveNodeStatusMirrorJsonPatch.isEmpty) {
                this.slaveNodeReportMirror = slaveNodeReportMirror

                val action = Action().apply {
                    actionEnum = ActionEnum.SlaveNodeStatusMirrorPatch
                    data = slaveNodeStatusMirrorJsonPatch.toString()
                }
                webSocketClient.sendTextMessage(JsonUtils.writeToJsonString(action))
            }
        }
    }

    override fun sendNotice(notice: Notice) {
        if (isAuthed) {
            val action = Action().apply {
                actionEnum = ActionEnum.NoticeRtn
                data = JsonUtils.writeToJsonString(notice)
            }
            webSocketClient.sendTextMessage(JsonUtils.writeToJsonString(action))
        } else {
            logger.error("未能发出的通知,Notice={}", JsonUtils.writeToJsonString(notice))
        }
    }

    override fun sendTick(tick: Tick) {
        if (isAuthed) {

            val tickRtnPatch = TickRtnPatch()
            if (quoteMirror!!.tickMap.containsKey(tick.contract.uniformSymbol)) {
                // 传递Patch
                val oldTick = quoteMirror!!.tickMap[tick.contract.uniformSymbol]!!
                tickRtnPatch.uniformSymbol = tick.contract.uniformSymbol
                tickRtnPatch.jsonPath = JsonUtils.diffAsJsonPatch(oldTick, tick)
            } else {
                // 传递完整的Tick
                tickRtnPatch.uniformSymbol = ""
                tickRtnPatch.tick = tick
            }
            // 更新本地Tick
            quoteMirror!!.tickMap[tick.contract.uniformSymbol] = tick


            val action = Action().apply {
                actionEnum = ActionEnum.TickRtnPatch
                data = JsonUtils.writeToJsonString(tickRtnPatch)
            }

            webSocketClient.sendTextMessage(JsonUtils.writeToJsonString(action))
        } else {
            logger.error("未能发出的回报,Tick={}", JsonUtils.writeToJsonString(tick))
        }
    }

    override fun sendTrade(trade: Trade) {
        if (isAuthed) {

            val tradeRtnPatch = TradeRtnPatch()
            if (baseMirror!!.contractMap.containsKey(trade.contract.uniformSymbol)) {
                // 如果合约存在,则传递Patch
                val sourceTrade = Trade()
                sourceTrade.contract = baseMirror!!.contractMap[trade.contract.uniformSymbol]!!

                tradeRtnPatch.uniformSymbol = trade.contract.uniformSymbol
                tradeRtnPatch.jsonPath = JsonUtils.diffAsJsonPatch(sourceTrade, trade)
            } else {
                // 否则传递完整数据
                tradeRtnPatch.uniformSymbol = ""
                tradeRtnPatch.trade = trade
            }

            // 更新本地Trade
            transactionMirror!!.tradeMap[trade.tradeId] = trade

            val action = Action().apply {
                actionEnum = ActionEnum.TradeRtnPatch
                data = JsonUtils.writeToJsonString(tradeRtnPatch)
            }
            webSocketClient.sendTextMessage(JsonUtils.writeToJsonString(action))
        } else {
            logger.error("未能发出的回报,Trade={}", JsonUtils.writeToJsonString(trade))
        }
    }

    override fun sendOrder(order: Order) {
        if (isAuthed) {
            val orderRtnPatch = OrderRtnPatch()
            if (transactionMirror!!.orderMap.containsKey(order.orderId)) {
                // 传递Patch
                val oldOrder = transactionMirror!!.orderMap[order.orderId]!!
                orderRtnPatch.orderId = order.orderId
                orderRtnPatch.jsonPath = JsonUtils.diffAsJsonPatch(oldOrder, order)
            } else {
                // 传递完整的Order
                orderRtnPatch.orderId = ""
                orderRtnPatch.order = order
            }
            // 更新本地Order
            transactionMirror!!.orderMap[order.orderId] = order

            val action = Action().apply {
                actionEnum = ActionEnum.OrderRtnPatch
                data = JsonUtils.writeToJsonString(orderRtnPatch)
            }
            webSocketClient.sendTextMessage(JsonUtils.writeToJsonString(action))
        } else {
            logger.error("未能发出的回报,Trade={}", JsonUtils.writeToJsonString(order))
        }
    }
}