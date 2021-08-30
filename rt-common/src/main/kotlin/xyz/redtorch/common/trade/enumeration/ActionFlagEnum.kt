package xyz.redtorch.common.trade.enumeration

// ActionFlagEnum是一个操作标志类型
enum class ActionFlagEnum(val value: Int) {
    Unknown(0), // 未知
    Delete(1), // 删除
    Modify(2) // 修改
}