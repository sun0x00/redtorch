package xyz.redtorch.core.entity;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * @author sun0x00@gmail.com
 */
public class Trade implements Serializable {

	private static final long serialVersionUID = -6691915458395088529L;

	private String gatewayID; // 网关
	private String gatewayDisplayName; // 网关显示名称
	// 账号代码相关
	private String accountID; // 账户代码
	private String rtAccountID; // 账户在RedTorch中的唯一代码,通常 账户代码.网关

	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码

	private String tradeID; // 成交编号
	private String rtTradeID; // 成交在rt系统中的唯一编号,通常是 Gateway名.成交编号

	private String orderID; // 订单编号
	private String rtOrderID; // 订单在rt系统中的唯一编号,通常是 Gateway名.订单编号
	private String originalOrderID; // 原始订单编号

	// 成交相关
	private String direction; // 成交方向
	private String offset; // 成交开平仓
	private double price; // 成交价格
	private int volume; // 成交数量

	private String tradingDay; // 交易日
	private String tradeDate; // 业务发生日
	private String tradeTime; // 时间(HHMMSSmmm)
	private DateTime dateTime;

	public Trade setAllValue(String gatewayID, String gatewayDisplayName, String accountID, String rtAccountID,
			String symbol, String exchange, String rtSymbol, String tradeID, String rtTradeID, String orderID,
			String rtOrderID, String originalOrderID, String direction, String offset, double price, int volume,
			String tradingDay, String tradeDate, String tradeTime, DateTime dateTime) {
		this.gatewayID = gatewayID;
		this.gatewayDisplayName = gatewayDisplayName;
		this.accountID = accountID;
		this.rtAccountID = rtAccountID;
		this.symbol = symbol;
		this.exchange = exchange;
		this.rtSymbol = rtSymbol;
		this.tradeID = tradeID;
		this.rtTradeID = rtTradeID;
		this.orderID = orderID;
		this.rtOrderID = rtOrderID;
		this.originalOrderID = originalOrderID;
		this.direction = direction;
		this.offset = offset;
		this.price = price;
		this.volume = volume;
		this.tradingDay = tradingDay;
		this.tradeDate = tradeDate;
		this.tradeTime = tradeTime;
		this.dateTime = dateTime;
		return this;
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

	public String getTradeID() {
		return tradeID;
	}

	public void setTradeID(String tradeID) {
		this.tradeID = tradeID;
	}

	public String getRtTradeID() {
		return rtTradeID;
	}

	public void setRtTradeID(String rtTradeID) {
		this.rtTradeID = rtTradeID;
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

	public String getOriginalOrderID() {
		return originalOrderID;
	}

	public void setOriginalOrderID(String originalOrderID) {
		this.originalOrderID = originalOrderID;
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

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public String getTradingDay() {
		return tradingDay;
	}

	public void setTradingDay(String tradingDay) {
		this.tradingDay = tradingDay;
	}

	public String getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}

	public String getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(String tradeTime) {
		this.tradeTime = tradeTime;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountID == null) ? 0 : accountID.hashCode());
		result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + ((gatewayDisplayName == null) ? 0 : gatewayDisplayName.hashCode());
		result = prime * result + ((gatewayID == null) ? 0 : gatewayID.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result + ((orderID == null) ? 0 : orderID.hashCode());
		result = prime * result + ((originalOrderID == null) ? 0 : originalOrderID.hashCode());
		long temp;
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((rtAccountID == null) ? 0 : rtAccountID.hashCode());
		result = prime * result + ((rtOrderID == null) ? 0 : rtOrderID.hashCode());
		result = prime * result + ((rtSymbol == null) ? 0 : rtSymbol.hashCode());
		result = prime * result + ((rtTradeID == null) ? 0 : rtTradeID.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((tradeDate == null) ? 0 : tradeDate.hashCode());
		result = prime * result + ((tradeID == null) ? 0 : tradeID.hashCode());
		result = prime * result + ((tradeTime == null) ? 0 : tradeTime.hashCode());
		result = prime * result + ((tradingDay == null) ? 0 : tradingDay.hashCode());
		result = prime * result + volume;
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
		Trade other = (Trade) obj;
		if (accountID == null) {
			if (other.accountID != null)
				return false;
		} else if (!accountID.equals(other.accountID))
			return false;
		if (dateTime == null) {
			if (other.dateTime != null)
				return false;
		} else if (!dateTime.equals(other.dateTime))
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
		if (orderID == null) {
			if (other.orderID != null)
				return false;
		} else if (!orderID.equals(other.orderID))
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
		if (rtTradeID == null) {
			if (other.rtTradeID != null)
				return false;
		} else if (!rtTradeID.equals(other.rtTradeID))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (tradeDate == null) {
			if (other.tradeDate != null)
				return false;
		} else if (!tradeDate.equals(other.tradeDate))
			return false;
		if (tradeID == null) {
			if (other.tradeID != null)
				return false;
		} else if (!tradeID.equals(other.tradeID))
			return false;
		if (tradeTime == null) {
			if (other.tradeTime != null)
				return false;
		} else if (!tradeTime.equals(other.tradeTime))
			return false;
		if (tradingDay == null) {
			if (other.tradingDay != null)
				return false;
		} else if (!tradingDay.equals(other.tradingDay))
			return false;
		if (volume != other.volume)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trade [gatewayID=" + gatewayID + ", gatewayDisplayName=" + gatewayDisplayName + ", accountID="
				+ accountID + ", rtAccountID=" + rtAccountID + ", symbol=" + symbol + ", exchange=" + exchange
				+ ", rtSymbol=" + rtSymbol + ", tradeID=" + tradeID + ", rtTradeID=" + rtTradeID + ", orderID="
				+ orderID + ", rtOrderID=" + rtOrderID + ", originalOrderID=" + originalOrderID + ", direction="
				+ direction + ", offset=" + offset + ", price=" + price + ", volume=" + volume + ", tradingDay="
				+ tradingDay + ", tradeDate=" + tradeDate + ", tradeTime=" + tradeTime + ", dateTime=" + dateTime + "]";
	}

}
