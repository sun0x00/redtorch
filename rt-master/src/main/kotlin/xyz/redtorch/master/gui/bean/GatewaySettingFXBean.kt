package xyz.redtorch.master.gui.bean

import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.storage.po.GatewaySetting

class GatewaySettingFXBean(private val gatewaySetting: GatewaySetting) {
    private val id = SimpleStringProperty()
    private var name = SimpleStringProperty()
    private var description = SimpleStringProperty()
    private var connectionStatus = SimpleStringProperty()
    private var targetSlaveNodeId = SimpleStringProperty()
    private var gatewayType = SimpleStringProperty()
    private var implementClassName = SimpleStringProperty()
    private var version = SimpleLongProperty()

    init {
        id.set(gatewaySetting.id ?: "")
        name.set(gatewaySetting.name ?: "")
        description.set(gatewaySetting.description ?: "")
        targetSlaveNodeId.set(gatewaySetting.targetSlaveNodeId)
        gatewayType.set(gatewaySetting.gatewayType.toString())
        implementClassName.set(gatewaySetting.implementClassName ?: "")
        version.set(gatewaySetting.version)

        when (gatewaySetting.connectionStatus) {
            ConnectionStatusEnum.Connected -> connectionStatus.set("已连接")
            ConnectionStatusEnum.Connecting -> connectionStatus.set("连接中")
            ConnectionStatusEnum.Disconnected -> connectionStatus.set("已断开")
            ConnectionStatusEnum.Disconnecting -> connectionStatus.set("断开中")
            else -> connectionStatus.set("未知")
        }
    }

    fun getGatewaySetting(): GatewaySetting {
        return gatewaySetting
    }

    fun getId(): String {
        return id.get()
    }

    fun setId(value: String) {
        return id.set(value)
    }

    fun idProperty(): SimpleStringProperty {
        return id
    }

    fun getName(): String {
        return name.get()
    }

    fun setName(value: String) {
        return name.set(value)
    }

    fun nameProperty(): SimpleStringProperty {
        return name
    }

    fun getDescription(): String {
        return description.get()
    }

    fun setDescription(value: String) {
        return description.set(value)
    }

    fun descriptionProperty(): SimpleStringProperty {
        return description
    }

    fun getConnectionStatus(): String {
        return connectionStatus.get()
    }

    fun setConnectionStatus(value: String) {
        return connectionStatus.set(value)
    }

    fun connectionStatusProperty(): SimpleStringProperty {
        return connectionStatus
    }


    fun getTargetSlaveNodeId(): String {
        return targetSlaveNodeId.get()
    }

    fun setTargetSlaveNodeId(value: String) {
        return targetSlaveNodeId.set(value)
    }

    fun targetSlaveNodeIdProperty(): SimpleStringProperty {
        return targetSlaveNodeId
    }


    fun getGatewayType(): String {
        return gatewayType.get()
    }

    fun setGatewayType(value: String) {
        return gatewayType.set(value)
    }

    fun gatewayTypeProperty(): SimpleStringProperty {
        return gatewayType
    }

    fun getImplementClassName(): String {
        return implementClassName.get()
    }

    fun setImplementClassName(value: String) {
        return implementClassName.set(value)
    }

    fun implementClassNameProperty(): SimpleStringProperty {
        return implementClassName
    }

    fun getVersion(): Long {
        return version.get()
    }

    fun setVersion(value: Long) {
        return version.set(value)
    }

    fun versionProperty(): SimpleLongProperty {
        return version
    }

}