package xyz.redtorch.common.trade.enumeration

// ForceCloseReasonEnum是一个强平原因类型
enum class ForceCloseReasonEnum(val value: Int) {
    Unknown(0), // 未知
    NotForceClose(1), // 非强平
    LackDeposit(2), // 资金不足
    ClientOverPositionLimit(3), // 客户超仓
    MemberOverPositionLimit(4), // 会员超仓
    NotMultiple(5), // 持仓非整数倍
    Violation(6), // 违规
    Other(7), // 其它
    PersonDeliver(8) // 自然人临近交割
}