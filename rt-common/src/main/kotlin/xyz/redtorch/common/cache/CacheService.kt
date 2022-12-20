package xyz.redtorch.common.cache

import xyz.redtorch.common.trade.dto.*

interface CacheService {
    fun getTickList(): List<Tick>
    fun getPositionList(): List<Position>
    fun getAccountList(): List<Account>
    fun getOrderList(): List<Order>
    fun getTradeList(): List<Trade>
    fun getContractList(): List<Contract>
    fun updateAccountMap(accountMap: Map<String, Account>)
    fun updatePositionMap(positionMap: Map<String, Position>)
    fun updateContractMap(contractMap: Map<String, Contract>)
    fun updateTickMap(tickMap: Map<String, Tick>)
    fun updateOrderMap(orderMap: Map<String, Order>)
    fun updateTradeMap(tradeMap: Map<String, Trade>)
    fun updateTick(tick: Tick)
    fun updateTrade(trade: Trade)
    fun updateOrder(order: Order)
    fun queryAccountByAccountId(accountId: String): Account?
    fun queryPositionByPositionId(positionId: String): Position?
    fun queryTickByUniformSymbol(uniformSymbol: String): Tick?
    fun queryContractByUniformSymbol(uniformSymbol: String): Contract?
}