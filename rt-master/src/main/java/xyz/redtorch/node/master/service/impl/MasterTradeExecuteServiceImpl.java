package xyz.redtorch.node.master.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.master.rpc.service.RpcServerApiService;
import xyz.redtorch.node.master.rpc.service.RpcServerOverWebSocketProcessService;
import xyz.redtorch.node.master.service.MasterSystemService;
import xyz.redtorch.node.master.service.MasterTradeCachesService;
import xyz.redtorch.node.master.service.MasterTradeExecuteService;
import xyz.redtorch.node.master.service.OperatorService;
import xyz.redtorch.node.master.web.socket.WebSocketServerHandler;
import xyz.redtorch.pb.CoreField.*;
import xyz.redtorch.pb.CoreRpc.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MasterTradeExecuteServiceImpl implements MasterTradeExecuteService {
    private static final Logger logger = LoggerFactory.getLogger(MasterTradeExecuteServiceImpl.class);
    private final Set<String> originOrderIdSet = new HashSet<>(5000);
    private final Map<String, SubmitOrderReqField> orderIdToSubmitOrderReqMap = new ConcurrentHashMap<>(5000);
    private final Map<String, SubmitOrderReqField> originOrderIdToSubmitOrderReqMap = new ConcurrentHashMap<>(5000);
    private final Map<String, String> orderIdToSourceSessionIdMap = new ConcurrentHashMap<>(5000);
    private final Map<String, String> originOrderIdToSourceSessionIdMap = new ConcurrentHashMap<>(5000);
    private final Map<String, ContractField> unifiedSymbolToSubscribedContractMap = new HashMap<>();
    // key可能是unifiedSymbol也可能是dataSourceId
    private final Map<String, Set<String>> subscribeKeyToSubscribedSessionIdSetMap = new HashMap<>();
    private final Map<String, Set<String>> subscribedSessionIdToSubscribeKeySetMap = new HashMap<>();
    private final Map<String, String> subscribeKeyToUnifiedSymbolMap = new HashMap<>();
    private final Lock subscribeLock = new ReentrantLock();
    @Autowired
    private MasterSystemService masterSystemService;
    @Autowired
    private RpcServerOverWebSocketProcessService rpcOverWebSocketProcessService;
    @Autowired
    private RpcServerApiService rpcServerApiService;
    @Autowired
    private MasterTradeCachesService masterTradeCachesService;
    @Autowired
    private OperatorService operatorService;
    @Autowired
    private WebSocketServerHandler webSocketServerHandler;

    @Value("${rt.master.operatorId}")
    private String masterOperatorId;

    @Override
    public String getSessionIdByOrderId(String orderId) {
        return orderIdToSourceSessionIdMap.get(orderId);
    }

    @Override
    public String getSessionIdByOriginOrderId(String originOrderId) {
        return originOrderIdToSourceSessionIdMap.get(originOrderId);
    }

    @Override
    public void subscribe(String sessionId, CommonReqField commonReq, ContractField contract) {

        String operatorId = commonReq.getOperatorId();
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId); //

        int errorId = 0;
        String errorMsg = "";

        if (contract == null) {
            logger.error("订阅错误,业务ID:{},参数contract为空", transactionId);
            errorId = 1;
            errorMsg = "订阅错误,参数contract为空";
        } else if (StringUtils.isBlank(contract.getUnifiedSymbol())) {
            logger.error("订阅错误,业务ID:{},合约统一标识为空", transactionId);
            errorId = 1;
            errorMsg = "订阅错误,参数contract为空";
        } else if (masterTradeCachesService.queryContractByUnifiedSymbol(masterOperatorId, contract.getUnifiedSymbol()) == null) {
            logger.error("订阅错误,业务ID:{},未能找到合约:{},请尝试搜寻", transactionId, contract.getUnifiedSymbol());
            errorId = 1;
            errorMsg = "订阅错误未能找到合约,请尝试搜寻";
        } else {

            String unifiedSymbol = contract.getUnifiedSymbol();
            boolean canSubscribe = operatorService.checkSubscribePermission(operatorId, unifiedSymbol);

            if (canSubscribe) {
                subscribeLock.lock();
                try {
                    ContractField targetContract = masterTradeCachesService.queryContractByUnifiedSymbol(masterOperatorId, contract.getUnifiedSymbol());
                    unifiedSymbolToSubscribedContractMap.put(unifiedSymbol, targetContract);

                    String subscribeKey = unifiedSymbol;
                    if (StringUtils.isNotBlank(contract.getGatewayId())) {
                        subscribeKey = subscribeKey + "@" + contract.getGatewayId();
                    }

                    // 记录订阅节点信息
                    Set<String> sessionIdSet = new HashSet<>();
                    if (subscribeKeyToSubscribedSessionIdSetMap.containsKey(subscribeKey)) {
                        sessionIdSet = subscribeKeyToSubscribedSessionIdSetMap.get(subscribeKey);
                    }
                    sessionIdSet.add(sessionId);
                    subscribeKeyToSubscribedSessionIdSetMap.put(subscribeKey, sessionIdSet);
                    subscribeKeyToUnifiedSymbolMap.put(subscribeKey, unifiedSymbol);

                    Set<String> subscribeKeySet = subscribedSessionIdToSubscribeKeySetMap.computeIfAbsent(sessionId, k -> new HashSet<>());
                    subscribeKeySet.add(subscribeKey);

                    logger.info("会话ID:{},业务ID:{},操作员ID:{},订阅合约:{}", sessionId, transactionId, operatorId, subscribeKey);

                } catch (Exception e) {
                    throw e;
                } finally {
                    subscribeLock.unlock();
                }
            } else {
                logger.info("订阅错误,业务ID:{},操作员ID:{},无权订阅合约:{}", transactionId, operatorId, unifiedSymbol);
            }

        }

        commonRspBuilder.setErrorId(errorId);
        commonRspBuilder.setErrorMsg(errorMsg);
        RpcSubscribeRsp.Builder rpcSubscribeRspBuilder = RpcSubscribeRsp.newBuilder().setCommonRsp(commonRspBuilder);
        rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.SUBSCRIBE_RSP, commonReq.getTransactionId(), rpcSubscribeRspBuilder.build().toByteString());
    }

    @Override
    public void unsubscribe(String sessionId, CommonReqField commonReq, ContractField contract) {

        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId); //

        int errorId = 0;
        String errorMsg = "";

        if (contract == null) {
            logger.error("取消订阅错误,业务ID:{},参数contract为空", transactionId);
            errorId = 1;
            errorMsg = "取消订阅错误,参数contract为空";
        } else if (StringUtils.isBlank(contract.getUnifiedSymbol())) {
            logger.error("取消订阅错误,业务ID:{},合约统一标识为空", transactionId);
            errorId = 1;
            errorMsg = "取消订阅错误,参数contract为空";
        } else if (masterTradeCachesService.queryContractByUnifiedSymbol(masterOperatorId, contract.getUnifiedSymbol()) == null) {
            logger.error("取消订阅错误,业务ID:{},未能找到合约:{},请尝试搜寻", transactionId, contract.getUnifiedSymbol());
            errorId = 1;
            errorMsg = "取消订阅错误未能找到合约,请尝试搜寻";
        } else {

            subscribeLock.lock();
            try {
                String unifiedSymbol = contract.getUnifiedSymbol();

                String subscribeKey = unifiedSymbol;
                if (StringUtils.isNotBlank(contract.getGatewayId())) {
                    subscribeKey = subscribeKey + "@" + contract.getGatewayId();
                }

                Set<String> subscribeKeySet = subscribedSessionIdToSubscribeKeySetMap.get(sessionId);
                if (subscribeKeySet != null) {
                    subscribeKeySet.remove(subscribeKey);
                }

                if (subscribeKeyToSubscribedSessionIdSetMap.containsKey(subscribeKey)) {
                    Set<String> sessionIdSet = subscribeKeyToSubscribedSessionIdSetMap.get(subscribeKey);
                    if (sessionIdSet.contains(sessionId)) {
                        sessionIdSet.remove(sessionId);
                        if (sessionIdSet.isEmpty()) {
                            logger.info("订阅键{}已经没有节点订阅,删除", subscribeKey);
                            subscribeKeyToSubscribedSessionIdSetMap.remove(subscribeKey);
                            // 如果不存在其它key的订阅关系了,则完全删除订阅关系
                            if (!subscribeKeyToSubscribedSessionIdSetMap.containsKey(unifiedSymbol)) {
                                logger.info("合约{}已经没有节点订阅,删除", unifiedSymbol);
                                unifiedSymbolToSubscribedContractMap.remove(unifiedSymbol);
                            }
                        }
                    } else {
                        errorId = 1;
                        errorMsg = "订阅关系不存在";
                    }
                } else {
                    errorId = 1;
                    errorMsg = "订阅关系不存在";
                }

            } catch (Exception e) {
                throw e;
            } finally {
                subscribeLock.unlock();
            }
        }

        commonRspBuilder.setErrorId(errorId);
        commonRspBuilder.setErrorMsg(errorMsg);

        RpcUnsubscribeRsp.Builder rpcUnsubscribeRspBuilder = RpcUnsubscribeRsp.newBuilder().setCommonRsp(commonRspBuilder);
        rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.UNSUBSCRIBE_RSP, commonReq.getTransactionId(), rpcUnsubscribeRspBuilder.build().toByteString());
    }

    @Override
    public void removeSubscribeRelationBySessionId(String sessionId) {
        subscribeLock.lock();
        try {
            subscribedSessionIdToSubscribeKeySetMap.remove(sessionId);
            List<String> removeKeyList = new ArrayList<>();
            for (Entry<String, Set<String>> entry : subscribeKeyToSubscribedSessionIdSetMap.entrySet()) {
                String subscribeKey = entry.getKey();
                Set<String> sessionIdSet = entry.getValue();
                sessionIdSet.remove(sessionId);
                if (sessionIdSet.isEmpty()) {
                    logger.info("订阅键{}已经没有节点订阅,删除", subscribeKey);
                    removeKeyList.add(subscribeKey);
                }
            }
            for (String subscribeKey : removeKeyList) {
                subscribeKeyToSubscribedSessionIdSetMap.remove(subscribeKey);
                if (subscribeKeyToUnifiedSymbolMap.containsKey(subscribeKey)) {
                    String unifiedSymbol = subscribeKeyToUnifiedSymbolMap.get(subscribeKey);
                    // 如果不存在其它key的订阅关系了,则完全删除订阅关系
                    if (!subscribeKeyToSubscribedSessionIdSetMap.containsKey(unifiedSymbol)) {
                        logger.info("合约{}已经没有节点订阅,删除", unifiedSymbol);
                        unifiedSymbolToSubscribedContractMap.remove(unifiedSymbol);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("根据会话ID删除订阅关系错误", e);
        } finally {
            subscribeLock.unlock();
        }
    }

    @Override
    public Set<String> getSubscribedSessionIdSet(String subscribeKey) {
        subscribeLock.lock();
        Set<String> sessionIdSet = new HashSet<>();
        try {
            if (subscribeKeyToSubscribedSessionIdSetMap.containsKey(subscribeKey)) {
                sessionIdSet = new HashSet<>(subscribeKeyToSubscribedSessionIdSetMap.get(subscribeKey));
            }
        } catch (Exception e) {
            logger.error("根据订阅键获取订阅会话ID列表错误", e);
        } finally {
            subscribeLock.unlock();
        }
        return sessionIdSet;
    }

    @Override
    public void submitOrder(String sessionId, CommonReqField commonReq, SubmitOrderReqField submitOrderReq) {

        String operatorId = commonReq.getOperatorId();
        String transactionId = commonReq.getTransactionId();

        RpcSubmitOrderRsp.Builder rpcSubmitOrderRsp = RpcSubmitOrderRsp.newBuilder();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId); //

        int errorId = 0;
        String errorMsg = "";

        if (submitOrderReq == null) {
            logger.error("提交定单错误,业务ID:{},参数submitOrderReq缺失", transactionId);
            errorId = 1;
            errorMsg = "提交定单错误,参数submitOrderReq缺失";
        } else if (StringUtils.isBlank(submitOrderReq.getContract().getUnifiedSymbol())) {
            logger.error("提交定单错误,业务ID:{},参数contract不正确", transactionId);
            errorId = 1;
            errorMsg = "提交定单错误,参数contract不正确";
        } else if (StringUtils.isBlank(submitOrderReq.getOriginOrderId())) {
            logger.error("提交定单错误,业务ID:{},原始定单ID为空", transactionId);
            errorId = 1;
            errorMsg = "提交定单错误,原始定单ID为空";
        } else if (StringUtils.isBlank(submitOrderReq.getGatewayId())) {
            logger.error("提交定单错误,业务ID:{},网关ID为空", transactionId);
            errorMsg = "提交定单错误,网关ID为空";
        } else if (masterSystemService.getNodeIdByGatewayId(submitOrderReq.getGatewayId()) == null) {
            logger.error("提交定单错误,业务ID:{},无法找到网关所在的节点ID,网关ID:{}", transactionId, submitOrderReq.getGatewayId());
            errorId = 1;
            errorMsg = "提交定单错误，无法找到网关所在的节点ID";
        } else if (webSocketServerHandler.getSessionIdByNodeId(masterSystemService.getNodeIdByGatewayId(submitOrderReq.getGatewayId())) == null) {
            logger.error("提交定单错误,业务ID:{},无法找到网关所在的会话ID,网关ID:{}", transactionId, submitOrderReq.getGatewayId());
            errorId = 1;
            errorMsg = "提交定单错误，无法找到网关所在的会话ID";
        } else if (originOrderIdSet.contains(submitOrderReq.getOriginOrderId())) {
            logger.error("提交定单错误,业务ID:{},原始定单ID重复,原始订单ID{}", transactionId, submitOrderReq.getOriginOrderId());
            errorId = 1;
            errorMsg = "提交定单错误,原始定单ID重复";
        } else {
            originOrderIdSet.add(submitOrderReq.getOriginOrderId());

            // 验证权限
            String unifiedSymbol = submitOrderReq.getContract().getUnifiedSymbol();
            String accountId = submitOrderReq.getAccountCode() + "@" + submitOrderReq.getCurrency().getValueDescriptor().getName() + "@" + submitOrderReq.getGatewayId();

            boolean canTradeAccount = operatorService.checkTradeAccountPermission(operatorId, accountId);
            boolean canTradeContract = false;

            if (canTradeAccount) {
                canTradeContract = operatorService.checkTradeContractPermission(operatorId, unifiedSymbol);
            }

            Integer nodeId = masterSystemService.getNodeIdByGatewayId(submitOrderReq.getGatewayId());

            String targetSessionId = webSocketServerHandler.getSessionIdByNodeId(nodeId);

            if (canTradeAccount && canTradeContract) {
                originOrderIdToSubmitOrderReqMap.put(submitOrderReq.getOriginOrderId(), submitOrderReq);
                originOrderIdToSourceSessionIdMap.put(submitOrderReq.getOriginOrderId(), sessionId);
                String orderId = rpcServerApiService.submitOrder(submitOrderReq, targetSessionId, transactionId, null);

                if (StringUtils.isBlank(orderId)) {
                    originOrderIdToSubmitOrderReqMap.remove(submitOrderReq.getOriginOrderId());
                    originOrderIdToSourceSessionIdMap.remove(submitOrderReq.getOriginOrderId());
                    logger.error("提交定单错误,业务ID:{},深度调用返回空定单ID", transactionId);
                    errorId = 1;
                    errorMsg = "提交定单错误,深度调用返回空定单ID";
                } else {
                    orderIdToSubmitOrderReqMap.put(orderId, submitOrderReq);
                    orderIdToSourceSessionIdMap.put(orderId, sessionId);
                    rpcSubmitOrderRsp.setOrderId(orderId);
                }

            } else if (!canTradeAccount) {
                commonRspBuilder.setErrorId(1).setErrorMsg("" + accountId);
                logger.warn("会话ID:{},业务ID:{},操作员ID:{},无权交易账户:{}", sessionId, transactionId, operatorId, unifiedSymbol);

                errorId = 1;
                errorMsg = "此操作员无权交易此账户";

            } else if (!canTradeContract) {
                logger.info("会话ID:{},业务ID:{},操作员ID:{},无权交易合约:{}", sessionId, transactionId, operatorId, unifiedSymbol);

                errorId = 1;
                errorMsg = "此操作员无权交易此合约";
            }

        }

        commonRspBuilder.setErrorId(errorId);
        commonRspBuilder.setErrorMsg(errorMsg);
        rpcSubmitOrderRsp.setCommonRsp(commonRspBuilder);
        rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.SUBMIT_ORDER_RSP, commonReq.getTransactionId(), rpcSubmitOrderRsp.build().toByteString());

    }

    @Override
    public void cancelOrder(String sessionId, CommonReqField commonReq, CancelOrderReqField cancelOrderReq) {

        String operatorId = commonReq.getOperatorId();
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId); //

        int errorId = 0;
        String errorMsg = "";

        if (cancelOrderReq == null) {
            logger.error("撤销定单错误,业务ID:{},参数cancelOrderReq缺失", transactionId);
            errorId = 1;
            errorMsg = "撤销定单错误，参数cancelOrderReq缺失";
        } else if (StringUtils.isBlank(cancelOrderReq.getOrderId()) && StringUtils.isBlank(cancelOrderReq.getOriginOrderId())) {
            logger.error("撤销定单错误,业务ID:{},参数orderId与originOrderId同时缺失", transactionId);
            errorId = 1;
            errorMsg = "撤销定单错误，参数orderId与originOrderId同时缺失";
        } else {

            String gatewayId = null;
            String accountId = null;
            String unifiedSymbol = null;

            if (StringUtils.isBlank(cancelOrderReq.getOriginOrderId())) {
                String orderId = cancelOrderReq.getOrderId();

                if (orderIdToSubmitOrderReqMap.containsKey(orderId)) {
                    SubmitOrderReqField submitOrderReq = orderIdToSubmitOrderReqMap.get(orderId);
                    gatewayId = submitOrderReq.getGatewayId();
                    unifiedSymbol = submitOrderReq.getContract().getUnifiedSymbol();
                    String currency = submitOrderReq.getContract().getCurrency().getValueDescriptor().getName();
                    String accountCode = submitOrderReq.getAccountCode();
                    accountId = accountCode + "@" + currency + "@" + gatewayId;
                } else {
                    OrderField order = masterTradeCachesService.queryOrderByOrderId(masterOperatorId, orderId);
                    if (order != null) {
                        gatewayId = order.getContract().getGatewayId();
                        accountId = order.getAccountId();
                        unifiedSymbol = order.getContract().getUnifiedSymbol();
                    }
                }

            } else {

                String originOrderId = cancelOrderReq.getOriginOrderId();

                if (originOrderIdToSubmitOrderReqMap.containsKey(originOrderId)) {
                    SubmitOrderReqField submitOrderReq = originOrderIdToSubmitOrderReqMap.get(originOrderId);
                    gatewayId = submitOrderReq.getGatewayId();
                    unifiedSymbol = submitOrderReq.getContract().getUnifiedSymbol();
                    String currency = submitOrderReq.getContract().getCurrency().getValueDescriptor().getName();
                    String accountCode = submitOrderReq.getAccountCode();
                    accountId = accountCode + "@" + currency + "@" + gatewayId;
                } else {
                    OrderField order = masterTradeCachesService.queryOrderByOriginOrderId(masterOperatorId, originOrderId);
                    if (order != null) {
                        gatewayId = order.getContract().getGatewayId();
                        accountId = order.getAccountId();
                        unifiedSymbol = order.getContract().getUnifiedSymbol();
                    }
                }
            }

            if (StringUtils.isBlank(gatewayId)) {
                logger.error("撤销定单错误,业务ID:{},无法找到网关信息,网关ID:{}", transactionId, gatewayId);
                errorId = 1;
                errorMsg = "撤销定单错误,无法找到网关信息";
            } else if (masterSystemService.getNodeIdByGatewayId(gatewayId) == null) {
                logger.error("提交定单错误,业务ID:{},无法找到网关所在的节点ID,网关ID:{}", transactionId, gatewayId);
                errorId = 1;
                errorMsg = "提交定单错误，无法找到网关所在的节点ID";
            } else if (webSocketServerHandler.getSessionIdByNodeId(masterSystemService.getNodeIdByGatewayId(gatewayId)) == null) {
                logger.error("提交定单错误,业务ID:{},无法找到网关所在的会话ID,网关ID:{}", transactionId, gatewayId);
                errorId = 1;
                errorMsg = "提交定单错误，无法找到网关所在的会话ID";
            } else {
                boolean canTradeAccount = operatorService.checkTradeAccountPermission(operatorId, accountId);
                boolean canTradeContract = false;

                if (canTradeAccount) {
                    canTradeContract = operatorService.checkTradeContractPermission(operatorId, unifiedSymbol);
                }

                if (canTradeAccount && canTradeContract) {

                    Integer nodeId = masterSystemService.getNodeIdByGatewayId(gatewayId);

                    String targetSessionId = webSocketServerHandler.getSessionIdByNodeId(nodeId);

                    boolean result = rpcServerApiService.cancelOrder(cancelOrderReq, targetSessionId, transactionId, null);

                    if (!result) {
                        logger.error("撤销定单错误,业务ID{},深度调用返回失败", transactionId);
                        errorId = 1;
                        errorMsg = "撤销定单错误,深度调用返回失败";
                    }
                } else if (!canTradeAccount) {
                    commonRspBuilder.setErrorId(1).setErrorMsg("" + accountId);
                    logger.warn("业务ID:{},会话ID:{},操作员ID:{},无权交易账户:{}", transactionId, sessionId, operatorId, unifiedSymbol);

                    errorId = 1;
                    errorMsg = "此操作员无权交易此账户";

                } else if (!canTradeContract) {
                    logger.info("业务ID:{},会话ID:{},操作员ID:{},无权交易合约:{}", transactionId, sessionId, operatorId, unifiedSymbol);

                    errorId = 1;
                    errorMsg = "此操作员无权交易此合约";
                }

            }
        }
        commonRspBuilder.setErrorId(errorId);
        commonRspBuilder.setErrorMsg(errorMsg);
        RpcCancelOrderRsp.Builder rpcCancelOrderRsp = RpcCancelOrderRsp.newBuilder().setCommonRsp(commonRspBuilder);
        rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.CANCEL_ORDER_RSP, commonReq.getTransactionId(), rpcCancelOrderRsp.build().toByteString());
    }

    @Override
    public void searchContract(String sessionId, CommonReqField commonReq, ContractField contract) {
        String transactionId = commonReq.getTransactionId();
        int errorId = 0;
        String errorMsg = "";

        if (contract == null) {
            logger.error("搜索合约错误,业务ID:{},参数contract缺失", transactionId);
            errorId = 1;
            errorMsg = "搜索合约错误,参数contract缺失";
        } else {

            List<Integer> nodeIdList = masterSystemService.getNodeIdList();

            for (Integer nodeId : nodeIdList) {
                try {
                    String targetSessionId = webSocketServerHandler.getSessionIdByNodeId(nodeId);

                    if (targetSessionId != null) {
                        boolean result = rpcServerApiService.searchContract(contract, targetSessionId, transactionId, null);
                        if (!result) {
                            logger.error("搜寻合约错误,业务ID:{},深度调用返回失败,从节点会话ID:{}", transactionId, targetSessionId);
                        }
                    }
                } catch (Exception e) {
                    logger.error("搜寻合约异常,业务ID:{}", transactionId, e);
                }
            }
        }

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(errorId)//
                .setErrorMsg(errorMsg);

        RpcSearchContractRsp.Builder rpcSearchContractRsp = RpcSearchContractRsp.newBuilder().setCommonRsp(commonRspBuilder);
        rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.SEARCH_CONTRACT_RSP, commonReq.getTransactionId(), rpcSearchContractRsp.build().toByteString());
    }

    @Override
    public List<ContractField> getSubscribedContract() {
        subscribeLock.lock();
        try {
            return new ArrayList<>(unifiedSymbolToSubscribedContractMap.values());
        } catch (Exception e) {
            logger.error("获取已经订阅合约列表发生错误", e);
        } finally {
            subscribeLock.unlock();
        }
        return new ArrayList<>();
    }

    @Override
    public Set<String> getSubscribeKeySet(String sessionId) {
        return subscribedSessionIdToSubscribeKeySetMap.get(sessionId);
    }

    @Override
    public SubmitOrderReqField getSubmitOrderReqByOrderId(String orderId) {
        return orderIdToSubmitOrderReqMap.get(orderId);
    }

}
