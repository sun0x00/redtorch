package xyz.redtorch.common.trade.enumeration

// PositionDirectionEnum是一个持仓多空方向类型
enum class PositionDirectionEnum(val value: Int) {
    Unknown(0),
    Net(1),
    Long(2),
    Short(3)
}