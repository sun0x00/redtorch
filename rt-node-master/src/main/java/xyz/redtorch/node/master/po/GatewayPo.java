package xyz.redtorch.node.master.po;

public class GatewayPo {

	private String gatewayId; // 网关ID
	private String gatewayName; // 网关名称
	private String gatewayDescription; // 网关描述
	private String implementClassName; // 实现类
	private Integer gatewayType; // 网关类型
	private Integer gatewayAdapterType; // 网关适配器类型
	private Integer targetNodeId; // 目标节点
	private String autoConnectTimeRanges; // 855-1555#2033-2359#0-300

	private CtpSetting ctpSetting;
	private IbSetting ibSetting;

	private Integer status;

	private Long version; // 版本

	public String getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	public String getGatewayDescription() {
		return gatewayDescription;
	}

	public void setGatewayDescription(String gatewayDescription) {
		this.gatewayDescription = gatewayDescription;
	}

	public String getImplementClassName() {
		return implementClassName;
	}

	public void setImplementClassName(String implementClassName) {
		this.implementClassName = implementClassName;
	}

	public Integer getGatewayType() {
		return gatewayType;
	}

	public void setGatewayType(Integer gatewayType) {
		this.gatewayType = gatewayType;
	}

	public Integer getGatewayAdapterType() {
		return gatewayAdapterType;
	}

	public void setGatewayAdapterType(Integer gatewayAdapterType) {
		this.gatewayAdapterType = gatewayAdapterType;
	}

	public Integer getTargetNodeId() {
		return targetNodeId;
	}

	public void setTargetNodeId(Integer targetNodeId) {
		this.targetNodeId = targetNodeId;
	}

	public CtpSetting getCtpSetting() {
		return ctpSetting;
	}

	public void setCtpSetting(CtpSetting ctpSetting) {
		this.ctpSetting = ctpSetting;
	}

	public IbSetting getIbSetting() {
		return ibSetting;
	}

	public void setIbSetting(IbSetting ibSetting) {
		this.ibSetting = ibSetting;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getAutoConnectTimeRanges() {
		return autoConnectTimeRanges;
	}

	public void setAutoConnectTimeRanges(String autoConnectTimeRanges) {
		this.autoConnectTimeRanges = autoConnectTimeRanges;
	}

	public static class CtpSetting {
		private String userId;
		private String password;
		private String brokerId;
		private String tdHost;
		private String tdPort;
		private String mdHost;
		private String mdPort;
		private String authCode;
		private String userProductInfo;
		private String appId;

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getBrokerId() {
			return brokerId;
		}

		public void setBrokerId(String brokerId) {
			this.brokerId = brokerId;
		}

		public String getTdHost() {
			return tdHost;
		}

		public void setTdHost(String tdHost) {
			this.tdHost = tdHost;
		}

		public String getTdPort() {
			return tdPort;
		}

		public void setTdPort(String tdPort) {
			this.tdPort = tdPort;
		}

		public String getMdHost() {
			return mdHost;
		}

		public void setMdHost(String mdHost) {
			this.mdHost = mdHost;
		}

		public String getMdPort() {
			return mdPort;
		}

		public void setMdPort(String mdPort) {
			this.mdPort = mdPort;
		}

		public String getAuthCode() {
			return authCode;
		}

		public void setAuthCode(String authCode) {
			this.authCode = authCode;
		}

		public String getUserProductInfo() {
			return userProductInfo;
		}

		public void setUserProductInfo(String userProductInfo) {
			this.userProductInfo = userProductInfo;
		}

		public String getAppId() {
			return appId;
		}

		public void setAppId(String appId) {
			this.appId = appId;
		}
	}

	public static class IbSetting {
		// 连接地址
		private String host;
		// 连接端口
		private int port;
		// 客户端ID
		private int clientId;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public int getClientId() {
			return clientId;
		}

		public void setClientId(int clientId) {
			this.clientId = clientId;
		}
	}
}
