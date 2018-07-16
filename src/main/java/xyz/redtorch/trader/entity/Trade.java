package xyz.redtorch.trader.entity;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * @author sun0x00@gmail.com
 */
public class Trade implements Serializable{

	private static final long serialVersionUID = -6691915458395088529L;

	private String gatewayID; // 接口

	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码

	private String tradeID; // 成交编号
	private String rtTradeID; // 成交在rt系统中的唯一编号,通常是 Gateway名.成交编号

	private String orderID; // 订单编号
	private String rtOrderID; // 订单在rt系统中的唯一编号,通常是 Gateway名.订单编号

	// 成交相关
	private String direction; // 成交方向
	private String offset; // 成交开平仓
	private double price; // 成交价格
	private int volume; // 成交数量

	private String tradingDay; // 交易日
	private String tradeDate; // 业务发生日
	private String tradeTime; // 时间(HHMMSSmmm)
    private DateTime dateTime;
    
    
	public void setAllValue(String gatewayID, String symbol, String exchange, String rtSymbol, String tradeID, String rtTradeID,
			String orderID, String rtOrderID, String direction, String offset, double price, int volume,
			String tradingDay, String tradeDate, String tradeTime, DateTime dateTime) {
		this.gatewayID = gatewayID;
		this.symbol = symbol;
		this.exchange = exchange;
		this.rtSymbol = rtSymbol;
		this.tradeID = tradeID;
		this.rtTradeID = rtTradeID;
		this.orderID = orderID;
		this.rtOrderID = rtOrderID;
		this.direction = direction;
		this.offset = offset;
		this.price = price;
		this.volume = volume;
		this.tradingDay = tradingDay;
		this.tradeDate = tradeDate;
		this.tradeTime = tradeTime;
		this.dateTime = dateTime;
	}
	
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
	public String toString() {
		return "Trade [gatewayID=" + gatewayID + ", symbol=" + symbol + ", exchange=" + exchange + ", rtSymbol="
				+ rtSymbol + ", tradeID=" + tradeID + ", rtTradeID=" + rtTradeID + ", orderID=" + orderID
				+ ", rtOrderID=" + rtOrderID + ", direction=" + direction + ", offset=" + offset + ", price=" + price
				+ ", volume=" + volume + ", tradingDay=" + tradingDay + ", tradeDate=" + tradeDate + ", tradeTime="
				+ tradeTime + ", dateTime=" + dateTime + "]";
	}

}
