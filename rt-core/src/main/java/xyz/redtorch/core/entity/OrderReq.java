package xyz.redtorch.core.entity;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author sun0x00@gmail.com
 */
public class OrderReq implements Serializable {

	private static final long serialVersionUID = -8783647687127541104L;

	private String gatewayID; // 网关
	private String gatewayDisplayName; // 网关
	private String accountID; // 账户代码
	private String rtAccountID; // 系统中的唯一账户代码,通常是   账户代码.币种.网关

	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码

	// 报单相关
	private double price; // 报单价格
	private int volume; // 报单总数量
	private String direction; // 报单方向
	private String offset; // 报单开平仓
	private String priceType; // 报单成交数量

	private String originalOrderID;// 原始ID
	private String operatorID;// 操作者ID

	// IB预留
	private String productClass; // 合约类型
	private String currency; // 合约货币
	private String expiry; // 到期日
	private double strikePrice; // 行权价
	private String optionType; // 期权类型
	private String lastTradeDateOrContractMonth; // 合约月,IB专用
	private String multiplier; // 乘数,IB专用
	
	public OrderReq() {
		this.originalOrderID = UUID.randomUUID().toString();
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

	public String getPriceType() {
		return priceType;
	}

	public void setPriceType(String priceType) {
		this.priceType = priceType;
	}

	public String getOriginalOrderID() {
		return originalOrderID;
	}

	public void setOriginalOrderID(String originalOrderID) {
		this.originalOrderID = originalOrderID;
	}

	public String getOperatorID() {
		return operatorID;
	}

	public void setOperatorID(String operatorID) {
		this.operatorID = operatorID;
	}

	public String getProductClass() {
		return productClass;
	}

	public void setProductClass(String productClass) {
		this.productClass = productClass;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public double getStrikePrice() {
		return strikePrice;
	}

	public void setStrikePrice(double strikePrice) {
		this.strikePrice = strikePrice;
	}

	public String getOptionType() {
		return optionType;
	}

	public void setOptionType(String optionType) {
		this.optionType = optionType;
	}

	public String getLastTradeDateOrContractMonth() {
		return lastTradeDateOrContractMonth;
	}

	public void setLastTradeDateOrContractMonth(String lastTradeDateOrContractMonth) {
		this.lastTradeDateOrContractMonth = lastTradeDateOrContractMonth;
	}

	public String getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(String multiplier) {
		this.multiplier = multiplier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountID == null) ? 0 : accountID.hashCode());
		result = prime * result + ((currency == null) ? 0 : currency.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + ((expiry == null) ? 0 : expiry.hashCode());
		result = prime * result + ((gatewayDisplayName == null) ? 0 : gatewayDisplayName.hashCode());
		result = prime * result + ((gatewayID == null) ? 0 : gatewayID.hashCode());
		result = prime * result
				+ ((lastTradeDateOrContractMonth == null) ? 0 : lastTradeDateOrContractMonth.hashCode());
		result = prime * result + ((multiplier == null) ? 0 : multiplier.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result + ((operatorID == null) ? 0 : operatorID.hashCode());
		result = prime * result + ((optionType == null) ? 0 : optionType.hashCode());
		result = prime * result + ((originalOrderID == null) ? 0 : originalOrderID.hashCode());
		long temp;
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((priceType == null) ? 0 : priceType.hashCode());
		result = prime * result + ((productClass == null) ? 0 : productClass.hashCode());
		result = prime * result + ((rtAccountID == null) ? 0 : rtAccountID.hashCode());
		result = prime * result + ((rtSymbol == null) ? 0 : rtSymbol.hashCode());
		temp = Double.doubleToLongBits(strikePrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
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
		OrderReq other = (OrderReq) obj;
		if (accountID == null) {
			if (other.accountID != null)
				return false;
		} else if (!accountID.equals(other.accountID))
			return false;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
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
		if (expiry == null) {
			if (other.expiry != null)
				return false;
		} else if (!expiry.equals(other.expiry))
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
		if (lastTradeDateOrContractMonth == null) {
			if (other.lastTradeDateOrContractMonth != null)
				return false;
		} else if (!lastTradeDateOrContractMonth.equals(other.lastTradeDateOrContractMonth))
			return false;
		if (multiplier == null) {
			if (other.multiplier != null)
				return false;
		} else if (!multiplier.equals(other.multiplier))
			return false;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		if (operatorID == null) {
			if (other.operatorID != null)
				return false;
		} else if (!operatorID.equals(other.operatorID))
			return false;
		if (optionType == null) {
			if (other.optionType != null)
				return false;
		} else if (!optionType.equals(other.optionType))
			return false;
		if (originalOrderID == null) {
			if (other.originalOrderID != null)
				return false;
		} else if (!originalOrderID.equals(other.originalOrderID))
			return false;
		if (Double.doubleToLongBits(price) != Double.doubleToLongBits(other.price))
			return false;
		if (priceType == null) {
			if (other.priceType != null)
				return false;
		} else if (!priceType.equals(other.priceType))
			return false;
		if (productClass == null) {
			if (other.productClass != null)
				return false;
		} else if (!productClass.equals(other.productClass))
			return false;
		if (rtAccountID == null) {
			if (other.rtAccountID != null)
				return false;
		} else if (!rtAccountID.equals(other.rtAccountID))
			return false;
		if (rtSymbol == null) {
			if (other.rtSymbol != null)
				return false;
		} else if (!rtSymbol.equals(other.rtSymbol))
			return false;
		if (Double.doubleToLongBits(strikePrice) != Double.doubleToLongBits(other.strikePrice))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (volume != other.volume)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OrderReq [gatewayID=" + gatewayID + ", gatewayDisplayName=" + gatewayDisplayName + ", accountID="
				+ accountID + ", rtAccountID=" + rtAccountID + ", symbol=" + symbol + ", exchange=" + exchange
				+ ", rtSymbol=" + rtSymbol + ", price=" + price + ", volume=" + volume + ", direction=" + direction
				+ ", offset=" + offset + ", priceType=" + priceType + ", originalOrderID=" + originalOrderID
				+ ", operatorID=" + operatorID + ", productClass=" + productClass + ", currency=" + currency
				+ ", expiry=" + expiry + ", strikePrice=" + strikePrice + ", optionType=" + optionType
				+ ", lastTradeDateOrContractMonth=" + lastTradeDateOrContractMonth + ", multiplier=" + multiplier + "]";
	}

}
