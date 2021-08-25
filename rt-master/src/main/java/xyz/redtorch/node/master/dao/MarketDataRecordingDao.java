package xyz.redtorch.node.master.dao;

import xyz.redtorch.node.master.po.ContractPo;

import java.util.List;

public interface MarketDataRecordingDao {
    List<ContractPo> queryContractList();

    void deleteContractByUniformSymbol(String uniformSymbol);

    void upsertContractByUniformSymbol(ContractPo contract);
}
