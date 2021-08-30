package xyz.redtorch.common.storage.po

import xyz.redtorch.common.enumeration.ConnectionStatusEnum

class SlaveNode : BasePo() {
    var token: String? = null
    var connectionStatus: ConnectionStatusEnum? = ConnectionStatusEnum.Unknown
    var description: String? = null
}