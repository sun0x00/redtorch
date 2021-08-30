package xyz.redtorch.common.event

import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.trade.dto.*

class Event {
    var eventType = EventTypeEnum.Unknown
    var tick: Tick? = null
    var trade: Trade? = null
    var order: Order? = null
    var position: Position? = null
    var account: Account? = null
    var contract: Contract? = null
    var notice: Notice? = null
    var tradeList: List<Trade>? = null
    var orderList: List<Order>? = null
    var contractList: List<Contract>? = null
}