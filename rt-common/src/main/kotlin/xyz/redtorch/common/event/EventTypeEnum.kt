package xyz.redtorch.common.event

enum class EventTypeEnum(val value: Int) {
    Unknown(1),
    Tick(2),
    Trade(3),
    Order(4),
    Position(5),
    Account(6),
    Contract(7),
    TradeList(8),
    OrderList(9),
    ContractList(10),
    Notice(11)
}