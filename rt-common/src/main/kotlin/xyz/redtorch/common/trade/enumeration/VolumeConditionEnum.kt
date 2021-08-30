package xyz.redtorch.common.trade.enumeration

// VolumeConditionEnum是一个成交量类型类型
enum class VolumeConditionEnum(val value: Int) {
    Unknown(0), // 未知
    AV(1), // 任何数量
    MV(2), // 最小数量
    CV(3) // 全部数量
}