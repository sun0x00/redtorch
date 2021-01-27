package xyz.redtorch.desktop.service;

import xyz.redtorch.pb.CoreField.ContractField;

import java.util.Set;

public interface GuiMainService {
    void reloadData();

    void updateSelectedAccountIdSet(Set<String> selectedAccountIdSet);

    Set<String> getSelectedAccountIdSet();

    void updateSelectedContract(ContractField contractField);

    ContractField getSelectedContract();

    void refreshContractData();

    void writeAccountsDataToFile();

    boolean isSelectedAccountId(String accountId);

    boolean isSelectedContract(ContractField contractField);
}
