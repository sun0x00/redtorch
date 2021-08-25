package xyz.redtorch.common.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class ZookeeperUtils {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperUtils.class);

    private final ZooKeeper zookeeper;
    private final List<ACL> aclList;

    public ZookeeperUtils(String connectStr, String username, String password, Watcher watcher) throws Exception {

        // 创建用户名密码组合
        String usernameAndPassword = username + ":" + password;

        // 生成加密字符
        byte[] digest = MessageDigest.getInstance("SHA1").digest(usernameAndPassword.getBytes());

        // 创建Id，也可以设置构造方法传入scheme和id
        Id id = new Id("digest", username + ":" + new Base64().encodeToString(digest));

        // 创建ACL
        ACL acl = new ACL();
        acl.setId(id);
        acl.setPerms(Perms.ALL);

        // 加入ACL集合
        List<ACL> aclList = new ArrayList<>();
        aclList.add(acl);
        this.aclList = aclList;

        zookeeper = new ZooKeeper(connectStr, 3000, watcher);
        zookeeper.addAuthInfo("digest", usernameAndPassword.getBytes());
    }

    /**
     * 获取Zookeeper实例
     *
     * @return Zookeeper实例
     */
    public ZooKeeper getZookeeper() {
        return zookeeper;
    }

    /**
     * 创建结点
     *
     * @param path 结点路径
     * @param data 数据
     * @param mode 创建模式
     * @return true 创建结点成功 false表示结点存在
     */
    public boolean addNodeData(String path, String data, CreateMode mode) throws KeeperException, InterruptedException {
        if (zookeeper.exists(path, false) == null) {
            zookeeper.create(path, data.getBytes(), aclList, mode);
            return true;
        }
        return false;
    }

    /**
     * 创建永久结点
     *
     * @param path 结点路径
     * @param data 数据
     * @return true 创建结点成功 false表示结点存在
     */
    public boolean addPersistentNode(String path, String data) throws KeeperException, InterruptedException {
        return addNodeData(path, data, CreateMode.PERSISTENT);
    }

    /**
     * 修改节点
     *
     * @param path 结点路径
     * @param data 数据
     * @return 修改结点成功   false表示结点不存在
     */
    public boolean updateNode(String path, String data) throws KeeperException, InterruptedException {
        Stat stat;
        if ((stat = zookeeper.exists(path, false)) != null) {
            zookeeper.setData(path, data.getBytes(), stat.getVersion());
            return true;
        }
        return false;
    }

    /**
     * 删除结点
     *
     * @param path 结点路径
     * @return true 删除键结点成功  false表示结点不存在
     */
    public boolean deleteNode(String path) throws KeeperException, InterruptedException {
        Stat stat;
        if ((stat = zookeeper.exists(path, false)) != null) {
            List<String> subPaths = zookeeper.getChildren(path, false);
            if (!subPaths.isEmpty()) {
                for (String subPath : subPaths) {
                    // 递归删除
                    deleteNode(path + "/" + subPath);
                }
            }
            zookeeper.delete(path, stat.getVersion());
            return true;
        }
        return false;
    }

    /**
     * 获取结点数据
     *
     * @param path 结点路径
     * @return null表示结点不存在 否则返回结点数据
     */
    public String getNodeData(String path) throws KeeperException, InterruptedException {
        String data = null;
        Stat stat;
        if ((stat = zookeeper.exists(path, false)) != null) {
            data = new String(zookeeper.getData(path, false, stat));
        }
        return data;
    }

    /**
     * 判断结点是否存在
     *
     * @param path 结点路径
     * @return true 存在 false 不存在
     */
    public boolean exists(String path) throws KeeperException, InterruptedException {
        return zookeeper.exists(path, false) != null;
    }

    /**
     * 获取子结点
     *
     * @param path 结点路径
     * @return null表示子结结点不存在 否则返回子结点路径集合
     */
    public List<String> getChildrenNodeList(String path) throws KeeperException, InterruptedException {
        if (zookeeper.exists(path, false) != null) {
            return zookeeper.getChildren(path, false);
        }
        return null;
    }

    /**
     * 自动创建父结点
     *
     * @param path       结点路径,不包括父路径,/开头）
     * @param parentPath 父路径,/开头,如果留空,则默认为根路径）
     * @param mode       模式
     */
    public void autoCreateParentNode(String path, String parentPath, CreateMode mode) {
        try {
            if (StringUtils.isBlank(parentPath)) {
                parentPath = "/";
            } else {
                parentPath = parentPath + "/";
            }
            String[] nodeArray = path.split("/", 3);

            if (nodeArray.length == 3) {
                String currentPath = parentPath + nodeArray[1];
                addNodeData(currentPath, "", mode);
                // 递归
                autoCreateParentNode("/" + nodeArray[2], currentPath, mode);
            }

        } catch (Exception e) {
            logger.error("自动创建父路径发生错误", e);
        }
    }

    /**
     * 自动创建临时父路径
     *
     * @param path 结点全路径
     */
    public void autoCreateEphemeralParentNode(String path) {
        autoCreateParentNode(path, null, CreateMode.EPHEMERAL);
    }

    /**
     * 自动创建永久父路径
     *
     * @param path 结点全路径
     */
    public void autoCreatePersistentParentNode(String path) {
        autoCreateParentNode(path, null, CreateMode.PERSISTENT);
    }

}