package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.NoticeField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;

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
