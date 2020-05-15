package xyz.redtorch.node.master.web.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import xyz.redtorch.RtConstant;
import xyz.redtorch.common.web.socket.ThreadSafeWebSocketSession;
import xyz.redtorch.node.master.po.NodePo;
import xyz.redtorch.node.master.rpc.service.RpcServerProcessService;
import xyz.redtorch.node.master.service.MasterSystemService;
import xyz.redtorch.node.master.service.MasterTradeExecuteService;
import xyz.redtorch.node.master.service.NodeService;

@Component
public class WebSocketServerHandler extends AbstractWebSocketHandler implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

	@Autowired
	private NodeService nodeService;
	@Autowired
	private MasterSystemService masterSystemService;
	@Autowired
	private MasterTradeExecuteService masterTradeExecuteService;
	@Autowired
	private RpcServerProcessService rpcServerProcessService;

	private Map<String, ThreadSafeWebSocketSession> sessionIdSessionMap = new ConcurrentHashMap<>();
	private Map<Integer, String> nodeIdSessionIdMap = new ConcurrentHashMap<>();
	private Map<Integer, String> nodeIdOperatorIdMap = new ConcurrentHashMap<>();
	private Map<String, Set<Integer>> operatorIdNodeIdSetMap = new ConcurrentHashMap<>();
	private Map<String, Integer> sessionIdNodeIdMap = new ConcurrentHashMap<>();
	private Map<Integer, LinkedBlockingQueue<byte[]>> nodeIdDataQueueMap = new ConcurrentHashMap<>();
	private Set<Integer> skipLoginNodeIdSet = ConcurrentHashMap.newKeySet();
	private Set<Integer> skipTradeEventsNodeIdSet = ConcurrentHashMap.newKeySet();

	private ExecutorService executor = Executors.newCachedThreadPool();
	
	private Map<String,Long> sessionIdPingStartTimeMap = new ConcurrentHashMap<>();

	@Override
	public void afterPropertiesSet() throws Exception {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						Thread.sleep(10000);

						for (ThreadSafeWebSocketSession threadSafeWebSocketSession : sessionIdSessionMap.values()) {
							if(sessionIdPingStartTimeMap.containsKey(threadSafeWebSocketSession.getId())) {
								Long startPingTimestamp = sessionIdPingStartTimeMap.get(threadSafeWebSocketSession.getId());
								if(System.currentTimeMillis()-startPingTimestamp>21000) {
									logger.error("PING超时,会话ID:{}",threadSafeWebSocketSession.getId());
									try {
										threadSafeWebSocketSession.close(CloseStatus.NORMAL.withReason("管理服务主动关闭!"));
									} catch (IOException e) {
										logger.error("PING超时后关闭会话警告", e);
									}
									sessionIdPingStartTimeMap.remove(threadSafeWebSocketSession.getId());
								}	 
							}

							if (threadSafeWebSocketSession.isOpen()) {
								Long pingStartTime = System.currentTimeMillis();
								ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES).putLong(System.currentTimeMillis()).flip();
								PingMessage message = new PingMessage(byteBuffer);
								threadSafeWebSocketSession.sendMessage(message);
								if(!sessionIdPingStartTimeMap.containsKey(threadSafeWebSocketSession.getId())) {
									sessionIdPingStartTimeMap.put(threadSafeWebSocketSession.getId(), pingStartTime);
								}
							}
						}
					} catch (Exception e) {
						logger.error("定时PING线程异常", e);
					}
				}
			}
		});

	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		// nodeId已经在拦截器中校验过,可直接获取
		int nodeId = (int) session.getAttributes().get(RtConstant.KEY_NODE_ID);

		// 判断是否会话冲突
		if (nodeIdSessionIdMap.containsKey(nodeId)) {
			logger.error("节点冲突,断开新会话,节点ID:{},会话ID:{}", nodeId, session.getId());
			try {
				session.close(CloseStatus.NORMAL.withReason("节点冲突!"));
			} catch (Exception e) {
				logger.error("关闭异常", e);
			}
			return;
		}

		String operatorId = (String) session.getAttributes().get(RtConstant.KEY_OPERATOR_ID);

		// 判断是否免登录
		if (!session.getAttributes().containsKey(WebSocketConstant.KEY_SKIP_LOGIN)) {
			String token = (String) session.getAttributes().get(RtConstant.KEY_TOKEN);

			logger.info("连接已建立,节点ID:{},会话ID:{},需要验证登录信息", nodeId, session.getId());
			NodePo nodePo = new NodePo();
			nodePo.setNodeId(nodeId);
			nodePo.setToken(token);
			NodePo loggedinNodePo = nodeService.nodeAuth(nodePo);
			if (loggedinNodePo != null) {
				logger.info("验证登录信息成功,节点ID:{},远程地址:{}", nodeId, session.getRemoteAddress().toString());
			} else {
				logger.error("验证登录信息失败,节点ID:{},远程地址:{}", nodeId, session.getRemoteAddress().toString());
				try {
					session.close(CloseStatus.NORMAL.withReason("验证登录信息失败!"));
				} catch (Exception e) {
					logger.error("关闭异常", e);
				}
				return;
			}
		} else {
			logger.info("连接已建立,节点ID:{},会话ID:{},免登录", nodeId, session.getId());
		}

		if (session.getAttributes().containsKey(WebSocketConstant.KEY_SKIP_TRADE_EVENTS)) {
			skipTradeEventsNodeIdSet.add(nodeId);
		}
		nodeIdOperatorIdMap.put(nodeId, operatorId);
		synchronized (operatorIdNodeIdSetMap) {
			Set<Integer> nodeIdSet = operatorIdNodeIdSetMap.get(operatorId);
			if (nodeIdSet == null) {
				nodeIdSet = new HashSet<>();
			}
			nodeIdSet.add(nodeId);
			operatorIdNodeIdSetMap.put(operatorId, nodeIdSet);
		}
		nodeIdSessionIdMap.put(nodeId, session.getId());
		sessionIdNodeIdMap.put(session.getId(), nodeId);
		sessionIdSessionMap.put(session.getId(), new ThreadSafeWebSocketSession(session));

		if (!nodeIdDataQueueMap.containsKey(nodeId)) {
			nodeIdDataQueueMap.put(nodeId, new LinkedBlockingQueue<byte[]>());
			executor.execute(new Runnable() {
				@Override
				public void run() {
					var threadNodeId = nodeId;
					logger.info("节点数据队列处理线程启动！节点ID:{}", threadNodeId);
					var dataQueue = nodeIdDataQueueMap.get(threadNodeId);
					while (true) {
						try {
							byte[] data = dataQueue.take();
							if (!nodeIdDataQueueMap.containsKey(threadNodeId)) {
								logger.info("节点数据队列处理线程退出！节点ID:{}", threadNodeId);
								break;
							}
							rpcServerProcessService.processData(threadNodeId, data);
						} catch (Exception e) {
							logger.error("数据队列处理线程发生错误", e);
						}

					}
				}
			});
		}

		// 对于使用令牌登录的节点,更新节点信息
		if (!session.getAttributes().containsKey(WebSocketConstant.KEY_SKIP_LOGIN)) {
			nodeService.updateNodeLoginInfo(nodeId, session.getId(), session.getRemoteAddress().getAddress().getHostAddress(), session.getRemoteAddress().getPort());
		} else {
			skipLoginNodeIdSet.add(nodeId);
		}

		session.getAttributes().put(WebSocketConstant.KEY_AUTHED, true);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, IOException {
		int nodeId = (int) session.getAttributes().get(RtConstant.KEY_NODE_ID);
		logger.warn("接收到文本消息,节点ID:{},会话ID:{}", nodeId, session.getId());
		try {
			session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Text messages not supported!"));
		} catch (Exception e) {
			logger.error("关闭异常", e);
		}
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		if (session.getAttributes().get(WebSocketConstant.KEY_AUTHED) == null || !(boolean) session.getAttributes().get(WebSocketConstant.KEY_AUTHED)) {
			int nodeId = (int) session.getAttributes().get(RtConstant.KEY_NODE_ID);
			logger.error("接收到二进制消息,丢弃,会话尚未得到验证,节点ID:{},会话ID:{}", nodeId, session.getId());
			return;
		}

		ByteBuffer byteBuffer = message.getPayload();
		byte[] data = new byte[byteBuffer.remaining()];
		byteBuffer.get(data);
		Integer nodeId = (int) session.getAttributes().get(RtConstant.KEY_NODE_ID);
		nodeIdDataQueueMap.get(nodeId).put(data);

	}

	@Override
	protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
		int nodeId = (int) session.getAttributes().get(RtConstant.KEY_NODE_ID);
		if (session.getAttributes().get(WebSocketConstant.KEY_AUTHED) == null || !(boolean) session.getAttributes().get(WebSocketConstant.KEY_AUTHED)) {
			logger.error("接收PONE消息,丢弃,会话尚未得到验证,节点ID:{},会话ID:{}", nodeId, session.getId());
			return;
		}
		Long pingTimestamp = message.getPayload().asLongBuffer().get();
		sessionIdPingStartTimeMap.remove(session.getId());
		logger.info("收到PONG,节点ID:{},会话ID:{},延时{}ms", nodeId, session.getId(), System.currentTimeMillis() - pingTimestamp);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		int nodeId = (int) session.getAttributes().get(RtConstant.KEY_NODE_ID);
		logger.error("传输错误,节点ID:{},会话ID:{}", nodeId, session.getId(), exception);
		try {
			if (session.isOpen()) {
				session.close();
			}
		} catch (Exception e) {
			logger.error("关闭会话错误,节点ID:{},会话ID:{}", nodeId, session.getId(), exception);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

		String sessionId = session.getId();
		int nodeId = (int) session.getAttributes().get(RtConstant.KEY_NODE_ID);
		if (sessionIdNodeIdMap.containsKey(sessionId)) {
			logger.info("连接已关闭,节点ID:{},会话ID:{}", nodeId, sessionId);
			sessionIdSessionMap.remove(sessionId);
			nodeIdSessionIdMap.remove(nodeId);
			sessionIdNodeIdMap.remove(sessionId);
			masterSystemService.removeGatewayIdByNodeId(nodeId);
			masterTradeExecuteService.removeSubscribeRelationByNodeId(nodeId);
			skipLoginNodeIdSet.remove(nodeId);
			skipTradeEventsNodeIdSet.remove(nodeId);
			String operatorId = nodeIdOperatorIdMap.remove(nodeId);
			synchronized (operatorIdNodeIdSetMap) {
				Set<Integer> nodeIdSet = operatorIdNodeIdSetMap.get(operatorId);
				if (nodeIdSet != null) {
					nodeIdSet.remove(nodeId);
				}
				if (nodeIdSet != null && nodeIdSet.isEmpty()) {
					operatorIdNodeIdSetMap.remove(operatorId);
				}
			}
			var dataQueue = nodeIdDataQueueMap.remove(nodeId);
			if (dataQueue != null) {
				byte[] shutdownData = new byte[] { 0 };
				dataQueue.put(shutdownData);
			}

			if (session.getAttributes().get(WebSocketConstant.KEY_AUTHED) != null && (boolean) session.getAttributes().get(WebSocketConstant.KEY_AUTHED)) {
				nodeService.updateNodeLogoutInfo(nodeId);
			}
		} else {
			logger.info("连接已关闭,连接未认证，节点ID:{},会话ID:{}", nodeId, sessionId);
		}

	}

	public boolean sendDataByNodeId(int nodeId, byte[] data) {
		String tragetSessionId = nodeIdSessionIdMap.get(nodeId);
		if (tragetSessionId == null) {
			logger.error("发送RPC错误,未找到目标节点ID对应的会话ID,目标节点ID:{}", nodeId);
			return false;
		}

		ThreadSafeWebSocketSession session = sessionIdSessionMap.get(tragetSessionId);
		if (session == null) {
			logger.error("发送二进制数据错误,未能找到会话,目标节点ID:{},会话ID:{}", nodeId, tragetSessionId);
			return false;
		}
		if (!session.isOpen()) {
			logger.error("发送二进制数据错误,会话处于关闭状态,目标节点ID:{},会话ID:{}", nodeId, tragetSessionId);
			return false;
		}
		BinaryMessage message = new BinaryMessage(data);
		try {
			session.sendMessage(message);
			return true;
		} catch (IOException e) {
			logger.error("发送二进制数据错误,目标节点ID:{},会话ID:{}", nodeId, tragetSessionId, e);
			return false;
		}
	}

	public void closeByNodeId(int nodeId) {
		if (!nodeIdSessionIdMap.containsKey(nodeId)) {
			logger.warn("根据节点ID关闭会话警告,无法通过节点ID找到会话ID,节点ID:{}", nodeId);
			return;
		} else {
			String sessionId = nodeIdSessionIdMap.get(nodeId);
			ThreadSafeWebSocketSession session = sessionIdSessionMap.get(sessionId);
			if (session != null && session.isOpen()) {
				try {
					session.close(CloseStatus.NORMAL.withReason("管理服务主动关闭!"));
				} catch (IOException e) {
					logger.error("根据节点ID关闭会话警告,无法通过会话ID找到会话或会话已关闭,节点ID:{},会话ID:{}", nodeId, sessionId, e);
				}
			} else {

				logger.warn("根据节点ID关闭会话警告,无法通过会话ID找到会话或会话已关闭,节点ID:{},会话ID:{}", nodeId, sessionId);
			}
		}
	}

	public boolean containsNodeId(int nodeId) {
		return nodeIdSessionIdMap.containsKey(nodeId);
	}

	public Set<Integer> getSkipTradeEventsNodeIdSet() {
		return skipTradeEventsNodeIdSet;
	}

	public Map<String, Set<Integer>> getOperatorIdNodeIdSetMap() {
		return operatorIdNodeIdSetMap;
	}

}