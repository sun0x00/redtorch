package xyz.redtorch.core.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class Contract implements Serializable {

	private static final long serialVersionUID = -2126532217233428316L;

	private String gatewayID; // 网关
	private String gatewayDisplayName; // 网关显示名称

	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码
	private String rtContractID; // 系统中的唯一代码,通常是 合约代码.交易所代码.网关代码

	private String name;// 合约中文名
	private String productClass; // 合约类型
	private int size; // 合约大小
	private double priceTick; // 最小变动价位

	// 期权相关
	private double strikePrice; // 期权行权价
	private String underlyingSymbol; // 标的物合约代码
	private String optionType; /// 期权类型
	private String expiryDate; // 到期日

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

	public String getRtContractID() {
		return rtContractID;
	}

	public void setRtContractID(String rtContractID) {
		this.rtContractID = rtContractID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProductClass() {
		return productClass;
	}

	public void setProductClass(String productClass) {
		this.productClass = productClass;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public double getPriceTick() {
		return priceTick;
	}

	public void setPriceTick(double priceTick) {
		this.priceTick = priceTick;
	}

	public double getStrikePrice() {
		return strikePrice;
	}

	public void setStrikePrice(double strikePrice) {
		this.strikePrice = strikePrice;
	}

	public String getUnderlyingSymbol() {
		return underlyingSymbol;
	}

	public void setUnderlyingSymbol(String underlyingSymbol) {
		this.underlyingSymbol = underlyingSymbol;
	}

	public String getOptionType() {
		return optionType;
	}

	public void setOptionType(String optionType) {
		this.optionType = optionType;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rtContractID == null) ? 0 : rtContractID.hashCode());
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + ((expiryDate == null) ? 0 : expiryDate.hashCode());
		result = prime * result + ((gatewayDisplayName == null) ? 0 : gatewayDisplayName.hashCode());
		result = prime * result + ((gatewayID == null) ? 0 : gatewayID.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((optionType == null) ? 0 : optionType.hashCode());
		long temp;
		temp = Double.doubleToLongBits(priceTick);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((productClass == null) ? 0 : productClass.hashCode());
		result = prime * result + ((rtSymbol == null) ? 0 : rtSymbol.hashCode());
		result = prime * result + size;
		temp = Double.doubleToLongBits(strikePrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((underlyingSymbol == null) ? 0 : underlyingSymbol.hashCode());
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
		Contract other = (Contract) obj;
		if (rtContractID == null) {
			if (other.rtContractID != null)
				return false;
		} else if (!rtContractID.equals(other.rtContractID))
			return false;
		if (exchange == null) {
			if (other.exchange != null)
				return false;
		} else if (!exchange.equals(other.exchange))
			return false;
		if (expiryDate == null) {
			if (other.expiryDate != null)
				return false;
		} else if (!expiryDate.equals(other.expiryDate))
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (optionType == null) {
			if (other.optionType != null)
				return false;
		} else if (!optionType.equals(other.optionType))
			return false;
		if (Double.doubleToLongBits(priceTick) != Double.doubleToLongBits(other.priceTick))
			return false;
		if (productClass == null) {
			if (other.productClass != null)
				return false;
		} else if (!productClass.equals(other.productClass))
			return false;
		if (rtSymbol == null) {
			if (other.rtSymbol != null)
				return false;
		} else if (!rtSymbol.equals(other.rtSymbol))
			return false;
		if (size != other.size)
			return false;
		if (Double.doubleToLongBits(strikePrice) != Double.doubleToLongBits(other.strikePrice))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (underlyingSymbol == null) {
			if (other.underlyingSymbol != null)
				return false;
		} else if (!underlyingSymbol.equals(other.underlyingSymbol))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Contract [gatewayID=" + gatewayID + ", gatewayDisplayName=" + gatewayDisplayName + ", symbol=" + symbol
				+ ", exchange=" + exchange + ", rtSymbol=" + rtSymbol + ", rtContractID=" + rtContractID + ", name="
				+ name + ", productClass=" + productClass + ", size=" + size + ", priceTick=" + priceTick
				+ ", strikePrice=" + strikePrice + ", underlyingSymbol=" + underlyingSymbol + ", optionType="
				+ optionType + ", expiryDate=" + expiryDate + "]";
	}

}
