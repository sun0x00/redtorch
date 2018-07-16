package xyz.redtorch.trader.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class Contract  implements Serializable{

	private static final long serialVersionUID = -2126532217233428316L;

	private String gatewayID; // 接口
	
	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码

	private String name;// 合约中文名
	private String productClass; // 合约类型
	private int size; //合约大小
	private double priceTick; //最小变动价位

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
	public String toString() {
		return "Contract [gatewayID=" + gatewayID + ", symbol=" + symbol + ", exchange=" + exchange + ", rtSymbol="
				+ rtSymbol + ", name=" + name + ", productClass=" + productClass + ", size=" + size + ", priceTick="
				+ priceTick + ", strikePrice=" + strikePrice + ", underlyingSymbol=" + underlyingSymbol
				+ ", optionType=" + optionType + ", expiryDate=" + expiryDate + "]";
	}
}
