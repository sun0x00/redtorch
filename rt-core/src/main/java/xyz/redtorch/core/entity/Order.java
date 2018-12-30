package xyz.redtorch.core.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class Order implements Serializable {

	private static final long serialVersionUID = 7932302478961553376L;

	private String originalOrderID; // 原始委托ID

	private String gatewayID; // 网关
	private String gatewayDisplayName; // 网关显示名称
	// 账号代码相关
	private String accountID; // 账户代码
	private String rtAccountID; // 账户在RedTorch中的唯一代码,通常 账户代码.网关

	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码
	private String contractName; // 名称

	private String orderID; // 订单编号
	private String rtOrderID; // 订单在rt系统中的唯一编号,通常是 Gateway名.订单编号

	// 报单相关
	private String direction; // 报单方向
	private String offset; // 报单开平仓
	private double price; // 报单价格
	private int totalVolume; // 报单总数量
	private int tradedVolume; // 报单成交数量
	private String status; // 报单状态

	private String tradingDay;

	private String orderDate; // 发单日期
	private String orderTime; // 发单时间
	private String cancelTime; // 撤单时间
	private String activeTime; // 激活时间
	private String updateTime; // 最后修改时间

	// CTP/LTS相关
	private int frontID; // 前置机编号
	private int sessionID; // 连接编号

	public Order setAllValue(String originalOrderID, String gatewayID, String gatewayDisplayName, String accountID,
			String rtAccountID, String symbol, String exchange, String rtSymbol, String contractName, String orderID,
			String rtOrderID, String direction, String offset, double price, int totalVolume, int tradedVolume,
			String status, String tradingDay, String orderDate, String orderTime, String cancelTime, String activeTime,
			String updateTime, int frontID, int sessionID) {
		this.originalOrderID = originalOrderID;
		this.gatewayID = gatewayID;
		this.gatewayDisplayName = gatewayDisplayName;
		this.accountID = accountID;
		this.rtAccountID = rtAccountID;
		this.symbol = symbol;
		this.exchange = exchange;
		this.rtSymbol = rtSymbol;
		this.contractName = contractName;
		this.orderID = orderID;
		this.rtOrderID = rtOrderID;
		this.direction = direction;
		this.offset = offset;
		this.price = price;
		this.totalVolume = totalVolume;
		this.tradedVolume = tradedVolume;
		this.status = status;
		this.tradingDay = tradingDay;
		this.orderDate = orderDate;
		this.orderTime = orderTime;
		this.cancelTime = cancelTime;
		this.activeTime = activeTime;
		this.updateTime = updateTime;
		this.frontID = frontID;
		this.sessionID = sessionID;
		return this;
	}

	public String getOriginalOrderID() {
		return originalOrderID;
	}

	public void setOriginalOrderID(String originalOrderID) {
		this.originalOrderID = originalOrderID;
	}

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

	public String getAccountID() {
		return accountID;
	}

	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}

	public String getRtAccountID() {
		return rtAccountID;
	}

	public void setRtAccountID(String rtAccountID) {
		this.rtAccountID = rtAccountID;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getRtSymbol() {
		return rtSymbol;
	}

	public void setRtSymbol(String rtSymbol) {
		this.rtSymbol = rtSymbol;
	}

	public String getContractName() {
		return contractName;
	}

	public void setContractName(String contractName) {
		this.contractName = contractName;
	}

	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public String getRtOrderID() {
		return rtOrderID;
	}

	public void setRtOrderID(String rtOrderID) {
		this.rtOrderID = rtOrderID;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getTotalVolume() {
		return totalVolume;
	}

	public void setTotalVolume(int totalVolume) {
		this.totalVolume = totalVolume;
	}

	public int getTradedVolume() {
		return tradedVolume;
	}

	public void setTradedVolume(int tradedVolume) {
		this.tradedVolume = tradedVolume;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTradingDay() {
		return tradingDay;
	}

	public void setTradingDay(String tradingDay) {
		this.tradingDay = tradingDay;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}

	public String getCancelTime() {
		return cancelTime;
	}

	public void setCancelTime(String cancelTime) {
		this.cancelTime = cancelTime;
	}

	public String getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(String activeTime) {
		this.activeTime = activeTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public int getFrontID() {
		return frontID;
	}

	public void setFrontID(int frontID) {
		this.frontID = frontID;
	}

	public int getSessionID() {
		return sessionID;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountID == null) ? 0 : accountID.hashCode());
		result = prime * result + ((activeTime == null) ? 0 : activeTime.hashCode());
		result = prime * result + ((cancelTime == null) ? 0 : cancelTime.hashCode());
		result = prime * result + ((contractName == null) ? 0 : contractName.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + frontID;
		result = prime * result + ((gatewayDisplayName == null) ? 0 : gatewayDisplayName.hashCode());
		result = prime * result + ((gatewayID == null) ? 0 : gatewayID.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result + ((orderDate == null) ? 0 : orderDate.hashCode());
		result = prime * result + ((orderID == null) ? 0 : orderID.hashCode());
		result = prime * result + ((orderTime == null) ? 0 : orderTime.hashCode());
		result = prime * result + ((originalOrderID == null) ? 0 : originalOrderID.hashCode());
		long temp;
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((rtAccountID == null) ? 0 : rtAccountID.hashCode());
		result = prime * result + ((rtOrderID == null) ? 0 : rtOrderID.hashCode());
		result = prime * result + ((rtSymbol == null) ? 0 : rtSymbol.hashCode());
		result = prime * result + sessionID;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + totalVolume;
		result = prime * result + tradedVolume;
		result = prime * result + ((tradingDay == null) ? 0 : tradingDay.hashCode());
		result = prime * result + ((updateTime == null) ? 0 : updateTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (accountID == null) {
			if (other.accountID != null)
				return false;
		} else if (!accountID.equals(other.accountID))
			return false;
		if (activeTime == null) {
			if (other.activeTime != null)
				return false;
		} else if (!activeTime.equals(other.activeTime))
			return false;
		if (cancelTime == null) {
			if (other.cancelTime != null)
				return false;
		} else if (!cancelTime.equals(other.cancelTime))
			return false;
		if (contractName == null) {
			if (other.contractName != null)
				return false;
		} else if (!contractName.equals(other.contractName))
			return false;
		if (direction == null) {
			if (other.direction != null)
				return false;
		} else if (!direction.equals(other.direction))
			return false;
		if (exchange == null) {
			if (other.exchange != null)
				return false;
		} else if (!exchange.equals(other.exchange))
			return false;
		if (frontID != other.frontID)
			return false;
		if (gatewayDisplayName == null) {
			if (other.gatewayDisplayName != null)
				return false;
		} else if (!gatewayDisplayName.equals(other.gatewayDisplayName))
			return false;
		if (gatewayID == null) {
			if (other.gatewayID != null)
				return false;
		} else if (!gatewayID.equals(other.gatewayID))
			return false;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		if (orderDate == null) {
			if (other.orderDate != null)
				return false;
		} else if (!orderDate.equals(other.orderDate))
			return false;
		if (orderID == null) {
			if (other.orderID != null)
				return false;
		} else if (!orderID.equals(other.orderID))
			return false;
		if (orderTime == null) {
			if (other.orderTime != null)
				return false;
		} else if (!orderTime.equals(other.orderTime))
			return false;
		if (originalOrderID == null) {
			if (other.originalOrderID != null)
				return false;
		} else if (!originalOrderID.equals(other.originalOrderID))
			return false;
		if (Double.doubleToLongBits(price) != Double.doubleToLongBits(other.price))
			return false;
		if (rtAccountID == null) {
			if (other.rtAccountID != null)
				return false;
		} else if (!rtAccountID.equals(other.rtAccountID))
			return false;
		if (rtOrderID == null) {
			if (other.rtOrderID != null)
				return false;
		} else if (!rtOrderID.equals(other.rtOrderID))
			return false;
		if (rtSymbol == null) {
			if (other.rtSymbol != null)
				return false;
		} else if (!rtSymbol.equals(other.rtSymbol))
			return false;
		if (sessionID != other.sessionID)
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (totalVolume != other.totalVolume)
			return false;
		if (tradedVolume != other.tradedVolume)
			return false;
		if (tradingDay == null) {
			if (other.tradingDay != null)
				return false;
		} else if (!tradingDay.equals(other.tradingDay))
			return false;
		if (updateTime == null) {
			if (other.updateTime != null)
				return false;
		} else if (!updateTime.equals(other.updateTime))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Order [originalOrderID=" + originalOrderID + ", gatewayID=" + gatewayID + ", gatewayDisplayName="
				+ gatewayDisplayName + ", accountID=" + accountID + ", rtAccountID=" + rtAccountID + ", symbol="
				+ symbol + ", exchange=" + exchange + ", rtSymbol=" + rtSymbol + ", contractName=" + contractName
				+ ", orderID=" + orderID + ", rtOrderID=" + rtOrderID + ", direction=" + direction + ", offset="
				+ offset + ", price=" + price + ", totalVolume=" + totalVolume + ", tradedVolume=" + tradedVolume
				+ ", status=" + status + ", tradingDay=" + tradingDay + ", orderDate=" + orderDate + ", orderTime="
				+ orderTime + ", cancelTime=" + cancelTime + ", activeTime=" + activeTime + ", updateTime=" + updateTime
				+ ", frontID=" + frontID + ", sessionID=" + sessionID + "]";
	}

}
