package xyz.redtorch.node.master.dao;

import java.util.List;

import xyz.redtorch.node.master.po.ContractPo;

public interface MarketDataRecordingDao {
	List<ContractPo> queryContractList();

	void deleteContractByUnifiedSymbol(String unifiedSymbol);

	void upsertContractByUnifiedSymbol(ContractPo contract);
}
