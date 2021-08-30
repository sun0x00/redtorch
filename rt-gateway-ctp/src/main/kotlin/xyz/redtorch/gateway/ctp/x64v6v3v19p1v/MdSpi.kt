package xyz.redtorch.gateway.ctp.x64v6v3v19p1v

import org.slf4j.LoggerFactory
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.trade.dto.Tick
import xyz.redtorch.common.trade.enumeration.ExchangeEnum
import xyz.redtorch.common.utils.CommonUtils
import xyz.redtorch.common.utils.CommonUtils.forceMkdirParent
import xyz.redtorch.gateway.ctp.x64v6v3v19p1v.api.*
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class MdSpi internal constructor(private val ctpGatewayImpl: CtpGatewayImpl) : CThostFtdcMdSpi() {
    companion object {
        private val logger = LoggerFactory.getLogger(MdSpi::class.java)
    }

    private val mdHost = ctpGatewayImpl.gatewayAdapterCtpSetting.mdHost!!
    private val mdPort = ctpGatewayImpl.gatewayAdapterCtpSetting.mdPort!!
    private val brokerId = ctpGatewayImpl.gatewayAdapterCtpSetting.brokerId!!
    private val userId = ctpGatewayImpl.gatewayAdapterCtpSetting.userId!!
    private val password = ctpGatewayImpl.gatewayAdapterCtpSetting.password!!
    private val logInfo = ctpGatewayImpl.logInfo
    private val gatewayId = ctpGatewayImpl.gatewayId
    private val eventService = ctpGatewayImpl.getEventService()

    // 获取交易日
    var tradingDay = 0
        private set
    private val preTickMap: MutableMap<String, Tick> = HashMap()
    private val subscribedSymbolSet: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private var cThostFtdcMdApi: CThostFtdcMdApi? = null
    private var connectionStatus = ConnectionStatusEnum.Disconnected // 避免重复调用
    private var loginStatus = false // 登陆状态
    fun connect() {
        if (isConnected || connectionStatus == ConnectionStatusEnum.Connecting) {
            return
        }
        if (connectionStatus == ConnectionStatusEnum.Connected) {
            login()
            return
        }
        connectionStatus = ConnectionStatusEnum.Connecting
        loginStatus = false
        if (cThostFtdcMdApi != null) {
            try {
                logger.warn("{}行情接口检测到旧实例,准备释放", logInfo)
                val cThostFtdcMdApiForRelease: CThostFtdcMdApi = cThostFtdcMdApi!!
                cThostFtdcMdApi = null
                cThostFtdcMdApiForRelease.RegisterSpi(null)
                Thread {
                    Thread.currentThread().name =
                        "GatewayId [$gatewayId] MD API Release Thread, Start Time${System.currentTimeMillis()} "
                    try {
                        logger.warn("行情接口异步释放启动！")
                        cThostFtdcMdApiForRelease.Release()
                        logger.warn("行情接口异步释放完成！")
                    } catch (t: Throwable) {
                        logger.error("行情接口异步释放发生异常！", t)
                    }
                }.start()
                Thread.sleep(100)
            } catch (t: Throwable) {
                logger.warn("{}交易接口连接前释放异常", logInfo, t)
            }
        }
        logger.warn("{}行情接口实例初始化", logInfo)
        val envTmpDir = System.getProperty("java.io.tmpdir")
        val separator = File.separator
        val tempFilePath =
            "${envTmpDir}${separator}xyz${separator}redtorch${separator}gateway${separator}ctp${separator}jctpv6v3v19p1x64api${separator}CTP_FLOW_TEMP${separator}MD_${gatewayId}"
        val tempFile = File(tempFilePath)
        if (!tempFile.parentFile.exists()) {
            try {
                forceMkdirParent(tempFile)
                logger.info("{}行情接口创建临时文件夹:{}", logInfo, tempFile.parentFile.absolutePath)
            } catch (e: IOException) {
                logger.error("{}行情接口创建临时文件夹失败", logInfo, e)
            }
        }
        logger.warn("{}行情接口使用临时文件夹:{}", logInfo, tempFile.parentFile.absolutePath)
        try {
            cThostFtdcMdApi = CThostFtdcMdApi.CreateFtdcMdApi(tempFile.absolutePath)
            cThostFtdcMdApi!!.RegisterSpi(this)
            cThostFtdcMdApi!!.RegisterFront("tcp://$mdHost:$mdPort")
            cThostFtdcMdApi!!.Init()
        } catch (t: Throwable) {
            logger.error("{}行情接口连接异常", logInfo, t)
        }
        Thread {
            try {
                Thread.sleep((60 * 1000).toLong())
                if (!isConnected) {
                    logger.error("{}行情接口连接超时,尝试断开", logInfo)
                    ctpGatewayImpl.disconnect()
                }
            } catch (t: Throwable) {
                logger.error("{}行情接口处理连接超时线程异常", logInfo, t)
            }
        }.start()
    }

    // 关闭
    fun disconnect() {
        if (cThostFtdcMdApi != null && connectionStatus != ConnectionStatusEnum.Disconnecting) {
            logger.warn("{}行情接口实例开始关闭并释放", logInfo)
            loginStatus = false
            connectionStatus = ConnectionStatusEnum.Disconnecting
            if (cThostFtdcMdApi != null) {
                try {
                    val cThostFtdcMdApiForRelease: CThostFtdcMdApi = cThostFtdcMdApi!!
                    cThostFtdcMdApi = null
                    cThostFtdcMdApiForRelease.RegisterSpi(null)
                    Thread {
                        Thread.currentThread().name =
                            "GatewayId [$gatewayId] MD API Release Thread, Start Time${System.currentTimeMillis()} "
                        try {
                            logger.warn("行情接口异步释放启动！")
                            cThostFtdcMdApiForRelease.Release()
                            logger.warn("行情接口异步释放完成！")
                        } catch (t: Throwable) {
                            logger.error("行情接口异步释放发生异常", t)
                        }
                    }.start()
                    Thread.sleep(100)
                } catch (t: Throwable) {
                    logger.error("{}行情接口实例关闭并释放异常", logInfo, t)
                }
            }
            connectionStatus = ConnectionStatusEnum.Disconnected
            logger.warn("{}行情接口实例关闭并释放", logInfo)
        } else {
            logger.warn("{}行情接口实例不存在,无需关闭释放", logInfo)
        }
    }

    // 返回接口状态
    val isConnected: Boolean
        get() = connectionStatus == ConnectionStatusEnum.Connected && loginStatus

    // 订阅行情
    fun subscribe(symbol: String): Boolean {
        if (subscribedSymbolSet.contains(symbol)) {
            return true
        }

        subscribedSymbolSet.add(symbol)
        return if (isConnected) {
            val symbolArray = arrayOfNulls<String>(1)
            symbolArray[0] = symbol
            try {
                cThostFtdcMdApi!!.SubscribeMarketData(symbolArray, 1)
            } catch (t: Throwable) {
                logger.error("{}订阅行情异常,合约代码{}", logInfo, symbol, t)
                return false
            }
            true
        } else {
            logger.warn("{}无法订阅行情,行情服务器尚未连接成功,合约代码:{}", logInfo, symbol)
            false
        }
    }

    // 退订行情
    fun unsubscribe(symbol: String): Boolean {
        if (!subscribedSymbolSet.contains(symbol)) {
            return true
        }
        subscribedSymbolSet.remove(symbol)
        return if (isConnected) {
            val symbolArray = arrayOfNulls<String>(1)
            symbolArray[0] = symbol
            symbolArray[0] = symbol
            try {
                cThostFtdcMdApi!!.UnSubscribeMarketData(symbolArray, 1)
            } catch (t: Throwable) {
                logger.error("{}行情退订异常,合约代码{}", logInfo, symbol, t)
                return false
            }
            true
        } else {
            logger.warn("{}行情退订无效,行情服务器尚未连接成功,合约代码:{}", logInfo, symbol)
            false
        }
    }

    private fun login() {
        if (brokerId.isBlank() || userId.isBlank() || password.isBlank()) {
            logger.error("{}BrokerId UserId Password 不可为空", logInfo)
            return
        }
        try {
            // 登录
            val userLoginField = CThostFtdcReqUserLoginField()
            userLoginField.brokerID = brokerId
            userLoginField.userID = userId
            userLoginField.password = password
            cThostFtdcMdApi!!.ReqUserLogin(userLoginField, 0)
        } catch (t: Throwable) {
            logger.error("{}登录异常", logInfo, t)
        }
    }

    // 前置机联机回报
    override fun OnFrontConnected() {
        try {
            logger.warn(logInfo + "行情接口前置机已连接")
            // 修改前置机连接状态
            connectionStatus = ConnectionStatusEnum.Connected
            login()
        } catch (t: Throwable) {
            logger.error("{} OnFrontConnected Exception", logInfo, t)
        }
    }

    // 前置机断开回报
    override fun OnFrontDisconnected(nReason: Int) {
        try {
            logger.warn("{}行情接口前置机已断开, 原因:{}", logInfo, nReason)
            ctpGatewayImpl.disconnect()
        } catch (t: Throwable) {
            logger.error("{} OnFrontDisconnected Exception", logInfo, t)
        }
    }

    // 登录回报
    override fun OnRspUserLogin(
        pRspUserLogin: CThostFtdcRspUserLoginField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        try {
            if (pRspInfo != null) {
                if (pRspInfo.errorID == 0 && pRspUserLogin != null) {
                    logger.info(
                        "{}OnRspUserLogin TradingDay:{},SessionID:{},BrokerId:{},UserID:{}",
                        logInfo,
                        pRspUserLogin.tradingDay,
                        pRspUserLogin.sessionID,
                        pRspUserLogin.brokerID,
                        pRspUserLogin.userID
                    )
                    // 修改登录状态为true
                    loginStatus = true
                    if (!pRspUserLogin.tradingDay.isNullOrBlank()) {
                        tradingDay = pRspUserLogin.tradingDay.toInt()
                    }
                    logger.warn("{}行情接口获取到的交易日为{}", logInfo, tradingDay)
                    if (subscribedSymbolSet.isNotEmpty()) {
                        val symbolArray = subscribedSymbolSet.toTypedArray()
                        cThostFtdcMdApi!!.SubscribeMarketData(symbolArray, subscribedSymbolSet.size)
                    }
                } else {
                    logger.warn("{}行情接口登录回报错误 错误ID:{},错误信息:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg)
                    // 不合法的登录
                    if (pRspInfo.errorID == 3) {
                        ctpGatewayImpl.isAuthError()
                    }
                }
            }

        } catch (t: Throwable) {
            logger.error("{} OnRspUserLogin Exception", logInfo, t)
        }
    }

    // 心跳警告
    override fun OnHeartBeatWarning(nTimeLapse: Int) {
        logger.warn("{}行情接口心跳警告 nTimeLapse:{}", logInfo, nTimeLapse)
    }

    // 登出回报
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
                    logger.warn(
                        "{}OnRspUserLogout!BrokerId:{},UserID:{}",
                        logInfo,
                        pUserLogout.brokerID,
                        pUserLogout.userID
                    )
                }
            }
        } catch (t: Throwable) {
            logger.error("{} OnRspUserLogout Exception", logInfo, t)
        }
        loginStatus = false
    }

    // 错误回报
    override fun OnRspError(pRspInfo: CThostFtdcRspInfoField?, nRequestID: Int, bIsLast: Boolean) {
        if (pRspInfo != null) {
            logger.error("{}行情接口错误回报!错误ID:{},错误信息:{},请求ID:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg, nRequestID)
        } else {
            logger.error("{}行情接口错误回报!不存在错误回报信息", logInfo)
        }
    }

    // 订阅合约回报
    override fun OnRspSubMarketData(
        pSpecificInstrument: CThostFtdcSpecificInstrumentField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        if (pRspInfo != null) {
            if (pRspInfo.errorID == 0) {
                if (pSpecificInstrument != null) {
                    logger.info("{}行情接口订阅合约成功:{}", logInfo, pSpecificInstrument.instrumentID)
                } else {
                    logger.error("{}行情接口订阅合约成功,不存在合约信息", logInfo)
                }
            } else {
                logger.error("{}行情接口订阅合约失败,错误ID:{} 错误信息:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg)
            }
        } else {
            logger.info("{}行情接口订阅回报，不存在回报信息", logInfo)
        }
    }

    // 退订合约回报
    override fun OnRspUnSubMarketData(
        pSpecificInstrument: CThostFtdcSpecificInstrumentField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        if (pRspInfo != null) {
            if (pRspInfo.errorID == 0) {
                if (pSpecificInstrument != null) {
                    logger.info("{}行情接口退订合约成功:{}", logInfo, pSpecificInstrument.instrumentID)
                } else {
                    logger.error("{}行情接口退订合约成功,不存在合约信息", logInfo)
                }
            } else {
                logger.error("{}行情接口退订合约失败,错误ID:{} 错误信息:{}", logInfo, pRspInfo.errorID, pRspInfo.errorMsg)
            }
        } else {
            logger.info("{}行情接口退订回报，不存在回报信息", logInfo)
        }
    }

    // 合约行情推送
    override fun OnRtnDepthMarketData(pDepthMarketData: CThostFtdcDepthMarketDataField?) {
        if (pDepthMarketData != null) {
            try {
                val symbol = pDepthMarketData.instrumentID
                if (!ctpGatewayImpl.contractMap.containsKey(symbol)) {
                    logger.warn("{}行情接口收到合约{}数据,但尚未获取到合约信息,丢弃", logInfo, symbol)
                    return
                }
                val contract = ctpGatewayImpl.contractMap[symbol]!!
                var actionDay = pDepthMarketData.actionDay
                val updateTime = pDepthMarketData.updateTime.replace(":".toRegex(), "").toLong()
                val updateMillisec = pDepthMarketData.updateMillisec.toLong()
                /*
                 * 大商所获取的ActionDay可能是不正确的,因此这里采用本地时间修正 1.请注意，本地时间应该准确 2.使用 SimNow 7x24
                 * 服务器获取行情时,这个修正方式可能会导致问题
                 */
                if (contract.exchange == ExchangeEnum.DCE) {
                    // 只修正夜盘
                    if (updateTime in 200001..235959) {
                        actionDay = LocalDateTime.now().format(Constant.D_FORMAT_INT_FORMATTER)
                    }
                }
                val actionDayInt = actionDay.toInt()
                val updateDateTimeWithMS = (actionDayInt.toLong() * 1000000 * 1000 + updateTime * 1000 + updateMillisec).toString() + ""

                val dateTime: LocalDateTime = try {
                    LocalDateTime.parse(updateDateTimeWithMS, Constant.DT_FORMAT_WITH_MS_INT_FORMATTER)
                } catch (e: Exception) {
                    logger.error("{}解析日期发生异常", logInfo, e)
                    return
                }
                val actionTimestamp: Long = CommonUtils.localDateTimeToMills(dateTime)

//				// 以下代码可用于实盘过滤垃圾数据
//				if(Math.abs(actionTimestamp-ctpGatewayImpl.getApproximatelyTimestamp())>5*60*1000) {
//					logger.error("接收到与本地时间戳相差较大的行情数据,疑似错误,合约{},发生时间{}",symbol,actionTimestamp);
//					return;
//				}
                val uniformSymbol: String = contract.uniformSymbol
                val actionTimeInt = dateTime.format(Constant.T_FORMAT_WITH_MS_INT_FORMATTER).toInt()
                val lastPrice = pDepthMarketData.lastPrice

                val volume = pDepthMarketData.volume
                var volumeDelta = 0L

                val turnover = pDepthMarketData.turnover
                var turnoverDelta = 0.0

                val preOpenInterest = pDepthMarketData.preOpenInterest.toLong()
                val openInterest = pDepthMarketData.openInterest
                var openInterestDelta = 0.0

                preTickMap[uniformSymbol]?.let {
                    volumeDelta = volume - it.volume
                    turnoverDelta = turnover - it.turnover
                    openInterestDelta = openInterest - it.openInterest
                }


                val tick = Tick()
                tick.contract = contract
                tick.actionDay = actionDayInt
                tick.actionTime = actionTimeInt
                tick.actionTimestamp = actionTimestamp
                tick.avgPrice = pDepthMarketData.averagePrice
                tick.highPrice = pDepthMarketData.highestPrice
                tick.lowPrice = pDepthMarketData.lowestPrice
                tick.openPrice = pDepthMarketData.openPrice
                tick.lastPrice = lastPrice
                tick.settlePrice = pDepthMarketData.settlementPrice
                tick.openInterest = openInterest
                tick.openInterestDelta = openInterestDelta
                tick.volume = volume.toLong()
                tick.volumeDelta = volumeDelta
                tick.turnover = turnover
                tick.turnoverDelta = turnoverDelta
                tick.tradingDay = tradingDay
                tick.lowerLimit = pDepthMarketData.lowerLimitPrice
                tick.upperLimit = pDepthMarketData.upperLimitPrice
                tick.preClosePrice = pDepthMarketData.preClosePrice
                tick.preSettlePrice = pDepthMarketData.preSettlementPrice
                tick.preOpenInterest = preOpenInterest.toDouble()

                tick.askPriceMap["1"] = pDepthMarketData.askPrice1
                tick.askPriceMap["2"] = pDepthMarketData.askPrice2
                tick.askPriceMap["3"] = pDepthMarketData.askPrice3
                tick.askPriceMap["4"] = pDepthMarketData.askPrice4
                tick.askPriceMap["5"] = pDepthMarketData.askPrice5

                tick.askVolumeMap["1"] = pDepthMarketData.askVolume1
                tick.askVolumeMap["2"] = pDepthMarketData.askVolume2
                tick.askVolumeMap["3"] = pDepthMarketData.askVolume3
                tick.askVolumeMap["4"] = pDepthMarketData.askVolume4
                tick.askVolumeMap["5"] = pDepthMarketData.askVolume5

                tick.bidPriceMap["1"] = pDepthMarketData.bidPrice1
                tick.bidPriceMap["2"] = pDepthMarketData.bidPrice2
                tick.bidPriceMap["3"] = pDepthMarketData.bidPrice3
                tick.bidPriceMap["4"] = pDepthMarketData.bidPrice4
                tick.bidPriceMap["5"] = pDepthMarketData.bidPrice5

                tick.bidVolumeMap["1"] = pDepthMarketData.bidVolume1
                tick.bidVolumeMap["2"] = pDepthMarketData.bidVolume2
                tick.bidVolumeMap["3"] = pDepthMarketData.bidVolume3
                tick.bidVolumeMap["4"] = pDepthMarketData.bidVolume4
                tick.bidVolumeMap["5"] = pDepthMarketData.bidVolume5

                tick.gatewayId = gatewayId
                preTickMap[uniformSymbol] = tick
                eventService.emit(tick)
            } catch (t: Throwable) {
                logger.error("{} OnRtnDepthMarketData Exception", logInfo, t)
            }
        } else {
            logger.warn("{}行情接口收到空数据", logInfo)
        }
    }

    // 订阅期权询价
    override fun OnRspSubForQuoteRsp(
        pSpecificInstrument: CThostFtdcSpecificInstrumentField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        logger.info("{}OnRspSubForQuoteRsp", logInfo)
    }

    // 退订期权询价
    override fun OnRspUnSubForQuoteRsp(
        pSpecificInstrument: CThostFtdcSpecificInstrumentField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        logger.info("{}OnRspUnSubForQuoteRsp", logInfo)
    }

    // 期权询价推送
    override fun OnRtnForQuoteRsp(pForQuoteRsp: CThostFtdcForQuoteRspField?) {
        logger.info("{}OnRspUnSubForQuoteRsp", logInfo)
    }

    override fun OnRspQryMulticastInstrument(
        pMulticastInstrument: CThostFtdcMulticastInstrumentField?,
        pRspInfo: CThostFtdcRspInfoField?,
        nRequestID: Int,
        bIsLast: Boolean
    ) {
        logger.info("{}OnRspQryMulticastInstrument", logInfo)
    }

}