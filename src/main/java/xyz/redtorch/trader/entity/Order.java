package xyz.redtorch.trader.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class Order implements Serializable{

	private static final long serialVersionUID = 7932302478961553376L;

	private String gatewayID; // 接口

	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码

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
	
	public Order() {}
	
	public void setAllValue(String gatewayID, String symbol, String exchange, String rtSymbol, String orderID, String rtOrderID,
			String direction, String offset, double price, int totalVolume, int tradedVolume, String status,
			String tradingDay, String orderDate, String orderTime, String cancelTime, String activeTime,
			String updateTime, int frontID, int sessionID) {
		this.gatewayID = gatewayID;
		this.symbol = symbol;
		this.exchange = exchange;
		this.rtSymbol = rtSymbol;
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

	@Override
	public String toString() {
		return "Order [gatewayID=" + gatewayID + ", symbol=" + symbol + ", exchange=" + exchange + ", rtSymbol="
				+ rtSymbol + ", orderID=" + orderID + ", rtOrderID=" + rtOrderID + ", direction=" + direction
				+ ", offset=" + offset + ", price=" + price + ", totalVolume=" + totalVolume + ", tradedVolume="
				+ tradedVolume + ", status=" + status + ", tradingDay=" + tradingDay + ", orderDate=" + orderDate
				+ ", orderTime=" + orderTime + ", cancelTime=" + cancelTime + ", activeTime=" + activeTime
				+ ", updateTime=" + updateTime + ", frontID=" + frontID + ", sessionID=" + sessionID + "]";
	}
	
}
