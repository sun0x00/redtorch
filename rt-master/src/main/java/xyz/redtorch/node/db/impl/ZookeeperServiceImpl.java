package xyz.redtorch.node.db.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.util.ZookeeperUtils;
import xyz.redtorch.node.db.ZookeeperService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ZookeeperServiceImpl implements ZookeeperService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceImpl.class);

    @Value("${rt.master.zookeeperConnectStr}")
    private String zookeeperConnectStr;
    @Value("${rt.master.zookeeperUsername}")
    private String zookeeperUsername;
    @Value("${rt.master.zookeeperPassword}")
    private String zookeeperPassword;
    @Value("${rt.master.zookeeperRootPath}")
    private String zookeeperRootPath;

    private ZookeeperUtils zkUtils;

    @Override
    public ZookeeperUtils getZkUtils() {
        return zkUtils;
    }


    @Override
    public void afterPropertiesSet() {

        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                logger.info("Zookeeper事件,{}", event.toString());
            }
        };

        try {
            zkUtils = new ZookeeperUtils(zookeeperConnectStr, zookeeperUsername, zookeeperPassword, watcher);
        } catch (Exception e) {
            logger.error("Zookeeper初始化连接失败,程序终止", e);
            System.exit(0);
        }

    }


    @Override
    public List<JSONObject> find(String collection) {

        List<JSONObject> dataList = new ArrayList<>();

        try {
            String collectionPath = zookeeperRootPath + "/" + collection;

            List<String> childrenNodeList = zkUtils.getChildrenNodeList(collectionPath);

            if (childrenNodeList != null) {
                for (String childrenNode : childrenNodeList) {
                    String dataPath = zookeeperRootPath + "/" + collection + "/" + childrenNode;
                    if (zkUtils.exists(dataPath)) {
                        String data = zkUtils.getNodeData(dataPath);
                        if (StringUtils.isNotBlank(data)) {
                            dataList.add(JSON.parseObject(data));
                        } else {
                            logger.error("查询数据错误,数据不存,collection:{},id:{}", collection, childrenNode);
                        }
                    } else {
                        logger.error("查询数据错误,路径不存在collection:{},id:{}", collection, childrenNode);
                    }
                }
                return dataList;
            }

        } catch (Exception e) {
            logger.error("查询数据发生异常collection:{}", collection, e);
        }
        return null;
    }

    @Override
    public JSONObject findById(String collection, String id) {
        try {
            String dataPath = zookeeperRootPath + "/" + collection + "/" + id;
            if (zkUtils.exists(dataPath)) {
                String data = zkUtils.getNodeData(dataPath);
                if (StringUtils.isNotBlank(data)) {
                    return JSON.parseObject(data);
                } else {
                    logger.info("查询数据错误,数据不存,collection:{},id:{}", collection, id);
                }
            } else {
                logger.error("查询数据错误,路径不存在,collection:{},id:{}", collection, id);
            }
        } catch (Exception e) {
            logger.error("查询数据发生异常,collection:{},id:{}", collection, id, e);
        }
        return null;
    }

    @Override
    public boolean update(String collection, String id, String data) {
        try {
            String dataPath = zookeeperRootPath + "/" + collection + "/" + id;
            return zkUtils.updateNode(dataPath, data);
        } catch (Exception e) {
            logger.error("更新数据发生异常,collection:{},id:{}", collection, id, e);
        }
        return false;
    }


    @Override
    public boolean insert(String collection, String id, String data) {
        try {
            String dataPath = zookeeperRootPath + "/" + collection + "/" + id;
            zkUtils.autoCreatePersistentParentNode(dataPath);
            return zkUtils.addPersistentNode(dataPath, data);
        } catch (Exception e) {
            logger.error("插入数据发生异常collection:{},id:{}", collection, id, e);
        }
        return false;
    }

    @Override
    public boolean upsert(String collection, String id, String data) {
        try {
            String dataPath = zookeeperRootPath + "/" + collection + "/" + id;
            if (zkUtils.exists(dataPath)) {
                return zkUtils.updateNode(dataPath, data);
            } else {
                zkUtils.autoCreatePersistentParentNode(dataPath);
                return zkUtils.addPersistentNode(dataPath, data);
            }
        } catch (Exception e) {
            logger.error("更新插入数据发生异常collection:{},id:{}", collection, id, e);
        }
        return false;
    }

    @Override
    public boolean deleteById(String collection, String id) {
        try {
            String dataPath = zookeeperRootPath + "/" + collection + "/" + id;
            return zkUtils.deleteNode(dataPath);
        } catch (Exception e) {
            logger.error("删除数据发生异常collection:{},id:{}", collection, id, e);
        }
        return false;
    }


}
