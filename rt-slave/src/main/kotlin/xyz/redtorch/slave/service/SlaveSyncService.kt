package xyz.redtorch.slave.service

import xyz.redtorch.common.sync.dto.GatewayStatus
import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.trade.dto.*

interface SlaveSyncService {
    fun updateTransactionMirror(orderMap: MutableMap<String, Order>, tradeMap: MutableMap<String, Trade>)
    fun updateQuoteMirror(tickMap: MutableMap<String, Tick>)
    fun updateBaseMirror(contractMap: MutableMap<String, Contract>)
    fun updatePortfolioMirror(accountMap: MutableMap<String, Account>, positionMap: MutableMap<String, Position>)
    fun updateSlaveNodeReportMirror(gatewayStatusList: ArrayList<GatewayStatus>)
    fun sendNotice(notice: Notice)
    fun sendTick(tick: Tick)
    fun sendTrade(trade: Trade)
    fun sendOrder(order: Order)
}