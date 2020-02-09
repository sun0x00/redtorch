package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.node.master.po.NodePo;

public interface NodeService {
	List<NodePo> getNodeList();

	void deleteNodeByNodeId(Integer nodeId);

	NodePo resetNodeTokenByNodeId(Integer nodeId);

	NodePo createNode();

	NodePo nodeAuth(NodePo node);

	void updateNodeLoginInfo(Integer nodeId, String sessionId, String ipAddress, int port);

	void updateNodeLogoutInfo(Integer nodeId);

	void updateNodeDescriptionByNodeId(NodePo node);
}
