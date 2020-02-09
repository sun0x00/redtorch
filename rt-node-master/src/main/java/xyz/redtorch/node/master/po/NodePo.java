package xyz.redtorch.node.master.po;

public class NodePo {

	public static final Integer NODE_STATUS_DISCONNECTED = 0;
	public static final Integer NODE_STATUS_CONNECTED = 1;

	private Integer nodeId;
	private Integer status;
	private String token;
	private String description;
	private Integer loginTimes;
	private String recentlySessionId;
	private String recentlyIpAddress;
	private Integer recentlyPort;
	private String recentlyLoginTime;
	private String recentlyLogoutTime;

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getRecentlySessionId() {
		return recentlySessionId;
	}

	public void setRecentlySessionId(String recentlySessionId) {
		this.recentlySessionId = recentlySessionId;
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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

}
