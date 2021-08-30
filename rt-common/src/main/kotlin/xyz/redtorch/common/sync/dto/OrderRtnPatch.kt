package xyz.redtorch.common.sync.dto

import com.fasterxml.jackson.databind.JsonNode
import xyz.redtorch.common.trade.dto.Order

class OrderRtnPatch {
    var orderId: String? = null
    var jsonPath: JsonNode? = null
    var order: Order? = null
}