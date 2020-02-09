package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;

public interface MasterTradeCachesService {

	List<OrderField> getOrderList(String operatorId);

	List<OrderField> getWorkingOrderList(String operatorId);

	OrderField queryOrderByOrderId(String operatorId, String orderId);

	OrderField queryOrderByOriginOrderId(String operatorId, String originOrderId);

	List<OrderField> queryOrderListByAccountId(String operatorId, String accountId);

	List<OrderField> queryOrderListByUnifiedSymbol(String operatorId, String unifiedSymbol);

	List<TradeField> getTradeList(String operatorId);

	TradeField queryTradeByTradeId(String operatorId, String tradeId);

	List<TradeField> queryTradeListByUnifiedSymbol(String operatorId, String unifiedSymbol);

	List<TradeField> queryTradeListByAccountId(String operatorId, String accountId);

	List<TradeField> queryTradeListByOrderId(String operatorId, String orderId);

	List<TradeField> queryTradeListByOriginOrderId(String operatorId, String originOrderId);

	List<PositionField> getPositionList(String operatorId);

	PositionField queryPositionByPositionId(String operatorId, String positionId);

	List<PositionField> queryPositionListByAccountId(String operatorId, String accountId);

	List<PositionField> queryPositionListByUnifiedSymbol(String operatorId, String unifiedSymbol);

	List<AccountField> getAccountList(String operatorId);

	AccountField queryAccountByAccountId(String operatorId, String accountId);

	List<AccountField> queryAccountListByAccountCode(String operatorId, String accountCode);

	List<ContractField> getContractList(String operatorId);

	ContractField queryContractByContractId(String operatorId, String contractId);

	List<ContractField> queryContractListByUnifiedSymbol(String operatorId, String unifiedSymbol);

	List<ContractField> queryContractListByGatewayId(String operatorId, String gatewayId);

	List<ContractField> getMixContractList(String operatorId);

	ContractField queryContractByUnifiedSymbol(String operatorId, String unifiedSymbol);

	List<TickField> getTickList(String operatorId);

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

	void clearAndCacheOrderList(List<OrderField> orderList, int sourceNodeId);

	void clearAndCacheTradeList(List<TradeField> tradeList, int sourceNodeId);

	void clearAndCacheContractList(List<ContractField> contractList, int sourceNodeId);

	void clearAndCachePositionList(List<PositionField> positionList, int sourceNodeId);

	void clearAndCacheAccountList(List<AccountField> accountList, int sourceNodeId);

	void clearAndCacheTickList(List<TickField> tickList, int sourceNodeId);

	void clearAllCachesByGatewayId(String gatewayId);

}
