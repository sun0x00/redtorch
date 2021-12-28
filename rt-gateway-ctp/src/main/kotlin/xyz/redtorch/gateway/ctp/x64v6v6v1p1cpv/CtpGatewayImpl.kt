package xyz.redtorch.gateway.ctp.x64v6v6v1p1cpv

import org.slf4j.LoggerFactory
import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.event.service.EventService
import xyz.redtorch.common.storage.enumeration.GatewayTypeEnum
import xyz.redtorch.common.storage.po.GatewayAdapterCtpSetting
import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.common.sync.dto.CancelOrder
import xyz.redtorch.common.sync.dto.GatewayStatus
import xyz.redtorch.common.sync.dto.InsertOrder
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.utils.CommonUtils.copyFileByURL
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.gateway.GatewayAbstract
import java.io.File
import java.util.*

class CtpGatewayImpl(eventService: EventService, gatewaySetting: GatewaySetting) :
    GatewayAbstract(eventService, gatewaySetting) {
    companion object {
        private val logger = LoggerFactory.getLogger(CtpGatewayImpl::class.java)

        init {
            val envTmpDir: String
            var tempLibPath: String? = null
            try {
                if (System.getProperties().getProperty("os.name").uppercase(Locale.getDefault()).contains("WINDOWS")) {
                    envTmpDir = System.getProperty("java.io.tmpdir")
                    val separator = File.separator
                    tempLibPath =
                        "${envTmpDir}${separator}xyz${separator}redtorch${separator}api${separator}jctp${separator}lib${separator}jctpv6v6v1p1cpx64api${separator}"

                    copyFileByURL(tempLibPath, CtpGatewayImpl::class.java.getResource("/assembly/libiconv.dll")!!)
                    copyFileByURL(
                        tempLibPath,
                        CtpGatewayImpl::class.java.getResource("/assembly/jctpv6v6v1p1cpx64api/thostmduserapi_se.dll")!!
                    )
                    copyFileByURL(
                        tempLibPath,
                        CtpGatewayImpl::class.java.getResource("/assembly/jctpv6v6v1p1cpx64api/thosttraderapi_se.dll")!!
                    )
                    copyFileByURL(
                        tempLibPath,
                        CtpGatewayImpl::class.java.getResource("/assembly/jctpv6v6v1p1cpx64api/jctpv6v6v1p1cpx64api.dll")!!
                    )
                } else {
                    envTmpDir = "/tmp"
                    val separator = File.separator
                    tempLibPath =
                        "${envTmpDir}${separator}xyz${separator}redtorch${separator}api${separator}jctp${separator}lib${separator}jctpv6v6v1p1cpx64api${separator}"
                    copyFileByURL(
                        tempLibPath,
                        CtpGatewayImpl::class.java.getResource("/assembly/jctpv6v6v1p1cpx64api/libthostmduserapi_se.so")!!
                    )
                    copyFileByURL(
                        tempLibPath,
                        CtpGatewayImpl::class.java.getResource("/assembly/jctpv6v6v1p1cpx64api/libthosttraderapi_se.so")!!
                    )
                    copyFileByURL(
                        tempLibPath,
                        CtpGatewayImpl::class.java.getResource("/assembly/jctpv6v6v1p1cpx64api/libjctpv6v6v1p1cpx64api.so")!!
                    )
                }
            } catch (e: Exception) {
                logger.warn("复制运行库失败", e)
            }
            try {
                if (System.getProperties().getProperty("os.name").uppercase(Locale.getDefault()).contains("WINDOWS")) {
                    System.load("$tempLibPath${File.separator}libiconv.dll")
                    System.load("$tempLibPath${File.separator}thostmduserapi_se.dll")
                    System.load("$tempLibPath${File.separator}thosttraderapi_se.dll")
                    System.load("$tempLibPath${File.separator}jctpv6v6v1p1cpx64api.dll")
                } else {
                    System.load("$tempLibPath${File.separator}libthostmduserapi_se.so")
                    System.load("$tempLibPath${File.separator}libthosttraderapi_se.so")
                    System.load("$tempLibPath${File.separator}libjctpv6v6v1p1cpx64api.so")
                }
            } catch (e: Exception) {
                logger.warn("加载运行库失败", e)
            }
        }
    }

    private var mdSpi: MdSpi? = null

    private var tdSpi: TdSpi? = null

    val gatewayAdapterCtpSetting: GatewayAdapterCtpSetting =
        JsonUtils.readToObject(getGatewaySetting().adapterSettingJsonString!!, GatewayAdapterCtpSetting::class.java)

    val contractMap = HashMap<String, Contract>()

    init {

        if (getGatewaySetting().gatewayType == GatewayTypeEnum.Trade) {
            tdSpi = TdSpi(this)
        } else if (getGatewaySetting().gatewayType == GatewayTypeEnum.Quote) {
            mdSpi = MdSpi(this)
        } else {
            mdSpi = MdSpi(this)
            tdSpi = TdSpi(this)
        }
    }


    override fun subscribe(contract: Contract): Boolean {
        return if (getGatewaySetting().gatewayType == GatewayTypeEnum.Quote || getGatewaySetting().gatewayType == GatewayTypeEnum.TradeAndQuote) {
            if (mdSpi == null) {
                logger.error(logInfo + "行情接口尚未初始化或已断开")
                false
            } else {
                // 如果网关类型仅为行情,那就无法通过交易接口拿到合约信息，以订阅时的合约信息为准
                if (getGatewaySetting().gatewayType == GatewayTypeEnum.Quote) {
                    contractMap[contract.symbol] = contract
                }
                mdSpi!!.subscribe(contract.symbol)
            }
        } else {
            logger.warn(logInfo + "不包含订阅功能")
            false
        }
    }

    override fun unsubscribe(contract: Contract): Boolean {
        return if (getGatewaySetting().gatewayType == GatewayTypeEnum.Quote || getGatewaySetting().gatewayType == GatewayTypeEnum.TradeAndQuote) {
            if (mdSpi == null) {
                logger.error(logInfo + "行情接口尚未初始化或已断开")
                false
            } else {
                mdSpi!!.unsubscribe(contract.symbol)
            }
        } else {
            logger.warn(logInfo + "不包含取消订阅功能")
            false
        }
    }

    override fun submitOrder(insertOrder: InsertOrder): String {
        return if (getGatewaySetting().gatewayType == GatewayTypeEnum.Trade || getGatewaySetting().gatewayType == GatewayTypeEnum.TradeAndQuote) {
            if (tdSpi == null || !tdSpi!!.isConnected) {
                logger.error(logInfo + "交易接口尚未初始化或已断开")
                ""
            } else {
                tdSpi!!.submitOrder(insertOrder) ?: ""
            }
        } else {
            logger.warn(logInfo + "不包含提交定单功能")
            ""
        }
    }

    override fun cancelOrder(cancelOrder: CancelOrder): Boolean {
        return if (getGatewaySetting().gatewayType == GatewayTypeEnum.Trade || getGatewaySetting().gatewayType == GatewayTypeEnum.TradeAndQuote) {
            if (tdSpi == null || !tdSpi!!.isConnected) {
                logger.error(logInfo + "交易接口尚未初始化或已断开")
                false
            } else {
                tdSpi!!.cancelOrder(cancelOrder)
            }
        } else {
            logger.warn(logInfo + "不包含撤销定单功能")
            false
        }
    }

    override fun disconnect() {
        connectInitiateTimestamp = 0
        val tdSpiForDisconnect = tdSpi
        val mdSpiForDisconnect = mdSpi
        tdSpi = null
        mdSpi = null
        Thread {
            try {
                if (getGatewaySetting().gatewayType == GatewayTypeEnum.Trade && tdSpiForDisconnect != null) {
                    tdSpiForDisconnect.disconnect()
                } else if (getGatewaySetting().gatewayType == GatewayTypeEnum.Quote && mdSpiForDisconnect != null) {
                    mdSpiForDisconnect.disconnect()
                } else if (getGatewaySetting().gatewayType == GatewayTypeEnum.TradeAndQuote && tdSpiForDisconnect != null && mdSpiForDisconnect != null) {
                    tdSpiForDisconnect.disconnect()
                    mdSpiForDisconnect.disconnect()
                } else {
                    logger.error(logInfo + "检测到SPI实例为空")
                }
                logger.warn(logInfo + "异步断开操作完成")
            } catch (t: Throwable) {
                logger.error(logInfo + "异步断开操作错误", t)
            }
        }.start()
    }

    override fun connect() {
        connectInitiateTimestamp = System.currentTimeMillis()
        if (getGatewaySetting().gatewayType == GatewayTypeEnum.Trade) {
            if (tdSpi == null) {
                tdSpi = TdSpi(this)
            }
        } else if (getGatewaySetting().gatewayType == GatewayTypeEnum.Quote) {
            if (tdSpi == null) {
                mdSpi = MdSpi(this)
            }
        } else {
            if (tdSpi == null) {
                tdSpi = TdSpi(this)
            }
            if (mdSpi == null) {
                mdSpi = MdSpi(this)
            }
        }
        if (getGatewaySetting().gatewayType == GatewayTypeEnum.Trade && tdSpi != null) {
            tdSpi!!.connect()
        } else if (getGatewaySetting().gatewayType == GatewayTypeEnum.Quote && mdSpi != null) {
            mdSpi!!.connect()
        } else if (getGatewaySetting().gatewayType == GatewayTypeEnum.TradeAndQuote && tdSpi != null && mdSpi != null) {
            tdSpi!!.connect()
            mdSpi!!.connect()
        } else {
            logger.error(logInfo + "检测到SPI实例为空")
        }
    }

    override fun isConnected(): Boolean {
        if (getGatewaySetting().gatewayType == GatewayTypeEnum.Trade && tdSpi != null) {
            return tdSpi!!.isConnected
        } else if (getGatewaySetting().gatewayType == GatewayTypeEnum.Quote && mdSpi != null) {
            return mdSpi!!.isConnected
        } else if (getGatewaySetting().gatewayType == GatewayTypeEnum.TradeAndQuote && tdSpi != null && mdSpi != null) {
            return tdSpi!!.isConnected && mdSpi!!.isConnected
        } else {
            logger.error(logInfo + "检测到SPI实例为空")
        }
        return false
    }

    override fun getGatewayStatus(): GatewayStatus {

        return GatewayStatus().apply {
            gatewayId = getGatewaySetting().id!!

            connectionStatus = if (isConnected()) {
                ConnectionStatusEnum.Connected
            } else {
                ConnectionStatusEnum.Disconnected
            }

            isAuthError = isAuthError()
        }
    }

}