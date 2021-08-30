package xyz.redtorch.common.trade.client.service

import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.trade.dto.Order
import xyz.redtorch.common.trade.dto.Tick
import xyz.redtorch.common.trade.dto.Trade

interface TradeClientSyncServiceCallBack {
    fun handleTradeRtn(trade: Trade)
    fun handleOrderRtn(order: Order)
    fun handleTickRtn(tick: Tick)
    fun handleNoticeRtn(notice: Notice)
    fun handleAuthFailed()
}