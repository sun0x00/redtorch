package xyz.redtorch.gateway.ctp.x64v6v6v1p1cpv

import org.slf4j.LoggerFactory
import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.sync.dto.CancelOrder
import xyz.redtorch.common.sync.dto.InsertOrder
import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.sync.enumeration.InfoLevelEnum
import xyz.redtorch.common.trade.dto.*
import xyz.redtorch.common.trade.enumeration.*
import xyz.redtorch.common.utils.CommonUtils
import xyz.redtorch.gateway.ctp.x64v6v6v1p1cpv.api.*
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList

class TdSpi internal constructor(private val ctpGatewayImpl: CtpGatewayImpl) : CThostFtdcTraderSpi() {
    companion object {
        private val logger = LoggerFactory.getLogger(TdSpi::class.java)
    }

    private val tdHost = ctpGatewayImpl.gatewayAdapterCtpSetting.tdHost!!
    private val tdPort = ctpGatewayImpl.gatewayAdapterCtpSetting.tdPort!!
    private val brokerId = ctpGatewayImpl.gatewayAdapterCtpSetting.brokerId!!
    private val userId = ctpGatewayImpl.gatewayAdapterCtpSetting.userId!!
    private val password = ctpGatewayImpl.gatewayAdapterCtpSetting.password!!
    private val appId = ctpGatewayImpl.gatewayAdapterCtpSetting.appId!!
    private val authCode = ctpGatewayImpl.gatewayAdapterCtpSetting.authCode!!
    private val userProductInfo = ctpGatewayImpl.gatewayAdapterCtpSetting.userProductInfo!!
    private val logInfo = ctpGatewayImpl.logInfo
    private val gatewayId = ctpGatewayImpl.gatewayId
    private val eventService = ctpGatewayImpl.getEventService()

    private var investorName = ""
    private var positionMap = HashMap<String, Position>()
    private val orderIdToAdapterOrderIdMap = ConcurrentHashMap<String, String>(50000)
    private val orderIdToOrderRefMap = ConcurrentHashMap<String, String>(50000)
    private val orderIdToOriginalOrderIdMap = HashMap<String, String>()
    private val originalOrderIdToOrderIdMap = HashMap<String, String>()
    private val exchangeIdAndOrderSysIdToOrderIdMap = ConcurrentHashMap<String, String>(50000)
    private val orderIdToInsertOrderMap = HashMap<String, InsertOrder>()
    private val orderIdToOrderMap = ConcurrentHashMap<String, Order>(50000)
    private val insertOrderLock: Lock = ReentrantLock()
    private var intervalQueryThread: Thread? = null
    private var cThostFtdcTraderApi: CThostFtdcTraderApi? = null
    private var connectionStatus = ConnectionStatusEnum.Disconnected // 避免重复调用
    private var loginStatus = false // 登陆状态

    private var instrumentQueried = false
    private var investorNameQueried = false
    private val random = Random()
    private val reqId = AtomicInteger(random.nextInt(1800) % (1800 - 200 + 1) + 200) // 操作请求编

    var tradingDay: String? = null
        private set

    @Volatile
    private var orderRef = random.nextInt(1800) % (1800 - 200 + 1) + 200 // 订单编号
    private var loginFailed = false // 是否已经使用错误的信息尝试登录过
    private var frontId = 0 // 前置机编号
    private var sessionId = 0 // 会话编号

    private var orderCacheList = LinkedList<Order>() // 登录起始阶段缓存Order
    private var tradeCacheList = LinkedList<Trade>() // 登录起始阶段缓存Trade


    private fun startIntervalQuery() {
        if (intervalQueryThread != null) {
            logger.error("{}定时查询线程已存在,首先终止", logInfo)
            stopQuery()
        }
        intervalQueryThread = Thread {
            Thread.currentThread().name = "CTP Gateway Interval Query Thread, $gatewayId ${System.currentTimeMillis()}"
            while (!Thread.currentThread().isInterrupted) {
                try {
                    if (cThostFtdcTraderApi == null) {
                        logger.error("{}定时查询线程检测到API实例不存在,退出", logInfo)
                        break
                    }
                    if (loginStatus) {
                        queryAccount()
                        Thread.sleep(1250)
                        queryPosition()
                        Thread.sleep(1250)
                    } else {
                        logger.warn("{}尚未登录,跳过查询", logInfo)
                    }
                } catch (e: InterruptedException) {
                    logger.warn("{}定时查询线程睡眠时检测到中断,退出线程", logInfo)
                    break
                } catch (e: Exception) {
                    logger.error("{}定时查询线程发生异常", logInfo, e)
                }
            }
        }
        intervalQueryThread!!.start()
    }

    private fun stopQuery() {
        try {
            if (intervalQueryThread != null && !intervalQueryThread!!.isInterrupted) {
                intervalQueryThread!!.interrupt()
                intervalQueryThread = null
            }
        } catch (e: Exception) {
            logger.error(logInfo + "停止线程发生异常", e)
        }
    }

    fun connect() {
        if (isConnected || connectionStatus == ConnectionStatusEnum.Connecting) {
            logger.warn("{}交易接口已经连接或正在连接，不再重复连接", logInfo)
            return
        }
        if (connectionStatus == ConnectionStatusEnum.Connected) {
            reqAuth()
            return
        }
        connectionStatus = ConnectionStatusEnum.Connecting
        loginStatus = false
        instrumentQueried = false
        investorNameQueried = false
        if (cThostFtdcTraderApi != null) {
            try {
                val cThostFtdcTraderApiForRelease: CThostFtdcTraderApi = cThostFtdcTraderApi!!
                cThostFtdcTraderApi = null
                cThostFtdcTraderApiForRelease.RegisterSpi(null)
                Thread {
                    Thread.currentThread().name = "GatewayId $gatewayId TD API Release Thread, Time ${System.currentTimeMillis()}"
                    try {
                        logger.warn("交易接口异步释放启动！")
                        cThostFtdcTraderApiForRelease.Release()
                        logger.warn("交易接口异步释放完成！")
                    } catch (t: Throwable) {
                        logger.error("交易接口异步释放发生异常！", t)
                    }
                }.start()
                Thread.sleep(100)
            } catch (t: Throwable) {
                logger.warn("{}交易接口连接前释放异常", logInfo, t)
            }
        }
        logger.warn("{}交易接口实例初始化", logInfo)
        val envTmpDir = System.getProperty("java.io.tmpdir")
        val separator = File.separator
        val tempFilePath =
            "${envTmpDir}${separator}xyz${separator}redtorch${separator}gateway${separator}ctp${separator}jctpv6v6v1p1cpx64api${separator}CTP_FLOW_TEMP${separator}TD_${gatewayId}"
        val tempFile = File(tempFilePath)
        if (!tempFile.parentFile.exists()) {
            try {
                CommonUtils.forceMkdirParent(tempFile)
                logger.info("{}交易接口创建临时文件夹 {}", logInfo, tempFile.parentFile.absolutePath)
            } catch (e: IOException) {
                logger.error("{}交易接口创建临时文件夹失败{}", logInfo, tempFile.parentFile.absolutePath, e)
            }
        }
        logger.warn("{}交易接口使用临时文件夹{}", logInfo, tempFile.parentFile.absolutePath)
        try {
            cThostFtdcTraderApi = CThostFtdcTraderApi.CreateFtdcTraderApi(tempFile.absolutePath)
            cThostFtdcTraderApi!!.RegisterSpi(this)
            cThostFtdcTraderApi!!.RegisterFront("tcp://$tdHost:$tdPort")
            cThostFtdcTraderApi!!.Init()
        } catch (t: Throwable) {
            logger.error("{}交易接口连接异常", logInfo, t)
        }
        Thread {
            try {
                Thread.sleep((60 * 1000).toLong())
                if (!(isConnected && investorNameQueried && instrumentQueried)) {
                    logger.error("{}交易接口连接超时,尝试断开", logInfo)
                    ctpGatewayImpl.disconnect()
                }
            } catch (t: Throwable) {
                logger.error("{}交易接口处理连接超时线程异常", logInfo, t)
            }
        }.start()
    }

    fun disconnect() {
        try {
            stopQuery()
            if (cThostFtdcTraderApi != null && connectionStatus != ConnectionStatusEnum.Disconnecting) {
                logger.warn("{}交易接口实例开始关闭并释放", logInfo)
                loginStatus = false
                instrumentQueried = false
                investorNameQueried = false
                connectionStatus = ConnectionStatusEnum.Disconnecting
                try {
                    if (cThostFtdcTraderApi != null) {
                        val cThostFtdcTraderApiForRelease: CThostFtdcTraderApi = cThostFtdcTraderApi!!
                        cThostFtdcTraderApi = null
                        cThostFtdcTraderApiForRelease.RegisterSpi(null)
                        Thread {
                            Thread.currentThread().name = "GatewayId $gatewayId TD API Release Thread,Start Time ${System.currentTimeMillis()}"
                            try {
                                logger.warn("交易接口异步释放启动！")
                                cThostFtdcTraderApiForRelease.Release()
                                logger.warn("交易接口异步释放完成！")
                            } catch (t: Throwable) {
                                logger.error("交易接口异步释放发生异常！", t)
                            }
                        }.start()
                    }
                    Thread.sleep(100)
                } catch (t: Throwable) {
                    logger.error("{}交易接口实例关闭并释放异常", logInfo, t)
                }
                connectionStatus = ConnectionStatusEnum.Disconnected
                logger.warn("{}交易接口实例关闭并异步释放", logInfo)
            } else {
                logger.warn("{}交易接口实例不存在或正在关闭释放,无需操作", logInfo)
            }
        } catch (t: Throwable) {
            logger.error("{}交易接口实例关闭并释放异常", logInfo, t)
        }
    }

