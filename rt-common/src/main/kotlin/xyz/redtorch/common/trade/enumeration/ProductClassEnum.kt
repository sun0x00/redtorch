package xyz.redtorch.common.trade.enumeration

// ProductClassEnum是一个产品类型类型
enum class ProductClassEnum(val value: Int) {
    Unknown(0), // 未知
    Equity(1),  // 股票
    Futures(2),  // 期货
    Options(3),  // 期权
    Index(4),  // 指数
    Combination(5),  // 组合
    Bond(6),  // 债券
    FOREX(7),  // 外汇
    Spot(8),  // 即期
    Defer(9),  // 展期
    ETF(10),  // ETF
    Warrants(11),  // 权证
    Spread(12),  // 价差
    Fund(13),  // 基金
    EFP(14),  // 期转现
    SpotOption(15),  // 现货期权
    TAS(16),  // TAS
    MI(17), // 金属指数
}