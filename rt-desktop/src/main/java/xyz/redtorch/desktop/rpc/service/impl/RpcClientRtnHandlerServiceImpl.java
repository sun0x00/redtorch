package xyz.redtorch.desktop.rpc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.service.FastEventService;
import xyz.redtorch.desktop.rpc.service.RpcClientRtnHandlerService;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.pb.CoreField.*;
import xyz.redtorch.pb.CoreRpc.*;

import java.util.List;

@Service
public class RpcClientRtnHandlerServiceImpl implements RpcClientRtnHandlerService {

    @Autowired
    private DesktopTradeCachesService desktopTradeCachesService;
    @Autowired
    private FastEventService fastEventService;

    @Override
    public void onOrderRtn(RpcOrderRtn rpcOrderRtn) {
        OrderField order = rpcOrderRtn.getOrder();
        fastEventService.emitOrder(order);
        desktopTradeCachesService.cacheOrder(order);
    }

    @Override
    public void onTradeRtn(RpcTradeRtn rpcTradeRtn) {
        TradeField trade = rpcTradeRtn.getTrade();
        fastEventService.emitTrade(trade);
        desktopTradeCachesService.cacheTrade(trade);
    }

    @Override
    public void onContractRtn(RpcContractRtn rpcContractRtn) {
        ContractField contract = rpcContractRtn.getContract();
        desktopTradeCachesService.cacheContract(contract);
    }

    @Override
    public void onPositionRtn(RpcPositionRtn rpcPositionRtn) {
        PositionField position = rpcPositionRtn.getPosition();
        desktopTradeCachesService.cachePosition(position);
    }

    @Override
    public void onAccountRtn(RpcAccountRtn rpcAccountRtn) {
        AccountField account = rpcAccountRtn.getAccount();
        desktopTradeCachesService.cacheAccount(account);
    }

    @Override
    public void onTickRtn(RpcTickRtn rpcTickRtn) {
        TickField tick = rpcTickRtn.getTick();
        fastEventService.emitTick(tick);
        desktopTradeCachesService.cacheTick(tick);

    }

    @Override
    public void onNoticeRtn(RpcNoticeRtn rpcNoticeRtn) {
        // CommonRtnField commonRtn = rpcNoticeRtn.getCommonRtn();
        // TODO 需要补充相关功能

    }

    @Override
    public void onOrderListRtn(RpcOrderListRtn rpcOrderListRtn) {
        List<OrderField> orderList = rpcOrderListRtn.getOrderList();
        for (OrderField order : orderList) {
            fastEventService.emitOrder(order);
        }
        desktopTradeCachesService.cacheOrderList(orderList);
    }

    @Override
    public void onTradeListRtn(RpcTradeListRtn rpcTradeListRtn) {
        List<TradeField> tradeList = rpcTradeListRtn.getTradeList();
        for (TradeField trade : tradeList) {
            fastEventService.emitTrade(trade);
        }
        desktopTradeCachesService.cacheTradeList(tradeList);
    }

    @Override
    public void onContractListRtn(RpcContractListRtn rpcContractListRtn) {
        List<ContractField> contractList = rpcContractListRtn.getContractList();
        desktopTradeCachesService.cacheContractList(contractList);

    }

    @Override
    public void onPositionListRtn(RpcPositionListRtn rpcPositionListRtn) {
        List<PositionField> positionList = rpcPositionListRtn.getPositionList();
        desktopTradeCachesService.cachePositionList(positionList);
    }

    @Override
    public void onAccountListRtn(RpcAccountListRtn rpcAccountListRtn) {
        List<AccountField> accountList = rpcAccountListRtn.getAccountList();
        desktopTradeCachesService.cacheAccountList(accountList);

    }

    @Override
    public void onTickListRtn(RpcTickListRtn rpcTickListRtn) {
        List<TickField> tickList = rpcTickListRtn.getTickList();
        for (TickField tick : tickList) {
            fastEventService.emitTick(tick);
        }
        desktopTradeCachesService.cacheTickList(tickList);
    }

}
