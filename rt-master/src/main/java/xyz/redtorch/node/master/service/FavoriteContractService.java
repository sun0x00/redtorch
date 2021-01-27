package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.node.master.po.ContractPo;

public interface FavoriteContractService {
	List<ContractPo> getContractListByUsername(String username);

	void deleteContractByUsername(String username);

	void deleteContractByUsernameAndUniformSymbol(String username, String uniformSymbol);

	void upsertContractByUsernameAndUniformSymbol(String username, String uniformSymbol);
}
