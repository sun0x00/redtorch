package xyz.redtorch.node.master.dao;

import xyz.redtorch.node.master.po.NodePo;

import java.util.List;

public interface NodeDao {
    NodePo queryNodeByNodeId(Integer nodeId);

    List<NodePo> queryNodeList();

    void deleteNodeByNodeId(Integer nodeId);

    void upsertNodeByNodeId(NodePo node);
}
