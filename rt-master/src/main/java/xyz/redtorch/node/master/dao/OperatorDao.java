package xyz.redtorch.node.master.dao;

import xyz.redtorch.node.master.po.OperatorPo;

import java.util.List;

public interface OperatorDao {
    OperatorPo queryOperatorByOperatorId(String operatorId);

    List<OperatorPo> queryOperatorList();

    void upsertOperatorByOperatorId(OperatorPo operator);

    void deleteOperatorByOperatorId(String operatorId);
}
