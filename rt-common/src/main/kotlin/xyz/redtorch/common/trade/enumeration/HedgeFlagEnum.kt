package xyz.redtorch.common.trade.enumeration

// HedgeFlagEnum是一个投机套保标志类型
enum class HedgeFlagEnum(val value: Int) {
    Unknown(0), // 未知
    Speculation(1), // 投机
    Arbitrage(2), // 套利
    Hedge(3), // 套保
    MarketMaker(4), // 做市商
    SpecHedge(5), // 第一腿投机第二腿套保 大商所专用
    HedgeSpec(6) // 第一腿套保第二腿投机  大商所专用
}