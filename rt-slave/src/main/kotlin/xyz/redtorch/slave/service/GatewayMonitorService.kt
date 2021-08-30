package xyz.redtorch.slave.service

import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.gateway.Gateway

interface GatewayMonitorService {
    fun disconnectGateway(gatewayId: String)

    fun connectGateway(gatewaySetting: GatewaySetting)

    fun getGatewayList(): List<Gateway>

    fun getGateway(gatewayId: String): Gateway?

    fun getConnectedGatewayIdList(): List<String>

    fun updateGatewaySetting(gatewaySettingMap: Map<String, GatewaySetting>)

    fun updateSubscribedContract(subscribedContractMap: Map<String, Contract>)
}