package xyz.redtorch.node.master.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.db.ZookeeperService;
import xyz.redtorch.node.master.dao.NodeDao;
import xyz.redtorch.node.master.po.NodePo;

import java.util.ArrayList;
import java.util.List;

@Service
public class NodeDaoImpl implements NodeDao {
    private static final Logger logger = LoggerFactory.getLogger(NodeDaoImpl.class);

    private static final String NODE_COLLECTION_NAME = "node";

    @Autowired
    ZookeeperService zookeeperService;

    @Override
    public NodePo queryNodeByNodeId(Integer nodeId) {
        if (nodeId == null) {
            logger.error("根据节点ID查询节点错误,参数nodeId缺失");
            throw new IllegalArgumentException("根据节点ID查询节点错误,参数nodeId缺失");
        }

        try {
            JSONObject jsonObject = zookeeperService.findById(NODE_COLLECTION_NAME, nodeId + "");
            if (jsonObject != null) {
                return jsonObject.toJavaObject(NodePo.class);
            }
        } catch (Exception e) {
            logger.error("根据节点ID查询节点,数据转换发生错误", e);
        }
        return null;
    }

    @Override
    public List<NodePo> queryNodeList() {
        List<NodePo> nodeList = new ArrayList<>();
        try {
            List<JSONObject> jsonObjectList = zookeeperService.find(NODE_COLLECTION_NAME);
            if (jsonObjectList != null) {
                for (JSONObject jsonObject : jsonObjectList) {
                    try {
                        nodeList.add(jsonObject.toJavaObject(NodePo.class));
                    } catch (Exception e) {
                        logger.error("查询节点列表,数据转换发生错误", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询节点列表发生错误", e);
        }

        return nodeList;
    }

    @Override
    public void upsertNodeByNodeId(NodePo node) {
        if (node == null) {
            logger.error("根据节点ID更新或保存节点错误,参数node缺失");
            throw new IllegalArgumentException("根据节点ID更新或保存节点错误,参数node缺失");
        }
        if (node.getNodeId() == null) {
            logger.error("根据节点ID更新或保存节点错误,参数nodeId缺失");
            throw new IllegalArgumentException("根据节点ID更新或保存节点错误,参数nodeId缺失");
        }

        try {
            zookeeperService.upsert(NODE_COLLECTION_NAME, node.getNodeId() + "", JSON.toJSONString(node));
        } catch (Exception e) {
            logger.error("根据节点ID更新或保存节点错误", e);
        }
    }

    @Override
    public void deleteNodeByNodeId(Integer nodeId) {
        if (nodeId == null) {
            logger.error("根据节点ID删除节点错误,参数nodeId缺失");
            throw new IllegalArgumentException("根据节点ID删除节点错误,参数nodeId缺失");
        }

        try {
            zookeeperService.deleteById(NODE_COLLECTION_NAME, nodeId + "");
        } catch (Exception e) {
            logger.error("根据节点ID删除节点发生错误,节点ID:{}", nodeId, e);
        }
    }

}
