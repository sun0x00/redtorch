package xyz.redtorch.node.master.po;

public class UserPo {
	private String username;
	private String password;
	private String newPassword;
	private String description;
	private Integer loginTimes;
	private String operatorId;
	private String recentlySessionId;
	private Integer recentlyNodeId;
	private String recentlyIpAddress;
	private Integer recentlyPort;
	private String recentlyLoginTime;
	private String recentlyLogoutTime;
	// 简单的权限管理
	private boolean canReadGateway = false;
	private boolean canChangeGatewayStatus = false;
	private boolean canWriteGateway = false;
	private boolean canReadUser = false;
	private boolean canWriteUser = false;
	private boolean canReadOperator = false;
	private boolean canChangeOperatorStatus = false;
	private boolean canWriteOperator = false;
	private boolean canReadNode = false;
	private boolean canChangeNodeToken = false;
	private boolean canWriteNode = false;
	private boolean canReadLog = false;
	private boolean canReadMarketDataRecording = false;
	private boolean canWriteMarketDataRecording = false;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getLoginTimes() {
		return loginTimes;
	}

	public void setLoginTimes(Integer loginTimes) {
		this.loginTimes = loginTimes;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getRecentlySessionId() {
		return recentlySessionId;
	}

	public void setRecentlySessionId(String recentlySessionId) {
		this.recentlySessionId = recentlySessionId;
	}

	public Integer getRecentlyNodeId() {
		return recentlyNodeId;
	}

	public void setRecentlyNodeId(Integer recentlyNodeId) {
		this.recentlyNodeId = recentlyNodeId;
	}

	public String getRecentlyIpAddress() {
		return recentlyIpAddress;
	}

	public void setRecentlyIpAddress(String recentlyIpAddress) {
		this.recentlyIpAddress = recentlyIpAddress;
	}

	public Integer getRecentlyPort() {
		return recentlyPort;
	}

	public void setRecentlyPort(Integer recentlyPort) {
		this.recentlyPort = recentlyPort;
	}

	public String getRecentlyLoginTime() {
		return recentlyLoginTime;
	}

	public void setRecentlyLoginTime(String recentlyLoginTime) {
		this.recentlyLoginTime = recentlyLoginTime;
	}

	public String getRecentlyLogoutTime() {
		return recentlyLogoutTime;
	}

	public void setRecentlyLogoutTime(String recentlyLogoutTime) {
		this.recentlyLogoutTime = recentlyLogoutTime;
	}

	public boolean isCanReadGateway() {
		return canReadGateway;
	}

	public void setCanReadGateway(boolean canReadGateway) {
		this.canReadGateway = canReadGateway;
	}

	public boolean isCanChangeGatewayStatus() {
		return canChangeGatewayStatus;
	}

	public void setCanChangeGatewayStatus(boolean canChangeGatewayStatus) {
		this.canChangeGatewayStatus = canChangeGatewayStatus;
	}

	public boolean isCanWriteGateway() {
		return canWriteGateway;
	}

	public void setCanWriteGateway(boolean canWriteGateway) {
		this.canWriteGateway = canWriteGateway;
	}

	public boolean isCanReadUser() {
		return canReadUser;
	}

	public void setCanReadUser(boolean canReadUser) {
		this.canReadUser = canReadUser;
	}

	public boolean isCanWriteUser() {
		return canWriteUser;
	}

	public void setCanWriteUser(boolean canWriteUser) {
		this.canWriteUser = canWriteUser;
	}

	public boolean isCanReadOperator() {
		return canReadOperator;
	}

	public void setCanReadOperator(boolean canReadOperator) {
		this.canReadOperator = canReadOperator;
	}

	public boolean isCanChangeOperatorStatus() {
		return canChangeOperatorStatus;
	}

	public void setCanChangeOperatorStatus(boolean canChangeOperatorStatus) {
		this.canChangeOperatorStatus = canChangeOperatorStatus;
	}

	public boolean isCanWriteOperator() {
		return canWriteOperator;
	}

	public void setCanWriteOperator(boolean canWriteOperator) {
		this.canWriteOperator = canWriteOperator;
	}

	public boolean isCanReadNode() {
		return canReadNode;
	}

	public void setCanReadNode(boolean canReadNode) {
		this.canReadNode = canReadNode;
	}

	public boolean isCanChangeNodeToken() {
		return canChangeNodeToken;
	}

	public void setCanChangeNodeToken(boolean canChangeNodeToken) {
		this.canChangeNodeToken = canChangeNodeToken;
	}

	public boolean isCanWriteNode() {
		return canWriteNode;
	}

	public void setCanWriteNode(boolean canWriteNode) {
		this.canWriteNode = canWriteNode;
	}

	public boolean isCanReadLog() {
		return canReadLog;
	}

	public void setCanReadLog(boolean canReadLog) {
		this.canReadLog = canReadLog;
	}

	public boolean isCanReadMarketDataRecording() {
		return canReadMarketDataRecording;
	}

	public void setCanReadMarketDataRecording(boolean canReadMarketDataRecording) {
		this.canReadMarketDataRecording = canReadMarketDataRecording;
	}

	public boolean isCanWriteMarketDataRecording() {
		return canWriteMarketDataRecording;
	}

	public void setCanWriteMarketDataRecording(boolean canWriteMarketDataRecording) {
		this.canWriteMarketDataRecording = canWriteMarketDataRecording;
	}
}