    val isConnected: Boolean
        get() = connectionStatus == ConnectionStatusEnum.Connected && loginStatus

    private fun queryAccount() {
        if (cThostFtdcTraderApi == null) {
            logger.warn("{}交易接口尚未初始化,无法查询账户", logInfo)
            return
        }
        if (!loginStatus) {
            logger.warn("{}交易接口尚未登录,无法查询账户", logInfo)
            return
        }
        if (!instrumentQueried) {
            logger.warn("{}交易接口尚未获取到合约信息,无法查询账户", logInfo)
            return
        }
        if (!investorNameQueried) {
            logger.warn("{}交易接口尚未获取到投资者姓名,无法查询账户", logInfo)
            return
        }
        try {
            cThostFtdcTraderApi!!.ReqQryTradingAccount(CThostFtdcQryTradingAccountField(), reqId.incrementAndGet())
        } catch (t: Throwable) {
            logger.error("{}交易接口查询账户异常", logInfo, t)
        }
    }

    private fun queryPosition() {
        if (cThostFtdcTraderApi == null) {
            logger.warn("{}交易接口尚未初始化,无法查询持仓", logInfo)
            return
        }
        if (!loginStatus) {
            logger.warn("{}交易接口尚未登录,无法查询持仓", logInfo)
            return
        }
        if (!instrumentQueried) {
            logger.warn("{}交易接口尚未获取到合约信息,无法查询持仓", logInfo)
            return
        }
        if (!investorNameQueried) {
            logger.warn("{}交易接口尚未获取到投资者姓名,无法查询持仓", logInfo)
            return
        }
        try {
            val cThostFtdcQryInvestorPositionField = CThostFtdcQryInvestorPositionField()
            cThostFtdcQryInvestorPositionField.brokerID = brokerId
            cThostFtdcQryInvestorPositionField.investorID = userId
            cThostFtdcTraderApi!!.ReqQryInvestorPosition(cThostFtdcQryInvestorPositionField, reqId.incrementAndGet())
        } catch (t: Throwable) {
            logger.error("{}交易接口查询持仓异常", logInfo, t)
        }
    }

    fun submitOrder(insertOrder: InsertOrder): String? {
        if (cThostFtdcTraderApi == null) {
            logger.warn("{}交易接口尚未初始化,无法发单", logInfo)
            return null
        }
        if (!loginStatus) {
            logger.warn("{}交易接口尚未登录,无法发单", logInfo)
            return null
        }
        val cThostFtdcInputOrderField = CThostFtdcInputOrderField()
        cThostFtdcInputOrderField.instrumentID = insertOrder.contract!!.symbol
        cThostFtdcInputOrderField.limitPrice = insertOrder.price
        cThostFtdcInputOrderField.volumeTotalOriginal = insertOrder.volume
        cThostFtdcInputOrderField.orderPriceType = CtpConstant.orderPriceTypeMap.getOrDefault(insertOrder.orderPriceType, '\u0000')
        cThostFtdcInputOrderField.direction = CtpConstant.directionMap.getOrDefault(insertOrder.direction, '\u0000')
        cThostFtdcInputOrderField.combOffsetFlag = CtpConstant.offsetFlagMap.getOrDefault(insertOrder.offsetFlag, '\u0000').toString()
        cThostFtdcInputOrderField.investorID = userId
        cThostFtdcInputOrderField.userID = userId
        cThostFtdcInputOrderField.brokerID = brokerId
        cThostFtdcInputOrderField.exchangeID = CtpConstant.exchangeMap.getOrDefault(insertOrder.contract!!.exchange, "")
        cThostFtdcInputOrderField.combHedgeFlag = CtpConstant.hedgeFlagMap[insertOrder.hedgeFlag].toString()
        cThostFtdcInputOrderField.contingentCondition = CtpConstant.contingentConditionMap[insertOrder.contingentCondition]!!
        cThostFtdcInputOrderField.forceCloseReason = CtpConstant.forceCloseReasonMap[insertOrder.forceCloseReason]!!
        cThostFtdcInputOrderField.isAutoSuspend = insertOrder.autoSuspend
        cThostFtdcInputOrderField.isSwapOrder = insertOrder.swapOrder
        cThostFtdcInputOrderField.minVolume = insertOrder.minVolume
        cThostFtdcInputOrderField.timeCondition = CtpConstant.timeConditionMap.getOrDefault(insertOrder.timeCondition, '\u0000')
        cThostFtdcInputOrderField.volumeCondition = CtpConstant.volumeConditionMap.getOrDefault(insertOrder.volumeCondition, '\u0000')
        cThostFtdcInputOrderField.stopPrice = insertOrder.stopPrice

        // 部分多线程场景下,如果不加锁,可能会导致自增乱序,因此导致发单失败
        insertOrderLock.lock()
        return try {
            val orderRef = ++orderRef
            val adapterOrderId = frontId.toString() + "_" + sessionId + "_" + orderRef

            val orderId = "$gatewayId@$adapterOrderId"
            if (insertOrder.originOrderId.isNotBlank()) {
                orderIdToOriginalOrderIdMap[orderId] = insertOrder.originOrderId
                originalOrderIdToOrderIdMap[insertOrder.originOrderId] = orderId
            }
            orderIdToInsertOrderMap[orderId] = insertOrder
            orderIdToAdapterOrderIdMap[orderId] = adapterOrderId
            orderIdToOrderRefMap[orderId] = orderRef.toString().padStart(20)
            cThostFtdcInputOrderField.orderRef = orderRef.toString()
            logger.error(
                "{}交易接口发单记录->{InstrumentID:{},LimitPrice:{}, VolumeTotalOriginal:{}, OrderPriceType:{}, Direction:{}, CombOffsetFlag:{}, OrderRef:{}, InvestorID:{}, UsrID:{}, BrokerID:{}, ExchangeID:{},CombHedgeFlag:{},ContingentCondition:{},ForceCloseReason:{},IsAutoSuspend:{},IsSwapOrder:{},MinVolume:{},TimeCondition:{},VolumeCondition:{},StopPrice:{}}",  //
                logInfo,  //
                cThostFtdcInputOrderField.instrumentID,  //
                cThostFtdcInputOrderField.limitPrice,  //
                cThostFtdcInputOrderField.volumeTotalOriginal,  //
                cThostFtdcInputOrderField.orderPriceType,  //
                cThostFtdcInputOrderField.direction,  //
                cThostFtdcInputOrderField.combOffsetFlag,  //
                cThostFtdcInputOrderField.orderRef,  //
                cThostFtdcInputOrderField.investorID,  //
                cThostFtdcInputOrderField.userID,  //
                cThostFtdcInputOrderField.brokerID,  //
                cThostFtdcInputOrderField.exchangeID,  //
                cThostFtdcInputOrderField.combHedgeFlag,  //
                cThostFtdcInputOrderField.contingentCondition,  //
                cThostFtdcInputOrderField.forceCloseReason,  //
                cThostFtdcInputOrderField.isAutoSuspend,  //
                cThostFtdcInputOrderField.isSwapOrder,  //
                cThostFtdcInputOrderField.minVolume,  //
                cThostFtdcInputOrderField.timeCondition,  //
                cThostFtdcInputOrderField.volumeCondition,  //
                cThostFtdcInputOrderField.stopPrice
            )
            cThostFtdcTraderApi!!.ReqOrderInsert(cThostFtdcInputOrderField, reqId.incrementAndGet())
            orderId
        } catch (t: Throwable) {
            logger.error("{}交易接口发单错误", logInfo, t)
            null
        } finally {
            insertOrderLock.unlock()
        }
    }

