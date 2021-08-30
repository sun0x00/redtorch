package xyz.redtorch.common.storage.po

import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.storage.enumeration.GatewayTypeEnum

class GatewaySetting : BasePo() {

    // 网关名称
    var name: String? = null

    // 网关描述
    var description: String? = null

    // 实现类
    var implementClassName: String? = null

    // 网关类型
    var gatewayType: GatewayTypeEnum = GatewayTypeEnum.TradeAndQuote

    // 目标节点
    var targetSlaveNodeId: String = "0"

    // 855-1555#2033-2359#0-300
    var autoConnectTimeRanges: String? = null

    // 状态
    var connectionStatus: ConnectionStatusEnum = ConnectionStatusEnum.Unknown

    // 适配器配置JSON字符串
    var adapterSettingJsonString: String? = null

    // 版本
    var version: Long = 0

}