package xyz.redtorch.node.master.service.impl;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.common.util.UuidStringPoolUtils;
import xyz.redtorch.node.master.dao.NodeDao;
import xyz.redtorch.node.master.po.NodePo;
import xyz.redtorch.node.master.service.NodeService;
import xyz.redtorch.node.master.web.socket.WebSocketServerHandler;

@Service
public class NodeServiceImpl implements NodeService, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);

	@Autowired
	private NodeDao nodeDao;

	@Autowired
	private WebSocketServerHandler webSocketServerHandler;

	private ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();

	@Override
	public void afterPropertiesSet() throws Exception {
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			try {
				List<NodePo> nodeList = this.getNodeList();
				if (nodeList != null) {
					for (NodePo node : nodeList) {
						if (webSocketServerHandler.containsNodeId(node.getNodeId())) {
							node.setStatus(NodePo.NODE_STATUS_CONNECTED);
						} else {
							node.setStatus(NodePo.NODE_STATUS_DISCONNECTED);
						}
						nodeDao.upsertNodeByNodeId(node);
					}
				}
			} catch (Exception e) {
				logger.error("定时检查节点状态异常", e);
			}

		}, 5, 5, TimeUnit.SECONDS);
	}

	@Override
	public List<NodePo> getNodeList() {
		return nodeDao.queryNodeList();
	}

	@Override
	public synchronized NodePo createNode() {
		List<NodePo> nodeList = nodeDao.queryNodeList();
		NodePo node = new NodePo();
		node.setStatus(NodePo.NODE_STATUS_DISCONNECTED);
		if (nodeList == null || nodeList.size() == 0) {
			// 如果数据库中没有节点,使用10001作为起始值创建节点
			node.setNodeId(10001);
			node.setToken(UuidStringPoolUtils.getUuidString());

			nodeDao.upsertNodeByNodeId(node);
		} else {
			// 如果数据库中有节点,尝试寻找节点ID最大值
			Set<Integer> nodeIdSet = new HashSet<Integer>();
			Integer maxNodeId = null;
			for (NodePo tempNode : nodeList) {
				nodeIdSet.add(maxNodeId);
				if (maxNodeId == null) {
					maxNodeId = tempNode.getNodeId();
				} else {
					if (tempNode.getNodeId() > maxNodeId) {
						maxNodeId = tempNode.getNodeId();
					}
				}
			}

			// 极端情况下+1有可能溢出
			Integer newNodeId = maxNodeId + 1;

			// 使用循环进行再次校验
			// 一是避免极端情况下可能产生的重复
			// 二是不允许自动生成[0,10000]之间的ID，预留
			// 三是不允许自动生成[60000000,90000000]之间的ID,这段ID将用于用户登录自动生成ID
			while (nodeIdSet.contains(newNodeId) || (newNodeId >= 0 && newNodeId <= 10000)
					|| (newNodeId >= 60000000 && newNodeId <= 90000000)) {
				newNodeId++;
			}
			node.setNodeId(newNodeId);
			node.setToken(UuidStringPoolUtils.getUuidString());

			nodeDao.upsertNodeByNodeId(node);
		}
		return node;
	}

	@Override
	public void deleteNodeByNodeId(Integer nodeId) {
		if (nodeId == null) {
			logger.error("根据节点ID删除节点错误,参数nodeId缺失");
			throw new IllegalArgumentException("根据节点ID删除节点错误,参数nodeId缺失");
		}
		// 断开可能存在的WebSocket连接
		webSocketServerHandler.closeByNodeId(nodeId);

		nodeDao.deleteNodeByNodeId(nodeId);

	}

	@Override
	public NodePo nodeAuth(NodePo node) {
		if (node == null) {
			logger.error("节点审核错误,参数node缺失");
			throw new IllegalArgumentException("节点审核错误,参数node缺失");
		}
		if (node.getNodeId() == null) {
			logger.error("节点审核错误,参数nodeId缺失");
			throw new IllegalArgumentException("节点审核错误,参数nodeId缺失");
		}
		if (StringUtils.isAllBlank(node.getToken())) {
			logger.error("节点审核错误,参数token缺失");
			throw new IllegalArgumentException("节点审核错误,参数token缺失");
		}

		NodePo queriedNode = nodeDao.queryNodeByNodeId(node.getNodeId());
		if (queriedNode != null && node.getToken().equals(queriedNode.getToken())) {
			logger.info("节点审核成功,节点ID:{}", node.getNodeId());
			return queriedNode;
		} else {
			logger.info("节点审核失败,节点ID:{}", node.getNodeId());
			return null;
		}
	}

	@Override
	public NodePo resetNodeTokenByNodeId(Integer nodeId) {
		if (nodeId == null) {
			logger.error("根据节点ID重置节点令牌错误,参数nodeId缺失");
			throw new IllegalArgumentException("根据节点ID重置节点令牌错误,参数nodeId缺失");
		}
		// 断开可能存在的WebSocket连接
		webSocketServerHandler.closeByNodeId(nodeId);

		NodePo node = nodeDao.queryNodeByNodeId(nodeId);
		if (node == null) {
			logger.error("更新令牌失败,未能查出节点,节点ID:{}", nodeId);
			return null;
		}
		node.setToken(UuidStringPoolUtils.getUuidString());
		nodeDao.upsertNodeByNodeId(node);
		return node;
	}

	@Override
	public void updateNodeLoginInfo(Integer nodeId, String sessionId, String ipAddress, int port) {
		if (nodeId == null) {
			logger.error("更新节点登录信息错误,参数nodeId缺失");
			throw new IllegalArgumentException("更新节点登录信息错误,参数nodeId缺失");
		}
		NodePo node = nodeDao.queryNodeByNodeId(nodeId);

		if (node == null) {
			logger.warn("更新节点登录信息,未找到节点记录,节点ID:{}", nodeId);
			return;
		}
		node.setRecentlySessionId(sessionId);
		node.setRecentlyIpAddress(ipAddress);
		node.setRecentlyPort(port);
		node.setStatus(NodePo.NODE_STATUS_CONNECTED);
		node.setRecentlyLoginTime(CommonUtils.DT_FORMAT_WITH_MS_FORMATTER.format(LocalDateTime.now()));
		nodeDao.upsertNodeByNodeId(node);

	}

	@Override
	public void updateNodeLogoutInfo(Integer nodeId) {
		if (nodeId == null) {
			logger.error("更新节点注销信息错误,参数nodeId缺失");
			throw new IllegalArgumentException("更新节点注销信息错误,参数nodeId缺失");
		}

		NodePo node = nodeDao.queryNodeByNodeId(nodeId);

		if (node == null) {
			logger.warn("更新节点注销信息,未找到节点记录,节点ID:{}", nodeId);
			return;
		}

		node.setStatus(NodePo.NODE_STATUS_DISCONNECTED);
		node.setRecentlyLogoutTime(CommonUtils.DT_FORMAT_WITH_MS_FORMATTER.format(LocalDateTime.now()));
		nodeDao.upsertNodeByNodeId(node);
	}

	@Override
	public void updateNodeDescriptionByNodeId(NodePo node) {
		if (node == null) {
			logger.error("根据节点ID更新节点描述错误,参数node缺失");
			throw new IllegalArgumentException("根据节点ID更新节点描述错误,参数node缺失");
		}
		if (node.getNodeId() == null) {
			logger.error("根据节点ID更新节点描述错误,参数nodeId缺失");
			throw new IllegalArgumentException("根据节点ID更新节点描述错误,参数nodeId缺失");
		}
		int nodeId = node.getNodeId();
		NodePo queriedNode = nodeDao.queryNodeByNodeId(nodeId);
		if (queriedNode != null) {
			queriedNode.setDescription(node.getDescription());
			nodeDao.upsertNodeByNodeId(queriedNode);
		} else {
			logger.warn("根据节点ID更新节点描述错误,未查出节点,节点ID:{}", nodeId);
		}

	}
}
