package xyz.redtorch.node.master.service;

import xyz.redtorch.node.master.po.NodePo;

import java.util.List;

public interface NodeService {
    List<NodePo> getNodeList();

    void deleteNodeByNodeId(Integer nodeId);

    NodePo resetNodeTokenByNodeId(Integer nodeId);

    NodePo createNode();

    NodePo nodeAuth(NodePo node);

    void updateNodeDescriptionByNodeId(NodePo node);

    Integer getNodeIdByToken(String authToken);
}
