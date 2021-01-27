package xyz.redtorch.node.master.service;

import java.util.List;
import java.util.Set;

import xyz.redtorch.node.master.po.ContractPo;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

public interface MarketDataRecordingService {
	List<ContractPo> getContractList();

	void deleteContractByUniformSymbol(String uniformSymbol);

	void addContractByUniformSymbol(String uniformSymbol);

	List<ContractField> getSubscribedContractFieldList();

	Set<String> getSubscribedUniformSymbolSet();
	
	void processTick(TickField tick);
}
