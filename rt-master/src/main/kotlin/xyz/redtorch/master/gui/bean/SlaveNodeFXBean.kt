package xyz.redtorch.master.gui.bean

import javafx.beans.property.SimpleStringProperty
import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.storage.po.SlaveNode

class SlaveNodeFXBean(private val slaveNode: SlaveNode) {
    private val id = SimpleStringProperty()
    private var token = SimpleStringProperty()
    private var description = SimpleStringProperty()
    private var connectionStatus = SimpleStringProperty()

    init {
        id.set(slaveNode.id ?: "")
        token.set(slaveNode.token ?: "")
        description.set(slaveNode.description ?: "")

        when (slaveNode.connectionStatus) {
            ConnectionStatusEnum.Connected -> connectionStatus.set("已连接")
            ConnectionStatusEnum.Disconnected -> connectionStatus.set("已断开")
            else -> connectionStatus.set("未知")
        }
    }

    fun getSlaveNode(): SlaveNode {
        return slaveNode
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

    fun getToken(): String {
        return token.get()
    }

    fun setToken(value: String) {
        return token.set(value)
    }

    fun tokenProperty(): SimpleStringProperty {
        return token
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

}