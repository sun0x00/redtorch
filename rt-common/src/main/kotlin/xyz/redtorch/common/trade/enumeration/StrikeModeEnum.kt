package xyz.redtorch.common.trade.enumeration

// StrikeModeEnum是一个执行方式类型
enum class StrikeModeEnum(val value: Int) {
    Unknown(0), // 未知
    Continental(1), // 欧式
    American(2), // 美式
    Bermuda(3) // 百慕大
}