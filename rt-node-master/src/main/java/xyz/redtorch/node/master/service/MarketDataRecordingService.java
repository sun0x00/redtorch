package xyz.redtorch.node.master.service;

import java.util.List;
import java.util.Set;

import xyz.redtorch.node.master.po.ContractPo;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

public interface MarketDataRecordingService {
	List<ContractPo> getContractList();

	void deleteContractByUnifiedSymbol(String unifiedSymbol);

	void addContractByUnifiedSymbol(String unifiedSymbol);

	List<ContractField> getSubscribedContractFieldList();

	Set<String> getSubscribedUnifiedSymbolSet();
	
	void processTick(TickField tick);
}
