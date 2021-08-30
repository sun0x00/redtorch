package xyz.redtorch.common.utils

import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooDefs.Perms
import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.data.ACL
import org.apache.zookeeper.data.Id
import org.apache.zookeeper.data.Stat
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.util.*

class ZookeeperUtils(connectStr: String?, username: String, password: String, watcher: Watcher?) {

    private val logger = LoggerFactory.getLogger(ZookeeperUtils::class.java)

    private var zookeeper: ZooKeeper

    private var aclList: List<ACL>

    init {
        // 创建用户名密码组合
        val usernameAndPassword = "$username:$password"

        // 生成加密字符
        val digest = MessageDigest.getInstance("SHA1").digest(usernameAndPassword.toByteArray())

        // 创建ID，也可以设置构造方法传入scheme和id
        val id = Id("digest", username + ":" + Base64.getEncoder().encodeToString(digest))

        // 创建ACL
        val acl = ACL()
        acl.id = id
        acl.perms = Perms.ALL

        // 加入ACL集合
        val aclList: MutableList<ACL> = ArrayList()
        aclList.add(acl)
        this.aclList = aclList
        zookeeper = ZooKeeper(connectStr, 3000, watcher)
        zookeeper.addAuthInfo("digest", usernameAndPassword.toByteArray())
    }


    /**
     * 获取Zookeeper实例
     *
     * @return Zookeeper实例
     */
    fun getZookeeper(): ZooKeeper {
        return zookeeper
    }

    /**
     * 创建结点
     *
     * @param path 结点路径
     * @param data 数据
     * @param mode 创建模式
     * @return true 创建结点成功 false表示结点存在
     */
    @Throws(KeeperException::class, InterruptedException::class)
    fun addNodeData(path: String, data: String, mode: CreateMode): Boolean {
        if (zookeeper.exists(path, false) == null) {
            zookeeper.create(path, data.toByteArray(), aclList, mode)
            return true
        }
        return false
    }

    /**
     * 创建永久结点
     *
     * @param path 结点路径
     * @param data 数据
     * @return true 创建结点成功 false表示结点存在
     */
    @Throws(KeeperException::class, InterruptedException::class)
    fun addPersistentNode(path: String, data: String): Boolean {
        return addNodeData(path, data, CreateMode.PERSISTENT)
    }

    /**
     * 修改节点
     *
     * @param path 结点路径
     * @param data 数据
     * @return 修改结点成功   false表示结点不存在
     */
    @Throws(KeeperException::class, InterruptedException::class)
    fun updateNode(path: String, data: String): Boolean {
        var stat: Stat
        if (zookeeper.exists(path, false).also { stat = it } != null) {
            zookeeper.setData(path, data.toByteArray(), stat.version)
            return true
        }
        return false
    }

    /**
     * 递归删除结点
     *
     * @param path 结点路径
     * @return true 删除键结点成功  false表示结点不存在
     */
    @Throws(KeeperException::class, InterruptedException::class)
    fun recursiveDeleteNode(path: String): Boolean {
        var stat: Stat
        if (zookeeper.exists(path, false).also { stat = it } != null) {
            val subPaths = zookeeper.getChildren(path, false)
            if (subPaths.isNotEmpty()) {
                for (subPath in subPaths) {
                    // 递归删除
                    recursiveDeleteNode("$path/$subPath")
                }
            }
            zookeeper.delete(path, stat.version)
            return true
        }
        return false
    }

    /**
     * 删除结点
     *
     * @param path 结点路径
     * @return true 删除键结点成功  false表示结点不存在
     */
    @Throws(KeeperException::class, InterruptedException::class)
    fun deleteNode(path: String): Boolean {
        var stat: Stat
        if (zookeeper.exists(path, false).also { stat = it } != null) {
            val subPaths = zookeeper.getChildren(path, false)
            if (subPaths.isNotEmpty()) {
                return false
            }else{
                zookeeper.delete(path, stat.version)
            }
            return true
        }
        return false
    }

    /**
     * 获取结点数据
     *
     * @param path 结点路径
     * @return null表示结点不存在 否则返回结点数据
     */
    @Throws(KeeperException::class, InterruptedException::class)
    fun getNodeData(path: String): String? {
        var data: String? = null
        var stat: Stat?
        if (zookeeper.exists(path, false).also { stat = it } != null) {
            data = String(zookeeper.getData(path, false, stat))
        }
        return data
    }

    /**
     * 判断结点是否存在
     *
     * @param path 结点路径
     * @return true 存在 false 不存在
     */
    @Throws(KeeperException::class, InterruptedException::class)
    fun exists(path: String): Boolean {
        return zookeeper.exists(path, false) != null
    }

    /**
     * 获取子结点
     *
     * @param path 结点路径
     * @return null表示子结结点不存在 否则返回子结点路径集合
     */
    @Throws(KeeperException::class, InterruptedException::class)
    fun getChildrenNodeList(path: String): List<String>? {
        return if (zookeeper.exists(path, false) != null) {
            zookeeper.getChildren(path, false)
        } else null
    }

    /**
     * 自动创建父结点
     *
     * @param path       结点路径,不包括父路径,/开头）
     * @param parentPath 父路径,/开头,如果留空,则默认为根路径）
     * @param mode       模式
     */
    fun autoCreateParentNode(path: String, parentPath: String?, mode: CreateMode) {
        var scopeParentPath = parentPath
        try {
            scopeParentPath = if (scopeParentPath.isNullOrBlank()) {
                "/"
            } else {
                "$scopeParentPath/"
            }
            val nodeArray = path.split("/", limit = 3).toTypedArray()
            if (nodeArray.size == 3) {
                val currentPath = scopeParentPath + nodeArray[1]
                addNodeData(currentPath, "", mode)
                // 递归
                autoCreateParentNode("/" + nodeArray[2], currentPath, mode)
            }
        } catch (e: Exception) {
            logger.error("自动创建父路径发生异常", e)
        }
    }

    /**
     * 自动创建临时父路径
     *
     * @param path 结点全路径
     */
    fun autoCreateEphemeralParentNode(path: String) {
        autoCreateParentNode(path, null, CreateMode.EPHEMERAL)
    }

    /**
     * 自动创建永久父路径
     *
     * @param path 结点全路径
     */
    fun autoCreatePersistentParentNode(path: String) {
        autoCreateParentNode(path, null, CreateMode.PERSISTENT)
    }
}