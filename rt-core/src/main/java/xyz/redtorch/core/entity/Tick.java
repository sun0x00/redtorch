package xyz.redtorch.core.entity;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * @author sun0x00@gmail.com
 */
public class Tick implements Serializable {

	private static final long serialVersionUID = -2066668386737336931L;

	private String gatewayID; // 网关ID
	private String gatewayDisplayName; // 网关显示名称

	// 代码相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码
	private String rtTickID; // 系统中的唯一代码,通常是 合约代码.交易所代码.网关ID

	private String tradingDay; // 交易日
	private String actionDay; // 业务发生日
	private String actionTime; // 时间(HHMMSSmmm)
	private DateTime dateTime;

	private int status; // 状态

	// 成交数据
	private double lastPrice = 0d; // 最新成交价
	private int lastVolume = 0; // 最新成交量
	private int volume = 0; // 今天总成交量
	private double openInterest = 0d; // 持仓量

	private Long preOpenInterest = 0L;// 昨持仓
	private double preClosePrice = 0d; // 前收盘价
	private double preSettlePrice = 0d; // 昨结算

	// 常规行情
	private double openPrice = 0d; // 今日开盘价
	private double highPrice = 0d; // 今日最高价
	private double lowPrice = 0d; // 今日最低价

	private double upperLimit = 0d; // 涨停价
	private double lowerLimit = 0d; // 跌停价

	private double bidPrice1 = 0d;
	private double bidPrice2 = 0d;
	private double bidPrice3 = 0d;
	private double bidPrice4 = 0d;
	private double bidPrice5 = 0d;
	private double bidPrice6 = 0d;
	private double bidPrice7 = 0d;
	private double bidPrice8 = 0d;
	private double bidPrice9 = 0d;
	private double bidPrice10 = 0d;

	private double askPrice1 = 0d;
	private double askPrice2 = 0d;
	private double askPrice3 = 0d;
	private double askPrice4 = 0d;
	private double askPrice5 = 0d;
	private double askPrice6 = 0d;
	private double askPrice7 = 0d;
	private double askPrice8 = 0d;
	private double askPrice9 = 0d;
	private double askPrice10 = 0d;

	private int bidVolume1 = 0;
	private int bidVolume2 = 0;
	private int bidVolume3 = 0;
	private int bidVolume4 = 0;
	private int bidVolume5 = 0;
	private int bidVolume6 = 0;
	private int bidVolume7 = 0;
	private int bidVolume8 = 0;
	private int bidVolume9 = 0;
	private int bidVolume10 = 0;

	private int askVolume1 = 0;
	private int askVolume2 = 0;
	private int askVolume3 = 0;
	private int askVolume4 = 0;
	private int askVolume5 = 0;
	private int askVolume6 = 0;
	private int askVolume7 = 0;
	private int askVolume8 = 0;
	private int askVolume9 = 0;
	private int askVolume10 = 0;

