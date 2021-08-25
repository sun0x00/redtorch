package xyz.redtorch.node.master.rpc.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.service.FastEventService;
import xyz.redtorch.node.master.rpc.service.RpcServerOverWebSocketRtnHandlerService;
import xyz.redtorch.node.master.service.MasterTradeCachesService;
import xyz.redtorch.node.master.service.MasterTradeExecuteService;
import xyz.redtorch.node.master.service.MasterTradeRtnRelayService;
import xyz.redtorch.pb.CoreField.*;
import xyz.redtorch.pb.CoreRpc.*;

import java.util.List;

@Service
public class RpcServerOverWebSocketRtnHandlerServiceImpl implements RpcServerOverWebSocketRtnHandlerService {

    @Autowired
    private MasterTradeCachesService masterTradeCachesService;
    @Autowired
    private MasterTradeRtnRelayService masterTradeRtnRelayService;
    @Autowired
    private MasterTradeExecuteService masterTradeExecuteService;
    @Autowired
    private FastEventService fastEventService;

    @Override
    public void onOrderRtn(RpcOrderRtn rpcOrderRtn) {
        OrderField order = rpcOrderRtn.getOrder();

        // 尝试补充原始委托ID
        if (StringUtils.isBlank(order.getOriginOrderId())) {
            SubmitOrderReqField submitOrderReq = masterTradeExecuteService.getSubmitOrderReqByOrderId(order.getOrderId());
            if (submitOrderReq != null) {
                order = order.toBuilder().setOriginOrderId(submitOrderReq.getOriginOrderId()).build();
            }
        }

        fastEventService.emitOrder(order);
        masterTradeRtnRelayService.onOrder(order);
        masterTradeCachesService.cacheOrder(order);
    }

    @Override
    public void onTradeRtn(RpcTradeRtn rpcTradeRtn) {
        TradeField trade = rpcTradeRtn.getTrade();

        // 尝试补充原始委托ID
        if (StringUtils.isBlank(trade.getOriginOrderId())) {
            SubmitOrderReqField submitOrderReq = masterTradeExecuteService.getSubmitOrderReqByOrderId(trade.getOrderId());
            if (submitOrderReq != null) {
                trade = trade.toBuilder().setOriginOrderId(submitOrderReq.getOriginOrderId()).build();
            }
        }

        fastEventService.emitTrade(trade);
        masterTradeRtnRelayService.onTrade(trade);
        masterTradeCachesService.cacheTrade(trade);
    }

    @Override
    public void onContractRtn(RpcContractRtn rpcContractRtn) {
        ContractField contract = rpcContractRtn.getContract();
        masterTradeCachesService.cacheContract(contract);
    }

    @Override
    public void onPositionRtn(RpcPositionRtn rpcPositionRtn) {
        PositionField position = rpcPositionRtn.getPosition();
        masterTradeRtnRelayService.onPosition(position);
        masterTradeCachesService.cachePosition(position);
    }

    @Override
    public void onAccountRtn(RpcAccountRtn rpcAccountRtn) {
        AccountField account = rpcAccountRtn.getAccount();
        masterTradeRtnRelayService.onAccount(account);
        masterTradeCachesService.cacheAccount(account);
    }

    @Override
    public void onTickRtn(RpcTickRtn rpcTickRtn) {
        TickField tick = rpcTickRtn.getTick();
        fastEventService.emitTick(tick);
        masterTradeRtnRelayService.onTick(tick);
        masterTradeCachesService.cacheTick(tick);
    }

    @Override
    public void onNoticeRtn(RpcNoticeRtn rpcNoticeRtn) {
        masterTradeRtnRelayService.onNotice(rpcNoticeRtn.getNotice());
    }

    @Override
    public void onOrderListRtn(RpcOrderListRtn rpcOrderListRtn) {
        List<OrderField> orderList = rpcOrderListRtn.getOrderList();
        for (OrderField order : orderList) {
            fastEventService.emitOrder(order);
        }
        masterTradeRtnRelayService.onOrderList(orderList);
        masterTradeCachesService.cacheOrderList(orderList);
    }

    @Override
    public void onTradeListRtn(RpcTradeListRtn rpcTradeListRtn) {
        List<TradeField> tradeList = rpcTradeListRtn.getTradeList();
        for (TradeField trade : tradeList) {
            fastEventService.emitTrade(trade);
        }
        masterTradeRtnRelayService.onTradeList(tradeList);
        masterTradeCachesService.cacheTradeList(tradeList);
    }

    @Override
    public void onContractListRtn(RpcContractListRtn rpcContractListRtn) {
        List<ContractField> contractList = rpcContractListRtn.getContractList();
        masterTradeCachesService.cacheContractList(contractList);
    }

    @Override
    public void onPositionListRtn(RpcPositionListRtn rpcPositionListRtn) {
        List<PositionField> positionList = rpcPositionListRtn.getPositionList();
        masterTradeRtnRelayService.onPositionList(positionList);
        masterTradeCachesService.cachePositionList(positionList);
    }

    @Override
    public void onAccountListRtn(RpcAccountListRtn rpcAccountListRtn) {
        List<AccountField> accountList = rpcAccountListRtn.getAccountList();
        masterTradeRtnRelayService.onAccountList(accountList);
        masterTradeCachesService.cacheAccountList(accountList);
    }

    @Override
    public void onTickListRtn(RpcTickListRtn rpcTickListRtn) {
        List<TickField> tickList = rpcTickListRtn.getTickList();
        for (TickField tick : tickList) {
            fastEventService.emitTick(tick);
        }
        masterTradeRtnRelayService.onTickList(tickList);
        masterTradeCachesService.cacheTickList(tickList);
    }

}
