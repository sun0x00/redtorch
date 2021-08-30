package xyz.redtorch.common.trade.enumeration

// OrderTypeEnum是一个报单类型类型
enum class OrderTypeEnum(val value: Int) {
    Unknown(0), // 未知
    Normal(1), // 正常
    DeriveFromQuote(2), // 报价衍生
    DeriveFromCombination(3), // 组合衍生
    Combination(4), // 组合报单
    ConditionalOrder(5), // 条件单
    Swap(6) // 互换单
}