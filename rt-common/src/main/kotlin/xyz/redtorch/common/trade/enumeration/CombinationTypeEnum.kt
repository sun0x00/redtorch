package xyz.redtorch.common.trade.enumeration

// CombinationTypeEnum是一个组合类型类型
enum class CombinationTypeEnum(val value: Int) {
    Unknown(0), // 未知
    Future(1), // 期货组合
    BUL(2), // 垂直价差BUL
    BER(3), // 垂直价差BER
    STD(4), // 跨式组合
    STG(5), // 宽跨式组合
    PRT(6), // 备兑组合
    CLD(7) // 时间价差组合
}