    // 撤单
    fun cancelOrder(cancelOrder: CancelOrder): Boolean {
        if (cThostFtdcTraderApi == null) {
            logger.warn("{}交易接口尚未初始化,无法撤单", logInfo)
            return false
        }
        if (!loginStatus) {
            logger.warn("{}交易接口尚未登录,无法撤单", logInfo)
            return false
        }
        if (cancelOrder.orderId.isBlank() && cancelOrder.originOrderId.isBlank()) {
            logger.error("{}参数为空,无法撤单", logInfo)
            return false
        }
        var orderId: String? = cancelOrder.orderId
        if (orderId.isNullOrBlank()) {
            orderId = originalOrderIdToOrderIdMap[cancelOrder.originOrderId]
            if (orderId.isNullOrBlank()) {
                logger.error("{}交易接口未能找到有效定单号,无法撤单", logInfo)
                return false
            }
        }
        return try {
            val cThostFtdcInputOrderActionField = CThostFtdcInputOrderActionField()
            if (orderIdToInsertOrderMap.containsKey(orderId)) {
                orderIdToInsertOrderMap[orderId]?.let {
                    cThostFtdcInputOrderActionField.instrumentID = it.contract!!.symbol
                    cThostFtdcInputOrderActionField.exchangeID = CtpConstant.exchangeMap.getOrDefault(it.contract!!.exchange, "")
                    cThostFtdcInputOrderActionField.orderRef = orderIdToOrderRefMap[orderId]
                    cThostFtdcInputOrderActionField.frontID = frontId
                    cThostFtdcInputOrderActionField.sessionID = sessionId
                    cThostFtdcInputOrderActionField.actionFlag = jctpv6v6v1p1cpx64apiConstants.THOST_FTDC_AF_Delete
                    cThostFtdcInputOrderActionField.brokerID = brokerId
                    cThostFtdcInputOrderActionField.investorID = userId
                    cThostFtdcInputOrderActionField.userID = userId
                    cThostFtdcInputOrderActionField.exchangeID = CtpConstant.exchangeMap.getOrDefault(it.contract!!.exchange, "")
                    cThostFtdcTraderApi!!.ReqOrderAction(cThostFtdcInputOrderActionField, reqId.incrementAndGet())
                }
                true
            } else if (orderIdToOrderMap.containsKey(orderId)) {
                orderIdToOrderMap[orderId]?.let {
                    cThostFtdcInputOrderActionField.instrumentID = it.contract.symbol
                    cThostFtdcInputOrderActionField.exchangeID = CtpConstant.exchangeMap.getOrDefault(it.contract.exchange, "")
                    cThostFtdcInputOrderActionField.orderRef = orderIdToOrderRefMap[orderId]
                    cThostFtdcInputOrderActionField.frontID = it.frontId
                    cThostFtdcInputOrderActionField.sessionID = it.sessionId
                    cThostFtdcInputOrderActionField.actionFlag = jctpv6v6v1p1cpx64apiConstants.THOST_FTDC_AF_Delete
                    cThostFtdcInputOrderActionField.brokerID = brokerId
                    cThostFtdcInputOrderActionField.investorID = userId
                    cThostFtdcInputOrderActionField.userID = userId
                    cThostFtdcInputOrderActionField.exchangeID = CtpConstant.exchangeMap.getOrDefault(it.contract.exchange, "")
                    cThostFtdcTraderApi!!.ReqOrderAction(cThostFtdcInputOrderActionField, reqId.incrementAndGet())
                }
                true
            } else {
                logger.error("{}无法找到定单请求或者回报,无法撤单", logInfo)
                false
            }
        } catch (t: Throwable) {
            logger.error("{}撤单异常", logInfo, t)
            false
        }
    }

    private fun reqAuth() {
        if (loginFailed) {
            logger.warn("{}交易接口登录曾发生错误,不再登录,以防被锁", logInfo)
            return
        }
        if (cThostFtdcTraderApi == null) {
            logger.warn("{}发起客户端验证请求错误,交易接口实例不存在", logInfo)
            return
        }
        if (brokerId.isBlank()) {
            logger.error("{}BrokerID不允许为空", logInfo)
            return
        }
        if (userId.isBlank()) {
            logger.error("{}UserId不允许为空", logInfo)
            return
        }
        if (password.isBlank()) {
            logger.error("{}Password不允许为空", logInfo)
            return
        }
        if (appId.isBlank()) {
            logger.error("{}AppId不允许为空", logInfo)
            return
        }
        if (authCode.isBlank()) {
            logger.error("{}AuthCode不允许为空", logInfo)
            return
        }
        try {
            val authenticateField = CThostFtdcReqAuthenticateField()
            authenticateField.appID = appId
            authenticateField.authCode = authCode
            authenticateField.brokerID = brokerId
            authenticateField.userProductInfo = userProductInfo
            authenticateField.userID = userId
            cThostFtdcTraderApi!!.ReqAuthenticate(authenticateField, reqId.incrementAndGet())
        } catch (t: Throwable) {
            logger.error("{}发起客户端验证异常", logInfo, t)
            ctpGatewayImpl.disconnect()
        }
    }


    override fun OnFrontConnected() {
        try {
            logger.warn("{}交易接口前置机已连接", logInfo)
            // 修改前置机连接状态
            connectionStatus = ConnectionStatusEnum.Connected
            reqAuth()
        } catch (t: Throwable) {
            logger.error("{}OnFrontConnected Exception", logInfo, t)
        }
    }

    override fun OnFrontDisconnected(nReason: Int) {
        try {
            logger.warn("{}交易接口前置机已断开, 原因:{}", logInfo, nReason)
            ctpGatewayImpl.disconnect()
        } catch (t: Throwable) {
            logger.error("{}OnFrontDisconnected Exception", logInfo, t)
        }
    }

    override fun OnHeartBeatWarning(nTimeLapse: Int) {
        logger.warn("{}交易接口心跳警告, Time Lapse:{}", logInfo, nTimeLapse)
    }

