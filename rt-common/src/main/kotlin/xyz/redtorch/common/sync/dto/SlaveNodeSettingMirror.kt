package xyz.redtorch.common.sync.dto

import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.common.trade.dto.Contract

class SlaveNodeSettingMirror {
    var subscribedMap = HashMap<String, Contract>()
    var gatewaySettingMap = HashMap<String, GatewaySetting>()

    fun clone(): SlaveNodeSettingMirror {
        val slaveNodeSettingMirrorCopy = SlaveNodeSettingMirror()
        slaveNodeSettingMirrorCopy.subscribedMap.putAll(subscribedMap)
        slaveNodeSettingMirrorCopy.gatewaySettingMap.putAll(gatewaySettingMap)
        return slaveNodeSettingMirrorCopy
    }
}