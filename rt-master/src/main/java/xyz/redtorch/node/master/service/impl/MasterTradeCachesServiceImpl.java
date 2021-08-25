package xyz.redtorch.node.master.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.node.master.po.GatewayPo;
import xyz.redtorch.node.master.rpc.service.RpcServerApiService;
import xyz.redtorch.node.master.service.GatewayService;
import xyz.redtorch.node.master.service.MasterTradeCachesService;
import xyz.redtorch.node.master.service.OperatorService;
import xyz.redtorch.node.master.web.socket.WebSocketServerHandler;
import xyz.redtorch.pb.CoreEnum.ConnectStatusEnum;
import xyz.redtorch.pb.CoreEnum.ContingentConditionEnum;
import xyz.redtorch.pb.CoreField.*;
import xyz.redtorch.pb.CoreRpc.*;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

@Service
public class MasterTradeCachesServiceImpl implements MasterTradeCachesService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MasterTradeCachesServiceImpl.class);

    private final ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();
    private final Lock contractMapLock = new ReentrantLock();
    private final Lock tickMapLock = new ReentrantLock();
    private final Lock orderMapLock = new ReentrantLock();
    private final Lock tradeMapLock = new ReentrantLock();
    private final Lock accountMapLock = new ReentrantLock();
    private final Lock positionMapLock = new ReentrantLock();
    // 合约信息长期缓存
    private final Map<String, ContractField> contractMap = new HashMap<>(200000);
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private OperatorService operatorService;
    @Autowired
    private WebSocketServerHandler webSocketServerHandler;
    @Autowired
    private RpcServerApiService rpcServerApiService;
    private final Map<String, OrderField> workingOrderMap = new HashMap<>(2000);
    private Map<String, TickField> tickMap = new HashMap<>(1000);
    private Map<String, OrderField> orderMap = new HashMap<>(50000);
    private Map<String, TradeField> tradeMap = new HashMap<>(100000);
    private Map<String, AccountField> accountMap = new HashMap<>(500);
    private Map<String, PositionField> positionMap = new HashMap<>(5000);

    // 用于降低合约缓存的同步频率
    private boolean fetchContractSwitch = false;

    @Override
    public void afterPropertiesSet() throws Exception {

        // 定时清理缓存任务
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                List<GatewayPo> gatewayList = gatewayService.getGatewayList();

                Set<String> retainGatewayIdSet = new HashSet<>();
                for (GatewayPo gateway : gatewayList) {
                    if (ConnectStatusEnum.CS_Connected_VALUE == gateway.getStatus() || ConnectStatusEnum.CS_Connecting_VALUE == gateway.getStatus()) {
                        retainGatewayIdSet.add(gateway.getGatewayId());
                    }
                }
                accountMapLock.lock();
                try {
                    // 删除账户缓存
                    accountMap = accountMap.entrySet().stream().filter(map -> retainGatewayIdSet.contains(map.getValue().getGatewayId()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                } catch (Exception e) {
                    logger.error("删除账户缓存异常", e);
                } finally {
                    accountMapLock.unlock();
                }

                positionMapLock.lock();
                try {
                    // 删除持仓缓存
                    positionMap = positionMap.entrySet().stream().filter(map -> retainGatewayIdSet.contains(map.getValue().getGatewayId()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                } catch (Exception e) {
                    logger.error("删除持仓缓存异常", e);
                } finally {
                    positionMapLock.unlock();
                }

                orderMapLock.lock();
                try {
                    // 删除定单缓存
                    orderMap = orderMap.entrySet().stream().filter(map -> retainGatewayIdSet.contains(map.getValue().getGatewayId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                } catch (Exception e) {
                    logger.error("删除定单缓存异常", e);
                } finally {
                    orderMapLock.unlock();
                }

                tradeMapLock.lock();
                try {
                    // 删除成交缓存
                    tradeMap = tradeMap.entrySet().stream().filter(map -> retainGatewayIdSet.contains(map.getValue().getGatewayId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                } catch (Exception e) {
                    logger.error("删除成交缓存异常", e);
                } finally {
                    tradeMapLock.unlock();
                }

                tickMapLock.lock();
                try {
                    // 删除Tick缓存
                    tickMap = tickMap.entrySet().stream().filter(map -> retainGatewayIdSet.contains(map.getValue().getGatewayId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                } catch (Exception e) {
                    logger.error("删除Tick缓存异常", e);
                } finally {
                    tickMapLock.unlock();
                }

            } catch (Exception e) {
                logger.error("定时清理数据异常", e);
            }
        }, 5, 5, TimeUnit.SECONDS);

        // 从子节点刷新缓存
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                List<GatewayPo> gatewayList = gatewayService.getGatewayList();

                Set<Integer> nodeIdSet = new HashSet<>();
                for (GatewayPo gateway : gatewayList) {
                    if (ConnectStatusEnum.CS_Connected_VALUE == gateway.getStatus() || ConnectStatusEnum.CS_Connecting_VALUE == gateway.getStatus()) {
                        Integer targetNodeId = gateway.getTargetNodeId();
                        if (targetNodeId != null && targetNodeId != 0) {
                            nodeIdSet.add(targetNodeId);
                        }
                    }
                }

                for (Integer nodeId : nodeIdSet) {
                    if (webSocketServerHandler.containsNodeId(nodeId)) {

                        String sessionId = webSocketServerHandler.getSessionIdByNodeId(nodeId);

                        RpcGetAccountListRsp rpcGetAccountListRsp = rpcServerApiService.getAccountList(sessionId, null, null);
                        if (rpcGetAccountListRsp != null) {
                            logger.info("RpcGetAccountListRsp返回{}条数据", rpcGetAccountListRsp.getAccountList().size());
                            clearAndCacheAccountList(rpcGetAccountListRsp.getAccountList(), nodeId);
                        }

                        Thread.sleep(200);

                        RpcGetPositionListRsp rpcGetPositionListRsp = rpcServerApiService.getPositionList(sessionId, null, null);
                        if (rpcGetPositionListRsp != null) {
                            logger.info("RpcGetPositionListRsp返回{}条数据", rpcGetPositionListRsp.getPositionList().size());
                            clearAndCachePositionList(rpcGetPositionListRsp.getPositionList(), nodeId);
                        }

                        Thread.sleep(200);

                        RpcGetTradeListRsp rpcGetTradeListRsp = rpcServerApiService.getTradeList(sessionId, null, null);
                        if (rpcGetTradeListRsp != null) {
                            logger.info("RpcGetTradeListRsp返回{}条数据", rpcGetTradeListRsp.getTradeList().size());
                            clearAndCacheTradeList(rpcGetTradeListRsp.getTradeList(), nodeId);
                        }

                        Thread.sleep(200);

                        RpcGetOrderListRsp rpcGetOrderListRsp = rpcServerApiService.getOrderList(sessionId, null, null);
                        if (rpcGetOrderListRsp != null) {
                            logger.info("RpcGetOrderListRsp返回{}条数据", rpcGetOrderListRsp.getOrderList().size());
                            clearAndCacheOrderList(rpcGetOrderListRsp.getOrderList(), nodeId);
                        }

                        Thread.sleep(200);

                        RpcGetTickListRsp rpcGetTickListRsp = rpcServerApiService.getTickList(sessionId, null, null);
                        if (rpcGetTickListRsp != null) {
                            logger.info("RpcGetTickListRsp{}条数据", rpcGetTickListRsp.getTickList().size());
                            clearAndCacheTickList(rpcGetTickListRsp.getTickList(), nodeId);
                        }

                        Thread.sleep(200);

                        if (fetchContractSwitch) {
                            RpcGetContractListRsp rpcGetContractListRsp = rpcServerApiService.getContractList(sessionId, null, null);
                            if (rpcGetContractListRsp != null) {
                                logger.info("RpcGetContractListRsp返回{}条数据", rpcGetContractListRsp.getContractList().size());
                                cacheContractList(rpcGetContractListRsp.getContractList());
                            }
                            fetchContractSwitch = false;
                        } else {
                            fetchContractSwitch = true;
                        }
                        Thread.sleep(200);

                    }
                }

            } catch (Exception e) {
                logger.error("定时从子节点刷新数据异常", e);
            }
        }, 30, 60, TimeUnit.SECONDS);
    }

    @Override
    public List<OrderField> getOrderList(String operatorId) {
        List<OrderField> orderList = new ArrayList<>();
        orderMapLock.lock();
        try {
            orderList = orderMap.values().stream().filter(order -> operatorService.checkReadAccountPermission(operatorId, order.getAccountId())).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取委托列表异常", e);
        } finally {
            orderMapLock.unlock();
        }
        return orderList;
    }

    @Override
    public List<OrderField> getWorkingOrderList(String operatorId) {
        List<OrderField> orderList = new ArrayList<>();
        orderMapLock.lock();
        try {
            orderList = workingOrderMap.values().stream().filter(order -> operatorService.checkReadAccountPermission(operatorId, order.getAccountId())).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取活动委托列表异常", e);
        } finally {
            orderMapLock.unlock();
        }
        return orderList;
    }

    @Override
    public OrderField queryOrderByOrderId(String operatorId, String orderId) {

        OrderField order = orderMap.get(orderId);

        if (order != null) {
            if (operatorService.checkReadAccountPermission(operatorId, order.getAccountId())) {
                return order;
            }
        }

        return null;
    }

    @Override
    public OrderField queryOrderByOriginOrderId(String operatorId, String originOrderId) {
        // TODO 需要补充
        return null;
    }

    @Override
    public List<OrderField> queryOrderListByAccountId(String operatorId, String accountId) {

        List<OrderField> orderList = new ArrayList<>();
        orderMapLock.lock();
        try {
            if (operatorService.checkReadAccountPermission(operatorId, accountId)) {
                orderList = orderMap.values().stream().filter(order -> order.getAccountId().equals(accountId)).collect(Collectors.toList());
            }

        } catch (Exception e) {
            logger.error("根据账户ID获取委托列表异常", e);
        } finally {
            orderMapLock.unlock();
        }
        return orderList;
    }

    @Override
    public List<OrderField> queryOrderListByUniformSymbol(String operatorId, String uniformSymbol) {
        List<OrderField> orderList = new ArrayList<>();
        orderMapLock.lock();
        try {
            orderList = orderMap.values().stream()
                    .filter(order -> order.getContract().getUniformSymbol().equals(uniformSymbol) && operatorService.checkReadAccountPermission(operatorId, order.getAccountId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根据统一标识获取委托列表异常", e);
        } finally {
            orderMapLock.unlock();
        }
        return orderList;
    }

    @Override
    public List<TradeField> getTradeList(String operatorId) {
        List<TradeField> tradeList = new ArrayList<>();
        tradeMapLock.lock();
        try {
            tradeList = tradeMap.values().stream().filter(trade -> operatorService.checkReadAccountPermission(operatorId, trade.getAccountId())).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取成交列表异常", e);
        } finally {
            tradeMapLock.unlock();
        }
        return tradeList;
    }

    @Override
    public TradeField queryTradeByTradeId(String operatorId, String tradeId) {
        TradeField trade = tradeMap.get(tradeId);

        if (trade != null) {
            if (operatorService.checkReadAccountPermission(operatorId, trade.getAccountId())) {
                return trade;
            }
        }

        return null;
    }

    @Override
    public List<TradeField> queryTradeListByUniformSymbol(String operatorId, String uniformSymbol) {
        List<TradeField> tradeList = new ArrayList<>();
        tradeMapLock.lock();
        try {
            tradeList = tradeMap.values().stream()
                    .filter(trade -> trade.getContract().getUniformSymbol().equals(uniformSymbol) && operatorService.checkReadAccountPermission(operatorId, trade.getAccountId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根据统一标识获取成交列表异常", e);
        } finally {
            tradeMapLock.unlock();
        }
        return tradeList;
    }

    @Override
    public List<TradeField> queryTradeListByAccountId(String operatorId, String accountId) {
        List<TradeField> tradeList = new ArrayList<>();
        tradeMapLock.lock();
        try {
            tradeList = tradeMap.values().stream().filter(trade -> trade.getAccountId().equals(accountId)).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根据账户ID获取成交列表异常", e);
        } finally {
            tradeMapLock.unlock();
        }
        return tradeList;
    }

    @Override
    public List<TradeField> queryTradeListByOrderId(String operatorId, String orderId) {
        List<TradeField> tradeList = new ArrayList<>();
        tradeMapLock.lock();
        try {
            tradeList = tradeMap.values().stream().filter(trade -> trade.getOrderId().equals(orderId) && operatorService.checkReadAccountPermission(operatorId, trade.getAccountId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根据委托ID获取成交列表异常", e);
        } finally {
            tradeMapLock.unlock();
        }
        return tradeList;
    }

    @Override
    public List<TradeField> queryTradeListByOriginOrderId(String operatorId, String originOrderId) {
        List<TradeField> tradeList = new ArrayList<>();
        tradeMapLock.lock();
        try {
            tradeList = tradeMap.values().stream().filter(trade -> trade.getOriginOrderId().equals(originOrderId) && operatorService.checkReadAccountPermission(operatorId, trade.getAccountId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根据原始委托ID获取成交列表异常", e);
        } finally {
            tradeMapLock.unlock();
        }
        return tradeList;
    }

    @Override
    public List<PositionField> getPositionList(String operatorId) {
        List<PositionField> positionList = new ArrayList<>();
        positionMapLock.lock();
        try {
            positionList = positionMap.values().stream().filter(position -> operatorService.checkReadAccountPermission(operatorId, position.getAccountId())).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取持仓列表异常", e);
        } finally {
            positionMapLock.unlock();
        }
        return positionList;
    }

    @Override
    public PositionField queryPositionByPositionId(String operatorId, String positionId) {
        PositionField position = positionMap.get(positionId);

        if (position != null) {
            if (operatorService.checkReadAccountPermission(operatorId, position.getAccountId())) {
                return position;
            }
        }

        return null;
    }

    @Override
    public List<PositionField> queryPositionListByAccountId(String operatorId, String accountId) {
        List<PositionField> positionList = new ArrayList<>();
        positionMapLock.lock();
        try {
            if (operatorService.checkReadAccountPermission(operatorId, accountId)) {
                positionList = positionMap.values().stream().filter(positionField -> positionField.getAccountId().equals(accountId)).collect(Collectors.toList());
            }

        } catch (Exception e) {
            logger.error("根据账户ID获取持仓列表异常", e);
        } finally {
            positionMapLock.unlock();
        }
        return positionList;
    }

    @Override
    public List<PositionField> queryPositionListByUniformSymbol(String operatorId, String uniformSymbol) {
        List<PositionField> positionList = new ArrayList<>();
        positionMapLock.lock();
        try {
            positionList = positionMap.values().stream()
                    .filter(position -> position.getContract().getUniformSymbol().equals(uniformSymbol) && operatorService.checkReadAccountPermission(operatorId, position.getAccountId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根据统一标识获取持仓列表异常", e);
        } finally {
            positionMapLock.unlock();
        }
        return positionList;
    }

    @Override
    public List<AccountField> getAccountList(String operatorId) {
        List<AccountField> accountList = new ArrayList<>();
        accountMapLock.lock();
        try {
            accountList = accountMap.values().stream().filter(account -> operatorService.checkReadAccountPermission(operatorId, account.getAccountId())).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取账户列表异常", e);
        } finally {
            accountMapLock.unlock();
        }
        return accountList;
    }

    @Override
    public AccountField queryAccountByAccountId(String operatorId, String accountId) {
        if (operatorService.checkReadAccountPermission(operatorId, accountId)) {
            return accountMap.get(accountId);
        } else {
            return null;
        }
    }

    @Override
    public List<AccountField> queryAccountListByAccountCode(String operatorId, String accountCode) {
        List<AccountField> accountList = new ArrayList<>();
        accountMapLock.lock();
        try {
            accountList = accountMap.values().stream().filter(account -> account.getCode().equals(accountCode) && operatorService.checkReadAccountPermission(operatorId, account.getAccountId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根据账户代码获取账户列表异常", e);
        } finally {
            accountMapLock.unlock();
        }
        return accountList;
    }

    @Override
    public List<ContractField> getContractList(String operatorId) {
        List<ContractField> contractList = new ArrayList<>();
        contractMapLock.lock();
        try {
            contractList = new ArrayList<ContractField>(contractMap.values());
        } catch (Exception e) {
            logger.error("获取合约列表异常", e);
        } finally {
            contractMapLock.unlock();
        }
        return contractList;
    }

    @Override
    public List<TickField> getTickList(String operatorId) {
        List<TickField> tickList = new ArrayList<>();
        tickMapLock.lock();
        try {
            tickList = tickMap.values().stream().filter(tick -> operatorService.checkSubscribePermission(operatorId, tick.getUniformSymbol())).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取Tick列表异常", e);
        } finally {
            tickMapLock.unlock();
        }
        return tickList;
    }


    @Override
    public ContractField queryContractByUniformSymbol(String operatorId, String uniformSymbol) {
        return contractMap.get(uniformSymbol);
    }


    @Override
    public void cacheOrder(OrderField order) {
        orderMapLock.lock();
        try {
            String orderId = order.getOrderId();
            if (!orderMap.containsKey(orderId) || order.getContingentCondition() != ContingentConditionEnum.CC_Immediately
                    || !CommonConstant.ORDER_STATUS_FINISHED_SET.contains(orderMap.get(orderId).getOrderStatus())) {
                orderMap.put(order.getOrderId(), order);
            }
        } catch (Exception e) {
            logger.error("存储定单异常", e);
        } finally {
            orderMapLock.unlock();
        }
    }

    @Override
    public void cacheTrade(TradeField trade) {
        tradeMapLock.lock();
        try {
            tradeMap.put(trade.getTradeId(), trade);
        } catch (Exception e) {
            logger.error("存储成交异常", e);
        } finally {
            tradeMapLock.unlock();
        }
    }

    @Override
    public void cacheContract(ContractField contract) {
        contractMapLock.lock();
        try {
            contractMap.put(contract.getUniformSymbol(), contract);
        } catch (Exception e) {
            logger.error("存储合约异常", e);
        } finally {
            contractMapLock.unlock();
        }
    }

    @Override
    public void cachePosition(PositionField position) {
        positionMapLock.lock();
        try {
            positionMap.put(position.getPositionId(), position);
        } catch (Exception e) {
            logger.error("存储持仓异常", e);
        } finally {
            positionMapLock.unlock();
        }
    }

    @Override
    public void cacheAccount(AccountField account) {
        accountMapLock.lock();
        try {
            accountMap.put(account.getAccountId(), account);
        } catch (Exception e) {
            logger.error("存储账户异常", e);
        } finally {
            accountMapLock.unlock();
        }
    }

    @Override
    public void cacheTick(TickField tick) {
        tickMapLock.lock();
        try {
            tickMap.put(tick.getUniformSymbol() + "@" + tick.getGatewayId(), tick);
        } catch (Exception e) {
            logger.error("存储Tick异常", e);
        } finally {
            tickMapLock.unlock();
        }

    }

    @Override
    public void cacheOrderList(List<OrderField> orderList) {
        orderMapLock.lock();
        try {
            for (OrderField order : orderList) {
                orderMap.put(order.getOrderId(), order);
            }
        } catch (Exception e) {
            logger.error("存储定单列表异常", e);
        } finally {
            orderMapLock.unlock();
        }
    }

    @Override
    public void cacheTradeList(List<TradeField> tradeList) {
        tradeMapLock.lock();
        try {
            for (TradeField trade : tradeList) {
                tradeMap.put(trade.getTradeId(), trade);
            }
        } catch (Exception e) {
            logger.error("存储成交列表异常", e);
        } finally {
            tradeMapLock.unlock();
        }

    }

    @Override
    public void cacheContractList(List<ContractField> contractList) {
        contractMapLock.lock();
        try {
            for (ContractField contract : contractList) {
                contractMap.put(contract.getUniformSymbol(), contract);
            }
        } catch (Exception e) {
            logger.error("存储合约列表异常", e);
        } finally {
            contractMapLock.unlock();
        }

    }

    @Override
    public void cachePositionList(List<PositionField> positionList) {
        positionMapLock.lock();
        try {
            for (PositionField position : positionList) {
                positionMap.put(position.getPositionId(), position);
            }
        } catch (Exception e) {
            logger.error("存储持仓列表异常", e);
        } finally {
            positionMapLock.unlock();
        }

    }

    @Override
    public void cacheAccountList(List<AccountField> accountList) {
        accountMapLock.lock();
        try {
            for (AccountField account : accountList) {
                accountMap.put(account.getAccountId(), account);
            }
        } catch (Exception e) {
            logger.error("存储账户列表异常", e);
        } finally {
            accountMapLock.unlock();
        }

    }

    @Override
    public void cacheTickList(List<TickField> tickList) {
        tickMapLock.lock();
        try {
            for (TickField tick : tickList) {
                tickMap.put(tick.getUniformSymbol() + "@" + tick.getGatewayId(), tick);
            }

        } catch (Exception e) {
            logger.error("存储Tick列表异常", e);
        } finally {
            tickMapLock.unlock();
        }
    }

    @Override
    public void clearAndCacheOrderList(List<OrderField> orderList, Integer sourceNodeId) {
        orderMapLock.lock();
        try {

            Set<String> removeGatewayIdSet = new HashSet<>();
            List<GatewayPo> gatewayList = gatewayService.getGatewayList();

            for (GatewayPo gateway : gatewayList) {
                if (gateway != null && gateway.getTargetNodeId() != null && gateway.getTargetNodeId().equals(sourceNodeId)) {
                    removeGatewayIdSet.add(gateway.getGatewayId());
                }
            }
            orderMap = orderMap.entrySet().stream().filter(map -> !removeGatewayIdSet.contains(map.getValue().getGatewayId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            for (OrderField order : orderList) {
                orderMap.put(order.getOrderId(), order);
            }
        } catch (Exception e) {
            logger.error("存储定单列表异常", e);
        } finally {
            orderMapLock.unlock();
        }
    }

    @Override
    public void clearAndCacheTradeList(List<TradeField> tradeList, Integer sourceNodeId) {
        tradeMapLock.lock();
        try {
            Set<String> removeGatewayIdSet = new HashSet<>();
            List<GatewayPo> gatewayList = gatewayService.getGatewayList();
            for (GatewayPo gateway : gatewayList) {
                if (gateway != null && gateway.getTargetNodeId() != null && gateway.getTargetNodeId().equals(sourceNodeId)) {
                    removeGatewayIdSet.add(gateway.getGatewayId());
                }
            }
            tradeMap = tradeMap.entrySet().stream().filter(map -> !removeGatewayIdSet.contains(map.getValue().getGatewayId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            for (TradeField trade : tradeList) {
                tradeMap.put(trade.getTradeId(), trade);
            }
        } catch (Exception e) {
            logger.error("存储成交列表异常", e);
        } finally {
            tradeMapLock.unlock();
        }
    }

    @Override
    public void clearAndCachePositionList(List<PositionField> positionList, Integer sourceNodeId) {
        positionMapLock.lock();
        try {
            Set<String> removeGatewayIdSet = new HashSet<>();
            List<GatewayPo> gatewayList = gatewayService.getGatewayList();

            for (GatewayPo gateway : gatewayList) {
                if (gateway != null && gateway.getTargetNodeId() != null && gateway.getTargetNodeId().equals(sourceNodeId)) {
                    removeGatewayIdSet.add(gateway.getGatewayId());
                }
            }
            positionMap = positionMap.entrySet().stream().filter(map -> !removeGatewayIdSet.contains(map.getValue().getGatewayId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            for (PositionField position : positionList) {
                positionMap.put(position.getPositionId(), position);
            }
        } catch (Exception e) {
            logger.error("存储持仓列表异常", e);
        } finally {
            positionMapLock.unlock();
        }
    }

    @Override
    public void clearAndCacheAccountList(List<AccountField> accountList, Integer sourceNodeId) {
        accountMapLock.lock();
        try {
            Set<String> removeGatewayIdSet = new HashSet<>();

            List<GatewayPo> gatewayList = gatewayService.getGatewayList();

            for (GatewayPo gateway : gatewayList) {
                if (gateway != null && gateway.getTargetNodeId() != null && gateway.getTargetNodeId().equals(sourceNodeId)) {
                    removeGatewayIdSet.add(gateway.getGatewayId());
                }
            }
            accountMap = accountMap.entrySet().stream().filter(map -> !removeGatewayIdSet.contains(map.getValue().getGatewayId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            for (AccountField account : accountList) {
                accountMap.put(account.getAccountId(), account);
            }
        } catch (Exception e) {
            logger.error("存储账户列表异常", e);
        } finally {
            accountMapLock.unlock();
        }
    }

    @Override
    public void clearAndCacheTickList(List<TickField> tickList, Integer sourceNodeId) {
        tickMapLock.lock();
        try {
            Set<String> removeGatewayIdSet = new HashSet<>();

            List<GatewayPo> gatewayList = gatewayService.getGatewayList();
            for (GatewayPo gateway : gatewayList) {
                if (gateway != null && gateway.getTargetNodeId() != null && gateway.getTargetNodeId().equals(sourceNodeId)) {
                    removeGatewayIdSet.add(gateway.getGatewayId());
                }
            }
            tickMap = tickMap.entrySet().stream().filter(map -> !removeGatewayIdSet.contains(map.getValue().getGatewayId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            for (TickField tick : tickList) {
                tickMap.put(tick.getUniformSymbol() + "@" + tick.getGatewayId(), tick);
            }

        } catch (Exception e) {
            logger.error("存储Tick列表异常", e);
        } finally {
            tickMapLock.unlock();
        }

    }

    @Override
    public void clearAllCachesByGatewayId(String gatewayId) {
        accountMapLock.lock();
        try {
            // 删除账户缓存
            accountMap = accountMap.entrySet().stream().filter(map -> !map.getValue().getGatewayId().equals(gatewayId)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            logger.error("删除账户缓存异常", e);
        } finally {
            accountMapLock.unlock();
        }

        positionMapLock.lock();
        try {
            // 删除持仓缓存
            positionMap = positionMap.entrySet().stream().filter(map -> !map.getValue().getContract().getGatewayId().equals(gatewayId))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            logger.error("删除持仓缓存异常", e);
        } finally {
            positionMapLock.unlock();
        }

        orderMapLock.lock();
        try {
            // 删除定单缓存
            orderMap = orderMap.entrySet().stream().filter(map -> !map.getValue().getContract().getGatewayId().equals(gatewayId)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            logger.error("删除定单缓存异常", e);
        } finally {
            orderMapLock.unlock();
        }

        tradeMapLock.lock();
        try {
            // 删除成交缓存
            tradeMap = tradeMap.entrySet().stream().filter(map -> !map.getValue().getContract().getGatewayId().equals(gatewayId)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            logger.error("删除成交缓存异常", e);
        } finally {
            tradeMapLock.unlock();
        }

        tickMapLock.lock();
        try {
            // 删除Tick缓存
            tickMap = tickMap.entrySet().stream().filter(map -> !map.getValue().getGatewayId().equals(gatewayId)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            logger.error("删除Tick缓存异常", e);
        } finally {
            tickMapLock.unlock();
        }
    }

}
