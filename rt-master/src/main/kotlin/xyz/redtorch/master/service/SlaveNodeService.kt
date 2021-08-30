package xyz.redtorch.master.service

import xyz.redtorch.common.storage.po.SlaveNode

interface SlaveNodeService {
    fun getSlaveNodeList(): List<SlaveNode>

    fun deleteSlaveNodeById(slaveNodeId: String)

    fun resetSlaveNodeTokenById(slaveNodeId: String): SlaveNode?

    fun createSlaveNode(): SlaveNode

    fun slaveNodeAuth(slaveNodeId: String, token: String): SlaveNode?

    fun updateSlaveNodeDescriptionById(slaveNodeId: String, description: String)

    fun upsertSlaveNodeById(slaveNode: SlaveNode)
}