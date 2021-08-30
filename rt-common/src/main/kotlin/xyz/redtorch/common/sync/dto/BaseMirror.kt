package xyz.redtorch.common.sync.dto

import xyz.redtorch.common.trade.dto.Contract

class BaseMirror {
    var contractMap = HashMap<String, Contract>()

    fun clone(): BaseMirror {
        val baseMirror = BaseMirror()
        baseMirror.contractMap.putAll(contractMap)
        return baseMirror
    }
}