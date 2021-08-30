package xyz.redtorch.gateway

import xyz.redtorch.common.event.service.EventService
import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.common.sync.dto.CancelOrder
import xyz.redtorch.common.sync.dto.GatewayStatus
import xyz.redtorch.common.sync.dto.InsertOrder
import xyz.redtorch.common.trade.dto.Contract

interface Gateway {
    /**
     * 获取配置
     */
    fun getGatewaySetting(): GatewaySetting

    /**
     * 获取网关状态
     */
    fun getGatewayStatus(): GatewayStatus

    /**
     * 获取事件服务
     */
    fun getEventService(): EventService

    /**
     * 订阅
     */
    fun subscribe(contract: Contract): Boolean

    /**
     * 退订
     */
    fun unsubscribe(contract: Contract): Boolean

    /**
     * 提交定单
     */
    fun submitOrder(insertOrder: InsertOrder): String?

    /**
     * 撤销定单
     */
    fun cancelOrder(cancelOrder: CancelOrder): Boolean

    /**
     * 连接
     */
    fun connect()

    /**
     * 断开
     */
    fun disconnect()

    /**
     * 网关连接状态
     */
    fun isConnected(): Boolean

    /**
     * 获取登录错误标记
     */
    fun isAuthError(): Boolean

    /**
     * 获取最后一次开始登陆的时间戳
     */
    fun getLastConnectInitiateTimestamp(): Long

}