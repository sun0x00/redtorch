package xyz.redtorch.node.master.service;

import xyz.redtorch.node.master.po.ContractPo;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

import java.util.List;
import java.util.Set;

public interface MarketDataRecordingService {
    List<ContractPo> getContractList();

    void deleteContractByUniformSymbol(String uniformSymbol);

    void addContractByUniformSymbol(String uniformSymbol);

    List<ContractField> getSubscribedContractFieldList();

    Set<String> getSubscribedUniformSymbolSet();

    void processTick(TickField tick);
}
