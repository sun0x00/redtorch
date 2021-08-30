package xyz.redtorch.common.trade.enumeration

// OffsetFlagEnum是一个开平标志类型
enum class OffsetFlagEnum(val value: Int) {
    Unknown(0), // 未知
    Open(1), // 开仓
    Close(2), // 平仓
    ForceClose(3), // 强平
    CloseToday(4), // 平今
    CloseYesterday(5), // 平昨
    ForceOff(6), // 强减
    LocalForceClose(7) // 本地强平
}