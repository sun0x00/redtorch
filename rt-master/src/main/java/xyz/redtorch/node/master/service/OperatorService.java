package xyz.redtorch.node.master.service;

import xyz.redtorch.node.master.po.OperatorPo;

import java.util.List;

public interface OperatorService {
    OperatorPo getOperatorByOperatorId(String operatorId);

    List<OperatorPo> getOperatorList();

    void upsertOperatorByOperatorId(OperatorPo operator);

    void deleteOperatorByOperatorId(String operatorId);

    OperatorPo createOperator();

    boolean checkSubscribePermission(String operatorId, String uniformSymbol);

    boolean checkReadAccountPermission(String operatorId, String accountId);

    boolean checkTradeAccountPermission(String operatorId, String accountId);

    boolean checkTradeContractPermission(String operatorId, String uniformSymbol);
}
