package xyz.redtorch.gateway

import org.slf4j.LoggerFactory
import xyz.redtorch.common.event.service.EventService
import xyz.redtorch.common.storage.po.GatewaySetting

abstract class GatewayAbstract(private val eventService: EventService, private val gatewaySetting: GatewaySetting) : Gateway {
    companion object {
        private val logger = LoggerFactory.getLogger(GatewayAbstract::class.java)
    }

    var gatewayId: String = gatewaySetting.id!!
    var gatewayName: String = gatewaySetting.name!!
    var logInfo: String = "网关ID-[$gatewayId] 名称-[$gatewayName] [→] "
    var connectInitiateTimestamp: Long = 0
    var authErrorFlag = false

    init {
        logger.info(logInfo + "开始初始化")
    }

    override fun getEventService(): EventService {
        return eventService
    }

    override fun isAuthError(): Boolean {
        return authErrorFlag
    }

    override fun getLastConnectInitiateTimestamp(): Long {
        return connectInitiateTimestamp
    }

    override fun getGatewaySetting(): GatewaySetting {
        return gatewaySetting
    }

}