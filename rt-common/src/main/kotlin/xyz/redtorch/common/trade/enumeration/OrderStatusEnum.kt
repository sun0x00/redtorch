package xyz.redtorch.common.trade.enumeration

// OrderStatusEnum是一个报单状态类型
enum class OrderStatusEnum(val value: Int) {
    Unknown(0), // 未知
    AllTraded(1), // 全部成交
    PartTradedQueueing(2), // 部分成交还在队列中
    PartTradedNotQueueing(3), // 部分成交不在队列中
    NoTradeQueueing(4), // 未成交还在队列中
    NoTradeNotQueueing(5), // 未成交不在队列中
    Canceled(6), // 撤单
    NotTouched(7), // 尚未触发
    Touched(8), // 已触发
    Rejected(9) // 已拒绝
}