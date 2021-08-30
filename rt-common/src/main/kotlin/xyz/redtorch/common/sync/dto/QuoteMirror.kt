package xyz.redtorch.common.sync.dto

import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.trade.dto.Tick

class QuoteMirror {
    var subscribedMap = HashMap<String, Contract>()
    var tickMap = HashMap<String, Tick>()

    fun clone(): QuoteMirror {
        val quoteMirror = QuoteMirror()
        quoteMirror.subscribedMap.putAll(subscribedMap)
        quoteMirror.tickMap.putAll(tickMap)
        return quoteMirror
    }
}