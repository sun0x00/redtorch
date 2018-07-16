package xyz.redtorch.trader.entity;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * @author sun0x00@gmail.com
 */
public class Bar implements Serializable{
    
	private static final long serialVersionUID = 9166305799616198661L;

	private String  gatewayID;    // 接口
	
    // 代码相关
    private String symbol;        // 代码
    private String exchange;      // 交易所代码
    private String rtSymbol;      // 系统中的唯一代码,通常是 合约代码.交易所代码

    private String tradingDay;       // 交易日
    private String actionDay;        // 业务发生日
    private String actionTime;       // 时间(HHmmssSSS)
    private DateTime dateTime;

    private Double open = 0d;
    private Double high = 0d;
    private Double low = 0d;
    private Double close = 0d;
    
    private Integer volume = 0;          // 成交量
    private Double openInterest = 0d;    // 持仓量
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
	public Double getOpen() {
		return open;
	}
	public void setOpen(Double open) {
		this.open = open;
	}
	public Double getHigh() {
		return high;
	}
	public void setHigh(Double high) {
		this.high = high;
	}
	public Double getLow() {
		return low;
	}
	public void setLow(Double low) {
		this.low = low;
	}
	public Double getClose() {
		return close;
	}
	public void setClose(Double close) {
		this.close = close;
	}
	public Integer getVolume() {
		return volume;
	}
	public void setVolume(Integer volume) {
		this.volume = volume;
	}
	public Double getOpenInterest() {
		return openInterest;
	}
	public void setOpenInterest(Double openInterest) {
		this.openInterest = openInterest;
	}
	@Override
	public String toString() {
		return "Bar [gatewayID=" + gatewayID + ", symbol=" + symbol + ", exchange=" + exchange + ", rtSymbol="
				+ rtSymbol + ", tradingDay=" + tradingDay + ", actionDay=" + actionDay + ", actionTime=" + actionTime
				+ ", dateTime=" + dateTime + ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close
				+ ", volume=" + volume + ", openInterest=" + openInterest + "]";
	}

}
