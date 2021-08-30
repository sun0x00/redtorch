package xyz.redtorch.master.db.impl

import com.fasterxml.jackson.databind.JsonNode
import org.apache.zookeeper.Watcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.common.utils.STD3DesUtils
import xyz.redtorch.common.utils.ZookeeperUtils
import xyz.redtorch.master.db.ZookeeperService
import kotlin.system.exitProcess

@Service
/**
 * 简单的存储结构 [/根/集合/ID]→数据
 * 数据使用3DES加密脱敏,防止Zookeeper被攻破导致数据直接泄露
 */
class ZookeeperServiceImpl : ZookeeperService, InitializingBean {

    private val logger = LoggerFactory.getLogger(ZookeeperServiceImpl::class.java)

    @Value("\${rt.zookeeper-connect-str}")
    private lateinit var zookeeperConnectStr: String

    @Value("\${rt.zookeeper-username}")
    private lateinit var zookeeperUsername: String

    @Value("\${rt.zookeeper-password}")
    private lateinit var zookeeperPassword: String

    @Value("\${rt.zookeeper-root-path}")
    private lateinit var zookeeperRootPath: String

    @Value("\${rt.zookeeper-3des-key}")
    private lateinit var zookeeper3DesKey: String

    private lateinit var zkUtils: ZookeeperUtils

    override fun getZkUtils(): ZookeeperUtils {
        return zkUtils
    }

    override fun get3DesKey(): String {
        return zookeeper3DesKey
    }

    override fun afterPropertiesSet() {
        val watcher = Watcher { event ->
            if (Constant.isDebugEnable) {
                logger.debug("Zookeeper事件,{}", event.toString())
            }
        }
        try {
            zkUtils = ZookeeperUtils(zookeeperConnectStr, zookeeperUsername, zookeeperPassword, watcher)
        } catch (e: Exception) {
            logger.error("Zookeeper初始化连接失败,程序终止", e)
            exitProcess(0)
        }
    }

    override fun find(collection: String, enableEncryption: Boolean): List<JsonNode>? {
        if (collection.isBlank()) {
            throw IllegalArgumentException("collection不允许为空字符串")
        }

        val dataList: MutableList<JsonNode> = ArrayList()
        try {
            val collectionPath = "$zookeeperRootPath/$collection"
            val childrenNodeList: List<String>? = zkUtils.getChildrenNodeList(collectionPath)
            if (childrenNodeList != null) {
                for (childrenNode in childrenNodeList) {
                    val dataPath = "$zookeeperRootPath/$collection/$childrenNode"
                    if (zkUtils.exists(dataPath)) {
                        val data: String? = zkUtils.getNodeData(dataPath)
                        if (!data.isNullOrBlank()) {
                            if (enableEncryption) {
                                dataList.add(
                                    JsonUtils.readToJsonNode(
                                        STD3DesUtils.des3DecodeECBBase64String(
                                            zookeeper3DesKey,
                                            data
                                        )
                                    )
                                )
                            } else {
                                dataList.add(JsonUtils.readToJsonNode(data))
                            }
                        } else {
                            logger.error("查询数据错误,数据不存,collection={},id={}", collection, childrenNode)
                        }
                    } else {
                        logger.error("查询数据错误,路径不存在collection={},id={}", collection, childrenNode)
                    }
                }
                return dataList
            }
        } catch (e: Exception) {
            logger.error("查询数据发生异常collection={}", collection, e)
        }
        return null
    }

