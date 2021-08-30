package xyz.redtorch.common.sync.dto

class CancelOrder {
    var gatewayId: String = ""
    var originOrderId: String = "" // 原始定单ID
    var orderId: String = "" // 定单ID
}