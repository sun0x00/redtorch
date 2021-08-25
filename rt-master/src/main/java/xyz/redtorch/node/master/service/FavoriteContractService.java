package xyz.redtorch.node.master.service;

import xyz.redtorch.node.master.po.ContractPo;

import java.util.List;

public interface FavoriteContractService {
    List<ContractPo> getContractListByUsername(String username);

    void deleteContractByUsername(String username);

    void deleteContractByUsernameAndUniformSymbol(String username, String uniformSymbol);

    void upsertContractByUsernameAndUniformSymbol(String username, String uniformSymbol);
}
