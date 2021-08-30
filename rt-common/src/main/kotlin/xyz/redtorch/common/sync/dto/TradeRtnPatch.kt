package xyz.redtorch.common.sync.dto

import com.fasterxml.jackson.databind.JsonNode
import xyz.redtorch.common.trade.dto.Trade

class TradeRtnPatch {
    var uniformSymbol: String? = null
    var jsonPath: JsonNode? = null
    var trade: Trade? = null
}