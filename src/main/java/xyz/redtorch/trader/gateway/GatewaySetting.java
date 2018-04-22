package xyz.redtorch.trader.gateway;

/**
 * @author sun0x00@gmail.com
 */
public class GatewaySetting {
	String gatewayID;
	String gatewayDisplayName;
	String userID;
	String password;
	String brokerID;
	String tdAddress;
	String mdAddress;
	String authCode;
	String userProductInfo;
	String gatewayClassName;
	
	//用于运行时区分是否连接
	Boolean runtimeStatus;

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
	
}
