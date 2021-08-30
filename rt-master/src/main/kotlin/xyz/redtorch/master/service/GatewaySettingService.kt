package xyz.redtorch.master.service

import xyz.redtorch.common.storage.po.GatewaySetting

interface GatewaySettingService {
    fun getGatewaySettingById(gatewayId: String): GatewaySetting?

    fun upsertGatewaySettingById(gatewaySetting: GatewaySetting)

    fun getGatewaySettingList(): List<GatewaySetting>

    fun deleteGatewayById(gatewayId: String)

    fun connectGatewayById(gatewayId: String)

    fun disconnectGatewayById(gatewayId: String)

    fun disconnectAllGateways()

    fun connectAllGateways()

    fun updateGatewaySettingDescriptionById(gatewayId: String, description: String)

    fun deleteGatewaySettingById(gatewayId: String)
}