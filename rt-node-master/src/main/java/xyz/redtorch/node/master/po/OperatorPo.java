package xyz.redtorch.node.master.po;

import java.util.HashSet;
import java.util.Set;

public class OperatorPo {
	private String operatorId;
	private boolean associatedToUser = false;
	private String username;
	private String description;

	private boolean canReadAllAccounts = true;
	private Set<String> acceptReadSpecialAccountIdSet = new HashSet<>();
	private Set<String> denyReadSpecialAccountIdSet = new HashSet<>();

	private boolean canTradeAllAccounts = false;
	private Set<String> acceptTradeSpecialAccountIdSet = new HashSet<>();
	private Set<String> denyTradeSpecialAccountIdSet = new HashSet<>();

	private boolean canTradeAllContracts = false;
	private Set<String> acceptTradeSpecialUnifiedSymbolSet = new HashSet<>();
	private Set<String> denyTradeSpecialUnifiedSymbolSet = new HashSet<>();

	private boolean canSubscribeAllContracts = true;
	private Set<String> acceptSubscribeSpecialUnifiedSymbolSet = new HashSet<>();
	private Set<String> denySubscribeSpecialUnifiedSymbolSet = new HashSet<>();

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public boolean isAssociatedToUser() {
		return associatedToUser;
	}

	public void setAssociatedToUser(boolean associatedToUser) {
		this.associatedToUser = associatedToUser;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCanReadAllAccounts() {
		return canReadAllAccounts;
	}

	public void setCanReadAllAccounts(boolean canReadAllAccounts) {
		this.canReadAllAccounts = canReadAllAccounts;
	}

	public Set<String> getAcceptReadSpecialAccountIdSet() {
		return acceptReadSpecialAccountIdSet;
	}

	public void setAcceptReadSpecialAccountIdSet(Set<String> acceptReadSpecialAccountIdSet) {
		this.acceptReadSpecialAccountIdSet = acceptReadSpecialAccountIdSet;
	}

	public Set<String> getDenyReadSpecialAccountIdSet() {
		return denyReadSpecialAccountIdSet;
	}

	public void setDenyReadSpecialAccountIdSet(Set<String> denyReadSpecialAccountIdSet) {
		this.denyReadSpecialAccountIdSet = denyReadSpecialAccountIdSet;
	}

	public boolean isCanTradeAllAccounts() {
		return canTradeAllAccounts;
	}

	public void setCanTradeAllAccounts(boolean canTradeAllAccounts) {
		this.canTradeAllAccounts = canTradeAllAccounts;
	}

	public Set<String> getAcceptTradeSpecialAccountIdSet() {
		return acceptTradeSpecialAccountIdSet;
	}

	public void setAcceptTradeSpecialAccountIdSet(Set<String> acceptTradeSpecialAccountIdSet) {
		this.acceptTradeSpecialAccountIdSet = acceptTradeSpecialAccountIdSet;
	}

	public Set<String> getDenyTradeSpecialAccountIdSet() {
		return denyTradeSpecialAccountIdSet;
	}

	public void setDenyTradeSpecialAccountIdSet(Set<String> denyTradeSpecialAccountIdSet) {
		this.denyTradeSpecialAccountIdSet = denyTradeSpecialAccountIdSet;
	}

	public boolean isCanTradeAllContracts() {
		return canTradeAllContracts;
	}

	public void setCanTradeAllContracts(boolean canTradeAllContracts) {
		this.canTradeAllContracts = canTradeAllContracts;
	}

	public Set<String> getAcceptTradeSpecialUnifiedSymbolSet() {
		return acceptTradeSpecialUnifiedSymbolSet;
	}

	public void setAcceptTradeSpecialUnifiedSymbolSet(Set<String> acceptTradeSpecialUnifiedSymbolSet) {
		this.acceptTradeSpecialUnifiedSymbolSet = acceptTradeSpecialUnifiedSymbolSet;
	}

	public Set<String> getDenyTradeSpecialUnifiedSymbolSet() {
		return denyTradeSpecialUnifiedSymbolSet;
	}

	public void setDenyTradeSpecialUnifiedSymbolSet(Set<String> denyTradeSpecialUnifiedSymbolSet) {
		this.denyTradeSpecialUnifiedSymbolSet = denyTradeSpecialUnifiedSymbolSet;
	}

	public boolean isCanSubscribeAllContracts() {
		return canSubscribeAllContracts;
	}

	public void setCanSubscribeAllContracts(boolean canSubscribeAllContracts) {
		this.canSubscribeAllContracts = canSubscribeAllContracts;
	}

	public Set<String> getAcceptSubscribeSpecialUnifiedSymbolSet() {
		return acceptSubscribeSpecialUnifiedSymbolSet;
	}

	public void setAcceptSubscribeSpecialUnifiedSymbolSet(Set<String> acceptSubscribeSpecialUnifiedSymbolSet) {
		this.acceptSubscribeSpecialUnifiedSymbolSet = acceptSubscribeSpecialUnifiedSymbolSet;
	}

	public Set<String> getDenySubscribeSpecialUnifiedSymbolSet() {
		return denySubscribeSpecialUnifiedSymbolSet;
	}

	public void setDenySubscribeSpecialUnifiedSymbolSet(Set<String> denySubscribeSpecialUnifiedSymbolSet) {
		this.denySubscribeSpecialUnifiedSymbolSet = denySubscribeSpecialUnifiedSymbolSet;
	}

}
