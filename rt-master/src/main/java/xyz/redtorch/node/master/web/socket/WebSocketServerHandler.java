package xyz.redtorch.node.master.web.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.web.socket.ThreadSafeWebSocketSession;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.rpc.service.RpcServerOverWebSocketProcessService;
import xyz.redtorch.node.master.service.HaSessionManger;
import xyz.redtorch.node.master.service.MasterSystemService;
import xyz.redtorch.node.master.service.MasterTradeExecuteService;
import xyz.redtorch.node.master.service.NodeService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class WebSocketServerHandler extends AbstractWebSocketHandler implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private final Map<String, ThreadSafeWebSocketSession> sessionIdSessionMap = new HashMap<>();
    private final ReentrantLock sessionIdSessionMapLock = new ReentrantLock();

    private final Map<String, String> authTokenToSessionIdMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdToAuthTokenMap = new ConcurrentHashMap<>();

    private final Map<Integer, String> nodeIdSessionIdMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> sessionIdNodeIdMap = new ConcurrentHashMap<>();

    private final Map<String, LinkedBlockingQueue<byte[]>> sessionIdDataQueueMap = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> operatorIdSessionIdSetMap = new HashMap<>();
    private final ReentrantLock operatorIdSessionIdSetMapLock = new ReentrantLock();
    private final Map<String, String> sessionIdOperatorIdMap = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Map<String, Long> sessionIdPingStartTimeMap = new ConcurrentHashMap<>();

    @Autowired
    private NodeService nodeService;
    @Autowired
    private MasterSystemService masterSystemService;
    @Autowired
    private MasterTradeExecuteService masterTradeExecuteService;
    @Autowired
    private RpcServerOverWebSocketProcessService rpcOverWebSocketProcessService;
    @Autowired
    private HaSessionManger haSessionManger;

    @Override
    public void afterPropertiesSet() throws Exception {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10000);
                    List<ThreadSafeWebSocketSession> sessionList = new ArrayList<>();
                    sessionIdSessionMapLock.lock();
                    try {
                        sessionList = new ArrayList<>(sessionIdSessionMap.values());
                    } finally {
                        sessionIdSessionMapLock.unlock();
                    }

                    for (ThreadSafeWebSocketSession threadSafeWebSocketSession : sessionList) {

                        if (sessionIdPingStartTimeMap.containsKey(threadSafeWebSocketSession.getId())) {

                            Long startPingTimestamp = sessionIdPingStartTimeMap.get(threadSafeWebSocketSession.getId());

                            if (System.currentTimeMillis() - startPingTimestamp > 21000) {
                                logger.error("PING超时,会话ID:{}", threadSafeWebSocketSession.getId());
                                try {
                                    threadSafeWebSocketSession.close(CloseStatus.NORMAL.withReason("管理服务主动关闭!"));
                                } catch (IOException e) {
                                    logger.error("PING超时,主动关闭会话错误", e);
                                }
                                sessionIdPingStartTimeMap.remove(threadSafeWebSocketSession.getId());
                            }
                        } else if (threadSafeWebSocketSession.isOpen()) {

                            Long pingStartTime = System.currentTimeMillis();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES).putLong(System.currentTimeMillis()).flip();
                            PingMessage message = new PingMessage(byteBuffer);
                            threadSafeWebSocketSession.sendMessage(message);

                            if (!sessionIdPingStartTimeMap.containsKey(threadSafeWebSocketSession.getId())) {
                                sessionIdPingStartTimeMap.put(threadSafeWebSocketSession.getId(), pingStartTime);
                            }
                        }

                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    logger.error("定时PING线程异常", e);
                }
            }
        });

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, IOException {
        if (session.getAttributes().get(CommonConstant.KEY_VERIFIED) != null && (boolean) session.getAttributes().get(CommonConstant.KEY_VERIFIED)) {
            logger.error("会话已经验证,不再接受新的文本消息,会话ID:{}", session.getId());
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("会话已经验证,不再接受新的文本消息!"));
            } catch (Exception e) {
                logger.error("关闭异常", e);
            }
        }
        try {
            String data = message.getPayload();
            String authToken = JSON.parseObject(data).getString(CommonConstant.KEY_AUTH_TOKEN);

            String remoteAddress = "";
            if (session.getRemoteAddress() != null) {
                remoteAddress = session.getRemoteAddress().toString();
            }

            if (StringUtils.isBlank(authToken)) {
                logger.error("连接前校验登录字段失败,未获取到参数authToken,远程地址{}", remoteAddress);
                try {
                    session.close(CloseStatus.NOT_ACCEPTABLE.withReason("登录失败!"));
                } catch (Exception e) {
                    logger.error("关闭异常", e);
                }
                return;
            }
            Integer nodeId = nodeService.getNodeIdByToken(authToken);

            if (nodeId != null && nodeId != 0) {
                logger.error("连接前校验authToken成功,NODE ID:{}接入,远程地址{}", nodeId, remoteAddress);
                session.getAttributes().put(CommonConstant.KEY_NODE_ID, nodeId);
                session.getAttributes().put(CommonConstant.KEY_AUTH_TOKEN, authToken);

            } else if (haSessionManger.getUserPoByAuthToken(authToken) != null) {
                logger.error("连接前校验authToken成功,HA接入,远程地址{}", remoteAddress);
                session.getAttributes().put(CommonConstant.KEY_AUTH_TOKEN, authToken);
            } else {
                logger.error("连接前校验authToken失败,远程地址{}", remoteAddress);
                try {
                    session.close(CloseStatus.NOT_ACCEPTABLE.withReason("登录失败!"));
                } catch (Exception e) {
                    logger.error("关闭异常", e);
                }
                return;
            }

        } catch (Exception e) {
            try {
                session.close(CloseStatus.SERVER_ERROR.withReason("处理文本错误!"));
            } catch (Exception e1) {
                logger.error("关闭异常", e);
            }
        }
        // ============================================
        try {
            String sessionId = session.getId();
            String authToken = (String) session.getAttributes().get(CommonConstant.KEY_AUTH_TOKEN);

            if (authTokenToSessionIdMap.containsKey(authToken)) {
                logger.error("会话冲突,断开新会话,会话ID:{}", sessionId);
                try {
                    session.close(CloseStatus.NORMAL.withReason("节点冲突!"));
                } catch (Exception e) {
                    logger.warn("关闭异常", e);
                }
                return;
            }

            if (session.getAttributes().containsKey(CommonConstant.KEY_NODE_ID)) {

                Integer nodeId = (Integer) session.getAttributes().get(CommonConstant.KEY_NODE_ID);

                nodeIdSessionIdMap.put(nodeId, sessionId);
                sessionIdNodeIdMap.put(sessionId, nodeId);

                if (session.getRemoteAddress() != null) {
                    nodeService.updateNodeLoginInfo(nodeId, sessionId, session.getRemoteAddress().getHostName(), session.getRemoteAddress().getPort());
                }

            } else {

                UserPo userPo = haSessionManger.getUserPoByAuthToken(authToken);
                String operatorId = userPo.getOperatorId();

                sessionIdOperatorIdMap.put(sessionId, operatorId);

                operatorIdSessionIdSetMapLock.lock();
                try {
                    Set<String> sessionIdSet = operatorIdSessionIdSetMap.get(operatorId);
                    if (sessionIdSet == null) {
                        sessionIdSet = new HashSet<>();
                    }
                    sessionIdSet.add(sessionId);
                    operatorIdSessionIdSetMap.put(operatorId, sessionIdSet);
                } finally {
                    operatorIdSessionIdSetMapLock.unlock();
                }
            }

            if (!sessionIdDataQueueMap.containsKey(sessionId)) {
                sessionIdDataQueueMap.put(sessionId, new LinkedBlockingQueue<>());
                executor.execute(() -> {
                    logger.info("节点数据队列处理线程启动！会话ID:{}", sessionId);
                    var dataQueue = sessionIdDataQueueMap.get(sessionId);
                    while (true) {
                        try {
                            byte[] data = dataQueue.take();
                            if(haSessionManger.getUserPoByAuthToken(authToken)==null&&!session.getAttributes().containsKey(CommonConstant.KEY_NODE_ID)){
                                try {
                                    session.close(CloseStatus.NORMAL.withReason("授权无效"));
                                } catch (Exception e) {
                                    logger.warn("授权过期,关闭会话,会话ID:{}", sessionId, e);
                                }
                                return;
                            }
                            if (!sessionIdDataQueueMap.containsKey(sessionId)) {
                                logger.info("节点数据队列处理线程退出！会话ID:{}", sessionId);
                                break;
                            }
                            // 预留,如果非标准客户端,可能不会返回PONE,导致TOKEN过期
                            // haSessionManger.freshAuthToken(authToken);
                            rpcOverWebSocketProcessService.processData(sessionId, data);
                        } catch (Exception e) {
                            logger.error("数据队列处理线程发生错误", e);
                        }

                    }
                });
            }
            // 加入SessionMap
            sessionIdSessionMapLock.lock();
            try {
                sessionIdSessionMap.put(sessionId, new ThreadSafeWebSocketSession(session));
            } finally {
                sessionIdSessionMapLock.unlock();
            }

            authTokenToSessionIdMap.put(authToken, sessionId);
            sessionIdToAuthTokenMap.put(sessionId, authToken);

            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put(CommonConstant.KEY_VERIFIED, true);
                session.sendMessage(new TextMessage(jsonData.toJSONString()));
            } catch (Exception e) {
                logger.error("回报验证状态", e);
            }

            session.getAttributes().put(CommonConstant.KEY_VERIFIED, true);
        } catch (Exception e) {
            logger.error("处理接入发生错误", e);
        }

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        if (session.getAttributes().get(CommonConstant.KEY_VERIFIED) == null || !(boolean) session.getAttributes().get(CommonConstant.KEY_VERIFIED)) {
            logger.error("接收到二进制消息,丢弃,会话尚未得到验证,会话ID:{}", session.getId());
            return;
        }

        ByteBuffer byteBuffer = message.getPayload();
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data);
        sessionIdDataQueueMap.get(session.getId()).put(data);

    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        if (session.getAttributes().get(CommonConstant.KEY_VERIFIED) == null || !(boolean) session.getAttributes().get(CommonConstant.KEY_VERIFIED)) {
            logger.error("接收PONE消息,丢弃,会话尚未得到验证,会话ID:{}", session.getId());
            return;
        }

        String authToken = sessionIdToAuthTokenMap.get(session.getId());
        haSessionManger.freshAuthToken(authToken);

        long pingTimestamp = message.getPayload().asLongBuffer().get();
        sessionIdPingStartTimeMap.remove(session.getId());
        logger.info("收到PONG,会话ID:{},延时{}ms", session.getId(), System.currentTimeMillis() - pingTimestamp);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("传输错误,会话ID:{}", session.getId(), exception);
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            logger.error("关闭会话错误,会话ID:{}", session.getId(), exception);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        String sessionId = session.getId();

        try {

            if (sessionIdNodeIdMap.containsKey(sessionId)) {
                Integer nodeId = (Integer) session.getAttributes().get(CommonConstant.KEY_NODE_ID);
                logger.info("连接已关闭,会话ID:{},节点ID:{}", sessionId, nodeId);

                masterSystemService.removeGatewayIdByNodeId(nodeId);
                nodeIdSessionIdMap.remove(nodeId);
                sessionIdNodeIdMap.remove(sessionId);

                if (session.getAttributes().get(CommonConstant.KEY_VERIFIED) != null && (boolean) session.getAttributes().get(CommonConstant.KEY_VERIFIED)) {
                    nodeService.updateNodeLogoutInfo(nodeId);
                }

            } else {
                String operatorId = sessionIdOperatorIdMap.remove(sessionId);
                operatorIdSessionIdSetMapLock.lock();
                try {
                    Set<String> sessionIdSet = operatorIdSessionIdSetMap.get(operatorId);
                    if (sessionIdSet != null) {
                        sessionIdSet.remove(sessionId);
                    }
                    if (sessionIdSet != null && sessionIdSet.isEmpty()) {
                        operatorIdSessionIdSetMap.remove(operatorId);
                    }
                } finally {
                    operatorIdSessionIdSetMapLock.unlock();
                }
            }

            sessionIdSessionMap.remove(sessionId);

            String authToken = sessionIdToAuthTokenMap.remove(sessionId);

            if (authToken != null) {
                authTokenToSessionIdMap.remove(authToken);
            }

            masterTradeExecuteService.removeSubscribeRelationBySessionId(sessionId);
        } catch (Exception e) {
            logger.error("处理关闭发生错误", e);
        }

        var dataQueue = sessionIdDataQueueMap.remove(sessionId);
        if (dataQueue != null) {
            byte[] shutdownData = new byte[]{0};
            dataQueue.put(shutdownData);
        }

    }

    public boolean sendDataBySessionId(String sessionId, byte[] data) {

        ThreadSafeWebSocketSession session = sessionIdSessionMap.get(sessionId);
        if (session == null) {
            logger.error("发送二进制数据错误,未能找到会话,会话ID:{}", sessionId);
            return false;
        }
        if (!session.isOpen()) {
            logger.error("发送二进制数据错误,会话处于关闭状态,会话ID:{}", sessionId);
            return false;
        }
        BinaryMessage message = new BinaryMessage(data);
        try {
            session.sendMessage(message);
            return true;
        } catch (IOException e) {
            logger.error("发送二进制数据错误,会话ID:{}", sessionId, e);
            return false;
        }
    }

    public void closeByNodeId(Integer nodeId) {
        if (!nodeIdSessionIdMap.containsKey(nodeId)) {
            logger.warn("根据节点ID关闭会话警告,无法通过节点ID找到会话ID,节点ID:{}", nodeId);
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

    public boolean containsNodeId(Integer nodeId) {
        return nodeIdSessionIdMap.containsKey(nodeId);
    }

    public Map<String, Set<String>> getOperatorIdSessionIdSetMap() {
        return operatorIdSessionIdSetMap;
    }

    public ReentrantLock getOperatorIdSessionIdSetMapLock() {
        return operatorIdSessionIdSetMapLock;
    }

    public String getSessionIdByNodeId(Integer nodeId) {
        return nodeIdSessionIdMap.get(nodeId);
    }

    public Integer getNodeIdBySessionId(String sessionId) {
        return sessionIdNodeIdMap.get(sessionId);
    }

    public String getSessionIdByAuthToken(String authToken) {
        return authTokenToSessionIdMap.get(authToken);
    }
}