    override fun OnRspAuthenticate(
        pRspAuthenticateField: CThostFtdcRspAuthenticateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pRspInfo != null) {
                if (pRspInfo.errorID == 0) {
                    logger.warn(logInfo + "交易接口客户端验证成功")
                    val reqUserLoginField = CThostFtdcReqUserLoginField()
                    reqUserLoginField.brokerID = brokerId
                    reqUserLoginField.userID = userId
                    reqUserLoginField.password = password
                    cThostFtdcTraderApi!!.ReqUserLogin(reqUserLoginField, reqId.incrementAndGet())
                } else {
                    logger.error("{}交易接口客户端验证失败 错误ID:{},错误信息:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg)
                    loginFailed = true

                    // 客户端验证失败
                    if (pRspInfo.errorID == 63) {
                        ctpGatewayImpl.authErrorFlag = true
                    }
                }
            } else {
                loginFailed = true
                logger.error("{}处理交易接口客户端验证回报错误,回报信息为空", logInfo)
            }
        } catch (t: Throwable) {
            loginFailed = true
            logger.error("{}处理交易接口客户端验证回报异常", logInfo, t)
        }
    }

    override fun OnRspUserLogin(
        pRspUserLogin: CThostFtdcRspUserLoginField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pRspInfo != null) {
                if (pRspInfo.errorID == 0) {
                    if (pRspUserLogin != null) {
                        logger.warn(
                            "{}交易接口登录成功 TradingDay:{},SessionID:{},BrokerID:{},UserID:{}",
                            logInfo,
                            pRspUserLogin.tradingDay,
                            pRspUserLogin.sessionID,
                            pRspUserLogin.brokerID,
                            pRspUserLogin.userID
                        )
                        sessionId = pRspUserLogin.sessionID
                        frontId = pRspUserLogin.frontID
                        // 修改登录状态为true
                        loginStatus = true
                        tradingDay = pRspUserLogin.tradingDay
                        logger.warn("{}交易接口获取到的交易日为{}", logInfo, tradingDay)

                        // 确认结算单
                        val settlementInfoConfirmField = CThostFtdcSettlementInfoConfirmField()
                        settlementInfoConfirmField.brokerID = brokerId
                        settlementInfoConfirmField.investorID = userId
                        cThostFtdcTraderApi!!.ReqSettlementInfoConfirm(settlementInfoConfirmField, reqId.incrementAndGet())
                    } else {
                        logger.error("{}交易接口处理登录回报数据为空", logInfo)
                    }
                } else {
                    // 不合法的登录
                    if (pRspInfo.errorID == 3) {
                        ctpGatewayImpl.authErrorFlag = true
                    }
                    logger.error("{}交易接口登录回报错误 错误ID:{},错误信息:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg)
                    loginFailed = true
                }
            } else {
                logger.error("{}交易接口处理登录回报信息为空", logInfo)
            }

        } catch (t: Throwable) {
            logger.error("{}交易接口处理登录回报异常", logInfo, t)
            loginFailed = true
        }
    }

    override fun OnRspUserLogout(
        pUserLogout: CThostFtdcUserLogoutField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pRspInfo != null && pRspInfo.errorID != 0) {
                logger.error("{}OnRspUserLogout!错误ID:{},错误信息:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg)
            } else {
                if (pUserLogout != null) {
                    logger.info("{}OnRspUserLogout!BrokerID:{},UserId:{}", logInfo, pUserLogout.brokerID, pUserLogout.userID)
                }
            }
        } catch (t: Throwable) {
            logger.error("{}交易接口处理登出回报错误", logInfo, t)
        }
        loginStatus = false
    }

    override fun OnRspUserPasswordUpdate(
        pUserPasswordUpdate: CThostFtdcUserPasswordUpdateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspTradingAccountPasswordUpdate(
        pTradingAccountPasswordUpdate: CThostFtdcTradingAccountPasswordUpdateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspUserAuthMethod(
        pRspUserAuthMethod: CThostFtdcRspUserAuthMethodField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspGenUserCaptcha(
        pRspGenUserCaptcha: CThostFtdcRspGenUserCaptchaField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspGenUserText(
        pRspGenUserText: CThostFtdcRspGenUserTextField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    // 发单错误
    override fun OnRspOrderInsert(
        pInputOrder: CThostFtdcInputOrderField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pInputOrder != null) {
                val symbol = pInputOrder.instrumentID

                // 无法获取账户信息,使用userID作为账户ID
                val accountCode = userId
                // 无法获取币种信息使用特定值CNY
                val accountId = "$accountCode@CNY@$gatewayId"
                val frontId = frontId
                val sessionId = sessionId
                val orderRef: String = pInputOrder.orderRef
                val adapterOrderId = "" + frontId + "_" + sessionId + "_" + orderRef.trim()
                val orderId = "$gatewayId@$adapterOrderId"
                val direction = CtpConstant.directionMapReverse[pInputOrder.direction] ?: DirectionEnum.Unknown
                val offsetFlag = CtpConstant.offsetMapReverse[pInputOrder.combOffsetFlag.toCharArray()[0]] ?: OffsetFlagEnum.Unknown
                val price = pInputOrder.limitPrice
                val totalVolume = pInputOrder.volumeTotalOriginal
                val tradedVolume = 0
                val orderStatus = OrderStatusEnum.Rejected
                val hedgeFlag = CtpConstant.hedgeFlagMapReverse.getOrDefault(pInputOrder.combHedgeFlag.toCharArray()[0], HedgeFlagEnum.Unknown)
                val contingentCondition = CtpConstant.contingentConditionMapReverse[pInputOrder.contingentCondition] ?: ContingentConditionEnum.Unknown
                val forceCloseReason = CtpConstant.forceCloseReasonMapReverse[pInputOrder.forceCloseReason] ?: ForceCloseReasonEnum.Unknown
                val timeCondition = CtpConstant.timeConditionMapReverse[pInputOrder.timeCondition] ?: TimeConditionEnum.Unknown
                val gtdDate = pInputOrder.gtdDate
                val autoSuspend = pInputOrder.isAutoSuspend
                val userForceClose = pInputOrder.userForceClose
                val swapOrder = pInputOrder.isSwapOrder
                val volumeCondition = CtpConstant.volumeConditionMapReverse[pInputOrder.volumeCondition] ?: VolumeConditionEnum.Unknown
                val orderPriceType = CtpConstant.orderPriceTypeMapReverse[pInputOrder.orderPriceType] ?: OrderPriceTypeEnum.Unknown
                val minVolume = pInputOrder.minVolume
                val stopPrice = pInputOrder.stopPrice
                val originalOrderId = orderIdToOriginalOrderIdMap.getOrDefault(orderId, "")
                val order = Order()

                order.accountId = accountId
                order.originOrderId = originalOrderId
                order.orderId = orderId
                order.adapterOrderId = adapterOrderId
                order.direction = direction
                order.offsetFlag = offsetFlag
                order.price = price
                order.totalVolume = totalVolume
                order.tradedVolume = tradedVolume
                order.orderStatus = orderStatus
                order.tradingDay = tradingDay ?: ""
                order.frontId = frontId
                order.sessionId = sessionId
                order.gatewayId = gatewayId
                order.hedgeFlag = hedgeFlag
                order.contingentCondition = contingentCondition
                order.forceCloseReason = forceCloseReason
                order.timeCondition = timeCondition
                order.gtdDate = gtdDate
                order.autoSuspend = autoSuspend
                order.volumeCondition = volumeCondition
                order.minVolume = minVolume
                order.stopPrice = stopPrice
                order.userForceClose = userForceClose
                order.swapOrder = swapOrder
                order.orderPriceType = orderPriceType

                if (pRspInfo != null && pRspInfo.errorMsg != null) {
                    order.statusMsg = pRspInfo.errorMsg
                }

                if (instrumentQueried && ctpGatewayImpl.contractMap.containsKey(symbol)) {
                    order.contract = ctpGatewayImpl.contractMap[symbol]!!
                    orderIdToOrderMap[order.orderId] = order
                    eventService.emit(order)
                } else {
                    val contract = Contract()
                    contract.symbol = symbol
                    order.contract = contract
                    orderCacheList.add(order)
                }
            } else {
                logger.error("{}处理交易接口发单错误回报(OnRspOrderInsert)错误,空数据", logInfo)
            }
            if (pRspInfo != null) {
                logger.error(
                    "{}交易接口发单错误回报(OnRspOrderInsert) 错误ID:{},错误信息:{}",
                    logInfo,
                    pRspInfo.errorID,
                    pRspInfo.errorMsg
                )
                if (instrumentQueried) {
                    val notice = Notice()
                    notice.info =
                        logInfo + "交易接口发单错误回报(OnRspOrderInsert) 错误ID:" + pRspInfo.errorID + ",错误信息:" + pRspInfo.errorMsg
                    notice.infoLevel = InfoLevelEnum.ERROR
                    notice.timestamp = System.currentTimeMillis()
                    eventService.emit(notice)
                }
            } else {
                logger.error("{}处理交易接口发单错误回报(OnRspOrderInsert)错误,回报信息为空", logInfo)
            }
        } catch (t: Throwable) {
            logger.error("{}处理交易接口发单错误回报(OnRspOrderInsert)异常", logInfo, t)
        }
    }

    override fun OnRspParkedOrderInsert(
        pParkedOrder: CThostFtdcParkedOrderField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspParkedOrderAction(
        pParkedOrderAction: CThostFtdcParkedOrderActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    // 撤单错误回报
    override fun OnRspOrderAction(
        pInputOrderAction: CThostFtdcInputOrderActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        if (pRspInfo != null) {
            logger.error("{}交易接口撤单错误回报(OnRspOrderAction) 错误ID:{},错误信息:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg)
            if (instrumentQueried) {
                val notice = Notice()
                notice.info =
                    logInfo + "交易接口撤单错误回报(OnRspOrderAction) 错误ID:" + pRspInfo.errorID + ",错误信息:" + pRspInfo.errorMsg
                notice.infoLevel = InfoLevelEnum.ERROR
                notice.timestamp = System.currentTimeMillis()
                eventService.emit(notice)
            }
        } else {
            logger.error("{}处理交易接口撤单错误回报(OnRspOrderAction)错误,无有效信息", logInfo)
        }
    }

    override fun OnRspQryMaxOrderVolume(
        pQueryMaxOrderVolume: CThostFtdcQryMaxOrderVolumeField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspSettlementInfoConfirm(
        pSettlementInfoConfirm: CThostFtdcSettlementInfoConfirmField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pRspInfo != null) {
                if (pRspInfo.errorID == 0) {
                    logger.warn("{}交易接口结算信息确认完成", logInfo)
                } else {
                    logger.error("{}交易接口结算信息确认出错 错误ID:{},错误信息:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg)
                    ctpGatewayImpl.disconnect()
                    return
                }

                // 防止被限流
                Thread.sleep(1000)
                logger.warn("{}交易接口开始查询投资者信息", logInfo)
                val pQryInvestor = CThostFtdcQryInvestorField()
                pQryInvestor.investorID = userId
                pQryInvestor.brokerID = brokerId
                cThostFtdcTraderApi!!.ReqQryInvestor(pQryInvestor, reqId.addAndGet(1))
            }
        } catch (t: Throwable) {
            logger.error("{}处理结算单确认回报错误", logInfo, t)
            ctpGatewayImpl.disconnect()
        }
    }

    override fun OnRspRemoveParkedOrder(
        pRemoveParkedOrder: CThostFtdcRemoveParkedOrderField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspRemoveParkedOrderAction(
        pRemoveParkedOrderAction: CThostFtdcRemoveParkedOrderActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspExecOrderInsert(
        pInputExecOrder: CThostFtdcInputExecOrderField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspExecOrderAction(
        pInputExecOrderAction: CThostFtdcInputExecOrderActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspForQuoteInsert(
        pInputForQuote: CThostFtdcInputForQuoteField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQuoteInsert(
        pInputQuote: CThostFtdcInputQuoteField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQuoteAction(
        pInputQuoteAction: CThostFtdcInputQuoteActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspBatchOrderAction(
        pInputBatchOrderAction: CThostFtdcInputBatchOrderActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspOptionSelfCloseInsert(
        pInputOptionSelfClose: CThostFtdcInputOptionSelfCloseField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspOptionSelfCloseAction(
        pInputOptionSelfCloseAction: CThostFtdcInputOptionSelfCloseActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspCombActionInsert(
        pInputCombAction: CThostFtdcInputCombActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryOrder(
        pOrder: CThostFtdcOrderField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryTrade(
        pTrade: CThostFtdcTradeField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    // 持仓查询回报
    override fun OnRspQryInvestorPosition(
        pInvestorPosition: CThostFtdcInvestorPositionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pInvestorPosition == null || pInvestorPosition.instrumentID.isBlank()) {
                return
            }
            val symbol = pInvestorPosition.instrumentID
            if (!(instrumentQueried && ctpGatewayImpl.contractMap.containsKey(symbol))) {
                logger.warn("{}尚未获取到合约信息,暂时不处理持仓数据,代码{}", logInfo, symbol)
            } else {
                val contract = ctpGatewayImpl.contractMap[symbol]
                val uniformSymbol = symbol + "@" + contract!!.exchange

                // 无法获取账户信息,使用userID作为账户ID
                val accountCode = userId
                // 无法获取币种信息使用特定值
                val accountId = "$accountCode@CNY@$gatewayId"
                val direction = CtpConstant.positionDirectionMapReverse[pInvestorPosition.posiDirection]
                    ?: PositionDirectionEnum.Unknown
                val hedgeFlag = CtpConstant.hedgeFlagMapReverse[pInvestorPosition.hedgeFlag] ?: HedgeFlagEnum.Unknown
                // 获取持仓缓存
                val positionId = "$uniformSymbol@$direction@$hedgeFlag@$accountId"
                val position: Position?
                if (positionMap.containsKey(positionId)) {
                    position = positionMap[positionId]
                } else {
                    position = Position()
                    positionMap[positionId] = position
                    position.contract = ctpGatewayImpl.contractMap[symbol]!!
                    position.positionDirection = CtpConstant.positionDirectionMapReverse[pInvestorPosition.posiDirection] ?: PositionDirectionEnum.Unknown
                    position.positionId = positionId
                    position.accountId = accountId
                    position.gatewayId = gatewayId
                    position.hedgeFlag = hedgeFlag
                }
                position!!.useMargin = position.useMargin + pInvestorPosition.useMargin
                position.exchangeMargin = position.exchangeMargin + pInvestorPosition.exchangeMargin
                position.positionProfit = position.positionProfit + pInvestorPosition.positionProfit

                // 计算旧成本
                val cost = position.price * position.position * position.contract.multiplier
                val openCost = position.openPrice * position.position * position.contract.multiplier

                // 汇总总仓
                position.position = position.position + pInvestorPosition.position

                // 计算新的持仓均价
                if (position.position != 0) {
                    position.price =
                        (cost + pInvestorPosition.positionCost) / (position.position * position.contract.multiplier)
                    position.openPrice =
                        (openCost + pInvestorPosition.openCost) / (position.position * position.contract.multiplier)
                }
                if (position.positionDirection == PositionDirectionEnum.Long) {
                    position.frozen = position.frozen + pInvestorPosition.shortFrozen
                } else {
                    position.frozen = position.frozen + pInvestorPosition.longFrozen
                }
                if (ExchangeEnum.INE == position.contract.exchange || ExchangeEnum.SHFE == position.contract.exchange) {
                    // 针对上期所、上期能源持仓的今昨分条返回（有昨仓、无今仓）,读取昨仓数据
                    if (pInvestorPosition.ydPosition > 0 && pInvestorPosition.todayPosition == 0) {
                        position.ydPosition = position.ydPosition + pInvestorPosition.position
                        if (position.positionDirection == PositionDirectionEnum.Long) {
                            position.ydFrozen = position.ydFrozen + pInvestorPosition.shortFrozen
                        } else {
                            position.ydFrozen = position.ydFrozen + pInvestorPosition.longFrozen
                        }
                    } else {
                        position.tdPosition = position.tdPosition + pInvestorPosition.position
                        if (position.positionDirection == PositionDirectionEnum.Long) {
                            position.tdFrozen = position.tdFrozen + pInvestorPosition.shortFrozen
                        } else {
                            position.tdFrozen = position.tdFrozen + pInvestorPosition.longFrozen
                        }
                    }
                } else {
                    position.tdPosition = position.tdPosition + pInvestorPosition.todayPosition
                    position.ydPosition = position.position - position.tdPosition

                    // 中金所优先平今
                    if (ExchangeEnum.CFFEX == position.contract.exchange) {
                        if (position.tdPosition > 0) {
                            if (position.tdPosition >= position.frozen) {
                                position.tdFrozen = position.frozen
                            } else {
                                position.tdFrozen = position.tdPosition
                                position.ydFrozen = position.frozen - position.tdPosition
                            }
                        } else {
                            position.ydFrozen = position.frozen
                        }
                    } else {
                        // 除了上面几个交易所之外的交易所，优先平昨
                        if (position.ydPosition > 0) {
                            if (position.ydPosition >= position.frozen) {
                                position.ydFrozen = position.frozen
                            } else {
                                position.ydFrozen = position.ydPosition
                                position.tdFrozen = position.frozen - position.ydPosition
                            }
                        } else {
                            position.tdFrozen = position.frozen
                        }
                    }
                }
            }


            // 回报结束
            if (bIsLast) {
                for (tmpPosition in positionMap.values) {
                    if (tmpPosition.position != 0) {
                        tmpPosition.priceDiff =
                            tmpPosition.positionProfit / tmpPosition.contract.multiplier / tmpPosition.position
                        if (tmpPosition.positionDirection == PositionDirectionEnum.Long
                            || tmpPosition.position > 0 && tmpPosition.positionDirection == PositionDirectionEnum.Net
                        ) {

                            // 计算最新价格
                            tmpPosition.lastPrice = tmpPosition.price + tmpPosition.priceDiff
                            // 计算开仓价格差距
                            tmpPosition.openPriceDiff = tmpPosition.lastPrice - tmpPosition.openPrice
                            // 计算开仓盈亏
                            tmpPosition.openPositionProfit =
                                tmpPosition.openPriceDiff * tmpPosition.position * tmpPosition.contract.multiplier
                        } else if (tmpPosition.positionDirection == PositionDirectionEnum.Short
                            || tmpPosition.position < 0 && tmpPosition.positionDirection == PositionDirectionEnum.Net
                        ) {

                            // 计算最新价格
                            tmpPosition.lastPrice = tmpPosition.price - tmpPosition.priceDiff
                            // 计算开仓价格差距
                            tmpPosition.openPriceDiff = tmpPosition.openPrice - tmpPosition.lastPrice
                            // 计算开仓盈亏
                            tmpPosition.openPositionProfit =
                                tmpPosition.openPriceDiff * tmpPosition.position * tmpPosition.contract.multiplier
                        } else {
                            logger.error("{}计算持仓时发现未处理方向，持仓详情{}", logInfo, tmpPosition.toString())
                        }

                        // 计算保最新合约价值
                        tmpPosition.contractValue =
                            tmpPosition.lastPrice * tmpPosition.contract.multiplier * tmpPosition.position
                        if (tmpPosition.useMargin != 0.0) {
                            tmpPosition.positionProfitRatio = tmpPosition.positionProfit / tmpPosition.useMargin
                            tmpPosition.openPositionProfitRatio = tmpPosition.openPositionProfit / tmpPosition.useMargin
                        }
                    }
                    // 发送持仓事件
                    tmpPosition.localCreatedTimestamp = System.currentTimeMillis()
                    eventService.emit(tmpPosition)
                }
                // 清空缓存
                positionMap = HashMap()
            }
        } catch (t: Throwable) {
            logger.error("{}处理查询持仓回报异常", logInfo, t)
            ctpGatewayImpl.disconnect()
        }
    }

    // 账户查询回报
    override fun OnRspQryTradingAccount(
        pTradingAccount: CThostFtdcTradingAccountField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pTradingAccount != null) {
                val accountCode = pTradingAccount.accountID
                var currency = pTradingAccount.currencyID
                if (currency.isBlank()) {
                    currency = "CNY"
                }
                val accountId = "$accountCode@$currency@$gatewayId"
                val account = Account()
                account.code = accountCode
                account.currency = CurrencyEnum.valueOf(currency)
                account.available = pTradingAccount.available
                account.closeProfit = pTradingAccount.closeProfit
                account.commission = pTradingAccount.commission
                account.gatewayId = gatewayId
                account.margin = pTradingAccount.currMargin
                account.positionProfit = pTradingAccount.positionProfit
                account.preBalance = pTradingAccount.preBalance
                account.accountId = accountId
                account.deposit = pTradingAccount.deposit
                account.withdraw = pTradingAccount.withdraw
                account.name = investorName
                account.balance = pTradingAccount.balance
                account.localCreatedTimestamp = System.currentTimeMillis()
                eventService.emit(account)
            }
        } catch (t: Throwable) {
            logger.error("{}处理查询账户回报异常", logInfo, t)
            ctpGatewayImpl.disconnect()
        }
    }

    override fun OnRspQryInvestor(
        pInvestor: CThostFtdcInvestorField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pRspInfo != null && pRspInfo.errorID != 0) {
                logger.error("{}查询投资者信息失败 错误ID:{},错误信息:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg)
                ctpGatewayImpl.disconnect()
            } else {
                if (pInvestor != null) {
                    investorName = pInvestor.investorName
                    logger.warn("{}交易接口获取到的投资者名为:{}", logInfo, investorName)
                } else {
                    logger.error("{}交易接口未能获取到投资者名", logInfo)
                }
            }
            if (bIsLast) {
                if (investorName.isBlank()) {
                    logger.warn("{}交易接口未能获取到投资者名,准备断开", logInfo)
                    ctpGatewayImpl.disconnect()
                }
                investorNameQueried = true
                // 防止被限流
                Thread.sleep(1000)
                // 查询所有合约
                logger.warn("{}交易接口开始查询合约信息", logInfo)
                val cThostFtdcQryInstrumentField = CThostFtdcQryInstrumentField()
                cThostFtdcTraderApi!!.ReqQryInstrument(cThostFtdcQryInstrumentField, reqId.incrementAndGet())
            }
        } catch (t: Throwable) {
            logger.error("{}处理查询投资者回报异常", logInfo, t)
            ctpGatewayImpl.disconnect()
        }


    }

    override fun OnRspQryTradingCode(
        pTradingCode: CThostFtdcTradingCodeField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryInstrumentMarginRate(
        pInstrumentMarginRate: CThostFtdcInstrumentMarginRateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryInstrumentCommissionRate(
        pInstrumentCommissionRate: CThostFtdcInstrumentCommissionRateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryExchange(
        pExchange: CThostFtdcExchangeField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryProduct(
        pProduct: CThostFtdcProductField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryInstrument(
        pInstrument: CThostFtdcInstrumentField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pInstrument != null) {
                val productClass =
                    CtpConstant.productTypeMapReverse[pInstrument.productClass] ?: ProductClassEnum.Unknown
                var filterFlag = false
                if (productClass == ProductClassEnum.Futures || productClass == ProductClassEnum.Options || productClass == ProductClassEnum.SpotOption) {
                    filterFlag = true
                }
                if (filterFlag) {
                    val contract = Contract()
                    contract.symbol = pInstrument.instrumentID
                    contract.exchange = CtpConstant.exchangeMapReverse[pInstrument.exchangeID] ?: ExchangeEnum.Unknown
                    contract.productClass = productClass
                    contract.uniformSymbol = contract.symbol + "@" + contract.exchange
                    contract.name = pInstrument.instrumentName
                    contract.fullName = pInstrument.instrumentName
                    contract.thirdPartyId = contract.symbol
                    if (pInstrument.volumeMultiple <= 0) {
                        contract.multiplier = 1.0
                    } else {
                        contract.multiplier = pInstrument.volumeMultiple.toDouble()
                    }
                    contract.priceTick = pInstrument.priceTick
                    contract.currency = CurrencyEnum.CNY // 默认人民币
                    contract.lastTradeDateOrContractMonth = pInstrument.expireDate
                    contract.strikePrice = pInstrument.strikePrice
                    contract.optionsType =
                        CtpConstant.optionTypeMapReverse[pInstrument.optionsType] ?: OptionsTypeEnum.Unknown
                    if (pInstrument.underlyingInstrID != null) {
                        contract.underlyingSymbol = pInstrument.underlyingInstrID
                    }
                    contract.underlyingMultiplier = pInstrument.underlyingMultiple
                    contract.maxLimitOrderVolume = pInstrument.maxLimitOrderVolume
                    contract.maxMarketOrderVolume = pInstrument.maxMarketOrderVolume
                    contract.minLimitOrderVolume = pInstrument.minLimitOrderVolume
                    contract.minMarketOrderVolume = pInstrument.minMarketOrderVolume
                    contract.maxMarginSideAlgorithm = pInstrument.maxMarginSideAlgorithm == '1'
                    contract.longMarginRatio = pInstrument.longMarginRatio
                    contract.shortMarginRatio = pInstrument.shortMarginRatio
                    ctpGatewayImpl.contractMap[contract.symbol] = contract
                }
            }
            if (bIsLast) {
                if (ctpGatewayImpl.contractMap.isNotEmpty()) {
                    eventService.emitContractList(ArrayList(ctpGatewayImpl.contractMap.values))
                }
                logger.warn("{}交易接口合约信息获取完成!共计{}条", logInfo, ctpGatewayImpl.contractMap.size)
                instrumentQueried = true
                startIntervalQuery()
                logger.warn("{}交易接口开始推送缓存Order,共计{}条", logInfo, orderCacheList.size)

                if (orderCacheList.isNotEmpty()) {
                    val emitOrderList =ArrayList<Order>()
                    for (order in orderCacheList) {
                        ctpGatewayImpl.contractMap[order.contract.symbol].let {
                            if(it == null){
                                logger.error("{}处理Order缓存,未能正确获取到合约信息,代码{}", logInfo, order.contract.symbol)
                            }else{
                                order.contract = it
                                emitOrderList.add(order)
                            }
                        }
                    }
                    if(emitOrderList.isNotEmpty()){
                        eventService.emitOrderList(emitOrderList)
                    }
                }
                orderCacheList = LinkedList<Order>()

                logger.warn("{}交易接口开始推送缓存Trade,共计{}条", logInfo, tradeCacheList.size)
                if (tradeCacheList.isNotEmpty()) {
                    val emitTradeList = ArrayList<Trade>()
                    for (trade in tradeCacheList) {
                        ctpGatewayImpl.contractMap[trade.contract.symbol].let {
                            if(it == null){
                                logger.error("{}处理Trade缓存,未能正确获取到合约信息,代码{}", logInfo, trade.contract.symbol)
                            }else{
                                trade.contract = it
                                emitTradeList.add(trade)
                            }
                        }
                    }
                    if(emitTradeList.isNotEmpty()){
                        eventService.emitTradeList(emitTradeList)
                    }
                }
                tradeCacheList = LinkedList<Trade>()
            }
        } catch (t: Throwable) {
            logger.error("{}OnRspQryInstrument Exception", logInfo, t)
        }
    }

    override fun OnRspQryDepthMarketData(
        pDepthMarketData: CThostFtdcDepthMarketDataField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQrySettlementInfo(
        pSettlementInfo: CThostFtdcSettlementInfoField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryTransferBank(
        pTransferBank: CThostFtdcTransferBankField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryInvestorPositionDetail(
        pInvestorPositionDetail: CThostFtdcInvestorPositionDetailField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryNotice(
        pNotice: CThostFtdcNoticeField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQrySettlementInfoConfirm(
        pSettlementInfoConfirm: CThostFtdcSettlementInfoConfirmField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryInvestorPositionCombineDetail(
        pInvestorPositionCombineDetail: CThostFtdcInvestorPositionCombineDetailField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryCFMMCTradingAccountKey(
        pCFMMCTradingAccountKey: CThostFtdcCFMMCTradingAccountKeyField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryEWarrantOffset(
        pEWarrantOffset: CThostFtdcEWarrantOffsetField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryInvestorProductGroupMargin(
        pInvestorProductGroupMargin: CThostFtdcInvestorProductGroupMarginField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryExchangeMarginRate(
        pExchangeMarginRate: CThostFtdcExchangeMarginRateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryExchangeMarginRateAdjust(
        pExchangeMarginRateAdjust: CThostFtdcExchangeMarginRateAdjustField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryExchangeRate(
        pExchangeRate: CThostFtdcExchangeRateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQrySecAgentACIDMap(
        pSecAgentACIDMap: CThostFtdcSecAgentACIDMapField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryProductExchRate(
        pProductExchRate: CThostFtdcProductExchRateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryProductGroup(
        pProductGroup: CThostFtdcProductGroupField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryMMInstrumentCommissionRate(
        pMMInstrumentCommissionRate: CThostFtdcMMInstrumentCommissionRateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryMMOptionInstrCommRate(
        pMMOptionInstrCommRate: CThostFtdcMMOptionInstrCommRateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryInstrumentOrderCommRate(
        pInstrumentOrderCommRate: CThostFtdcInstrumentOrderCommRateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQrySecAgentTradingAccount(
        pTradingAccount: CThostFtdcTradingAccountField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQrySecAgentCheckMode(
        pSecAgentCheckMode: CThostFtdcSecAgentCheckModeField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQrySecAgentTradeInfo(
        pSecAgentTradeInfo: CThostFtdcSecAgentTradeInfoField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryOptionInstrTradeCost(
        pOptionInstrTradeCost: CThostFtdcOptionInstrTradeCostField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryOptionInstrCommRate(
        pOptionInstrCommRate: CThostFtdcOptionInstrCommRateField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryExecOrder(
        pExecOrder: CThostFtdcExecOrderField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryForQuote(
        pForQuote: CThostFtdcForQuoteField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryQuote(
        pQuote: CThostFtdcQuoteField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryOptionSelfClose(
        pOptionSelfClose: CThostFtdcOptionSelfCloseField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryInvestUnit(
        pInvestUnit: CThostFtdcInvestUnitField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryCombInstrumentGuard(
        pCombInstrumentGuard: CThostFtdcCombInstrumentGuardField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryCombAction(
        pCombAction: CThostFtdcCombActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryTransferSerial(
        pTransferSerial: CThostFtdcTransferSerialField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryAccountregister(
        pAccountregister: CThostFtdcAccountregisterField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspError(pRspInfo: CThostFtdcRspInfoField?, nRequestID: Int, bIsLast: Boolean) {
        try {
            if (pRspInfo != null) {
                logger.error("{}交易接口错误回报!错误ID:{},错误信息:{},请求ID:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg, nRequestID)
                if (instrumentQueried) {
                    if (pRspInfo.errorID == 0) {
                        val notice = Notice()
                        notice.info =
                            "网关:" + ctpGatewayImpl.gatewayName + ",网关ID:" + gatewayId + ",交易接口错误回报:" + pRspInfo.errorMsg + ",错误ID:" + pRspInfo.errorID
                        notice.infoLevel = InfoLevelEnum.INFO
                        notice.timestamp = System.currentTimeMillis()
                        eventService.emit(notice)
                    } else {
                        val notice = Notice()
                        notice.info =
                            "网关:" + ctpGatewayImpl.gatewayName + ",网关ID:" + gatewayId + ",交易接口错误回报:" + pRspInfo.errorMsg + ",错误ID:" + pRspInfo.errorID
                        notice.infoLevel = InfoLevelEnum.ERROR
                        notice.timestamp = System.currentTimeMillis()
                        eventService.emit(notice)
                    }
                }
                // CTP查询尚未就绪,断开
                if (pRspInfo.errorID == 90) {
                    ctpGatewayImpl.disconnect()
                }
            }
        } catch (t: Throwable) {
            logger.error("{}OnRspError Exception", logInfo, t)
        }
    }

    override fun OnRtnOrder(pOrder: CThostFtdcOrderField?) {
        try {
            if (pOrder != null) {

                val symbol = pOrder.instrumentID

                // 无法获取账户信息,使用userID作为账户ID
                val accountCode = userId
                // 无法获取币种信息使用特定值CNY
                val accountId = "$accountCode@CNY@$gatewayId"
                val frontId = pOrder.frontID
                val sessionId = pOrder.sessionID
                val orderRef: String = pOrder.orderRef
                val adapterOrderId = "" + frontId.toString() + "_" + sessionId + "_" + orderRef.trim()
                val orderId = "$gatewayId@$adapterOrderId"
                val exchangeAndOrderSysId = pOrder.exchangeID + "@" + pOrder.orderSysID
                exchangeIdAndOrderSysIdToOrderIdMap[exchangeAndOrderSysId] = orderId
                orderIdToOrderRefMap[orderId] = orderRef
                orderIdToAdapterOrderIdMap[orderId] = adapterOrderId
                val direction = CtpConstant.directionMapReverse[pOrder.direction] ?: DirectionEnum.Unknown
                val offsetFlag =
                    CtpConstant.offsetMapReverse[pOrder.combOffsetFlag.toCharArray()[0]] ?: OffsetFlagEnum.Unknown
                val price = pOrder.limitPrice
                val totalVolume = pOrder.volumeTotalOriginal
                val tradedVolume = pOrder.volumeTraded
                val orderStatus = CtpConstant.statusMapReverse[pOrder.orderStatus]
                val statusMsg = pOrder.statusMsg
                val orderDate = pOrder.insertDate
                val orderTime = pOrder.insertTime
                val cancelTime = pOrder.cancelTime
                val activeTime = pOrder.activeTime
                val updateTime = pOrder.updateTime
                val suspendTime = pOrder.suspendTime
                val hedgeFlag = CtpConstant.hedgeFlagMapReverse.getOrDefault(pOrder.combHedgeFlag.toCharArray()[0], HedgeFlagEnum.Unknown)
                val contingentCondition =
                    CtpConstant.contingentConditionMapReverse[pOrder.contingentCondition] ?: ContingentConditionEnum.Unknown
                val forceCloseReason =
                    CtpConstant.forceCloseReasonMapReverse[pOrder.forceCloseReason] ?: ForceCloseReasonEnum.Unknown
                val timeCondition = CtpConstant.timeConditionMapReverse[pOrder.timeCondition] ?: TimeConditionEnum.Unknown
                val userForceClose = pOrder.userForceClose
                val gtdDate = pOrder.gtdDate
                val autoSuspend = pOrder.isAutoSuspend
                val swapOrder = pOrder.isSwapOrder
                val volumeCondition =
                    CtpConstant.volumeConditionMapReverse[pOrder.volumeCondition] ?: VolumeConditionEnum.Unknown
                val orderPriceType =
                    CtpConstant.orderPriceTypeMapReverse[pOrder.orderPriceType] ?: OrderPriceTypeEnum.Unknown
                val minVolume = pOrder.minVolume
                val stopPrice = pOrder.stopPrice
                val orderLocalId: String = pOrder.orderLocalID
                val orderSysId: String = pOrder.orderSysID
                val sequenceNo = pOrder.sequenceNo.toString()
                val brokerOrderSeq = pOrder.brokerOrderSeq.toString()
                val originalOrderId = orderIdToOriginalOrderIdMap.getOrDefault(orderId, "")
                val orderSubmitStatus =
                    CtpConstant.orderSubmitStatusMapReverse[pOrder.orderSubmitStatus] ?: OrderSubmitStatusEnum.Unknown

                val order = Order()
                order.accountId = accountId
                order.activeTime = activeTime
                order.adapterOrderId = adapterOrderId
                order.cancelTime = cancelTime
                order.direction = direction
                order.frontId = frontId
                order.offsetFlag = offsetFlag
                order.orderDate = orderDate
                order.orderId = orderId
                order.orderStatus = orderStatus!!
                order.orderTime = orderTime
                order.originOrderId = originalOrderId
                order.price = price
                order.sessionId = sessionId
                order.totalVolume = totalVolume
                order.tradedVolume = tradedVolume
                order.tradingDay = tradingDay!!
                order.updateTime = updateTime
                order.statusMsg = statusMsg
                order.gatewayId = gatewayId
                order.hedgeFlag = hedgeFlag
                order.contingentCondition = contingentCondition
                order.forceCloseReason = forceCloseReason
                order.timeCondition = timeCondition
                order.gtdDate = gtdDate
                order.autoSuspend = autoSuspend
                order.volumeCondition = volumeCondition
                order.minVolume = minVolume
                order.stopPrice = stopPrice
                order.userForceClose = userForceClose
                order.swapOrder = swapOrder
                order.suspendTime = suspendTime
                order.orderLocalId = orderLocalId
                order.orderSysId = orderSysId
                order.sequenceNo = sequenceNo
                order.brokerOrderSeq = brokerOrderSeq
                order.orderPriceType = orderPriceType
                order.orderSubmitStatus = orderSubmitStatus
                if (instrumentQueried) {
                    if (ctpGatewayImpl.contractMap.containsKey(symbol)) {
                        order.contract = ctpGatewayImpl.contractMap[symbol]!!
                        orderIdToOrderMap[order.orderId] = order
                        eventService.emit(order)
                    } else {
                        logger.error("{}交易接口定单回报处理错误,未找到合约:{}", logInfo, symbol)
                    }
                } else {
                    val contract = Contract()
                    contract.symbol = symbol
                    order.contract = contract
                    orderCacheList.add(order)
                }

            }
        } catch (t: Throwable) {
            logger.error("{}OnRtnOrder Exception", logInfo, t)
        }
    }

    override fun OnRtnTrade(pTrade: CThostFtdcTradeField?) {
        try {
            if (pTrade != null) {

                val exchangeAndOrderSysId = pTrade.exchangeID + "@" + pTrade.orderSysID
                val orderId = exchangeIdAndOrderSysIdToOrderIdMap.getOrDefault(exchangeAndOrderSysId, "")
                val adapterOrderId = orderIdToAdapterOrderIdMap.getOrDefault(orderId, "")
                val symbol = pTrade.instrumentID
                val direction = CtpConstant.directionMapReverse[pTrade.direction] ?: DirectionEnum.Unknown
                val adapterTradeId = adapterOrderId + "@" + direction + "@" + pTrade.tradeID.trim()
                val tradeId = "$gatewayId@$adapterTradeId"
                val offsetFlag = CtpConstant.offsetMapReverse[pTrade.offsetFlag] ?: OffsetFlagEnum.Unknown
                val price = pTrade.price
                val volume = pTrade.volume
                val tradeDate = pTrade.tradeDate
                val tradeTime = pTrade.tradeTime
                val hedgeFlag =
                    CtpConstant.hedgeFlagMapReverse.getOrDefault(pTrade.hedgeFlag, HedgeFlagEnum.Unknown)
                val tradeType = CtpConstant.tradeTypeMapReverse[pTrade.tradeType] ?: TradeTypeEnum.Unknown
                val priceSource = CtpConstant.priceSourceMapReverse[pTrade.priceSource] ?: PriceSourceEnum.Unknown
                val orderLocalId = pTrade.orderLocalID
                val orderSysId = pTrade.orderSysID
                val sequenceNo = pTrade.sequenceNo.toString()
                val brokerOrderSeq = pTrade.brokerOrderSeq.toString()
                val settlementID = pTrade.settlementID.toString()
                val originalOrderId = orderIdToOriginalOrderIdMap.getOrDefault(orderId, "")

                // 无法获取账户信息,使用userID作为账户ID
                val accountCode = userId
                // 无法获取币种信息使用特定值CNY
                val accountId = "$accountCode@CNY@$gatewayId"
                val trade = Trade()
                trade.accountId = accountId
                trade.adapterOrderId = adapterOrderId
                trade.adapterTradeId = adapterTradeId
                trade.tradeDate = tradeDate
                trade.tradeId = tradeId
                trade.tradeTime = tradeTime
                trade.tradingDay = tradingDay!!
                trade.direction = direction
                trade.offsetFlag = offsetFlag
                trade.orderId = orderId
                trade.originOrderId = originalOrderId
                trade.price = price
                trade.volume = volume
                trade.gatewayId = gatewayId
                trade.orderLocalId = orderLocalId
                trade.orderSysId = orderSysId
                trade.sequenceNo = sequenceNo
                trade.brokerOrderSeq = brokerOrderSeq
                trade.settlementId = settlementID
                trade.hedgeFlag = hedgeFlag
                trade.tradeType = tradeType
                trade.priceSource = priceSource
                if (instrumentQueried && ctpGatewayImpl.contractMap.containsKey(symbol)) {
                    trade.contract = ctpGatewayImpl.contractMap[symbol]!!
                    eventService.emit(trade)
                } else {
                    val contract = Contract()
                    contract.symbol = symbol
                    trade.contract = contract
                    tradeCacheList.add(trade)
                }

            }
        } catch (t: Throwable) {
            logger.error("{}OnRtnTrade Exception", logInfo, t)
        }
    }

    override fun OnErrRtnOrderInsert(pInputOrder: CThostFtdcInputOrderField?, pRspInfo: CThostFtdcRspInfoField?) {
        try {
            if (pRspInfo != null) {
                logger.error(
                    "{}交易接口发单错误回报（OnErrRtnOrderInsert） 错误ID:{},错误信息:{}",
                    logInfo,
                    pRspInfo.errorID,
                    pRspInfo.errorMsg
                )

                if (instrumentQueried) {
                    val notice = Notice()
                    notice.info =
                        logInfo + "交易接口发单错误回报（OnErrRtnOrderInsert） 错误ID:" + pRspInfo.errorID + ",错误信息:" + pRspInfo.errorMsg
                    notice.infoLevel = InfoLevelEnum.ERROR
                    notice.timestamp = System.currentTimeMillis()
                    eventService.emit(notice)
                }
            }
            if (pInputOrder != null) {
                logger.error(
                    "{}交易接口发单错误回报（OnErrRtnOrderInsert） 定单详细信息 ->{InstrumentID:{}, LimitPrice:{}, VolumeTotalOriginal:{}, OrderPriceType:{}, Direction:{}, CombOffsetFlag:{}, OrderRef:{}, InvestorID:{}, UserID:{}, BrokerID:{}, ExchangeID:{}, CombHedgeFlag:{}, ContingentCondition:{}, ForceCloseReason:{}, IsAutoSuspend:{}, IsSwapOrder:{}, MinVolume:{}, TimeCondition:{}, VolumeCondition:{}, StopPrice:{}}",  //
                    logInfo,  //
                    pInputOrder.instrumentID,  //
                    pInputOrder.limitPrice,  //
                    pInputOrder.volumeTotalOriginal,  //
                    pInputOrder.orderPriceType,  //
                    pInputOrder.direction,  //
                    pInputOrder.combOffsetFlag,  //
                    pInputOrder.orderRef,  //
                    pInputOrder.investorID,  //
                    pInputOrder.userID,  //
                    pInputOrder.brokerID,  //
                    pInputOrder.exchangeID,  //
                    pInputOrder.combHedgeFlag,  //
                    pInputOrder.contingentCondition,  //
                    pInputOrder.forceCloseReason,  //
                    pInputOrder.isAutoSuspend,  //
                    pInputOrder.isSwapOrder,  //
                    pInputOrder.minVolume,  //
                    pInputOrder.timeCondition,  //
                    pInputOrder.volumeCondition,  //
                    pInputOrder.stopPrice
                )
            }


        } catch (t: Throwable) {
            logger.error("{}OnErrRtnOrderInsert Exception", logInfo, t)
        }
    }

    override fun OnErrRtnOrderAction(pOrderAction: CThostFtdcOrderActionField?, pRspInfo: CThostFtdcRspInfoField?) {
        if (pRspInfo != null) {
            logger.error(
                "{}交易接口撤单错误(OnErrRtnOrderAction) 错误ID:{},错误信息:{}",
                logInfo,
                pRspInfo.errorID,
                pRspInfo.errorMsg
            )
            if (instrumentQueried) {
                val notice = Notice()
                notice.info =
                    logInfo + "交易接口撤单错误回报(OnErrRtnOrderAction) 错误ID:" + pRspInfo.errorID + ",错误信息:" + pRspInfo.errorMsg
                notice.infoLevel = InfoLevelEnum.ERROR
                notice.timestamp = System.currentTimeMillis()
                eventService.emit(notice)
            }
        } else {
            logger.error("{}处理交易接口撤单错误(OnErrRtnOrderAction)错误,无有效信息", logInfo)
        }
    }

    override fun OnRtnInstrumentStatus(pInstrumentStatus: CThostFtdcInstrumentStatusField?) {

    }

    override fun OnRtnBulletin(pBulletin: CThostFtdcBulletinField?) {

    }

    override fun OnRtnTradingNotice(pTradingNoticeInfo: CThostFtdcTradingNoticeInfoField?) {

    }

    override fun OnRtnErrorConditionalOrder(pErrorConditionalOrder: CThostFtdcErrorConditionalOrderField?) {

    }

    override fun OnRtnExecOrder(pExecOrder: CThostFtdcExecOrderField?) {

    }

    override fun OnErrRtnExecOrderInsert(
        pInputExecOrder: CThostFtdcInputExecOrderField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnErrRtnExecOrderAction(
        pExecOrderAction: CThostFtdcExecOrderActionField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnErrRtnForQuoteInsert(
        pInputForQuote: CThostFtdcInputForQuoteField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnRtnQuote(pQuote: CThostFtdcQuoteField?) {

    }

    override fun OnErrRtnQuoteInsert(pInputQuote: CThostFtdcInputQuoteField?, pRspInfo: CThostFtdcRspInfoField?) {

    }

    override fun OnErrRtnQuoteAction(pQuoteAction: CThostFtdcQuoteActionField?, pRspInfo: CThostFtdcRspInfoField?) {

    }

    override fun OnRtnForQuoteRsp(pForQuoteRsp: CThostFtdcForQuoteRspField?) {

    }

    override fun OnRtnCFMMCTradingAccountToken(pCFMMCTradingAccountToken: CThostFtdcCFMMCTradingAccountTokenField?) {

    }

    override fun OnErrRtnBatchOrderAction(
        pBatchOrderAction: CThostFtdcBatchOrderActionField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnRtnOptionSelfClose(pOptionSelfClose: CThostFtdcOptionSelfCloseField?) {

    }

    override fun OnErrRtnOptionSelfCloseInsert(
        pInputOptionSelfClose: CThostFtdcInputOptionSelfCloseField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnErrRtnOptionSelfCloseAction(
        pOptionSelfCloseAction: CThostFtdcOptionSelfCloseActionField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnRtnCombAction(pCombAction: CThostFtdcCombActionField?) {

    }

    override fun OnErrRtnCombActionInsert(
        pInputCombAction: CThostFtdcInputCombActionField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnRspQryContractBank(
        pContractBank: CThostFtdcContractBankField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryParkedOrder(
        pParkedOrder: CThostFtdcParkedOrderField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryParkedOrderAction(
        pParkedOrderAction: CThostFtdcParkedOrderActionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryTradingNotice(
        pTradingNotice: CThostFtdcTradingNoticeField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryBrokerTradingParams(
        pBrokerTradingParams: CThostFtdcBrokerTradingParamsField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryBrokerTradingAlgos(
        pBrokerTradingAlgos: CThostFtdcBrokerTradingAlgosField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQueryCFMMCTradingAccountToken(
        pQueryCFMMCTradingAccountToken: CThostFtdcQueryCFMMCTradingAccountTokenField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRtnFromBankToFutureByBank(pRspTransfer: CThostFtdcRspTransferField?) {

    }

    override fun OnRtnFromFutureToBankByBank(pRspTransfer: CThostFtdcRspTransferField?) {

    }

    override fun OnRtnRepealFromBankToFutureByBank(pRspRepeal: CThostFtdcRspRepealField?) {

    }

    override fun OnRtnRepealFromFutureToBankByBank(pRspRepeal: CThostFtdcRspRepealField?) {

    }

    override fun OnRtnFromBankToFutureByFuture(pRspTransfer: CThostFtdcRspTransferField?) {

    }

    override fun OnRtnFromFutureToBankByFuture(pRspTransfer: CThostFtdcRspTransferField?) {

    }

    override fun OnRtnRepealFromBankToFutureByFutureManual(pRspRepeal: CThostFtdcRspRepealField?) {

    }

    override fun OnRtnRepealFromFutureToBankByFutureManual(pRspRepeal: CThostFtdcRspRepealField?) {

    }

    override fun OnRtnQueryBankBalanceByFuture(pNotifyQueryAccount: CThostFtdcNotifyQueryAccountField?) {

    }

    override fun OnErrRtnBankToFutureByFuture(
        pReqTransfer: CThostFtdcReqTransferField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnErrRtnFutureToBankByFuture(
        pReqTransfer: CThostFtdcReqTransferField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnErrRtnRepealBankToFutureByFutureManual(
        pReqRepeal: CThostFtdcReqRepealField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnErrRtnRepealFutureToBankByFutureManual(
        pReqRepeal: CThostFtdcReqRepealField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnErrRtnQueryBankBalanceByFuture(
        pReqQueryAccount: CThostFtdcReqQueryAccountField?,
        pRspInfo: CThostFtdcRspInfoField?
    ) {

    }

    override fun OnRtnRepealFromBankToFutureByFuture(pRspRepeal: CThostFtdcRspRepealField?) {

    }

    override fun OnRtnRepealFromFutureToBankByFuture(pRspRepeal: CThostFtdcRspRepealField?) {

    }

    override fun OnRspFromBankToFutureByFuture(
        pReqTransfer: CThostFtdcReqTransferField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspFromFutureToBankByFuture(
        pReqTransfer: CThostFtdcReqTransferField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQueryBankAccountMoneyByFuture(
        pReqQueryAccount: CThostFtdcReqQueryAccountField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRtnOpenAccountByBank(pOpenAccount: CThostFtdcOpenAccountField?) {

    }

    override fun OnRtnCancelAccountByBank(pCancelAccount: CThostFtdcCancelAccountField?) {

    }

    override fun OnRtnChangeAccountByBank(pChangeAccount: CThostFtdcChangeAccountField?) {

    }

    override fun OnRspQryClassifiedInstrument(pInstrument: CThostFtdcInstrumentField?, pRspInfo: CThostFtdcRspInfoField?, nRequestID: Int, bIsLast: Boolean) {
    
    }

    override fun OnRspQryCombPromotionParam(
        pCombPromotionParam: CThostFtdcCombPromotionParamField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {

    }

    override fun OnRspQryRiskSettleInvstPosition(
        pRiskSettleInvstPosition: CThostFtdcRiskSettleInvstPositionField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
    }

    override fun OnRspQryRiskSettleProductStatus(
        pRiskSettleProductStatus: CThostFtdcRiskSettleProductStatusField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
    }
}