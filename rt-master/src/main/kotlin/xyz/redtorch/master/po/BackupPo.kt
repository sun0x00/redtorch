package xyz.redtorch.master.po

import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.common.storage.po.SlaveNode
import xyz.redtorch.common.storage.po.User

class BackupPo {
    var gatewaySettingMap = HashMap<String,GatewaySetting>()
    var userMap = HashMap<String, User>()
    var slaveNodeMap = HashMap<String, SlaveNode>()
}