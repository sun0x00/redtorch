package xyz.redtorch.node.master.service.impl;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.node.master.rpc.service.RpcServerOverWebSocketProcessService;
import xyz.redtorch.node.master.service.MarketDataRecordingService;
import xyz.redtorch.node.master.service.MasterTradeExecuteService;
import xyz.redtorch.node.master.service.MasterTradeRtnRelayService;
import xyz.redtorch.node.master.service.OperatorService;
import xyz.redtorch.node.master.web.socket.WebSocketServerHandler;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.NoticeField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;
import xyz.redtorch.pb.CoreRpc.RpcAccountListRtn;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcNoticeRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderListRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTickRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeRtn;

@Service
public class MasterTradeRtnRelayServiceImpl implements MasterTradeRtnRelayService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MasterTradeRtnRelayServiceImpl.class);
    private final Map<String, Long> tickTimestampFilterMap = new ConcurrentHashMap<>();
    private final Map<String, Long> tickLastLocalTimestampFilterMap = new ConcurrentHashMap<>();
    private final Map<String, String> tickGatewayIdFilterMap = new ConcurrentHashMap<>();
    // 对时效性要求不高的数据使用Queue减少发送次数,减轻中心节点压力
    private final Queue<PositionField> positionQueue = new ConcurrentLinkedQueue<>();
    private final Queue<AccountField> accountQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService cachedThreadPoolService = Executors.newCachedThreadPool();
    private final ExecutorService tradeRtnQueueSingleExecutorService = Executors.newSingleThreadExecutor();
    private final ExecutorService marketRtnQueueSingleExecutorService = Executors.newSingleThreadExecutor();
    @Autowired
    private WebSocketServerHandler webSocketServerHandler;
    @Autowired
    private RpcServerOverWebSocketProcessService rpcOverWebSocketProcessService;
    @Autowired
    private MasterTradeExecuteService masterTradeExecuteService;
    @Autowired
    private OperatorService operatorService;
    @Autowired
    private MarketDataRecordingService marketDataRecordingService;

    @Override
    public void afterPropertiesSet() throws Exception {
        cachedThreadPoolService.execute(new Runnable() {
            long lastTimestamp = System.currentTimeMillis();
            List<PositionField> positionList = new ArrayList<>();

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (positionList.size() > 50 || (System.currentTimeMillis() - lastTimestamp > 200 && !positionList.isEmpty())) {

                            Map<String, Set<String>> operatorIdSessionIdSetMap = webSocketServerHandler.getOperatorIdSessionIdSetMap();
                            ReentrantLock operatorIdSessionIdSetMapLock = webSocketServerHandler.getOperatorIdSessionIdSetMapLock();
                            operatorIdSessionIdSetMapLock.lock();
                            try {
                                for (Entry<String, Set<String>> entry : operatorIdSessionIdSetMap.entrySet()) {

                                    String operatorId = entry.getKey();
                                    Set<String> sessionIdSet = entry.getValue();

                                    List<PositionField> filteredPositionList = new ArrayList<>();

                                    for (PositionField position : positionList) {
                                        String accountId = position.getAccountId();

                                        if (!operatorService.checkReadAccountPermission(operatorId, accountId)) {
                                            continue;
                                        }
                                        filteredPositionList.add(position);
                                    }

                                    if (filteredPositionList.isEmpty()) {
                                        continue;
                                    }

                                    for (String sessionId : sessionIdSet) {

                                        RpcPositionListRtn.Builder rpcPositionRtnBuilder = RpcPositionListRtn.newBuilder();
                                        rpcPositionRtnBuilder.addAllPosition(filteredPositionList);

                                        cachedThreadPoolService.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.POSITION_LIST_RTN, "", rpcPositionRtnBuilder.build().toByteString());
                                            }
                                        });

                                    }
                                }
                            } finally {
                                operatorIdSessionIdSetMapLock.unlock();
                            }

                            lastTimestamp = System.currentTimeMillis();
                            positionList = new ArrayList<>();
                        }

                        if (positionQueue.peek() != null) {
                            positionList.add(positionQueue.poll());
                        } else {
                            Thread.sleep(5);
                        }

                    } catch (Exception e) {
                        logger.error("定时发送持仓列表线程异常", e);
                    }
                }
            }
        });

        cachedThreadPoolService.execute(new Runnable() {
            long lastTimestamp = System.currentTimeMillis();
            List<AccountField> accountList = new ArrayList<>();

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (accountList.size() > 50 || (System.currentTimeMillis() - lastTimestamp > 200 && !accountList.isEmpty())) {

                            Map<String, Set<String>> operatorIdSessionIdSetMap = webSocketServerHandler.getOperatorIdSessionIdSetMap();
                            ReentrantLock operatorIdSessionIdSetMapLock = webSocketServerHandler.getOperatorIdSessionIdSetMapLock();
                            operatorIdSessionIdSetMapLock.lock();
                            try {
                                for (Entry<String, Set<String>> entry : operatorIdSessionIdSetMap.entrySet()) {

                                    String operatorId = entry.getKey();
                                    Set<String> sessionIdSet = entry.getValue();

                                    List<AccountField> filteredAccountList = new ArrayList<>();

                                    for (AccountField account : accountList) {
                                        String accountId = account.getAccountId();

                                        if (!operatorService.checkReadAccountPermission(operatorId, accountId)) {
                                            continue;
                                        }
                                        filteredAccountList.add(account);
                                    }

                                    if (filteredAccountList.isEmpty()) {
                                        continue;
                                    }

                                    for (String sessionId : sessionIdSet) {

                                        RpcAccountListRtn.Builder rpcAccountRtnBuilder = RpcAccountListRtn.newBuilder();
                                        rpcAccountRtnBuilder.addAllAccount(filteredAccountList);

                                        cachedThreadPoolService.execute(() -> rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.ACCOUNT_LIST_RTN, "", rpcAccountRtnBuilder.build().toByteString()));

                                    }
                                }
                            } finally {
                                operatorIdSessionIdSetMapLock.unlock();
                            }

                            lastTimestamp = System.currentTimeMillis();
                            accountList = new ArrayList<>();
                        }

                        if (accountQueue.peek() != null) {
                            accountList.add(accountQueue.poll());
                        } else {
                            Thread.sleep(5);
                        }

                    } catch (Exception e) {
                        logger.error("定时发送账户列表线程异常", e);
                    }
                }
            }
        });

    }

    @Override
    public void onOrder(OrderField order) {
        Map<String, Set<String>> operatorIdSessionIdSetMap = webSocketServerHandler.getOperatorIdSessionIdSetMap();
        ReentrantLock operatorIdSessionIdSetMapLock = webSocketServerHandler.getOperatorIdSessionIdSetMapLock();
        operatorIdSessionIdSetMapLock.lock();
        try {
            for (Entry<String, Set<String>> entry : operatorIdSessionIdSetMap.entrySet()) {

                String operatorId = entry.getKey();
                Set<String> sessionIdSet = entry.getValue();

                String accountId = order.getAccountId();

                if (!operatorService.checkReadAccountPermission(operatorId, accountId)) {
                    continue;
                }

                for (String sessionId : sessionIdSet) {

                    RpcOrderRtn.Builder rpcOrderRtnBuilder = RpcOrderRtn.newBuilder();
                    rpcOrderRtnBuilder.setOrder(order);

                    tradeRtnQueueSingleExecutorService.execute(() -> rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.ORDER_RTN, "", rpcOrderRtnBuilder.build().toByteString()));
                }
            }
        } finally {
            operatorIdSessionIdSetMapLock.unlock();
        }

    }

    @Override
    public void onTrade(TradeField trade) {
        Map<String, Set<String>> operatorIdSessionIdSetMap = webSocketServerHandler.getOperatorIdSessionIdSetMap();
        ReentrantLock operatorIdSessionIdSetMapLock = webSocketServerHandler.getOperatorIdSessionIdSetMapLock();
        operatorIdSessionIdSetMapLock.lock();
        try {
            for (Entry<String, Set<String>> entry : operatorIdSessionIdSetMap.entrySet()) {
                String operatorId = entry.getKey();
                Set<String> sessionIdSet = entry.getValue();

                String accountId = trade.getAccountId();

                if (!operatorService.checkReadAccountPermission(operatorId, accountId)) {
                    continue;
                }

                for (String sessionId : sessionIdSet) {


                    RpcTradeRtn.Builder rpcTradeRtnBuilder = RpcTradeRtn.newBuilder();
                    rpcTradeRtnBuilder.setTrade(trade);

                    tradeRtnQueueSingleExecutorService.execute(() -> rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.TRADE_RTN, "", rpcTradeRtnBuilder.build().toByteString()));
                }
            }

        } finally {
            operatorIdSessionIdSetMapLock.unlock();
        }

    }

    @Override
    public void onTick(TickField tick) {

        // --------------------------先根据 合约标识+网关ID 进行转发--------------------------
        Set<String> dataSourceIdSubscribedSessionIdSet = masterTradeExecuteService.getSubscribedSessionIdSet(tick.getUniformSymbol() + "@" + tick.getGatewayId());

        for (String sessionId : dataSourceIdSubscribedSessionIdSet) {

            RpcTickRtn.Builder rpcTickRtnBuilder = RpcTickRtn.newBuilder();
            rpcTickRtnBuilder.setTick(tick);

            marketRtnQueueSingleExecutorService.execute(() -> rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.TICK_RTN, "", rpcTickRtnBuilder.build().toByteString()));

        }

        // --------------------------过滤行情-----------------------------
        String uniformSymbol = tick.getUniformSymbol();

        long actionTimestamp = tick.getActionTimestamp();

        if (tick.getUniformSymbol().contains(ExchangeEnum.CZCE.getValueDescriptor().getName())) {
            if (tickGatewayIdFilterMap.containsKey(uniformSymbol)) {
                String gatewayId = tickGatewayIdFilterMap.get(uniformSymbol);
                if (!gatewayId.equals(tick.getGatewayId())) {
                    if (tickLastLocalTimestampFilterMap.containsKey(uniformSymbol)) {
                        if (System.currentTimeMillis() - tickLastLocalTimestampFilterMap.get(uniformSymbol) > 5 * 1000) {
                            logger.warn("超过5秒未能接收到网关发送的郑商所合约行情数据,切换网关数据,网关ID:{},合约统一标识:{}", gatewayId, uniformSymbol);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }

            }

            tickGatewayIdFilterMap.put(uniformSymbol, tick.getGatewayId());
            tickLastLocalTimestampFilterMap.put(uniformSymbol, System.currentTimeMillis());
        } else {
            if (tickTimestampFilterMap.containsKey(uniformSymbol)) {
                if (actionTimestamp <= tickTimestampFilterMap.get(uniformSymbol)) {
                    return;
                }
            }
        }

        tickTimestampFilterMap.put(uniformSymbol, actionTimestamp);

        Set<String> mdrSubscribedUniformSymbol = marketDataRecordingService.getSubscribedUniformSymbolSet();
        if (mdrSubscribedUniformSymbol.contains(tick.getUniformSymbol())) {
            marketDataRecordingService.processTick(tick);
        }


        // --------------------------先根据uniformSymbol进行转发--------------------------
        Set<String> uniformSymbolSubscribedSessionIdSet = masterTradeExecuteService.getSubscribedSessionIdSet(tick.getUniformSymbol());

        for (String sessionId : uniformSymbolSubscribedSessionIdSet) {
            // 过滤已经转发过的dataSourceId
            if (dataSourceIdSubscribedSessionIdSet.contains(sessionId)) {
                continue;
            }

            RpcTickRtn.Builder rpcTickRtnBuilder = RpcTickRtn.newBuilder();
            rpcTickRtnBuilder.setTick(tick);

            marketRtnQueueSingleExecutorService.execute(() -> rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.TICK_RTN, "", rpcTickRtnBuilder.build().toByteString()));

        }
    }

    @Override
    public void onAccount(AccountField account) {
        accountQueue.add(account);
    }

    @Override
    public void onPosition(PositionField position) {
        positionQueue.add(position);
    }

    @Override
    public void onNotice(NoticeField notice) {
        Map<String, Set<String>> operatorIdSessionIdSetMap = webSocketServerHandler.getOperatorIdSessionIdSetMap();
        ReentrantLock operatorIdSessionIdSetMapLock = webSocketServerHandler.getOperatorIdSessionIdSetMapLock();
        operatorIdSessionIdSetMapLock.lock();
        try {
            for (Entry<String, Set<String>> entry : operatorIdSessionIdSetMap.entrySet()) {
                Set<String> sessionIdSet = entry.getValue();

                for (String sessionId : sessionIdSet) {


                    RpcNoticeRtn.Builder rpcNoticeRtnBuilder = RpcNoticeRtn.newBuilder();
                    rpcNoticeRtnBuilder.setNotice(notice);

                    cachedThreadPoolService.execute(() -> rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.NOTICE_RTN, "", rpcNoticeRtnBuilder.build().toByteString()));
                }

            }
        } finally {
            operatorIdSessionIdSetMapLock.unlock();
        }

    }

    @Override
    public void onOrderList(List<OrderField> orderList) {
        Map<String, Set<String>> operatorIdSessionIdSetMap = webSocketServerHandler.getOperatorIdSessionIdSetMap();
        ReentrantLock operatorIdSessionIdSetMapLock = webSocketServerHandler.getOperatorIdSessionIdSetMapLock();
        operatorIdSessionIdSetMapLock.lock();
        try {
            for (Entry<String, Set<String>> entry : operatorIdSessionIdSetMap.entrySet()) {

                String operatorId = entry.getKey();
                Set<String> sessionIdSet = entry.getValue();

                List<OrderField> filteredOrderList = new ArrayList<>();

                for (OrderField order : orderList) {
                    String accountId = order.getAccountId();

                    if (!operatorService.checkReadAccountPermission(operatorId, accountId)) {
                        continue;
                    }
                    filteredOrderList.add(order);
                }

                if (filteredOrderList.isEmpty()) {
                    continue;
                }

                for (String sessionId : sessionIdSet) {


                    RpcOrderListRtn.Builder rpcOrderRtnBuilder = RpcOrderListRtn.newBuilder();
                    rpcOrderRtnBuilder.addAllOrder(filteredOrderList);

                    tradeRtnQueueSingleExecutorService.execute(() -> rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.ORDER_LIST_RTN, "", rpcOrderRtnBuilder.build().toByteString()));

                }
            }
        } finally {
            operatorIdSessionIdSetMapLock.unlock();
        }

    }

    @Override
    public void onTradeList(List<TradeField> tradeList) {
        Map<String, Set<String>> operatorIdSessionIdSetMap = webSocketServerHandler.getOperatorIdSessionIdSetMap();
        ReentrantLock operatorIdSessionIdSetMapLock = webSocketServerHandler.getOperatorIdSessionIdSetMapLock();
        operatorIdSessionIdSetMapLock.lock();
        try {
            for (Entry<String, Set<String>> entry : operatorIdSessionIdSetMap.entrySet()) {

                String operatorId = entry.getKey();
                Set<String> sessionIdSet = entry.getValue();

                List<TradeField> filteredTradeList = new ArrayList<>();

                for (TradeField trade : tradeList) {
                    String accountId = trade.getAccountId();

                    if (!operatorService.checkReadAccountPermission(operatorId, accountId)) {
                        continue;
                    }
                    filteredTradeList.add(trade);
                }

                if (filteredTradeList.isEmpty()) {
                    continue;
                }

                for (String sessionId : sessionIdSet) {


                    RpcTradeListRtn.Builder rpcTradeRtnBuilder = RpcTradeListRtn.newBuilder();
                    rpcTradeRtnBuilder.addAllTrade(filteredTradeList);

                    tradeRtnQueueSingleExecutorService.execute(() -> rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.TRADE_LIST_RTN, "", rpcTradeRtnBuilder.build().toByteString()));
                }
            }
        } finally {
            operatorIdSessionIdSetMapLock.unlock();
        }
    }

    @Override
    public void onTickList(List<TickField> tickList) {
        for (TickField tick : tickList) {
            this.onTick(tick);
        }
    }

    @Override
    public void onPositionList(List<PositionField> positionList) {
        positionQueue.addAll(positionList);
    }

    @Override
    public void onAccountList(List<AccountField> accountList) {
        accountQueue.addAll(accountList);
    }

}
