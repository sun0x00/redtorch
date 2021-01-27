package xyz.redtorch.node.master.dao;

import java.util.List;

import xyz.redtorch.node.master.po.ContractPo;

public interface MarketDataRecordingDao {
	List<ContractPo> queryContractList();

	void deleteContractByUniformSymbol(String uniformSymbol);

	void upsertContractByUniformSymbol(ContractPo contract);
}
