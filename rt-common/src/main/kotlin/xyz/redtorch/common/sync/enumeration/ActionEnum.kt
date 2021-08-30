package xyz.redtorch.common.sync.enumeration

enum class ActionEnum(val value: Int) {
    Unknown(0),
    Auth(1),
    AuthResult(2),
    TransactionMirrorPatch(3),
    QuoteMirrorPatch(4),
    NoticeRtn(5),
    TradeRtnPatch(6),
    OrderRtnPatch(7),
    TickRtnPatch(8),
    BaseMirrorPatch(9),
    PortfolioMirrorPatch(10),
    SlaveNodeStatusMirrorPatch(11),
    SlaveNodeSettingMirrorPatch(12),
    InsertOrder(13),
    CancelOrder(14),
    Subscribe(15),
    Unsubscribe(16)
}