package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.node.master.po.ContractPo;

public interface FavoriteContractService {
	List<ContractPo> getContractListByUsername(String username);

	void deleteContractByUsername(String username);

	void deleteContractByUsernameAndUnifiedSymbol(String username, String unifiedSymbol);

	void upsertContractByUsernameAndUnifiedSymbol(String username, String unifiedSymbol);
}
