package xyz.redtorch.core.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class Position implements Serializable {

	private static final long serialVersionUID = -7231668074296775467L;

	private String gatewayID; // 网关
	private String gatewayDisplayName; // 网关
	// 账号代码相关
	private String accountID; // 账户代码
	private String rtAccountID; // 账户在RedTorch中的唯一代码,通常 账户代码.币种.网关
	private String rtPositionID; // 持仓在系统中的唯一代码,通常是网关ID.代码.方向

	// 代码编号相关
	private String symbol; // 代码
	private String exchange; // 交易所代码
	private String rtSymbol; // 系统中的唯一代码,通常是 合约代码.交易所代码
	private String contractName; // 名称
	private int contractSize; // 合约大小

	// 持仓相关
	private String direction; // 持仓方向
	private int position; // 持仓量
	private int frozen; // 冻结数量
	private int ydPosition; // 昨持仓
	private int ydFrozen; // 冻结数量
	private int tdPosition; // 今持仓
	private int tdFrozen; // 冻结数量

	private double lastPrice; // 计算盈亏使用的行情最后价格
	private double price; // 持仓均价
	private double priceDiff; // 持仓价格差
	private double openPrice; // 开仓均价
	private double openPriceDiff; // 开仓价格差
	private double positionProfit; // 持仓盈亏
	private double positionProfitRatio; // 持仓盈亏率
	private double openPositionProfit; // 开仓盈亏
	private double openPositionProfitRatio; // 开仓盈亏率
	
	
	private double useMargin; // 占用的保证金
	private double exchangeMargin; // 交易所的保证金
	private double contractValue; // 最新合约价值
	
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
	public String getAccountID() {
		return accountID;
	}
	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}
	public String getRtAccountID() {
		return rtAccountID;
	}
	public void setRtAccountID(String rtAccountID) {
		this.rtAccountID = rtAccountID;
	}
	public String getRtPositionID() {
		return rtPositionID;
	}
	public void setRtPositionID(String rtPositionID) {
		this.rtPositionID = rtPositionID;
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
	public String getContractName() {
		return contractName;
	}
	public void setContractName(String contractName) {
		this.contractName = contractName;
	}
	public int getContractSize() {
		return contractSize;
	}
	public void setContractSize(int contractSize) {
		this.contractSize = contractSize;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getFrozen() {
		return frozen;
	}
	public void setFrozen(int frozen) {
		this.frozen = frozen;
	}
	public int getYdPosition() {
		return ydPosition;
	}
	public void setYdPosition(int ydPosition) {
		this.ydPosition = ydPosition;
	}
	public int getYdFrozen() {
		return ydFrozen;
	}
	public void setYdFrozen(int ydFrozen) {
		this.ydFrozen = ydFrozen;
	}
	public int getTdPosition() {
		return tdPosition;
	}
	public void setTdPosition(int tdPosition) {
		this.tdPosition = tdPosition;
	}
	public int getTdFrozen() {
		return tdFrozen;
	}
	public void setTdFrozen(int tdFrozen) {
		this.tdFrozen = tdFrozen;
	}
	public double getLastPrice() {
		return lastPrice;
	}
	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getPriceDiff() {
		return priceDiff;
	}
	public void setPriceDiff(double priceDiff) {
		this.priceDiff = priceDiff;
	}
	public double getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}
	public double getOpenPriceDiff() {
		return openPriceDiff;
	}
	public void setOpenPriceDiff(double openPriceDiff) {
		this.openPriceDiff = openPriceDiff;
	}
	public double getPositionProfit() {
		return positionProfit;
	}
	public void setPositionProfit(double positionProfit) {
		this.positionProfit = positionProfit;
	}
	public double getPositionProfitRatio() {
		return positionProfitRatio;
	}
	public void setPositionProfitRatio(double positionProfitRatio) {
		this.positionProfitRatio = positionProfitRatio;
	}
	public double getOpenPositionProfit() {
		return openPositionProfit;
	}
	public void setOpenPositionProfit(double openPositionProfit) {
		this.openPositionProfit = openPositionProfit;
	}
	public double getOpenPositionProfitRatio() {
		return openPositionProfitRatio;
	}
	public void setOpenPositionProfitRatio(double openPositionProfitRatio) {
		this.openPositionProfitRatio = openPositionProfitRatio;
	}
	public double getUseMargin() {
		return useMargin;
	}
	public void setUseMargin(double useMargin) {
		this.useMargin = useMargin;
	}
	public double getExchangeMargin() {
		return exchangeMargin;
	}
	public void setExchangeMargin(double exchangeMargin) {
		this.exchangeMargin = exchangeMargin;
	}
	public double getContractValue() {
		return contractValue;
	}
	public void setContractValue(double contractValue) {
		this.contractValue = contractValue;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountID == null) ? 0 : accountID.hashCode());
		result = prime * result + ((contractName == null) ? 0 : contractName.hashCode());
		result = prime * result + contractSize;
		long temp;
		temp = Double.doubleToLongBits(contractValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		temp = Double.doubleToLongBits(exchangeMargin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + frozen;
		result = prime * result + ((gatewayDisplayName == null) ? 0 : gatewayDisplayName.hashCode());
		result = prime * result + ((gatewayID == null) ? 0 : gatewayID.hashCode());
		temp = Double.doubleToLongBits(lastPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(openPositionProfit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(openPositionProfitRatio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(openPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(openPriceDiff);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + position;
		temp = Double.doubleToLongBits(positionProfit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(positionProfitRatio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(priceDiff);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((rtAccountID == null) ? 0 : rtAccountID.hashCode());
		result = prime * result + ((rtPositionID == null) ? 0 : rtPositionID.hashCode());
		result = prime * result + ((rtSymbol == null) ? 0 : rtSymbol.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + tdFrozen;
		result = prime * result + tdPosition;
		temp = Double.doubleToLongBits(useMargin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ydFrozen;
		result = prime * result + ydPosition;
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
		Position other = (Position) obj;
		if (accountID == null) {
			if (other.accountID != null)
				return false;
		} else if (!accountID.equals(other.accountID))
			return false;
		if (contractName == null) {
			if (other.contractName != null)
				return false;
		} else if (!contractName.equals(other.contractName))
			return false;
		if (contractSize != other.contractSize)
			return false;
		if (Double.doubleToLongBits(contractValue) != Double.doubleToLongBits(other.contractValue))
			return false;
		if (direction == null) {
			if (other.direction != null)
				return false;
		} else if (!direction.equals(other.direction))
			return false;
		if (exchange == null) {
			if (other.exchange != null)
				return false;
		} else if (!exchange.equals(other.exchange))
			return false;
		if (Double.doubleToLongBits(exchangeMargin) != Double.doubleToLongBits(other.exchangeMargin))
			return false;
		if (frozen != other.frozen)
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
		if (Double.doubleToLongBits(lastPrice) != Double.doubleToLongBits(other.lastPrice))
			return false;
		if (Double.doubleToLongBits(openPositionProfit) != Double.doubleToLongBits(other.openPositionProfit))
			return false;
		if (Double.doubleToLongBits(openPositionProfitRatio) != Double.doubleToLongBits(other.openPositionProfitRatio))
			return false;
		if (Double.doubleToLongBits(openPrice) != Double.doubleToLongBits(other.openPrice))
			return false;
		if (Double.doubleToLongBits(openPriceDiff) != Double.doubleToLongBits(other.openPriceDiff))
			return false;
		if (position != other.position)
			return false;
		if (Double.doubleToLongBits(positionProfit) != Double.doubleToLongBits(other.positionProfit))
			return false;
		if (Double.doubleToLongBits(positionProfitRatio) != Double.doubleToLongBits(other.positionProfitRatio))
			return false;
		if (Double.doubleToLongBits(price) != Double.doubleToLongBits(other.price))
			return false;
		if (Double.doubleToLongBits(priceDiff) != Double.doubleToLongBits(other.priceDiff))
			return false;
		if (rtAccountID == null) {
			if (other.rtAccountID != null)
				return false;
		} else if (!rtAccountID.equals(other.rtAccountID))
			return false;
		if (rtPositionID == null) {
			if (other.rtPositionID != null)
				return false;
		} else if (!rtPositionID.equals(other.rtPositionID))
			return false;
		if (rtSymbol == null) {
			if (other.rtSymbol != null)
				return false;
		} else if (!rtSymbol.equals(other.rtSymbol))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (tdFrozen != other.tdFrozen)
			return false;
		if (tdPosition != other.tdPosition)
			return false;
		if (Double.doubleToLongBits(useMargin) != Double.doubleToLongBits(other.useMargin))
			return false;
		if (ydFrozen != other.ydFrozen)
			return false;
		if (ydPosition != other.ydPosition)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Position [gatewayID=" + gatewayID + ", gatewayDisplayName=" + gatewayDisplayName + ", accountID="
				+ accountID + ", rtAccountID=" + rtAccountID + ", rtPositionID=" + rtPositionID + ", symbol=" + symbol
				+ ", exchange=" + exchange + ", rtSymbol=" + rtSymbol + ", contractName=" + contractName
				+ ", contractSize=" + contractSize + ", direction=" + direction + ", position=" + position + ", frozen="
				+ frozen + ", ydPosition=" + ydPosition + ", ydFrozen=" + ydFrozen + ", tdPosition=" + tdPosition
				+ ", tdFrozen=" + tdFrozen + ", lastPrice=" + lastPrice + ", price=" + price + ", priceDiff="
				+ priceDiff + ", openPrice=" + openPrice + ", openPriceDiff=" + openPriceDiff + ", positionProfit="
				+ positionProfit + ", positionProfitRatio=" + positionProfitRatio + ", openPositionProfit="
				+ openPositionProfit + ", openPositionProfitRatio=" + openPositionProfitRatio + ", useMargin="
				+ useMargin + ", exchangeMargin=" + exchangeMargin + ", contractValue=" + contractValue + "]";
	}
	
}
