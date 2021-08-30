package xyz.redtorch.slave.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.event.service.EventService
import xyz.redtorch.common.storage.enumeration.GatewayTypeEnum
import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.common.sync.dto.GatewayStatus
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.gateway.Gateway
import xyz.redtorch.slave.service.GatewayMonitorService
import xyz.redtorch.slave.service.SlaveCacheService
import xyz.redtorch.slave.service.SlaveSyncService
import java.time.LocalDateTime

@Service
class GatewayMonitorServiceImpl : GatewayMonitorService, InitializingBean {

    private val logger = LoggerFactory.getLogger(GatewayMonitorServiceImpl::class.java)

    private val gatewayMap = HashMap<String, Gateway>()
    private val authErrorGatewayVersionSet = HashSet<String>()
    private var gatewaySettingMap: Map<String, GatewaySetting> = HashMap()
    private var subscribedContractMap: Map<String, Contract> = HashMap()

    @Autowired
    private lateinit var eventService: EventService

    @Autowired
    private lateinit var slaveCacheService: SlaveCacheService

    @Autowired
    private lateinit var slaveSyncService: SlaveSyncService

    override fun disconnectGateway(gatewayId: String) {
        // 清除缓存
        slaveCacheService.clearAllCachesByGatewayId(gatewayId)

        if (gatewayMap.containsKey(gatewayId)) {
            gatewayMap[gatewayId]?.let {
                it.disconnect()
                gatewayMap.remove(gatewayId)
            }
        } else {
            logger.error("无法断开网关，实例不存在，Id={}", gatewayId)
        }

    }

    override fun connectGateway(gatewaySetting: GatewaySetting) {
        val authErrorFlagKey = "${gatewaySetting.id}@${gatewaySetting.version}"
        if (authErrorGatewayVersionSet.contains(authErrorFlagKey)) {
            logger.info(
                "拒绝连接网关,配置被标记为认证错误,网关ID:{},网关名称:{}",
                gatewaySetting.id,
                gatewaySetting.name
            )
            return
        }

        val gatewayId = gatewaySetting.id!!
        val gatewayName = gatewaySetting.name!!
        logger.info("连接网关,网关ID:{},网关名称:{}", gatewayId, gatewayName)
        if (gatewayMap.containsKey(gatewayId)) {
            logger.warn("网关已在缓存中存在,网关ID:{},网关名称:{}", gatewayId, gatewayName)
            gatewayMap[gatewaySetting.id]?.let {
                if (it.isConnected()) {
                    logger.error("连接网关错误,网关ID:{},网关名称:{},缓存中的网关处于连接状态", gatewayId, gatewayName)
                    return
                } else {
                    logger.warn("缓存中的网关已经断开,再次调用网关断开并删除,网关ID:{},网关名称:{}", gatewayId, gatewayName)
                    disconnectGateway(gatewayId)
                }
            }
        }

        val gatewayClassName: String = gatewaySetting.implementClassName!!
        try {
            logger.info("使用反射创建网关实例,网关ID:{},网关名称:{}", gatewayId, gatewayName)
            val clazz = Class.forName(gatewayClassName)
            val c = clazz.getConstructor(EventService::class.java, GatewaySetting::class.java)
            val gateway = c.newInstance(eventService, gatewaySetting) as Gateway
            logger.info("调用网关连接,网关ID:{},网关名称:{}", gatewayId, gatewayName)
            gateway.connect()
            logger.info("重新订阅合约,网关ID:{},网关名称:{}", gatewayId, gatewayName)
            // 重新订阅之前的合约
            for (contract in subscribedContractMap.values) {
                gateway.subscribe(contract)
            }
            gatewayMap[gatewayId] = gateway
        } catch (e: Exception) {
            logger.error("连接网关错误,创建网关实例发生异常,网关ID:{},Java实现类:{}", gatewayId, gatewayClassName, e)
        }
        logger.warn("连接网关完成,网关ID:{},网关名称:{}", gatewayId, gatewayName)
    }

    override fun getGatewayList(): List<Gateway> {
        return ArrayList(gatewayMap.values)
    }

    override fun getGateway(gatewayId: String): Gateway? {
        return gatewayMap[gatewayId]
    }

    override fun getConnectedGatewayIdList(): List<String> {
        val gatewayIdList = ArrayList<String>()
        for (gateway in gatewayMap.values) {
            if (gateway.isConnected()) {
                gatewayIdList.add(gateway.getGatewaySetting().id!!)
            }
        }
        return gatewayIdList
    }

    override fun updateGatewaySetting(gatewaySettingMap: Map<String, GatewaySetting>) {
        this.gatewaySettingMap = gatewaySettingMap
    }

