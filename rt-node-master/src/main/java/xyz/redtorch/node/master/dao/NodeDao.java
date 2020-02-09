package xyz.redtorch.node.master.dao;

import java.util.List;

import xyz.redtorch.node.master.po.NodePo;

public interface NodeDao {
	NodePo queryNodeByNodeId(Integer nodeId);

	List<NodePo> queryNodeList();

	void deleteNodeByNodeId(Integer nodeId);

	void upsertNodeByNodeId(NodePo node);
}
