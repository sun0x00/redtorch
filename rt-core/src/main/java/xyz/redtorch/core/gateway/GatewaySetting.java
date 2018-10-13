package xyz.redtorch.core.gateway;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class GatewaySetting implements Serializable {

	private static final long serialVersionUID = 4779933491540999914L;

	private String gatewayID;
	private String gatewayDisplayName;
	private String gatewayClassName;
	private String gatewayType;

	private CtpSetting ctpSetting;
	private IbSetting ibSetting;

	// 用于运行时区分是否连接
	private Boolean runtimeStatus;

	public String getGatewayID() {
		return gatewayID;
	}

	public void setGatewayID(String gatewayID) {
		this.gatewayID = gatewayID;
	}

	public String getGatewayDisplayName() {
		return gatewayDisplayName;
	}

	public void setGatewayDisplayName(String gatewayDisplayName) {
		this.gatewayDisplayName = gatewayDisplayName;
	}

	public String getGatewayType() {
		return gatewayType;
	}

	public void setGatewayType(String gatewayType) {
		this.gatewayType = gatewayType;
	}

	public String getGatewayClassName() {
		return gatewayClassName;
	}

	public void setGatewayClassName(String gatewayClassName) {
		this.gatewayClassName = gatewayClassName;
	}

	public Boolean isRuntimeStatus() {
		return runtimeStatus;
	}

	public void setRuntimeStatus(Boolean runtimeStatus) {
		this.runtimeStatus = runtimeStatus;
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

	public Boolean getRuntimeStatus() {
		return runtimeStatus;
	}

	public static class CtpSetting implements Serializable {
		private static final long serialVersionUID = -3881484318866766516L;
		private String userID;
		private String password;
		private String brokerID;
		private String tdAddress;
		private String mdAddress;
		private String authCode;
		private String userProductInfo;

		public String getUserID() {
			return userID;
		}

		public void setUserID(String userID) {
			this.userID = userID;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getBrokerID() {
			return brokerID;
		}

		public void setBrokerID(String brokerID) {
			this.brokerID = brokerID;
		}

		public String getTdAddress() {
			return tdAddress;
		}

		public void setTdAddress(String tdAddress) {
			this.tdAddress = tdAddress;
		}

		public String getMdAddress() {
			return mdAddress;
		}

		public void setMdAddress(String mdAddress) {
			this.mdAddress = mdAddress;
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
	}

	public static class IbSetting implements Serializable {
		private static final long serialVersionUID = 820059410308685143L;
		// 连接地址
		private String host;
		// 连接端口
		private int port;
		// 用户编号
		private int clientID;
		// 账户编号
		private String accountCode;

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

		public int getClientID() {
			return clientID;
		}

		public void setClientID(int clientID) {
			this.clientID = clientID;
		}

		public String getAccountCode() {
			return accountCode;
		}

		public void setAccountCode(String accountCode) {
			this.accountCode = accountCode;
		}
	}

	;

}
