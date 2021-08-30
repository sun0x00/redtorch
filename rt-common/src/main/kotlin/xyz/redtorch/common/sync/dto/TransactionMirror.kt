package xyz.redtorch.common.sync.dto

import xyz.redtorch.common.trade.dto.Order
import xyz.redtorch.common.trade.dto.Trade

class TransactionMirror {
    var orderMap = HashMap<String, Order>()
    var tradeMap = HashMap<String, Trade>()

    fun clone(): TransactionMirror {
        val transactionMirror = TransactionMirror()
        transactionMirror.tradeMap.putAll(tradeMap)
        transactionMirror.orderMap.putAll(orderMap)
        return transactionMirror
    }
}