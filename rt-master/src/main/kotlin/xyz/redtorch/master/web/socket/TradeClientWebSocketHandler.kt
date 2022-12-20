package xyz.redtorch.master.web.socket

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import xyz.redtorch.common.cache.CacheService
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.trade.dto.Order
import xyz.redtorch.common.trade.dto.Tick
import xyz.redtorch.common.trade.dto.Trade
import xyz.redtorch.common.trade.enumeration.ExchangeEnum
import xyz.redtorch.master.gui.bean.UserSessionFXBean
import xyz.redtorch.master.service.SystemService
import xyz.redtorch.master.service.UserService
import xyz.redtorch.master.sync.TradeClientTransceiver
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock


@Component
class TradeClientWebSocketHandler : AbstractWebSocketHandler(), InitializingBean {

    private val logger = LoggerFactory.getLogger(TradeClientWebSocketHandler::class.java)

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var cacheService: CacheService

    @Autowired
    lateinit var systemService: SystemService

    private val orderQueue = LinkedBlockingQueue<Order>()
    private val tradeQueue = LinkedBlockingQueue<Trade>()
    private val tickQueue = LinkedBlockingQueue<Tick>()
    private val noticeQueue = LinkedBlockingQueue<Notice>()

    private val sessionIdToTransceiverMap = HashMap<String, TradeClientTransceiver>()
    private val sessionIdToTransceiverMapLock = ReentrantLock()

    private val userIdToTransceiverMap = HashMap<String, TradeClientTransceiver>()
    private val userIdToTransceiverMapLock = ReentrantLock()

    // 并发处理同步镜像的线程池
    private val syncMirrorExecutor = Executors.newFixedThreadPool(4)

    // 并发处理推送事件的线程池
    private val tickExecutor = Executors.newFixedThreadPool(4)
    private val orderExecutor = Executors.newFixedThreadPool(4)
    private val tradeExecutor = Executors.newFixedThreadPool(4)
    private val noticeExecutor = Executors.newFixedThreadPool(2)

    // 用于提交定单并行处理加速
    val submitOrderExecutor: ExecutorService = Executors.newCachedThreadPool()

    // 订阅关系,Map不可修改,只能一次性替换
    var subscribedMap: Map<String, Contract> = HashMap()
        private set

    // 过滤字典,用于过滤多个网关推送过来的数据,UniformSymbol->Tick
    private val filterTickMap = HashMap<String, Tick>(10000)


