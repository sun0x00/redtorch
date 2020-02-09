package xyz.redtorch.desktop.service;

import java.util.List;

import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;

public interface DesktopTradeCachesService {
	List<OrderField> getOrderList();

	List<OrderField> getWorkingOrderList();

	OrderField queryOrderByOrderId(String orderId);

	OrderField queryOrderByOriginOrderId(String originOrderId);

	List<OrderField> queryOrderListByAccountId(String accountId);

	List<OrderField> queryOrderListByUnifiedSymbol(String unifiedSymbol);

	List<TradeField> getTradeList();

	TradeField queryTradeByTradeId(String tradeId);

	List<TradeField> queryTradeListByUnifiedSymbol(String unifiedSymbol);

	List<TradeField> queryTradeListByAccountId(String accountId);

	List<TradeField> queryTradeListByOrderId(String orderId);

	List<TradeField> queryTradeListByOriginOrderId(String originOrderId);

	List<PositionField> getPositionList();

	PositionField queryPositionByPositionId(String positionId);

	List<PositionField> queryPositionListByAccountId(String accountId);

	List<PositionField> queryPositionListByUnifiedSymbol(String unifiedSymbol);

	List<AccountField> getAccountList();

	AccountField queryAccountByAccountId(String accountId);

	List<AccountField> queryAccountListByAccountCode(String accountCode);

	List<ContractField> getContractList();

	ContractField queryContractByContractId(String contractId);

	List<ContractField> queryContractListByUnifiedSymbol(String unifiedSymbol);

	List<ContractField> queryContractListByGatewayId(String gatewayId);

	List<ContractField> getMixContractList();

	ContractField queryContractByUnifiedSymbol(String unifiedSymbol);

	List<TickField> getTickList();

	List<TickField> getMixTickList();

	TickField queryTickByUnifiedSymbol(String unifiedSymbol);
	// ------------------------------------------------------------

	void cacheOrder(OrderField order);

	void cacheTrade(TradeField trade);

	void cacheContract(ContractField contract);

	void cachePosition(PositionField position);

	void cacheAccount(AccountField account);

	void cacheTick(TickField tick);

	void cacheOrderList(List<OrderField> orderList);

	void cacheTradeList(List<TradeField> tradeList);

	void cacheContractList(List<ContractField> contractList);

	void cachePositionList(List<PositionField> positionList);

	void cacheAccountList(List<AccountField> accountList);

	void cacheTickList(List<TickField> tickList);

	// ------------------------------------------------------------

	void clearAndCacheOrderList(List<OrderField> orderList);

	void clearAndCacheTradeList(List<TradeField> tradeList);

	void clearAndCacheContractList(List<ContractField> contractList);

	void clearAndCachePositionList(List<PositionField> positionList);

	void clearAndCacheAccountList(List<AccountField> accountList);

	void clearAndCacheTickList(List<TickField> tickList);

	void reloadData();

}
