package xyz.redtorch.trader.module.zeus.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class StopOrder  implements Serializable{
	private static final long serialVersionUID = -9196544370046282764L;
	private String gatewayID;
	private String rtSymbol;
	private String orderType;
	private String direction;
	private String offset;
	private String priceType;
	private double price;
	private int volume;
	
	private String stopOrderID;
	private String status;
	public String getGatewayID() {
		return gatewayID;
	}
	public void setGatewayID(String gatewayID) {
		this.gatewayID = gatewayID;
	}
	public String getRtSymbol() {
		return rtSymbol;
	}
	public void setRtSymbol(String rtSymbol) {
		this.rtSymbol = rtSymbol;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
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
	public String getStopOrderID() {
		return stopOrderID;
	}
	public void setStopOrderID(String stopOrderID) {
		this.stopOrderID = stopOrderID;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "StopOrder [gatewayID=" + gatewayID + ", rtSymbol=" + rtSymbol + ", orderType=" + orderType
				+ ", direction=" + direction + ", offset=" + offset + ", priceType=" + priceType + ", price=" + price
				+ ", volume=" + volume + ", stopOrderID=" + stopOrderID + ", status=" + status + "]";
	}
	
}
