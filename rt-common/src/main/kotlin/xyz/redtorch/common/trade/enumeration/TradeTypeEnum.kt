package xyz.redtorch.common.trade.enumeration

// TradeTypeEnum是一个成交类型类型
enum class TradeTypeEnum(val value: Int) {
    Unknown(0), // 未知
    SplitCombination(1), // 组合持仓拆分为单一持仓,初始化不应包含该类型的持仓
    Common(2), // 普通成交
    OptionsExecution(3), // 期权执行
    OTC(4), // OTC成交 
    EFPDerived(5), // 期转现衍生成交
    CombinationDerived(6), // 组合衍生成交
    BlockTrade(7) // 大宗交易成交
}