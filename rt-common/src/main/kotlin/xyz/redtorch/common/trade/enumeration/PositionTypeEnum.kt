package xyz.redtorch.common.trade.enumeration

// PositionTypeEnum是一个持仓类型类型
enum class PositionTypeEnum(val value: Int) {
    Unknown(0), // 未知
    Net(1),// 净持仓
    Gross(2) // 综合持仓
}