package xyz.redtorch.common.sync.dto

import com.fasterxml.jackson.databind.JsonNode
import xyz.redtorch.common.trade.dto.Tick

class TickRtnPatch {
    var uniformSymbol: String? = null
    var jsonPath: JsonNode? = null
    var tick: Tick? = null
}