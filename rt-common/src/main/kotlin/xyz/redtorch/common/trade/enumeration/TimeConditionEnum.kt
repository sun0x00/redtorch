package xyz.redtorch.common.trade.enumeration

// TimeConditionEnum是一个有效期类型类型
enum class TimeConditionEnum(val value: Int) {
    Unknown(0), // 未知
    IOC(1), // 立即完成，否则撤销
    GFS(2), // 本节有效
    GFD(3), // 当日有效
    GTD(4), // 指定日期前有效
    GTC(5), // 撤销前有效
    GFA(6) // 集合竞价有效
}