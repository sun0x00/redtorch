package xyz.redtorch.node.master.service;

import xyz.redtorch.pb.CoreField.*;

import java.util.List;

public interface MasterTradeCachesService {

	List<OrderField> getOrderList(String operatorId);

	List<OrderField> getWorkingOrderList(String operatorId);

	OrderField queryOrderByOrderId(String operatorId, String orderId);

	OrderField queryOrderByOriginOrderId(String operatorId, String originOrderId);

	List<OrderField> queryOrderListByAccountId(String operatorId, String accountId);

	List<OrderField> queryOrderListByUniformSymbol(String operatorId, String uniformSymbol);

	List<TradeField> getTradeList(String operatorId);

	TradeField queryTradeByTradeId(String operatorId, String tradeId);

	List<TradeField> queryTradeListByUniformSymbol(String operatorId, String uniformSymbol);

	List<TradeField> queryTradeListByAccountId(String operatorId, String accountId);

	List<TradeField> queryTradeListByOrderId(String operatorId, String orderId);

	List<TradeField> queryTradeListByOriginOrderId(String operatorId, String originOrderId);

	List<PositionField> getPositionList(String operatorId);

	PositionField queryPositionByPositionId(String operatorId, String positionId);

	List<PositionField> queryPositionListByAccountId(String operatorId, String accountId);

	List<PositionField> queryPositionListByUniformSymbol(String operatorId, String uniformSymbol);

	List<AccountField> getAccountList(String operatorId);

	AccountField queryAccountByAccountId(String operatorId, String accountId);

	List<AccountField> queryAccountListByAccountCode(String operatorId, String accountCode);

	List<ContractField> getContractList(String operatorId);

	ContractField queryContractByUniformSymbol(String operatorId, String uniformSymbol);

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

	void clearAndCacheOrderList(List<OrderField> orderList, Integer sourceNodeId);

	void clearAndCacheTradeList(List<TradeField> tradeList, Integer sourceNodeId);

	void clearAndCachePositionList(List<PositionField> positionList, Integer sourceNodeId);

	void clearAndCacheAccountList(List<AccountField> accountList, Integer sourceNodeId);

	void clearAndCacheTickList(List<TickField> tickList, Integer sourceNodeId);

	void clearAllCachesByGatewayId(String gatewayId);

}
