package xyz.redtorch.trader.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class OrderReq  implements Serializable{

	private static final long serialVersionUID = -8783647687127541104L;

	private String gatewayID; // 接口
	
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

	// IB预留
	private String productClass; // 合约类型
	private String currency; // 合约货币
	private String expiry; // 到期日
	private double strikePrice; // 行权价
	private String optionType; // 期权类型
	private String lastTradeDateOrContractMonth; // 合约月,IB专用
	private String multiplier; // 乘数,IB专用
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
	public String toString() {
		return "OrderReq [gatewayID=" + gatewayID + ", symbol=" + symbol + ", exchange=" + exchange + ", rtSymbol="
				+ rtSymbol + ", price=" + price + ", volume=" + volume + ", direction=" + direction + ", offset="
				+ offset + ", priceType=" + priceType + ", productClass=" + productClass + ", currency=" + currency
				+ ", expiry=" + expiry + ", strikePrice=" + strikePrice + ", optionType=" + optionType
				+ ", lastTradeDateOrContractMonth=" + lastTradeDateOrContractMonth + ", multiplier=" + multiplier + "]";
	}

	
}
