package xyz.redtorch.node.master.service;

import xyz.redtorch.pb.CoreField.*;

import java.util.List;

public interface MasterTradeRtnRelayService {
    void onOrder(OrderField order);

    void onTrade(TradeField trade);

    void onTick(TickField tick);

    void onPosition(PositionField position);

    void onAccount(AccountField account);

    void onNotice(NoticeField notice);

    void onOrderList(List<OrderField> orderList);

    void onTradeList(List<TradeField> tradeList);

    void onTickList(List<TickField> tickList);

    void onPositionList(List<PositionField> positionList);

    void onAccountList(List<AccountField> accountList);
}
