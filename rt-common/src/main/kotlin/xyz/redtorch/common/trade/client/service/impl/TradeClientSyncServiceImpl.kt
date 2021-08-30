package xyz.redtorch.common.trade.client.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.socket.CloseStatus
import xyz.redtorch.common.cache.CacheService
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.sync.dto.*
import xyz.redtorch.common.sync.enumeration.ActionEnum
import xyz.redtorch.common.trade.client.service.TradeClientSyncService
import xyz.redtorch.common.trade.client.service.TradeClientSyncServiceCallBack
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.trade.dto.Order
import xyz.redtorch.common.trade.dto.Tick
import xyz.redtorch.common.trade.dto.Trade
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.common.web.socket.WebSocketClient
import xyz.redtorch.common.web.socket.WebSocketClientCallBack
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

//@Service
class TradeClientSyncServiceImpl : TradeClientSyncService, InitializingBean, WebSocketClientCallBack {

    private val logger = LoggerFactory.getLogger(TradeClientSyncServiceImpl::class.java)

    private var userId: String? = null
    private var authToken: String? = null
    private var isAutoReconnect = false

    @Value("\${rt.web-socket-url}")
    private lateinit var webSocketUrl: String

    private var webSocketClient: WebSocketClient? = null

    private var callBack: TradeClientSyncServiceCallBack? = null

    private var pingStartTimestamp = 0L
    private var delay = 0L

    @Autowired
    private lateinit var cacheService: CacheService

    // 镜像,用于数据同步
    private var baseMirror: BaseMirror = BaseMirror()
    private var portfolioMirror: PortfolioMirror = PortfolioMirror()
    private var transactionMirror: TransactionMirror = TransactionMirror()
    private var quoteMirror: QuoteMirror = QuoteMirror()

    // 记录已经订阅的合约
    private val subscribedMap: MutableMap<String, Contract> = HashMap()
    private val subscribedMapLock = ReentrantLock()

