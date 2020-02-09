package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.node.master.po.ContractPo;

public interface MarketDataRecordingService {
	List<ContractPo> getContractList();

	void deleteContractByUnifiedSymbol(String unifiedSymbol);

	void addContractByUnifiedSymbol(String unifiedSymbol);
}