    override fun findById(collection: String, id: String, enableEncryption: Boolean): JsonNode? {
        if (collection.isBlank()) {
            throw IllegalArgumentException("collection不允许为空字符串")
        }
        if (id.isBlank()) {
            throw IllegalArgumentException("id不允许为空字符串")
        }

        try {
            val dataPath = "$zookeeperRootPath/$collection/$id"
            if (zkUtils.exists(dataPath)) {
                val data: String? = zkUtils.getNodeData(dataPath)
                if (!data.isNullOrBlank()) {
                    return if (enableEncryption) {
                        JsonUtils.readToJsonNode(STD3DesUtils.des3DecodeECBBase64String(zookeeper3DesKey, data))
                    } else {
                        JsonUtils.readToJsonNode(zookeeper3DesKey)
                    }
                } else {
                    logger.info("查询数据错误,数据不存,collection={},id={}", collection, id)
                }
            } else {
                logger.error("查询数据错误,路径不存在,collection={},id={}", collection, id)
            }
        } catch (e: Exception) {
            logger.error("查询数据发生异常,collection={},id={}", collection, id, e)
        }
        return null
    }

    override fun update(collection: String, id: String, data: String, enableEncryption: Boolean): Boolean {
        if (collection.isBlank()) {
            throw IllegalArgumentException("collection不允许为空字符串")
        }
        if (id.isBlank()) {
            throw IllegalArgumentException("id不允许为空字符串")
        }

        try {
            val dataPath = "$zookeeperRootPath/$collection/$id"
            return if (enableEncryption) {
                zkUtils.updateNode(dataPath, STD3DesUtils.des3EncodeECBBase64String(zookeeper3DesKey, data))
            } else {
                zkUtils.updateNode(dataPath, data)
            }
        } catch (e: Exception) {
            logger.error("更新数据发生异常,collection={},id={}", collection, id, e)
        }
        return false
    }

    override fun insert(collection: String, id: String, data: String, enableEncryption: Boolean): Boolean {
        if (collection.isBlank()) {
            throw IllegalArgumentException("collection不允许为空字符串")
        }
        if (id.isBlank()) {
            throw IllegalArgumentException("id不允许为空字符串")
        }

        try {
            val dataPath = "$zookeeperRootPath/$collection/$id"
            zkUtils.autoCreatePersistentParentNode(dataPath)
            return if (enableEncryption) {
                zkUtils.addPersistentNode(dataPath, STD3DesUtils.des3EncodeECBBase64String(zookeeper3DesKey, data))
            } else {
                zkUtils.addPersistentNode(dataPath, data)
            }
        } catch (e: Exception) {
            logger.error("插入数据发生异常collection={},id={}", collection, id, e)
        }
        return false
    }

    override fun upsert(collection: String, id: String, data: String, enableEncryption: Boolean): Boolean {
        if (collection.isBlank()) {
            throw IllegalArgumentException("collection不允许为空字符串")
        }
        if (id.isBlank()) {
            throw IllegalArgumentException("id不允许为空字符串")
        }

        try {
            val dataPath = "$zookeeperRootPath/$collection/$id"
            return if (zkUtils.exists(dataPath)) {
                if (enableEncryption) {
                    zkUtils.updateNode(dataPath, STD3DesUtils.des3EncodeECBBase64String(zookeeper3DesKey, data))
                } else {
                    zkUtils.updateNode(dataPath, data)
                }
            } else {
                zkUtils.autoCreatePersistentParentNode(dataPath)
                if (enableEncryption) {
                    zkUtils.addPersistentNode(dataPath, STD3DesUtils.des3EncodeECBBase64String(zookeeper3DesKey, data))
                } else {
                    zkUtils.addPersistentNode(dataPath, data)
                }
            }
        } catch (e: Exception) {
            logger.error("更新插入数据发生异常collection={},id={}", collection, id, e)
        }
        return false
    }

    override fun deleteById(collection: String, id: String): Boolean {
        if (collection.isBlank()) {
            throw IllegalArgumentException("collection不允许为空字符串")
        }
        if (id.isBlank()) {
            throw IllegalArgumentException("id不允许为空字符串")
        }

        try {
            val dataPath = "$zookeeperRootPath/$collection/$id"
            return zkUtils.deleteNode(dataPath)
        } catch (e: Exception) {
            logger.error("删除数据发生异常collection={},id={}", collection, id, e)
        }
        return false
    }
}