package xyz.redtorch.common.trade.enumeration

// OrderPriceTypeEnum是一个报单价格条件类型
enum class OrderPriceTypeEnum(val value: Int) {
    Unknown(0), // 未知
    AnyPrice(1), // 任意价
    LimitPrice(2), // 限价
    BestPrice(3), // 最优价
    LastPrice(4), // 最新价
    LastPricePlusOneTicks(5), // 最新价浮动上浮1个ticks
    LastPricePlusTwoTicks(6), // 最新价浮动上浮2个ticks
    LastPricePlusThreeTicks(7), // 最新价浮动上浮3个ticks
    AskPrice1(8), // 卖一价
    AskPrice1PlusOneTicks(9), // 卖一价浮动上浮1个ticks
    AskPrice1PlusTwoTicks(10), // 卖一价浮动上浮2个ticks
    AskPrice1PlusThreeTicks(11), //  卖一价浮动上浮3个ticks
    BidPrice1(12), // 买一价
    BidPrice1PlusOneTicks(13), // 买一价浮动上浮1个ticks
    BidPrice1PlusTwoTicks(14), // 买一价浮动上浮2个ticks
    BidPrice1PlusThreeTicks(15), // 买一价浮动上浮3个ticks
    FiveLevelPrice(16) // 五档价
}