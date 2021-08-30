package xyz.redtorch.common.sync.dto

class SlaveNodeReportMirror {
    val gatewayStatusMap = HashMap<String, GatewayStatus>()

    fun clone(): SlaveNodeReportMirror {
        val slaveNodeReportMirror = SlaveNodeReportMirror()
        slaveNodeReportMirror.gatewayStatusMap.putAll(gatewayStatusMap)
        return slaveNodeReportMirror
    }
}