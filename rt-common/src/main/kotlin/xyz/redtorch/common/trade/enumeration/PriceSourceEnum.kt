package xyz.redtorch.common.trade.enumeration

// PriceSourceEnum是一个成交价来源类型
enum class PriceSourceEnum(val value: Int) {
    Unknown(0), // 未知
    LastPrice(1), // 前成交价
    Buy(2), // 买委托价
    Sell(3), // 卖委托价
    OTC(4) // 场外成交价
}