package xyz.redtorch.node.master.dao;

import java.util.List;

import xyz.redtorch.node.master.po.OperatorPo;

public interface OperatorDao {
	OperatorPo queryOperatorByOperatorId(String operatorId);

	List<OperatorPo> queryOperatorList();

	void upsertOperatorByOperatorId(OperatorPo operator);

	void deleteOperatorByOperatorId(String operatorId);
}
