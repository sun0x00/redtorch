package xyz.redtorch.common.sync.dto

import xyz.redtorch.common.enumeration.ConnectionStatusEnum

class GatewayStatus {
    var gatewayId = ""
    var connectionStatus = ConnectionStatusEnum.Unknown
    var isAuthError = false
}