    // 处理报文单线程线程池
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override fun afterPropertiesSet() {
        Thread {
            Thread.currentThread().name = "RT-TradeClientSyncServiceImpl-CheckWebSocketConnectionStatus"
            var count = 0
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(2000)
                    // 判断是否需要自动重连
                    if (isAutoReconnect) {
                        if (webSocketClient == null) {
                            // 如果当前不存在实例则新建
                            connectWebSocketClient()
                        } else {
                            // 如果当前存在的实例则判断是否已连接或正在连接
                            webSocketClient?.let {
                                if (!it.isConnected && !it.isConnecting) {
                                    connectWebSocketClient()

                                }
                            }
                        }
                    }

                    if (count % 10 == 0) {
                        ping()
                    }


                    count++
                    if (count == Int.MAX_VALUE) {
                        count = 0
                    }
                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("WebSocket连接状态检查异常", e)
                }
            }
        }.start()
    }

    override fun setCallBack(callBack: TradeClientSyncServiceCallBack) {
        this.callBack = callBack
    }

    private fun ping() {
        if (webSocketClient?.isConnected == true) {
            if (pingStartTimestamp != 0L) {
                if (System.currentTimeMillis() - pingStartTimestamp > 20 * 1000) {
                    logger.error("Ping超时,主动断开")
                    disconnectWebSocketClient()
                }
            } else {
                pingStartTimestamp = System.currentTimeMillis()
                webSocketClient?.ping()
            }
        }
    }


    override fun auth(userId: String, authToken: String): Boolean {
        isAutoReconnect = false
        this.userId = userId
        this.authToken = authToken
        val res = connectWebSocketClient()
        if (res) {
            isAutoReconnect = true
        }
        return res
    }

    override fun subscribeContract(contract: Contract) {
        val subscribeList = ArrayList<Contract>()
        subscribeList.add(contract)
        subscribeContractList(subscribeList)
    }

    override fun unsubscribeContract(contract: Contract) {
        val unsubscribeList = ArrayList<Contract>()
        unsubscribeList.add(contract)
        unsubscribeContractList(unsubscribeList)
    }

    override fun subscribeContractList(contractList: List<Contract>) {

        val subscribeContractList: MutableList<Contract> = ArrayList()
        subscribedMapLock.lock()
        try {
            for (contract in contractList) {
                // 如果镜像中不存在重新订阅的数据,则加入重新订阅
                if (!quoteMirror.subscribedMap.containsKey(contract.uniformSymbol)) {
                    subscribedMap[contract.uniformSymbol] = contract
                    subscribeContractList.add(contract)
                }
            }
        } finally {
            subscribedMapLock.unlock()
        }

        if (subscribeContractList.isNotEmpty()) {
            val action = Action().apply {
                actionEnum = ActionEnum.Subscribe
                data = JsonUtils.writeToJsonString(contractList)
            }

            // WS客户端存在且已经连接，则发送Action
            webSocketClient.let {
                if (it != null) {
                    if (it.isConnected) {
                        it.sendTextMessage(JsonUtils.writeToJsonString(action))
                    } else {
                        logger.error("订阅错误,WebSocket客户端非已连接")
                    }
                } else {
                    logger.error("订阅错误,WebSocket客户端不存在")
                }
            }
        }
    }

    override fun unsubscribeContractList(contractList: List<Contract>) {
        val unsubscribeContractList: MutableList<Contract> = ArrayList()
        subscribedMapLock.lock()
        try {
            for (contract in contractList) {
                if (subscribedMap.containsKey(contract.uniformSymbol)) {
                    subscribedMap.remove(contract.uniformSymbol)
                    unsubscribeContractList.add(contract)
                }
            }
        } finally {
            subscribedMapLock.unlock()
        }

        if (unsubscribeContractList.isNotEmpty()) {
            val action = Action().apply {
                actionEnum = ActionEnum.Unsubscribe
                data = JsonUtils.writeToJsonString(contractList)
            }

            // WS客户端存在且已经连接，则发送Action
            webSocketClient.let {
                if (it != null) {
                    if (it.isConnected) {
                        it.sendTextMessage(JsonUtils.writeToJsonString(action))
                    } else {
                        logger.error("退订错误,WebSocket客户端非已连接")
                    }
                } else {
                    logger.error("退订错误,WebSocket客户端不存在")
                }
            }
        }
    }

    override fun cancelOrder(cancelOrder: CancelOrder) {
        if (cancelOrder.gatewayId.isBlank()) {
            logger.error("撤销定单失败,gatewayId缺失,CancelOrder={}", JsonUtils.writeToJsonString(cancelOrder))
            return
        }

        if (cancelOrder.orderId.isBlank() && cancelOrder.originOrderId.isBlank()) {
            logger.error("撤销定单失败,orderId和originOrderId不可同时缺失,CancelOrder={}", JsonUtils.writeToJsonString(cancelOrder))
            return
        }

        val action = Action().apply {
            actionEnum = ActionEnum.CancelOrder
            data = JsonUtils.writeToJsonString(cancelOrder)
        }

        // WS客户端存在且已经连接，则发送Action
        webSocketClient.let {
            if (it != null) {
                if (it.isConnected) {
                    it.sendTextMessage(JsonUtils.writeToJsonString(action))
                } else {
                    logger.error("撤销定单失败,WebSocket客户端未连接,CancelOrder={}", JsonUtils.writeToJsonString(cancelOrder))
                }
            } else {
                logger.error("撤销定单失败,WebSocket客户端不存在,CancelOrder={}", JsonUtils.writeToJsonString(cancelOrder))
            }
        }


    }

    override fun submitOrder(insertOrder: InsertOrder) {
        if (insertOrder.contract == null) {
            logger.error("发送定单失败,contract缺失,InsertOrder={}", JsonUtils.writeToJsonString(insertOrder))
            return
        }

        val action = Action().apply {
            actionEnum = ActionEnum.InsertOrder
            data = JsonUtils.writeToJsonString(insertOrder)
        }

        // WS客户端存在且已经连接，则发送Action

        webSocketClient.let {
            if (it != null) {
                if (it.isConnected) {
                    it.sendTextMessage(JsonUtils.writeToJsonString(action))
                } else {
                    logger.error("发送定单失败,WebSocket客户端未连接,InsertOrder={}", JsonUtils.writeToJsonString(insertOrder))
                }
            } else {
                logger.error("发送定单失败,WebSocket客户端不存在,InsertOrder={}", JsonUtils.writeToJsonString(insertOrder))
            }
        }
    }

    override fun getDelay(): Long {
        return delay
    }

    override fun isConnected(): Boolean {
        return webSocketClient?.isConnected == true
    }

    override fun isAutoReconnect(): Boolean {
        return this.isAutoReconnect
    }

    private fun connectWebSocketClient(): Boolean {

        disconnectWebSocketClient()

        if (this.userId.isNullOrBlank() || this.authToken.isNullOrBlank()) {
            return false
        }

        webSocketClient = WebSocketClient(URI.create(webSocketUrl), this.userId!!, this.authToken!!, this)

        val validWebSocketClient = webSocketClient!!
        validWebSocketClient.connect()

        val beginTime = System.currentTimeMillis()
        while (!Thread.currentThread().isInterrupted) {
            try {
                Thread.sleep(100)
                val usedTime = System.currentTimeMillis() - beginTime
                if (validWebSocketClient.isConnected) {
                    logger.info("WebSocket认证成功")
                    return true
                } else if (validWebSocketClient.isConnecting) {
                    if (usedTime > 5 * 1000) {
                        disconnectWebSocketClient()
                        logger.error("WebSocket认证失败,超时")
                        return false
                    }
                } else if (!validWebSocketClient.isConnected && !validWebSocketClient.isConnecting) {
                    logger.error("WebSocket认证失败")
                    return false
                }

            } catch (ie: InterruptedException) {
                break
            } catch (e: Exception) {
                logger.error("WebSocket认证等待发生异常", e)
            }
        }
        return false
    }

    fun disconnectWebSocketClient() {
        webSocketClient?.let {
            if (it.isConnected || it.isConnecting) {
                it.close()
            }
        }
        pingStartTimestamp = 0
        webSocketClient = null
    }

    override fun afterConnectionEstablished() {
        pingStartTimestamp = 0
    }

    override fun handleTextMessage(message: String) {
        // 单线程线程池,用于排队异步执行，防止后续逻辑导致WebSocket阻塞
        singleThreadExecutor.execute {

            Thread.currentThread().name = "RT-TradeClientSyncServiceImpl-HandleAction"

            try {
                // 解析功能
                val action = JsonUtils.mapper.readValue(message, Action::class.java)

                if (Constant.isDebugEnable) {
                    if (action.data != null) {
                        // 合约数量过大,只打印Action类型
                        if (action.actionEnum == ActionEnum.BaseMirrorPatch) {
                            logger.info("处理ActionEnum={}", action.actionEnum)
                        } else {
                            logger.info(
                                "处理ActionEnum={},Data={}",
                                action.actionEnum,
                                JsonUtils.mapper.writeValueAsString(JsonUtils.readToJsonNode(action.data!!))
                            )
                        }
                    } else {
                        logger.debug("处理ActionEnum={}", action.actionEnum)
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
                    ActionEnum.TickRtnPatch -> {
                        handleTickRtnPatch(action)
                    }
                    ActionEnum.NoticeRtn -> {
                        handleNoticeRtn(action)
                    }
                    else -> logger.error("未能识别的Action={}", message)
                }

            } catch (e: Exception) {
                logger.error("处理报文异常", e)
            }

        }
    }

    override fun handleBinaryMessage(message: ByteBuffer) {
    }

    override fun handlePongMessage(delay: Long) {
        this.delay = delay
        this.pingStartTimestamp = 0
    }

    override fun handleTransportError(exception: Throwable) {
    }

    override fun afterConnectionClosed(status: CloseStatus) {
    }

    override fun handleAuthFailed() {
        // 认证错误,取消自动重连
        isAutoReconnect = false

        callBack?.handleAuthFailed()

    }

    override fun handleAuthSucceeded() {
        // 初始化同步镜像
        portfolioMirror = PortfolioMirror()
        transactionMirror = TransactionMirror()
        quoteMirror = QuoteMirror()

        // 清理缓存
        cacheService.updateContractMap(baseMirror.contractMap)
        cacheService.updateAccountMap(portfolioMirror.accountMap)
        cacheService.updatePositionMap(portfolioMirror.positionMap)
        cacheService.updateOrderMap(transactionMirror.orderMap)
        cacheService.updateTradeMap(transactionMirror.tradeMap)

        // 重新订阅合约
        subscribedMapLock.lock()
        try {
            subscribeContractList(ArrayList(subscribedMap.values))
        } finally {
            subscribedMapLock.unlock()
        }
    }


    private fun handleTransactionMirrorPatch(action: Action) {
        try {
            transactionMirror = JsonUtils.applyJsonPatch(transactionMirror, action.data!!)
            cacheService.updateOrderMap(transactionMirror.orderMap)
            cacheService.updateTradeMap(transactionMirror.tradeMap)
        } catch (e: Exception) {
            logger.error("处理TransactionMirrorPatch异常", e)
        }

    }

    private fun handleTradeRtnPatch(action: Action) {
        try {
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

            cacheService.updateTrade(trade)

            try {
                callBack?.handleTradeRtn(trade)
            } catch (e: Exception) {
                logger.error("处理OrderRtnPatch CallBack异常", e)
            }

        } catch (e: Exception) {
            logger.error("处理TradeRtnPatch异常", e)
        }

    }

    private fun handleOrderRtnPatch(action: Action) {

        try {
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

                if (orderRtnPatch.jsonPath!!.isEmpty) {
                    // 如果JsonPatch为空,则认为Order已经通过TransactionMirror更新,直接返回
                    oldOrder
                } else {
                    // 否则应用Patch,返回新的Order
                    JsonUtils.applyJsonPatch(oldOrder, orderRtnPatch.jsonPath!!)
                }
            }


            transactionMirror.orderMap[order.orderId] = order

            cacheService.updateOrder(order)

            try {
                callBack?.handleOrderRtn(order)
            } catch (e: Exception) {
                logger.error("处理OrderRtnPatch CallBack异常", e)
            }
        } catch (e: Exception) {
            logger.error("处理OrderRtnPatch异常", e)
        }


    }

    private fun handleQuoteMirrorPatch(action: Action) {
        try {
            quoteMirror = JsonUtils.applyJsonPatch(quoteMirror, action.data!!)
            cacheService.updateTickMap(quoteMirror.tickMap)
        } catch (e: Exception) {
            logger.error("处理QuoteMirrorPatch异常", e)
        }
    }

    private fun handleTickRtnPatch(action: Action) {
        try {
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
                if (tickRtnPatch.jsonPath!!.isEmpty) {
                    // 如果JsonPatch为空,则认为Tick已经通过QuoteMirror更新,直接返回
                    oldTick
                } else {
                    // 否则应用Patch,返回新的Tick
                    JsonUtils.applyJsonPatch(oldTick, tickRtnPatch.jsonPath!!)
                }
            }

            if (subscribedMap.containsKey(tick.contract.uniformSymbol)) {
                quoteMirror.tickMap[tick.contract.uniformSymbol] = tick

                cacheService.updateTick(tick)

                try {
                    callBack?.handleTickRtn(tick)
                } catch (e: Exception) {
                    logger.error("处理TickRtnPatch CallBack异常", e)
                }
            }
        } catch (e: Exception) {
            logger.error("处理TickRtnPatch异常", e)
        }

    }

    private fun handleNoticeRtn(action: Action) {
        val notice = JsonUtils.readToObject(action.data!!, Notice::class.java)
        try {
            callBack?.handleNoticeRtn(notice)
        } catch (e: Exception) {
            logger.error("处理Notice CallBack异常", e)
        }
    }

    private fun handlePortfolioMirrorPatch(action: Action) {
        try {
            portfolioMirror = JsonUtils.applyJsonPatch(portfolioMirror, action.data!!)
            cacheService.updateAccountMap(portfolioMirror.accountMap)
            cacheService.updatePositionMap(portfolioMirror.positionMap)
        } catch (e: Exception) {
            logger.error("处理PortfolioMirrorPatch异常", e)
        }
    }

    private fun handleBaseMirrorPatch(action: Action) {
        try {
            baseMirror = JsonUtils.applyJsonPatch(baseMirror, action.data!!)
            cacheService.updateContractMap(baseMirror.contractMap)
        } catch (e: Exception) {
            logger.error("处理BaseMirrorPatch异常", e)
        }
    }
}