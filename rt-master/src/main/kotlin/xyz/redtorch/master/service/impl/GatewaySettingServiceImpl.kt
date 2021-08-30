package xyz.redtorch.master.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.common.storage.po.SlaveNode
import xyz.redtorch.master.dao.GatewaySettingDao
import xyz.redtorch.master.service.GatewaySettingService
import xyz.redtorch.master.service.SlaveNodeService
import xyz.redtorch.master.web.socket.SlaveNodeWebSocketHandler

@Service
class GatewaySettingServiceImpl : GatewaySettingService {

    private val logger: Logger = LoggerFactory.getLogger(GatewaySettingServiceImpl::class.java)

    @Autowired
    lateinit var gatewaySettingDao: GatewaySettingDao

    @Autowired
    lateinit var slaveNodeService: SlaveNodeService

    @Autowired
    private lateinit var slaveNodeWebSocketHandler: SlaveNodeWebSocketHandler

    override fun getGatewaySettingById(gatewayId: String): GatewaySetting? {
        return gatewaySettingDao.queryById(gatewayId)
    }

    override fun getGatewaySettingList(): List<GatewaySetting> {
        val slaveNodeList = slaveNodeService.getSlaveNodeList()
        val slaveNodeMap = HashMap<String, SlaveNode>()
        for (slaveNode in slaveNodeList) {
            slaveNodeMap[slaveNode.id!!] = slaveNode
        }
        val gatewaySettingList = gatewaySettingDao.queryList()
        for (gatewaySetting in gatewaySettingList) {
            if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Connecting) {
                if (slaveNodeMap.containsKey(gatewaySetting.targetSlaveNodeId) && slaveNodeMap[gatewaySetting.targetSlaveNodeId]!!.connectionStatus == ConnectionStatusEnum.Connected) {
                    val transceiver = slaveNodeWebSocketHandler.slaveNodeIdToTransceiverMapGet(gatewaySetting.targetSlaveNodeId)
                    if (transceiver != null) {
                        val gatewayStatus = transceiver.slaveNodeReportMirror.gatewayStatusMap[gatewaySetting.id]
                        if (gatewayStatus != null && gatewayStatus.connectionStatus == ConnectionStatusEnum.Connected) {
                            gatewaySetting.connectionStatus = ConnectionStatusEnum.Connected
                        }
                    }
                }
            } else if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnecting) {
                if (slaveNodeMap.containsKey(gatewaySetting.targetSlaveNodeId) && slaveNodeMap[gatewaySetting.targetSlaveNodeId]!!.connectionStatus == ConnectionStatusEnum.Connected) {
                    val transceiver = slaveNodeWebSocketHandler.slaveNodeIdToTransceiverMapGet(gatewaySetting.targetSlaveNodeId)
                    if (transceiver != null) {
                        val gatewayStatus = transceiver.slaveNodeReportMirror.gatewayStatusMap[gatewaySetting.id]
                        if (gatewayStatus == null || gatewayStatus.connectionStatus == ConnectionStatusEnum.Disconnected) {
                            gatewaySetting.connectionStatus = ConnectionStatusEnum.Disconnected
                        }
                    }
                }
            }
        }
        return gatewaySettingList
    }

    override fun upsertGatewaySettingById(gatewaySetting: GatewaySetting) {
        if (gatewaySetting.description == null) {
            gatewaySetting.description = ""
        }
        if (gatewaySetting.name == null) {
            gatewaySetting.name = ""
        }
        if (gatewaySetting.implementClassName == null) {
            gatewaySetting.implementClassName = ""
        }

        if (gatewaySetting.id.isNullOrBlank()) {
            val gatewaySettingList: List<GatewaySetting> = gatewaySettingDao.queryList()
            if (gatewaySettingList.isEmpty()) {
                // 如果数据库中没有GatewaySetting,使用1001作为起始值创建GatewaySetting
                gatewaySetting.id = "1001"
            } else {
                // 如果数据库中有GatewaySetting,尝试寻找GatewaySettingID最大值
                val intGatewaySettingIdSet: MutableSet<Int> = HashSet()
                var maxIntGatewaySettingId = 1001
                for (tempGatewaySetting in gatewaySettingList) {
                    val tempIntGatewaySettingId = Integer.valueOf(tempGatewaySetting.id)
                    intGatewaySettingIdSet.add(tempIntGatewaySettingId)
                    if (tempIntGatewaySettingId > maxIntGatewaySettingId) {
                        maxIntGatewaySettingId = tempIntGatewaySettingId
                    }
                }

                // 极端情况下+1有可能溢出
                var newIntGatewaySettingId = maxIntGatewaySettingId + 1

                // 使用循环进行再次校验
                // 一是避免极端情况下可能产生的重复
                // 二是不允许自动生成[0,1000]之间的ID，预留
                while (intGatewaySettingIdSet.contains(newIntGatewaySettingId) || newIntGatewaySettingId in 0..1000) {
                    newIntGatewaySettingId++
                }
                gatewaySetting.id = newIntGatewaySettingId.toString()
            }
        }


        // 切换存储状态
        if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Connected) {
            gatewaySetting.connectionStatus = ConnectionStatusEnum.Connecting
        } else if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnected) {
            gatewaySetting.connectionStatus = ConnectionStatusEnum.Disconnecting
        }

        gatewaySettingDao.upsert(gatewaySetting)

    }

    override fun deleteGatewayById(gatewayId: String) {
        gatewaySettingDao.deleteById(gatewayId)
    }

    override fun connectGatewayById(gatewayId: String) {
        val gatewaySetting: GatewaySetting? = getGatewaySettingById(gatewayId)
        if (gatewaySetting == null) {
            logger.warn("根据网关Id连接网关,未找到网关记录,gatewayId={}", gatewayId)
            return
        }
        if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnected //
            || gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnecting //
            || gatewaySetting.connectionStatus == ConnectionStatusEnum.Unknown //
        ) {
            gatewaySetting.connectionStatus = ConnectionStatusEnum.Connecting
            upsertGatewaySettingById(gatewaySetting)
        } else {
            logger.info("根据网关Id连接网关,当前状态无需更改,gatewayId={},connectionStatus={}", gatewayId, gatewaySetting.connectionStatus)
        }
    }

    override fun disconnectGatewayById(gatewayId: String) {
        val gatewaySetting: GatewaySetting? = getGatewaySettingById(gatewayId)
        if (gatewaySetting == null) {
            logger.warn("根据网关Id连接网关,未找到网关记录,Id={}", gatewayId)
            return
        }
        if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Connected //
            || gatewaySetting.connectionStatus == ConnectionStatusEnum.Connecting //
            || gatewaySetting.connectionStatus == ConnectionStatusEnum.Unknown //
        ) {
            gatewaySetting.connectionStatus = ConnectionStatusEnum.Disconnecting
            upsertGatewaySettingById(gatewaySetting)
        } else {
            logger.info("根据网关Id断开网关,当前状态无需更改,gatewayId={},connectionStatus={}", gatewayId, gatewaySetting.connectionStatus)
        }
    }

    override fun connectAllGateways() {
        val gatewaySettingList: List<GatewaySetting> = gatewaySettingDao.queryList()
        for (gatewaySetting in gatewaySettingList) {
            if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnecting //
                || gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnected //
                || gatewaySetting.connectionStatus == ConnectionStatusEnum.Unknown //
            ) {
                gatewaySetting.connectionStatus = ConnectionStatusEnum.Connecting
                upsertGatewaySettingById(gatewaySetting)
            } else {
                logger.info("连接全部网关,当前状态无需更改,gatewayId={},connectionStatus={}", gatewaySetting.id, gatewaySetting.connectionStatus)
            }
        }
    }

    override fun disconnectAllGateways() {
        val gatewaySettingList: List<GatewaySetting> = gatewaySettingDao.queryList()
        for (gatewaySetting in gatewaySettingList) {
            if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Connecting //
                || gatewaySetting.connectionStatus == ConnectionStatusEnum.Connected //
                || gatewaySetting.connectionStatus == ConnectionStatusEnum.Unknown //
            ) {
                gatewaySetting.connectionStatus = ConnectionStatusEnum.Disconnecting
                upsertGatewaySettingById(gatewaySetting)
            } else {
                logger.info("断开全部网关,当前状态无需更改,gatewayId={},connectionStatus={}", gatewaySetting.id, gatewaySetting.connectionStatus)
            }
        }
    }

    override fun updateGatewaySettingDescriptionById(gatewayId: String, description: String) {
        val gatewaySetting = gatewaySettingDao.queryById(gatewayId)
        if (gatewaySetting != null) {
            gatewaySetting.description = description
            gatewaySettingDao.upsert(gatewaySetting)
        } else {
            logger.warn("更新网关描述失败,未查出节点,gatewayId={}", gatewayId)
        }
    }

    override fun deleteGatewaySettingById(gatewayId: String) {
        gatewaySettingDao.deleteById(gatewayId)
    }

}