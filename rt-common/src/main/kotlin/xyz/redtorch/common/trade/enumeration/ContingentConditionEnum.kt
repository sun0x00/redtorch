package xyz.redtorch.common.trade.enumeration

// ContingentConditionEnum是一个触发条件类型
enum class ContingentConditionEnum(val value: Int) {
    Unknown(0), // 未知
    Immediately(1), // 立即
    Touch(2), // 止损
    TouchProfit(3), // 止赢
    ParkedOrder(4), // 预埋单
    LastPriceGreaterThanStopPrice(5), // 最新价大于条件价
    LastPriceGreaterEqualStopPrice(6), // 最新价大于等于条件价
    LastPriceLesserThanStopPrice(7), // 最新价小于条件价
    LastPriceLesserEqualStopPrice(8), // 最新价小于等于条件价
    AskPriceGreaterThanStopPrice(9), // 卖一价大于条件价
    AskPriceGreaterEqualStopPrice(10), // 卖一价大于等于条件价 
    AskPriceLesserThanStopPrice(11), // 卖一价小于条件价
    AskPriceLesserEqualStopPrice(12), // 卖一价小于等于条件价
    BidPriceGreaterThanStopPrice(13), // 买一价大于条件价
    BidPriceGreaterEqualStopPrice(14), // 买一价大于等于条件价
    BidPriceLesserThanStopPrice(15), // 买一价小于条件价
    BidPriceLesserEqualStopPrice(16), // 买一价小于等于条件价
    LocalLastPriceLesserEqualStopPrice(17), // (本地)最新价小于等于条件价
    LocalLastPriceGreaterEqualStopPrice(18) // (本地)最新价大于等于条件价
}