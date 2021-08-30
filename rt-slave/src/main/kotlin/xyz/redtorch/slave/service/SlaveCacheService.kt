package xyz.redtorch.slave.service

import xyz.redtorch.common.trade.dto.Order

interface SlaveCacheService {
    fun filterTickMapByUniformSymbolSet(uniformSymbolSet: Set<String>)
    fun clearAllCachesByGatewayId(gatewayId: String)
    fun clearContractCaches()
    fun getOrderByOrderId(orderId: String): Order?
    fun syncQuoteMirror()
    fun syncTransactionMirror()
    fun syncPortfolioMirror()
    fun syncBaseMirror()
}