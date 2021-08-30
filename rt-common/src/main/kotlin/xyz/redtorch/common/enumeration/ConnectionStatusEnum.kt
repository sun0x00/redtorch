package xyz.redtorch.common.enumeration

enum class ConnectionStatusEnum(val value: Int) {
    Unknown(0),
    Connecting(1),
    Connected(2),
    Disconnecting(3),
    Disconnected(4)
}