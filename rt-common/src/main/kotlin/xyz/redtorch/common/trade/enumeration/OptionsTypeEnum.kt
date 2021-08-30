package xyz.redtorch.common.trade.enumeration

// OptionsTypeEnum是一个期权类型类型
enum class OptionsTypeEnum(val value: Int) {
    Unknown(0), // 未知
    CallOptions(1), // 看涨
    PutOptions(2) // 看跌
}