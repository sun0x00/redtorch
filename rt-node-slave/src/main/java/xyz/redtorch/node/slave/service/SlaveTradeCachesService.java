package xyz.redtorch.node.slave.service;

import java.util.List;

import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;

public interface SlaveTradeCachesService {
	void removeAllCachesByGatewayId(String gatewayId);

	OrderField queryOrderByOrderId(String orderId);

	List<OrderField> getOrderList();

	List<TradeField> getTradeList();

	List<PositionField> getPositionList();

	List<AccountField> getAccountList();

	List<ContractField> getContractList();

	List<TickField> getTickList();
}
