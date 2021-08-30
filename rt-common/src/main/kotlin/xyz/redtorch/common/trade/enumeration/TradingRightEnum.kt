package xyz.redtorch.common.trade.enumeration

// TradingRightEnum是一个交易权限类型
enum class TradingRightEnum(val value: Int) {
    Unknown(0), // 未知
    Allow(1), // 可以交易
    CloseOnly(2), // 只能平仓
    Forbidden(3) // 不能交易
}