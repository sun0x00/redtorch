package xyz.redtorch.common.trade.enumeration

// OrderActionStatusEnum是一个报单操作状态类型
enum class OrderActionStatusEnum(val value: Int) {
    Unknown(0), // 未知
    Submitted(1), // 已经提交
    Accepted(2), // 已经接受
    Rejected(3) // 已经被拒绝
}