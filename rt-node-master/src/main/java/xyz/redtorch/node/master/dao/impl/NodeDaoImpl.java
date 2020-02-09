package xyz.redtorch.node.master.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.common.mongo.MongoDBClient;
import xyz.redtorch.node.db.MongoDBClientService;
import xyz.redtorch.node.master.dao.NodeDao;
import xyz.redtorch.node.master.po.NodePo;

@Service
public class NodeDaoImpl implements NodeDao, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(NodeDaoImpl.class);

	private static final String NODE_COLLECTION_NAME = "node_collection";

	@Autowired
	private MongoDBClientService mongoDBClientService;

	private MongoDBClient managementDBClient;
	private String managementDBName;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.managementDBClient = mongoDBClientService.getManagementDBClient();
		this.managementDBName = mongoDBClientService.getManagementDBName();
	}

	@Override
	public List<NodePo> queryNodeList() {
		List<Document> documentList = this.managementDBClient.find(managementDBName, NODE_COLLECTION_NAME);
		List<NodePo> nodeList = new ArrayList<>();

		for (Document document : documentList) {
			try {
				NodePo node = JSON.parseObject(JSON.toJSONString(document), NodePo.class);
				nodeList.add(node);
			} catch (Exception e) {
				logger.error("查询节点列表,数据转换发生错误Document-{}", document.toJson(), e);
			}
		}
		
		return nodeList;
	}

	@Override
	public void upsertNodeByNodeId(NodePo node) {
		if (node == null) {
			logger.error("根据节点ID更新或保存节点错误,参数node缺失");
			throw new IllegalArgumentException("根据节点更新或保存节点错误,参数node缺失");
		}
		if (node.getNodeId() == null) {
			logger.error("根据节点ID更新或保存节点错误,参数gatewayId缺失");
			throw new IllegalArgumentException("根据节点更新或保存网关错误,参数nodeId缺失");
		}

		try {
			Document document = Document.parse(JSON.toJSONString(node));
			Document filter = new Document();
			filter.append("nodeId", node.getNodeId());
			this.managementDBClient.upsert(managementDBName, NODE_COLLECTION_NAME, document, filter);
		} catch (IllegalArgumentException e) {
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
			Document filter = new Document();
			filter.append("nodeId", nodeId);
			this.managementDBClient.delete(managementDBName, NODE_COLLECTION_NAME, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据节点ID删除节点错误,节点ID:{}", nodeId, e);
		}
	}

	@Override
	public NodePo queryNodeByNodeId(Integer nodeId) {
		if (nodeId == null) {
			logger.error("根据节点ID查询节点错误,参数nodeId缺失");
			throw new IllegalArgumentException("根据节点ID查询节点错误,参数nodeId缺失");
		}

		try {
			Document filter = new Document();
			filter.append("nodeId", nodeId);
			List<Document> documentList = this.managementDBClient.find(managementDBName, NODE_COLLECTION_NAME, filter);
			if (documentList == null || documentList.isEmpty()) {
				return null;
			}
			if (documentList.size() > 1) {
				logger.warn("根据节点ID查询出多个节点,仅取一个,节点ID：{}", nodeId);
			}

			NodePo node = JSON.parseObject(JSON.toJSONString(documentList.get(0)), NodePo.class);

			return node;

		} catch (IllegalArgumentException e) {
			logger.error("根据节点ID查询节点发生错误,节点ID:{}", nodeId, e);
			return null;
		}
	}
}
