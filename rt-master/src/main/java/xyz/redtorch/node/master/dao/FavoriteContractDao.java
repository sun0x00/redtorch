package xyz.redtorch.node.master.dao;

import xyz.redtorch.node.master.po.ContractPo;

import java.util.List;

public interface FavoriteContractDao {
    List<ContractPo> queryContractList();

    List<ContractPo> queryContractListByUsername(String username);

    void deleteContractByUsername(String username);

    void deleteContractByUsernameAndUniformSymbol(String username, String uniformSymbol);

    void upsertContractByUsernameAndUniformSymbol(String username, ContractPo contract);
}
