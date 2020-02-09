package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.node.master.po.OperatorPo;

public interface OperatorService {
	OperatorPo getOperatorByOperatorId(String operatorId);

	List<OperatorPo> getOperatorList();

	void upsertOperatorByOperatorId(OperatorPo operator);

	void deleteOperatorByOperatorId(String operatorId);

	OperatorPo createOperator();

	boolean checkSubscribePermission(String operatorId, String unifiedSymbol);

	boolean checkReadAccountPermission(String operatorId, String accountId);

	boolean checkTradeAccountPermission(String operatorId, String accountId);

	boolean checkTradeContractPermission(String operatorId, String unifiedSymbol);
}
