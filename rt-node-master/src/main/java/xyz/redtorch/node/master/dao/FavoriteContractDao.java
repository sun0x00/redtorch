package xyz.redtorch.node.master.dao;

import java.util.List;

import xyz.redtorch.node.master.po.ContractPo;

public interface FavoriteContractDao {
	List<ContractPo> queryContractList();

	List<ContractPo> queryContractListByUsername(String username);

	void deleteContractByUsername(String username);

	void deleteContractByUsernameAndUnifiedSymbol(String username, String unifiedSymbol);

	void upsertContractByUsernameAndUnifiedSymbol(String username, ContractPo contract);
}