    override fun afterPropertiesSet() {

        Thread {

            Thread.currentThread().name = "RT-TradeClientWebSocketHandler-SyncMirror"

            var count = 0
            while (!Thread.currentThread().isInterrupted) {

                try {
                    val accountList = cacheService.getAccountList()
                    val positionList = cacheService.getPositionList()
                    val orderList = cacheService.getOrderList()
                    val tradeList = cacheService.getTradeList()
                    val tickList = cacheService.getTickList()
                    val contractList = cacheService.getContractList()

                    val transceiverList = getTransceiverList()

                    // 此处单独处理订阅合约,可以修复一些transceiver断开后,无法触发更新订阅合约的问题
                    val subscribedMap = HashMap<String, Contract>()

                    val pingStartTimestamp = if (count % 10 == 0) {
                        System.currentTimeMillis()
                    } else {
                        0
                    }

                    val latch = CountDownLatch(transceiverList.size)

                    for (transceiver in transceiverList) {
                        // 更新订阅
                        if (transceiver.isAuthed) {
                            subscribedMap.putAll(transceiver.getSubscribedMap())
                        }
                        syncMirrorExecutor.execute {
                            try {
                                if (transceiver.isAuthed) {
                                    if (count % 10 == 0) {
                                        // 10*2000ms=20s Ping一次
                                        transceiver.ping(pingStartTimestamp)
                                    }
                                    transceiver.updatePortfolioMirror(accountList, positionList)
                                    transceiver.updateTransactionMirror(orderList, tradeList)
                                    transceiver.updateQuoteMirror(tickList)

                                    if (count % 2 == 0) {
                                        // 4s同步一次
                                        transceiver.updateBaseMirror(contractList)
                                    }
                                }else{
                                    if (pingStartTimestamp-transceiver.establishedTimestamp>30_000){
                                        logger.error("客户端超时未认证断开,目标sessionId={}", transceiver.id)
                                        transceiver.close()
                                    }
                                }
                            } catch (e: Exception) {
                                logger.error("同步镜像异常,目标sessionId={}", transceiver.id, e)
                            }
                            latch.countDown()
                        }
                    }

                    this.subscribedMap = subscribedMap

                    latch.await()

                    Thread.sleep(2000)

                    count++
                    // 重置为0
                    if (count == Int.MAX_VALUE) {
                        count = 0
                    }

                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("同步镜像异常", e)
                }
            }

        }.start()


        Thread {

            Thread.currentThread().name = "RT-TradeClientWebSocketHandler-ProcessTickQueue"

            while (!Thread.currentThread().isInterrupted) {
                try {
                    val tick = tickQueue.take()

                    val filteredTick = filterTick(tick)

                    filteredTick?.let {
                        val transceiverList = getTransceiverList()

                        val latch = CountDownLatch(transceiverList.size)
                        for (transceiver in transceiverList) {
                            tickExecutor.execute {
                                try {
                                    transceiver.sendTick(it)
                                } catch (e: Exception) {
                                    logger.error("处理Tick异常,目标sessionId={}", transceiver.id, e)
                                }
                                latch.countDown()
                            }
                        }
                        latch.await()
                    }

                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("处理Tick异常", e)
                }
            }
        }.start()

        Thread {

            Thread.currentThread().name = "RT-TradeClientWebSocketHandler-ProcessTradeQueue"

            while (!Thread.currentThread().isInterrupted) {
                try {
                    val trade = tradeQueue.take()

                    val transceiverList = getTransceiverList()

                    val latch = CountDownLatch(transceiverList.size)
                    for (transceiver in transceiverList) {
                        tradeExecutor.execute {
                            try {
                                transceiver.sendTrade(trade)
                            } catch (e: Exception) {
                                logger.error("处理Trade异常,目标sessionId={}", transceiver.id, e)
                            }
                            latch.countDown()
                        }
                    }
                    latch.await()
                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("处理Trade异常", e)
                }
            }
        }.start()

        Thread {

            Thread.currentThread().name = "RT-TradeClientWebSocketHandler-ProcessOrderQueue"

            while (!Thread.currentThread().isInterrupted) {
                try {
                    val order = orderQueue.take()

                    val transceiverList = getTransceiverList()

                    val latch = CountDownLatch(transceiverList.size)
                    for (transceiver in transceiverList) {
                        orderExecutor.execute {
                            try {
                                transceiver.sendOrder(order)
                            } catch (e: Exception) {
                                logger.error("处理Order异常,目标sessionId={}", transceiver.id, e)
                            }
                            latch.countDown()
                        }
                    }
                    latch.await()
                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("处理Order异常", e)
                }
            }
        }.start()



        Thread {

            Thread.currentThread().name = "RT-TradeClientWebSocketHandler-ProcessNoticeQueue"

            while (!Thread.currentThread().isInterrupted) {
                try {
                    val notice = noticeQueue.take()

                    val transceiverList = getTransceiverList()

                    val latch = CountDownLatch(transceiverList.size)
                    for (transceiver in transceiverList) {
                        noticeExecutor.execute {
                            try {
                                transceiver.sendNotice(notice)
                            } catch (e: Exception) {
                                logger.error("处理Notice异常,目标sessionId={}", transceiver.id, e)
                            }
                            latch.countDown()
                        }
                    }
                    latch.await()
                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("处理Notice异常", e)
                }
            }
        }.start()
    }


    // 获取交易客户端收发器列表
    private fun getTransceiverList(): ArrayList<TradeClientTransceiver> {
        val transceiverList = ArrayList<TradeClientTransceiver>()
        sessionIdToTransceiverMapLock.lock()
        try {
            transceiverList.addAll(sessionIdToTransceiverMap.values)
        } finally {
            sessionIdToTransceiverMapLock.unlock()
        }
        return transceiverList
    }

    fun sessionIdToTransceiverMapPut(sessionId: String, userTransceiver: TradeClientTransceiver) {
        sessionIdToTransceiverMapLock.lock()
        try {
            sessionIdToTransceiverMap[sessionId] = userTransceiver
        } finally {
            sessionIdToTransceiverMapLock.unlock()
        }
    }

    fun sessionIdToTransceiverMapRemove(sessionId: String): TradeClientTransceiver? {
        sessionIdToTransceiverMapLock.lock()
        try {
            return sessionIdToTransceiverMap.remove(sessionId)
        } finally {
            sessionIdToTransceiverMapLock.unlock()
        }
    }

    fun userIdToTransceiverMapPut(userId: String, userTransceiver: TradeClientTransceiver) {
        userIdToTransceiverMapLock.lock()
        try {
            userIdToTransceiverMap[userId] = userTransceiver
        } finally {
            userIdToTransceiverMapLock.unlock()
        }
    }

    fun userIdToTransceiverMapRemove(userId: String): TradeClientTransceiver? {
        userIdToTransceiverMapLock.lock()
        try {
            return userIdToTransceiverMap.remove(userId)
        } finally {
            userIdToTransceiverMapLock.unlock()
        }
    }

    fun userIdToTransceiverMapGet(userId: String): TradeClientTransceiver? {
        userIdToTransceiverMapLock.lock()
        try {
            return userIdToTransceiverMap[userId]
        } finally {
            userIdToTransceiverMapLock.unlock()
        }
    }

    fun closeByUserId(userId: String) {
        val transceiver = userIdToTransceiverMapRemove(userId)
        if (transceiver != null) {
            transceiver.close()
            sessionIdToTransceiverMapRemove(transceiver.id)
        }
    }

    fun closeBySessionId(sessionId: String) {
        val transceiver = sessionIdToTransceiverMapRemove(sessionId)
        if (transceiver != null) {
            transceiver.close()
            transceiver.userId?.let {
                userIdToTransceiverMapRemove(it)
            }
        }
    }

