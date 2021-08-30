package xyz.redtorch.master.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.redtorch.common.sync.dto.CancelOrder
import xyz.redtorch.common.sync.dto.InsertOrder
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.master.service.GatewaySettingService
import xyz.redtorch.master.service.SlaveNodeService
import xyz.redtorch.master.service.SystemService
import xyz.redtorch.master.service.UserService
import xyz.redtorch.master.web.socket.SlaveNodeWebSocketHandler
import xyz.redtorch.master.web.socket.TradeClientWebSocketHandler

@Service
class SystemServiceImpl : SystemService, InitializingBean {

    private val logger = LoggerFactory.getLogger(SystemServiceImpl::class.java)

    @Autowired
    private lateinit var slaveNodeService: SlaveNodeService

    @Autowired
    private lateinit var gatewaySettingService: GatewaySettingService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var slaveNodeWebSocketHandler: SlaveNodeWebSocketHandler

    @Autowired
    private lateinit var tradeClientWebSocketHandler: TradeClientWebSocketHandler

    private var lastSyncSlaveNodeSettingTimestamp = 0L

    private var gatewayIdToSlaveNodeIdMap = HashMap<String, String>()

    override fun afterPropertiesSet() {
        Thread {

            Thread.currentThread().name = "RT-SystemServiceImpl-SyncSlaveNodeSettingMirror"

            while (!Thread.currentThread().isInterrupted) {

                try {
                    // 距离上次更新配置小于10秒则不更新配置
                    if (System.currentTimeMillis() - lastSyncSlaveNodeSettingTimestamp < 10000) {
                        // 休眠100毫秒
                        Thread.sleep(100)
                        continue
                    } else {
                        val slaveNodeList = slaveNodeService.getSlaveNodeList()
                        val gatewaySettingList = gatewaySettingService.getGatewaySettingList()
                        val gatewayIdToSlaveNodeIdMap = HashMap<String, String>()
                        for (gatewaySetting in gatewaySettingList) {
                            gatewaySetting.id?.let {
                                gatewayIdToSlaveNodeIdMap[it] = gatewaySetting.targetSlaveNodeId
                            }
                        }
                        this.gatewayIdToSlaveNodeIdMap = gatewayIdToSlaveNodeIdMap

                        val subscribedList: List<Contract> = ArrayList(tradeClientWebSocketHandler.subscribedMap.values)
                        for (slaveNode in slaveNodeList) {
                            val transceiver = slaveNodeWebSocketHandler.slaveNodeIdToTransceiverMapGet(slaveNode.id!!)
                            if (transceiver != null) {
                                val filteredGatewaySettingList = gatewaySettingList.filter { it.targetSlaveNodeId == slaveNode.id }.toList()
                                try {
                                    transceiver.updateSlaveNodeSettingMirror(subscribedList, filteredGatewaySettingList)
                                } catch (e: Exception) {
                                    logger.error("更新节点配置异常,slaveNodeId={}", transceiver.slaveNodeId, e)
                                }
                            }
                        }

                        lastSyncSlaveNodeSettingTimestamp = System.currentTimeMillis()
                    }
                } catch (e: Exception) {
                    logger.error("同步数据异常", e)
                }
            }
        }.start()
    }

    override fun syncSlaveNodeSettingMirror() {
        // 触发尽快更新配置
        lastSyncSlaveNodeSettingTimestamp = 0
    }

    override fun cancelOrder(cancelOrder: CancelOrder) {
        gatewayIdToSlaveNodeIdMap[cancelOrder.gatewayId].let {
            if (it != null) {
                val transceiver = slaveNodeWebSocketHandler.slaveNodeIdToTransceiverMapGet(it)

                if (transceiver != null) {
                    transceiver.cancelOrder(cancelOrder)
                } else {
                    logger.error("无法执行撤销定单,节点Transceiver不存在,slaveNodeId={},CancelOrder={}", it, JsonUtils.writeToJsonString(cancelOrder))
                }
            } else {
                logger.error("无法执行撤销定单,无法通过网关ID找到节点ID,CancelOrder={}", JsonUtils.writeToJsonString(cancelOrder))
            }

        }
    }

    override fun submitOrder(insertOrder: InsertOrder) {
        gatewayIdToSlaveNodeIdMap[insertOrder.gatewayId].let {
            if (it != null) {
                val transceiver = slaveNodeWebSocketHandler.slaveNodeIdToTransceiverMapGet(it)

                if (transceiver != null) {
                    transceiver.submitOrder(insertOrder)
                } else {
                    logger.error("无法执行发送定单,节点Transceiver不存在,slaveNodeId={},InsertOrder={}", it, JsonUtils.writeToJsonString(insertOrder))
                }
            } else {
                logger.error("无法执行发送定单,无法通过网关ID找到节点ID,InsertOrder={}", JsonUtils.writeToJsonString(insertOrder))
            }
        }
    }
}