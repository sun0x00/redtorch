package xyz.redtorch.common.trade.enumeration

// OrderSubmitStatusEnum是一个报单提交状态类型
enum class OrderSubmitStatusEnum(val value: Int) {
    Unknown(0), // 未知
    InsertSubmitted(1), // 已经提交
    CancelSubmitted(2), // 撤单已经提交
    ModifySubmitted(3), // 修改已经提交
    Accepted(4), // 已经接受
    InsertRejected(5), // 报单已经被拒绝
    CancelRejected(6), // 撤单已经被拒绝
    ModifyRejected(7) // 改单已经被拒绝
}