	public Tick setAllValue(String gatewayID, String gatewayDisplayName, String symbol, String exchange,
			String rtSymbol, String rtTickID, String tradingDay, String actionDay, String actionTime, DateTime dateTime,
			int status, double lastPrice, int lastVolume, int volume, double openInterest, Long preOpenInterest,
			double preClosePrice, double preSettlePrice, double openPrice, double highPrice, double lowPrice,
			double upperLimit, double lowerLimit, double bidPrice1, double bidPrice2, double bidPrice3,
			double bidPrice4, double bidPrice5, double bidPrice6, double bidPrice7, double bidPrice8, double bidPrice9,
			double bidPrice10, double askPrice1, double askPrice2, double askPrice3, double askPrice4, double askPrice5,
			double askPrice6, double askPrice7, double askPrice8, double askPrice9, double askPrice10, int bidVolume1,
			int bidVolume2, int bidVolume3, int bidVolume4, int bidVolume5, int bidVolume6, int bidVolume7,
			int bidVolume8, int bidVolume9, int bidVolume10, int askVolume1, int askVolume2, int askVolume3,
			int askVolume4, int askVolume5, int askVolume6, int askVolume7, int askVolume8, int askVolume9,
			int askVolume10) {
		this.gatewayID = gatewayID;
		this.gatewayDisplayName = gatewayDisplayName;
		this.symbol = symbol;
		this.exchange = exchange;
		this.rtSymbol = rtSymbol;
		this.rtTickID = rtTickID;
		this.tradingDay = tradingDay;
		this.actionDay = actionDay;
		this.actionTime = actionTime;
		this.dateTime = dateTime;
		this.status = status;
		this.lastPrice = lastPrice;
		this.lastVolume = lastVolume;
		this.volume = volume;
		this.openInterest = openInterest;
		this.preOpenInterest = preOpenInterest;
		this.preClosePrice = preClosePrice;
		this.preSettlePrice = preSettlePrice;
		this.openPrice = openPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.upperLimit = upperLimit;
		this.lowerLimit = lowerLimit;
		this.bidPrice1 = bidPrice1;
		this.bidPrice2 = bidPrice2;
		this.bidPrice3 = bidPrice3;
		this.bidPrice4 = bidPrice4;
		this.bidPrice5 = bidPrice5;
		this.bidPrice6 = bidPrice6;
		this.bidPrice7 = bidPrice7;
		this.bidPrice8 = bidPrice8;
		this.bidPrice9 = bidPrice9;
		this.bidPrice10 = bidPrice10;
		this.askPrice1 = askPrice1;
		this.askPrice2 = askPrice2;
		this.askPrice3 = askPrice3;
		this.askPrice4 = askPrice4;
		this.askPrice5 = askPrice5;
		this.askPrice6 = askPrice6;
		this.askPrice7 = askPrice7;
		this.askPrice8 = askPrice8;
		this.askPrice9 = askPrice9;
		this.askPrice10 = askPrice10;
		this.bidVolume1 = bidVolume1;
		this.bidVolume2 = bidVolume2;
		this.bidVolume3 = bidVolume3;
		this.bidVolume4 = bidVolume4;
		this.bidVolume5 = bidVolume5;
		this.bidVolume6 = bidVolume6;
		this.bidVolume7 = bidVolume7;
		this.bidVolume8 = bidVolume8;
		this.bidVolume9 = bidVolume9;
		this.bidVolume10 = bidVolume10;
		this.askVolume1 = askVolume1;
		this.askVolume2 = askVolume2;
		this.askVolume3 = askVolume3;
		this.askVolume4 = askVolume4;
		this.askVolume5 = askVolume5;
		this.askVolume6 = askVolume6;
		this.askVolume7 = askVolume7;
		this.askVolume8 = askVolume8;
		this.askVolume9 = askVolume9;
		this.askVolume10 = askVolume10;

		return this;
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

	public String getRtTickID() {
		return rtTickID;
	}

	public void setRtTickID(String rtTickID) {
		this.rtTickID = rtTickID;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public double getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public int getLastVolume() {
		return lastVolume;
	}

	public void setLastVolume(int lastVolume) {
		this.lastVolume = lastVolume;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public double getOpenInterest() {
		return openInterest;
	}

	public void setOpenInterest(double openInterest) {
		this.openInterest = openInterest;
	}

	public Long getPreOpenInterest() {
		return preOpenInterest;
	}

	public void setPreOpenInterest(Long preOpenInterest) {
		this.preOpenInterest = preOpenInterest;
	}

	public double getPreClosePrice() {
		return preClosePrice;
	}

	public void setPreClosePrice(double preClosePrice) {
		this.preClosePrice = preClosePrice;
	}

	public double getPreSettlePrice() {
		return preSettlePrice;
	}

	public void setPreSettlePrice(double preSettlePrice) {
		this.preSettlePrice = preSettlePrice;
	}

	public double getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}

	public double getHighPrice() {
		return highPrice;
	}

	public void setHighPrice(double highPrice) {
		this.highPrice = highPrice;
	}

	public double getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}

	public double getUpperLimit() {
		return upperLimit;
	}

	public void setUpperLimit(double upperLimit) {
		this.upperLimit = upperLimit;
	}

	public double getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(double lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	public double getBidPrice1() {
		return bidPrice1;
	}

	public void setBidPrice1(double bidPrice1) {
		this.bidPrice1 = bidPrice1;
	}

	public double getBidPrice2() {
		return bidPrice2;
	}

	public void setBidPrice2(double bidPrice2) {
		this.bidPrice2 = bidPrice2;
	}

	public double getBidPrice3() {
		return bidPrice3;
	}

	public void setBidPrice3(double bidPrice3) {
		this.bidPrice3 = bidPrice3;
	}

	public double getBidPrice4() {
		return bidPrice4;
	}

	public void setBidPrice4(double bidPrice4) {
		this.bidPrice4 = bidPrice4;
	}

	public double getBidPrice5() {
		return bidPrice5;
	}

	public void setBidPrice5(double bidPrice5) {
		this.bidPrice5 = bidPrice5;
	}

	public double getBidPrice6() {
		return bidPrice6;
	}

	public void setBidPrice6(double bidPrice6) {
		this.bidPrice6 = bidPrice6;
	}

	public double getBidPrice7() {
		return bidPrice7;
	}

	public void setBidPrice7(double bidPrice7) {
		this.bidPrice7 = bidPrice7;
	}

	public double getBidPrice8() {
		return bidPrice8;
	}

	public void setBidPrice8(double bidPrice8) {
		this.bidPrice8 = bidPrice8;
	}

	public double getBidPrice9() {
		return bidPrice9;
	}

	public void setBidPrice9(double bidPrice9) {
		this.bidPrice9 = bidPrice9;
	}

	public double getBidPrice10() {
		return bidPrice10;
	}

	public void setBidPrice10(double bidPrice10) {
		this.bidPrice10 = bidPrice10;
	}

	public double getAskPrice1() {
		return askPrice1;
	}

	public void setAskPrice1(double askPrice1) {
		this.askPrice1 = askPrice1;
	}

	public double getAskPrice2() {
		return askPrice2;
	}

	public void setAskPrice2(double askPrice2) {
		this.askPrice2 = askPrice2;
	}

	public double getAskPrice3() {
		return askPrice3;
	}

	public void setAskPrice3(double askPrice3) {
		this.askPrice3 = askPrice3;
	}

	public double getAskPrice4() {
		return askPrice4;
	}

	public void setAskPrice4(double askPrice4) {
		this.askPrice4 = askPrice4;
	}

	public double getAskPrice5() {
		return askPrice5;
	}

	public void setAskPrice5(double askPrice5) {
		this.askPrice5 = askPrice5;
	}

	public double getAskPrice6() {
		return askPrice6;
	}

	public void setAskPrice6(double askPrice6) {
		this.askPrice6 = askPrice6;
	}

	public double getAskPrice7() {
		return askPrice7;
	}

	public void setAskPrice7(double askPrice7) {
		this.askPrice7 = askPrice7;
	}

	public double getAskPrice8() {
		return askPrice8;
	}

	public void setAskPrice8(double askPrice8) {
		this.askPrice8 = askPrice8;
	}

	public double getAskPrice9() {
		return askPrice9;
	}

	public void setAskPrice9(double askPrice9) {
		this.askPrice9 = askPrice9;
	}

	public double getAskPrice10() {
		return askPrice10;
	}

	public void setAskPrice10(double askPrice10) {
		this.askPrice10 = askPrice10;
	}

	public int getBidVolume1() {
		return bidVolume1;
	}

	public void setBidVolume1(int bidVolume1) {
		this.bidVolume1 = bidVolume1;
	}

	public int getBidVolume2() {
		return bidVolume2;
	}

	public void setBidVolume2(int bidVolume2) {
		this.bidVolume2 = bidVolume2;
	}

	public int getBidVolume3() {
		return bidVolume3;
	}

	public void setBidVolume3(int bidVolume3) {
		this.bidVolume3 = bidVolume3;
	}

	public int getBidVolume4() {
		return bidVolume4;
	}

	public void setBidVolume4(int bidVolume4) {
		this.bidVolume4 = bidVolume4;
	}

	public int getBidVolume5() {
		return bidVolume5;
	}

	public void setBidVolume5(int bidVolume5) {
		this.bidVolume5 = bidVolume5;
	}

	public int getBidVolume6() {
		return bidVolume6;
	}

	public void setBidVolume6(int bidVolume6) {
		this.bidVolume6 = bidVolume6;
	}

	public int getBidVolume7() {
		return bidVolume7;
	}

	public void setBidVolume7(int bidVolume7) {
		this.bidVolume7 = bidVolume7;
	}

	public int getBidVolume8() {
		return bidVolume8;
	}

	public void setBidVolume8(int bidVolume8) {
		this.bidVolume8 = bidVolume8;
	}

	public int getBidVolume9() {
		return bidVolume9;
	}

	public void setBidVolume9(int bidVolume9) {
		this.bidVolume9 = bidVolume9;
	}

	public int getBidVolume10() {
		return bidVolume10;
	}

	public void setBidVolume10(int bidVolume10) {
		this.bidVolume10 = bidVolume10;
	}

	public int getAskVolume1() {
		return askVolume1;
	}

	public void setAskVolume1(int askVolume1) {
		this.askVolume1 = askVolume1;
	}

	public int getAskVolume2() {
		return askVolume2;
	}

	public void setAskVolume2(int askVolume2) {
		this.askVolume2 = askVolume2;
	}

	public int getAskVolume3() {
		return askVolume3;
	}

	public void setAskVolume3(int askVolume3) {
		this.askVolume3 = askVolume3;
	}

	public int getAskVolume4() {
		return askVolume4;
	}

	public void setAskVolume4(int askVolume4) {
		this.askVolume4 = askVolume4;
	}

	public int getAskVolume5() {
		return askVolume5;
	}

	public void setAskVolume5(int askVolume5) {
		this.askVolume5 = askVolume5;
	}

	public int getAskVolume6() {
		return askVolume6;
	}

	public void setAskVolume6(int askVolume6) {
		this.askVolume6 = askVolume6;
	}

	public int getAskVolume7() {
		return askVolume7;
	}

	public void setAskVolume7(int askVolume7) {
		this.askVolume7 = askVolume7;
	}

	public int getAskVolume8() {
		return askVolume8;
	}

	public void setAskVolume8(int askVolume8) {
		this.askVolume8 = askVolume8;
	}

	public int getAskVolume9() {
		return askVolume9;
	}

	public void setAskVolume9(int askVolume9) {
		this.askVolume9 = askVolume9;
	}

	public int getAskVolume10() {
		return askVolume10;
	}

	public void setAskVolume10(int askVolume10) {
		this.askVolume10 = askVolume10;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actionDay == null) ? 0 : actionDay.hashCode());
		result = prime * result + ((actionTime == null) ? 0 : actionTime.hashCode());
		long temp;
		temp = Double.doubleToLongBits(askPrice1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(askPrice10);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(askPrice2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(askPrice3);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(askPrice4);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(askPrice5);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(askPrice6);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(askPrice7);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(askPrice8);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(askPrice9);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + askVolume1;
		result = prime * result + askVolume10;
		result = prime * result + askVolume2;
		result = prime * result + askVolume3;
		result = prime * result + askVolume4;
		result = prime * result + askVolume5;
		result = prime * result + askVolume6;
		result = prime * result + askVolume7;
		result = prime * result + askVolume8;
		result = prime * result + askVolume9;
		temp = Double.doubleToLongBits(bidPrice1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidPrice10);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidPrice2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidPrice3);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidPrice4);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidPrice5);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidPrice6);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidPrice7);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidPrice8);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidPrice9);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + bidVolume1;
		result = prime * result + bidVolume10;
		result = prime * result + bidVolume2;
		result = prime * result + bidVolume3;
		result = prime * result + bidVolume4;
		result = prime * result + bidVolume5;
		result = prime * result + bidVolume6;
		result = prime * result + bidVolume7;
		result = prime * result + bidVolume8;
		result = prime * result + bidVolume9;
		result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + ((gatewayDisplayName == null) ? 0 : gatewayDisplayName.hashCode());
		result = prime * result + ((gatewayID == null) ? 0 : gatewayID.hashCode());
		temp = Double.doubleToLongBits(highPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lastPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + lastVolume;
		temp = Double.doubleToLongBits(lowPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lowerLimit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(openInterest);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(openPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(preClosePrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((preOpenInterest == null) ? 0 : preOpenInterest.hashCode());
		temp = Double.doubleToLongBits(preSettlePrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((rtSymbol == null) ? 0 : rtSymbol.hashCode());
		result = prime * result + status;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((rtTickID == null) ? 0 : rtTickID.hashCode());
		result = prime * result + ((tradingDay == null) ? 0 : tradingDay.hashCode());
		temp = Double.doubleToLongBits(upperLimit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Tick other = (Tick) obj;
		if (actionDay == null) {
			if (other.actionDay != null)
				return false;
		} else if (!actionDay.equals(other.actionDay))
			return false;
		if (actionTime == null) {
			if (other.actionTime != null)
				return false;
		} else if (!actionTime.equals(other.actionTime))
			return false;
		if (Double.doubleToLongBits(askPrice1) != Double.doubleToLongBits(other.askPrice1))
			return false;
		if (Double.doubleToLongBits(askPrice10) != Double.doubleToLongBits(other.askPrice10))
			return false;
		if (Double.doubleToLongBits(askPrice2) != Double.doubleToLongBits(other.askPrice2))
			return false;
		if (Double.doubleToLongBits(askPrice3) != Double.doubleToLongBits(other.askPrice3))
			return false;
		if (Double.doubleToLongBits(askPrice4) != Double.doubleToLongBits(other.askPrice4))
			return false;
		if (Double.doubleToLongBits(askPrice5) != Double.doubleToLongBits(other.askPrice5))
			return false;
		if (Double.doubleToLongBits(askPrice6) != Double.doubleToLongBits(other.askPrice6))
			return false;
		if (Double.doubleToLongBits(askPrice7) != Double.doubleToLongBits(other.askPrice7))
			return false;
		if (Double.doubleToLongBits(askPrice8) != Double.doubleToLongBits(other.askPrice8))
			return false;
		if (Double.doubleToLongBits(askPrice9) != Double.doubleToLongBits(other.askPrice9))
			return false;
		if (askVolume1 != other.askVolume1)
			return false;
		if (askVolume10 != other.askVolume10)
			return false;
		if (askVolume2 != other.askVolume2)
			return false;
		if (askVolume3 != other.askVolume3)
			return false;
		if (askVolume4 != other.askVolume4)
			return false;
		if (askVolume5 != other.askVolume5)
			return false;
		if (askVolume6 != other.askVolume6)
			return false;
		if (askVolume7 != other.askVolume7)
			return false;
		if (askVolume8 != other.askVolume8)
			return false;
		if (askVolume9 != other.askVolume9)
			return false;
		if (Double.doubleToLongBits(bidPrice1) != Double.doubleToLongBits(other.bidPrice1))
			return false;
		if (Double.doubleToLongBits(bidPrice10) != Double.doubleToLongBits(other.bidPrice10))
			return false;
		if (Double.doubleToLongBits(bidPrice2) != Double.doubleToLongBits(other.bidPrice2))
			return false;
		if (Double.doubleToLongBits(bidPrice3) != Double.doubleToLongBits(other.bidPrice3))
			return false;
		if (Double.doubleToLongBits(bidPrice4) != Double.doubleToLongBits(other.bidPrice4))
			return false;
		if (Double.doubleToLongBits(bidPrice5) != Double.doubleToLongBits(other.bidPrice5))
			return false;
		if (Double.doubleToLongBits(bidPrice6) != Double.doubleToLongBits(other.bidPrice6))
			return false;
		if (Double.doubleToLongBits(bidPrice7) != Double.doubleToLongBits(other.bidPrice7))
			return false;
		if (Double.doubleToLongBits(bidPrice8) != Double.doubleToLongBits(other.bidPrice8))
			return false;
		if (Double.doubleToLongBits(bidPrice9) != Double.doubleToLongBits(other.bidPrice9))
			return false;
		if (bidVolume1 != other.bidVolume1)
			return false;
		if (bidVolume10 != other.bidVolume10)
			return false;
		if (bidVolume2 != other.bidVolume2)
			return false;
		if (bidVolume3 != other.bidVolume3)
			return false;
		if (bidVolume4 != other.bidVolume4)
			return false;
		if (bidVolume5 != other.bidVolume5)
			return false;
		if (bidVolume6 != other.bidVolume6)
			return false;
		if (bidVolume7 != other.bidVolume7)
			return false;
		if (bidVolume8 != other.bidVolume8)
			return false;
		if (bidVolume9 != other.bidVolume9)
			return false;
		if (dateTime == null) {
			if (other.dateTime != null)
				return false;
		} else if (!dateTime.equals(other.dateTime))
			return false;
		if (exchange == null) {
			if (other.exchange != null)
				return false;
		} else if (!exchange.equals(other.exchange))
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
		if (Double.doubleToLongBits(highPrice) != Double.doubleToLongBits(other.highPrice))
			return false;
		if (Double.doubleToLongBits(lastPrice) != Double.doubleToLongBits(other.lastPrice))
			return false;
		if (lastVolume != other.lastVolume)
			return false;
		if (Double.doubleToLongBits(lowPrice) != Double.doubleToLongBits(other.lowPrice))
			return false;
		if (Double.doubleToLongBits(lowerLimit) != Double.doubleToLongBits(other.lowerLimit))
			return false;
		if (Double.doubleToLongBits(openInterest) != Double.doubleToLongBits(other.openInterest))
			return false;
		if (Double.doubleToLongBits(openPrice) != Double.doubleToLongBits(other.openPrice))
			return false;
		if (Double.doubleToLongBits(preClosePrice) != Double.doubleToLongBits(other.preClosePrice))
			return false;
		if (preOpenInterest == null) {
			if (other.preOpenInterest != null)
				return false;
		} else if (!preOpenInterest.equals(other.preOpenInterest))
			return false;
		if (Double.doubleToLongBits(preSettlePrice) != Double.doubleToLongBits(other.preSettlePrice))
			return false;
		if (rtSymbol == null) {
			if (other.rtSymbol != null)
				return false;
		} else if (!rtSymbol.equals(other.rtSymbol))
			return false;
		if (status != other.status)
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (rtTickID == null) {
			if (other.rtTickID != null)
				return false;
		} else if (!rtTickID.equals(other.rtTickID))
			return false;
		if (tradingDay == null) {
			if (other.tradingDay != null)
				return false;
		} else if (!tradingDay.equals(other.tradingDay))
			return false;
		if (Double.doubleToLongBits(upperLimit) != Double.doubleToLongBits(other.upperLimit))
			return false;
		if (volume != other.volume)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Tick [gatewayID=" + gatewayID + ", gatewayDisplayName=" + gatewayDisplayName + ", symbol=" + symbol
				+ ", exchange=" + exchange + ", rtSymbol=" + rtSymbol + ", rtTickID=" + rtTickID + ", tradingDay="
				+ tradingDay + ", actionDay=" + actionDay + ", actionTime=" + actionTime + ", dateTime=" + dateTime
				+ ", status=" + status + ", lastPrice=" + lastPrice + ", lastVolume=" + lastVolume + ", volume="
				+ volume + ", openInterest=" + openInterest + ", preOpenInterest=" + preOpenInterest
				+ ", preClosePrice=" + preClosePrice + ", preSettlePrice=" + preSettlePrice + ", openPrice=" + openPrice
				+ ", highPrice=" + highPrice + ", lowPrice=" + lowPrice + ", upperLimit=" + upperLimit + ", lowerLimit="
				+ lowerLimit + ", bidPrice1=" + bidPrice1 + ", bidPrice2=" + bidPrice2 + ", bidPrice3=" + bidPrice3
				+ ", bidPrice4=" + bidPrice4 + ", bidPrice5=" + bidPrice5 + ", bidPrice6=" + bidPrice6 + ", bidPrice7="
				+ bidPrice7 + ", bidPrice8=" + bidPrice8 + ", bidPrice9=" + bidPrice9 + ", bidPrice10=" + bidPrice10
				+ ", askPrice1=" + askPrice1 + ", askPrice2=" + askPrice2 + ", askPrice3=" + askPrice3 + ", askPrice4="
				+ askPrice4 + ", askPrice5=" + askPrice5 + ", askPrice6=" + askPrice6 + ", askPrice7=" + askPrice7
				+ ", askPrice8=" + askPrice8 + ", askPrice9=" + askPrice9 + ", askPrice10=" + askPrice10
				+ ", bidVolume1=" + bidVolume1 + ", bidVolume2=" + bidVolume2 + ", bidVolume3=" + bidVolume3
				+ ", bidVolume4=" + bidVolume4 + ", bidVolume5=" + bidVolume5 + ", bidVolume6=" + bidVolume6
				+ ", bidVolume7=" + bidVolume7 + ", bidVolume8=" + bidVolume8 + ", bidVolume9=" + bidVolume9
				+ ", bidVolume10=" + bidVolume10 + ", askVolume1=" + askVolume1 + ", askVolume2=" + askVolume2
				+ ", askVolume3=" + askVolume3 + ", askVolume4=" + askVolume4 + ", askVolume5=" + askVolume5
				+ ", askVolume6=" + askVolume6 + ", askVolume7=" + askVolume7 + ", askVolume8=" + askVolume8
				+ ", askVolume9=" + askVolume9 + ", askVolume10=" + askVolume10 + "]";
	}
}
