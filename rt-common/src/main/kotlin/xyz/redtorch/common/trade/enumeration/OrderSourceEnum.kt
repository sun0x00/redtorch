package xyz.redtorch.common.trade.enumeration

// OrderSourceEnum是一个报单来源类型
enum class OrderSourceEnum(val value: Int) {
    Unknown(0), // 未知
    Participant(1), // 来自参与者
    Administrator(2) // 来自管理员
}