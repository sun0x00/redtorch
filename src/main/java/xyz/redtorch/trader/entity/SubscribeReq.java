package xyz.redtorch.trader.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class SubscribeReq  implements Serializable{

	private static final long serialVersionUID = -8669237992027524217L;
	
	private String gatewayID; // 接口
	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码

	// 以下为IB相关
	private String productClass; // 合约类型
	private String currency; // 合约货币
	private String expiry; // 到期日
	private double strikePrice; // 行权价
	private String optionType; // 期权类型
	public String getGatewayID() {
		return gatewayID;
	}
	public void setGatewayID(String gatewayID) {
		this.gatewayID = gatewayID;
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
	@Override
	public String toString() {
		return "SubscribeReq [gatewayID=" + gatewayID + ", symbol=" + symbol + ", exchange=" + exchange + ", rtSymbol="
				+ rtSymbol + ", productClass=" + productClass + ", currency=" + currency + ", expiry=" + expiry
				+ ", strikePrice=" + strikePrice + ", optionType=" + optionType + "]";
	}

}
