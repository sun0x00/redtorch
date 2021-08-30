package xyz.redtorch.master.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.storage.po.SlaveNode
import xyz.redtorch.master.dao.SlaveNodeDao
import xyz.redtorch.master.service.SlaveNodeService
import xyz.redtorch.master.web.socket.SlaveNodeWebSocketHandler
import java.util.*

@Service
class SlaveNodeServiceImpl : SlaveNodeService {

    private val logger: Logger = LoggerFactory.getLogger(SlaveNodeServiceImpl::class.java)

    @Autowired
    private lateinit var slaveNodeDao: SlaveNodeDao

    @Autowired
    private lateinit var slaveNodeWebSocketHandler: SlaveNodeWebSocketHandler


    override fun getSlaveNodeList(): List<SlaveNode> {
        val slaveNodeList = slaveNodeDao.queryList()
        for (slaveNode in slaveNodeList) {
            if (slaveNodeWebSocketHandler.slaveNodeIdToTransceiverMapGet(slaveNode.id!!) != null) {
                slaveNode.connectionStatus = ConnectionStatusEnum.Connected
            } else {
                slaveNode.connectionStatus = ConnectionStatusEnum.Disconnected
            }
        }
        return slaveNodeList
    }

    override fun createSlaveNode(): SlaveNode {
        val slaveNodeList: List<SlaveNode> = slaveNodeDao.queryList()
        val slaveNode = SlaveNode()
        slaveNode.connectionStatus = ConnectionStatusEnum.Disconnected
        if (slaveNodeList.isEmpty()) {
            // 如果数据库中没有SlaveNode,使用1001作为起始值创建SlaveNode
            slaveNode.id = "1001"
        } else {
            // 如果数据库中有SlaveNode,尝试寻找SlaveNodeID最大值
            val intSlaveNodeIdSet: MutableSet<Int> = HashSet()
            var maxIntSlaveNodeId = 1001
            for (tempSlaveNode in slaveNodeList) {
                val tempIntSlaveNodeId = Integer.valueOf(tempSlaveNode.id)
                intSlaveNodeIdSet.add(tempIntSlaveNodeId)
                if (tempIntSlaveNodeId > maxIntSlaveNodeId) {
                    maxIntSlaveNodeId = tempIntSlaveNodeId
                }
            }

            // 极端情况下+1有可能溢出
            var newIntSlaveNodeId = maxIntSlaveNodeId + 1

            // 使用循环进行再次校验
            // 一是避免极端情况下可能产生的重复
            // 二是不允许自动生成[0,1000]之间的ID，预留
            while (intSlaveNodeIdSet.contains(newIntSlaveNodeId) || newIntSlaveNodeId in 0..1000) {
                newIntSlaveNodeId++
            }
            slaveNode.id = newIntSlaveNodeId.toString()
        }

        slaveNode.token = UUID.randomUUID().toString()
        slaveNodeDao.upsert(slaveNode)
        return slaveNode
    }

    override fun deleteSlaveNodeById(slaveNodeId: String) {
        slaveNodeDao.deleteById(slaveNodeId)
        // 断开可能存在的WebSocket连接
        slaveNodeWebSocketHandler.closeBySlaveNodeId(slaveNodeId)
    }

    override fun slaveNodeAuth(slaveNodeId: String, token: String): SlaveNode? {
        val slaveNode = slaveNodeDao.queryById(slaveNodeId)
        return if (slaveNode != null && slaveNode.token == token) {
            logger.info("节点审核成功,slaveNodeId={}", slaveNodeId)
            slaveNode
        } else {
            logger.info("节点审核失败,slaveNodeId={}", slaveNodeId)
            null
        }
    }

    override fun resetSlaveNodeTokenById(slaveNodeId: String): SlaveNode? {
        val slaveNode = slaveNodeDao.queryById(slaveNodeId)
        if (slaveNode == null) {
            logger.error("更新令牌失败,未能查出节点,slaveNodeId={}", slaveNodeId)
            return null
        }

        slaveNode.token = UUID.randomUUID().toString()
        slaveNodeDao.upsert(slaveNode)

        // 断开可能存在的WebSocket连接
        slaveNodeWebSocketHandler.closeBySlaveNodeId(slaveNodeId)
        return slaveNode
    }

    override fun updateSlaveNodeDescriptionById(slaveNodeId: String, description: String) {
        val slaveNode = slaveNodeDao.queryById(slaveNodeId)
        if (slaveNode != null) {
            slaveNode.description = description
            slaveNodeDao.upsert(slaveNode)
        } else {
            logger.warn("更新节点描述失败,未查出节点,slaveNodeId={}", slaveNodeId)
        }
    }

    override fun upsertSlaveNodeById(slaveNode: SlaveNode) {
        slaveNodeDao.upsert(slaveNode)
    }

}