    fun getUserSessionFXBeanList(): List<UserSessionFXBean> {
        val userSessionFXBeanList = ArrayList<UserSessionFXBean>()
        val transceiverList = getTransceiverList()
        for (transceiver in transceiverList) {
            if (transceiver.isAuthed) {
                val sessionId = transceiver.id
                val address = transceiver.remoteAddress.hostName + ":" + transceiver.remoteAddress.port
                val userId = transceiver.userId.toString()
                val delay = transceiver.getDelay().toString() + "ms"

                val diffTime = System.currentTimeMillis() - transceiver.establishedTimestamp
                val periodAsDaysHHmmss = String.format(
                    "%03d Days %02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toDays(diffTime),
                    TimeUnit.MILLISECONDS.toHours(diffTime) % TimeUnit.DAYS.toHours(1),
                    TimeUnit.MILLISECONDS.toMinutes(diffTime) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(diffTime) % TimeUnit.MINUTES.toSeconds(1)
                )

                val userSessionFXBean = UserSessionFXBean(sessionId, address, userId, delay, periodAsDaysHHmmss)

                userSessionFXBeanList.add(userSessionFXBean)
            }
        }
        return userSessionFXBeanList
    }

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        // 理论上SessionID是很难重复的，但判断比较安全
        if (sessionIdToTransceiverMap.containsKey(session.id)) {
            logger.error("发现重复sessionId={}", session.id)
            sessionIdToTransceiverMap[session.id]?.close()
        }
        // 对于建立连接的会话,完全初始化镜像收发关系
        val transceiver = TradeClientTransceiver(this, session)
        sessionIdToTransceiverMapPut(session.id, transceiver)
    }

    @Throws(Exception::class)
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        sessionIdToTransceiverMap[session.id]?.handleTextMessage(message)
    }

    @Throws(Exception::class)
    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        logger.warn("未启用对BinaryMessage的支持,sessionId={}", session.id)
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
            transceiver.userId?.let {
                userIdToTransceiverMapRemove(it)
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
            transceiver.userId?.let {
                userIdToTransceiverMapRemove(it)
            }
        }
    }

    fun addTick(tick: Tick) {
        tickQueue.add(tick)
    }

    fun addOrder(order: Order) {
        orderQueue.add(order)
    }

    fun addTrade(trade: Trade) {
        tradeQueue.add(trade)
    }

    fun addNotice(notice: Notice) {
        noticeQueue.add(notice)
    }

    fun updateSubscribedMap() {
        val transceiverList = getTransceiverList()

        val subscribedMap = HashMap<String, Contract>()

        for (transceiver in transceiverList) {
            if (transceiver.isAuthed) {
                subscribedMap.putAll(transceiver.getSubscribedMap())
            }
        }
        this.subscribedMap = subscribedMap

        // 立即将配置同步到子节点
        systemService.syncSlaveNodeSettingMirror()
    }

    private fun filterTick(tick: Tick): Tick? {
        if (Constant.isDebugEnable) {
            // DEBUG状态下不过滤数据
            filterTickMap[tick.contract.uniformSymbol] = tick
            return tick
        }

        // 过滤字典中如果存在当前合约的Tick,则进入过滤逻辑
        filterTickMap[tick.contract.uniformSymbol].let {
            if (it != null) {
                if (tick.contract.exchange == ExchangeEnum.CZCE) {
                    // 郑商所行情没有毫秒时间戳,不同期货公司获取到的行情可能不一样,因此单独过滤

                    // 若果交易日不相同
                    if (tick.tradingDay != it.tradingDay) {
                        // 如果交易日增大,则认为是有效数据
                        if (tick.tradingDay > it.tradingDay) {
                            filterTickMap[tick.contract.uniformSymbol] = tick
                            return tick
                        }
                    } else {
                        // 如果成交量增加,或者成交量不变但是盘口变化,则认为是有效数据
                        if (tick.volume > it.volume || //
                            (tick.volume == it.volume //
                                    && (tick.askVolumeMap["1"] != it.askVolumeMap["1"] //
                                    || tick.bidVolumeMap["1"] != it.bidVolumeMap["1"] //
                                    || tick.askPriceMap["1"] != it.askPriceMap["1"] //
                                    || tick.bidPriceMap["1"] != it.bidPriceMap["1"])) //
                        ) {
                            tick.volumeDelta = tick.volume - it.volume
                            tick.turnoverDelta = tick.turnover - it.volume
                            tick.openInterestDelta = tick.openInterestDelta - it.openInterestDelta

                            filterTickMap[tick.contract.uniformSymbol] = tick
                            return tick
                        }
                    }
                } else if (tick.actionTimestamp > it.actionTimestamp) {
                    // 其他交易所对比时间戳,如果时间戳增长,则为有效数据

                    filterTickMap[tick.contract.uniformSymbol] = tick
                    return tick
                }
            } else {
                // 如果过滤字典中不存在,则默认为有效数据
                filterTickMap[tick.contract.uniformSymbol] = tick
                return tick
            }
        }
        return null
    }

}