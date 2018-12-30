package xyz.redtorch.core.entity;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * @author sun0x00@gmail.com
 */
public class Bar implements Serializable {

	private static final long serialVersionUID = 9166305799616198661L;

	private String gatewayID; // 网关
	private String gatewayDisplayName; // 网关显示名称

	// 代码相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码
	private String contractName; // 名称
	private String rtBarID; // 系统中的唯一代码,通常是 合约代码.交易所代码.网关ID

	private String tradingDay; // 交易日
	private String actionDay; // 业务发生日
	private String actionTime; // 时间(HHmmssSSS)
	private DateTime dateTime;

	private double open = 0d;
	private double high = 0d;
	private double low = 0d;
	private double close = 0d;

	private int volume = 0; // 成交量
	private double openInterest = 0d; // 持仓量

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

	public String getRtBarID() {
		return rtBarID;
	}

	public void setRtBarID(String rtBarID) {
		this.rtBarID = rtBarID;
	}

	public String getTradingDay() {
		return tradingDay;
	}

	public void setTradingDay(String tradingDay) {
		this.tradingDay = tradingDay;
	}

	public String getActionDay() {
		return actionDay;
	}

	public void setActionDay(String actionDay) {
		this.actionDay = actionDay;
	}

	public String getActionTime() {
		return actionTime;
	}

	public void setActionTime(String actionTime) {
		this.actionTime = actionTime;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public double getOpenInterest() {
		return openInterest;
	}

	public void setOpenInterest(double openInterest) {
		this.openInterest = openInterest;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actionDay == null) ? 0 : actionDay.hashCode());
		result = prime * result + ((actionTime == null) ? 0 : actionTime.hashCode());
		long temp;
		temp = Double.doubleToLongBits(close);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((contractName == null) ? 0 : contractName.hashCode());
		result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + ((gatewayDisplayName == null) ? 0 : gatewayDisplayName.hashCode());
		result = prime * result + ((gatewayID == null) ? 0 : gatewayID.hashCode());
		temp = Double.doubleToLongBits(high);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(low);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(open);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(openInterest);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((rtBarID == null) ? 0 : rtBarID.hashCode());
		result = prime * result + ((rtSymbol == null) ? 0 : rtSymbol.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
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
		Bar other = (Bar) obj;
		if (actionDay == null) {
			if (other.actionDay != null)
				return false;
		} else if (!actionDay.equals(other.actionDay))
			return false;
		if (actionTime == null) {
			if (other.actionTime != null)
				return false;
		} else if (!actionTime.equals(other.actionTime))
			return false;
		if (Double.doubleToLongBits(close) != Double.doubleToLongBits(other.close))
			return false;
		if (contractName == null) {
			if (other.contractName != null)
				return false;
		} else if (!contractName.equals(other.contractName))
			return false;
		if (dateTime == null) {
			if (other.dateTime != null)
				return false;
		} else if (!dateTime.equals(other.dateTime))
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
		if (Double.doubleToLongBits(high) != Double.doubleToLongBits(other.high))
			return false;
		if (Double.doubleToLongBits(low) != Double.doubleToLongBits(other.low))
			return false;
		if (Double.doubleToLongBits(open) != Double.doubleToLongBits(other.open))
			return false;
		if (Double.doubleToLongBits(openInterest) != Double.doubleToLongBits(other.openInterest))
			return false;
		if (rtBarID == null) {
			if (other.rtBarID != null)
				return false;
		} else if (!rtBarID.equals(other.rtBarID))
			return false;
		if (rtSymbol == null) {
			if (other.rtSymbol != null)
				return false;
		} else if (!rtSymbol.equals(other.rtSymbol))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
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
		return "Bar [gatewayID=" + gatewayID + ", gatewayDisplayName=" + gatewayDisplayName + ", symbol=" + symbol
				+ ", exchange=" + exchange + ", rtSymbol=" + rtSymbol + ", contractName=" + contractName + ", rtBarID="
				+ rtBarID + ", tradingDay=" + tradingDay + ", actionDay=" + actionDay + ", actionTime=" + actionTime
				+ ", dateTime=" + dateTime + ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close
				+ ", volume=" + volume + ", openInterest=" + openInterest + "]";
	}

}
