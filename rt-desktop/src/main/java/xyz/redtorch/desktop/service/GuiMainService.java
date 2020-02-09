package xyz.redtorch.desktop.service;

import java.util.Set;

import xyz.redtorch.pb.CoreField.ContractField;

public interface GuiMainService {
	void reloadData();

	void updateSelectedAccountIdSet(Set<String> selectedAccountIdSet);

	Set<String> getSelectedAccountIdSet();

	void updateSelectedContarct(ContractField contractField);

	ContractField getSelectedContract();

	void refreshContractData();
}