    override fun updateSubscribedContract(subscribedContractMap: Map<String, Contract>) {
        // 查找新订阅数据中不存在的合约
        val unsubscribeContractList = ArrayList<Contract>()
        for (contract in this.subscribedContractMap.values) {
            if (!subscribedContractMap.containsKey(contract.uniformSymbol)) {
                unsubscribeContractList.add(contract)
            }
        }
        // 取消订阅
        for (contract in unsubscribeContractList) {
            for (gateway in gatewayMap.values) {
                if (gateway.getGatewaySetting().gatewayType == GatewayTypeEnum.TradeAndQuote
                    || gateway.getGatewaySetting().gatewayType == GatewayTypeEnum.Quote
                ) {
                    try {
                        gateway.unsubscribe(contract)
                    } catch (e: Exception) {
                        logger.error("网关{}取消订阅发生异常", gateway.getGatewaySetting().id, e)
                    }
                }
            }
        }

        // 订阅
        for (contract in subscribedContractMap.values) {
            // 非已订阅
            if (!this.subscribedContractMap.containsKey(contract.uniformSymbol)) {
                for (gateway in gatewayMap.values) {
                    if (gateway.getGatewaySetting().gatewayType == GatewayTypeEnum.TradeAndQuote
                        || gateway.getGatewaySetting().gatewayType == GatewayTypeEnum.Quote
                    ) {

                        try {
                            gateway.subscribe(contract)
                        } catch (e: Exception) {
                            logger.error("网关{}订阅发生异常", gateway.getGatewaySetting().id, e)
                        }
                    }
                }
            }
        }


        this.subscribedContractMap = subscribedContractMap

        slaveCacheService.filterTickMapByUniformSymbolSet(subscribedContractMap.keys)

    }

    override fun afterPropertiesSet() {

        // 以下线程用于自动监控Gateway状态,同步状态数据
        Thread {
            var startTime = System.currentTimeMillis()
            while (!Thread.currentThread().isInterrupted) {
                // 三秒检查一次
                if (System.currentTimeMillis() - startTime < 3000L) {
                    try {
                        Thread.sleep(50)
                        continue
                    } catch (e: InterruptedException) {
                        logger.error("定时同步节点数据等待检测到线程中断", e)
                        break
                    }
                }
                try {
                    val gatewayStatusList = ArrayList<GatewayStatus>()
                    var allDisconnected = true
                    for (gateway in getGatewayList()) {
                        if (gateway.isAuthError()) {
                            val authErrorFlagKey = "${gateway.getGatewaySetting().id!!}@${gateway.getGatewaySetting().version}"
                            authErrorGatewayVersionSet.add(authErrorFlagKey)
                        }
                        if (gateway.isConnected()) {
                            allDisconnected = false
                        }
                        gatewayStatusList.add(gateway.getGatewayStatus())
                    }

                    slaveSyncService.updateSlaveNodeReportMirror(gatewayStatusList)

                    // 如果所有的网关都断开,则清空合约缓存
                    if (allDisconnected) {
                        slaveCacheService.clearContractCaches()
                    }

                    val gatewayList: List<Gateway> = getGatewayList()
                    for (gateway in gatewayList) {
                        val gatewayId: String = gateway.getGatewaySetting().id!!
                        val gatewaySetting = gatewaySettingMap[gatewayId]
                        if (gatewaySetting == null) {
                            disconnectGateway(gatewayId)
                        } else {
                            if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnecting || gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnected) {
                                logger.info("网关{}状态变更,执行断开操作", gatewayId)
                                disconnectGateway(gatewayId)
                            } else if (gatewaySetting.version != gateway.getGatewaySetting().version) {
                                logger.info("网关{}配置变更,执行断开操作", gatewayId)
                                disconnectGateway(gatewayId)
                            }
                        }
                    }
                    for (gatewaySetting in gatewaySettingMap.values) {
                        if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Connecting || gatewaySetting.connectionStatus == ConnectionStatusEnum.Connected) {
                            val gateway = getGateway(gatewaySetting.id!!)
                            var shouldConnectFlag = false
                            if (gateway == null) {
                                shouldConnectFlag = true
                            } else if (!gateway.isConnected()) {
                                if (System.currentTimeMillis() - gateway.getLastConnectInitiateTimestamp() > 60 * 1000) {
                                    shouldConnectFlag = true
                                } else {
                                    logger.info(
                                        "网关{}上次连接尚未超时,上次开始连接时间戳{}",
                                        gateway.getGatewaySetting().id,
                                        gateway.getLastConnectInitiateTimestamp()
                                    )
                                }
                            }
                            if (shouldConnectFlag) {
                                var connectFlag = false
                                if (gatewaySetting.autoConnectTimeRanges.isNullOrBlank()) {
                                    connectFlag = true
                                } else {
                                    try {
                                        val timeRangeArray = gatewaySetting.autoConnectTimeRanges!!.split("#")
                                        for (timeRange in timeRangeArray) {
                                            val timesArray = timeRange.split("-")
                                            val timeBegin = timesArray[0].toInt()
                                            val timeEnd = timesArray[1].toInt()
                                            val ldt = LocalDateTime.now()
                                            val timeNow = ldt.minute + ldt.hour * 100
                                            if (timeNow in timeBegin..timeEnd) {
                                                connectFlag = true
                                                break
                                            }
                                        }
                                    } catch (e: Exception) {
                                        logger.error("网关{}解析自动连接时间范围异常", gatewaySetting.id, e)
                                        connectFlag = false
                                    }
                                }
                                if (connectFlag) {
                                    logger.info("网关{}执行连接操作", gatewaySetting.id)
                                    connectGateway(gatewaySetting)
                                    logger.info("等待3秒")
                                    //  序列化登录，间隔等待3秒
                                    Thread.sleep(3000L)
                                } else {
                                    // 清除缓存
                                    slaveCacheService.clearAllCachesByGatewayId(gatewaySetting.id!!)

                                    logger.info(
                                        "网关{}不在自动连接时间范围:{}",
                                        gatewaySetting.id,
                                        gatewaySetting.autoConnectTimeRanges
                                    )
                                }
                            }
                        }
                    }
                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("网关监控线程发生异常", e)
                }
                startTime = System.currentTimeMillis()
            }

        }.start()
    }
}