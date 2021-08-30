package xyz.redtorch.common.event.service

import xyz.redtorch.common.event.EventObserver
import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.trade.dto.*

interface EventService {
    fun emit(account: Account)
    fun emit(position: Position)
    fun emit(order: Order)
    fun emit(trade: Trade)
    fun emit(tick: Tick)
    fun emit(contract: Contract)
    fun emit(notice: Notice)
    fun emitContractList(contractList: List<Contract>)
    fun emitTradeList(tradeList: List<Trade>)
    fun emitOrderList(orderList: List<Order>)
    fun register(eventObserver: EventObserver)
}