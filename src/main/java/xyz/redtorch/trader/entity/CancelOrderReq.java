package xyz.redtorch.trader.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class CancelOrderReq  implements Serializable{
	private static final long serialVersionUID = -8268383961926962032L;

	private String gatewayID; // 接口
	
	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码


	private String orderID; // 报单号

	// CTP LTS接口相关
	private int frontID; // 前置机号
	private int sessionID; // 回话号
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
	public String toString() {
		return "CancelOrderReq [gatewayID=" + gatewayID + ", symbol=" + symbol + ", exchange=" + exchange
				+ ", rtSymbol=" + rtSymbol + ", orderID=" + orderID + ", frontID=" + frontID + ", sessionID="
				+ sessionID + "]";
	}
}
