package xyz.redtorch.core.entity;

import java.io.Serializable;
import java.util.HashMap;

import xyz.redtorch.core.base.RtConstant;

/**
 * @author sun0x00@gmail.com
 */
public class LocalPositionDetail implements Serializable {

	private static final long serialVersionUID = 3912578572233290138L;

	public LocalPositionDetail(String gatewayID, String gatewayDisplayName, String accountID, String rtAccountID,
			String exchange, String rtSymbol, String symbol, String contractName, int contractSize) {

		this.rtSymbol = rtSymbol;
		this.gatewayID = gatewayID;
		this.gatewayDisplayName = gatewayDisplayName;
		this.contractName = contractName;
		this.contractSize = contractSize;
		this.exchange = exchange;
		this.symbol = symbol;
		this.accountID = accountID;
		this.rtAccountID = rtAccountID;
	}

	public LocalPositionDetail() {
	}

	private String rtSymbol;
	private String gatewayID;
	private String gatewayDisplayName;
	private String accountID;
	private String rtAccountID;

	private String symbol;
	private String contractName;
	private int contractSize;
	private String exchange;

	private int longPos;
	private int longYd;
	private int longTd;
	private int longPosFrozen;
	private int longYdFrozen;
	private int longTdFrozen;
	private double longPnl;
	private double longPrice;

	private int shortPos;
	private int shortYd;
	private int shortTd;
	private int shortPosFrozen;
	private int shortYdFrozen;
	private int shortTdFrozen;
	private double shortPnl;
	private double shortPrice;

	private double lastPrice;

	private HashMap<String, Order> workingOrderMap = new HashMap<>();

	public String getRtSymbol() {
		return rtSymbol;
	}

	public void setRtSymbol(String rtSymbol) {
		this.rtSymbol = rtSymbol;
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

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
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

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public int getLongPos() {
		return longPos;
	}

	public void setLongPos(int longPos) {
		this.longPos = longPos;
	}

	public int getLongYd() {
		return longYd;
	}

	public void setLongYd(int longYd) {
		this.longYd = longYd;
	}

	public int getLongTd() {
		return longTd;
	}

	public void setLongTd(int longTd) {
		this.longTd = longTd;
	}

	public int getLongPosFrozen() {
		return longPosFrozen;
	}

	public void setLongPosFrozen(int longPosFrozen) {
		this.longPosFrozen = longPosFrozen;
	}

	public int getLongYdFrozen() {
		return longYdFrozen;
	}

	public void setLongYdFrozen(int longYdFrozen) {
		this.longYdFrozen = longYdFrozen;
	}

	public int getLongTdFrozen() {
		return longTdFrozen;
	}

	public void setLongTdFrozen(int longTdFrozen) {
		this.longTdFrozen = longTdFrozen;
	}

	public double getLongPnl() {
		return longPnl;
	}

	public void setLongPnl(double longPnl) {
		this.longPnl = longPnl;
	}

	public double getLongPrice() {
		return longPrice;
	}

	public void setLongPrice(double longPrice) {
		this.longPrice = longPrice;
	}

	public int getShortPos() {
		return shortPos;
	}

	public void setShortPos(int shortPos) {
		this.shortPos = shortPos;
	}

	public int getShortYd() {
		return shortYd;
	}

	public void setShortYd(int shortYd) {
		this.shortYd = shortYd;
	}

	public int getShortTd() {
		return shortTd;
	}

	public void setShortTd(int shortTd) {
		this.shortTd = shortTd;
	}

	public int getShortPosFrozen() {
		return shortPosFrozen;
	}

	public void setShortPosFrozen(int shortPosFrozen) {
		this.shortPosFrozen = shortPosFrozen;
	}

	public int getShortYdFrozen() {
		return shortYdFrozen;
	}

	public void setShortYdFrozen(int shortYdFrozen) {
		this.shortYdFrozen = shortYdFrozen;
	}

	public int getShortTdFrozen() {
		return shortTdFrozen;
	}

	public void setShortTdFrozen(int shortTdFrozen) {
		this.shortTdFrozen = shortTdFrozen;
	}

	public double getShortPnl() {
		return shortPnl;
	}

	public void setShortPnl(double shortPnl) {
		this.shortPnl = shortPnl;
	}

	public double getShortPrice() {
		return shortPrice;
	}

	public void setShortPrice(double shortPrice) {
		this.shortPrice = shortPrice;
	}

	public double getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public HashMap<String, Order> getWorkingOrderMap() {
		return workingOrderMap;
	}

	public void setWorkingOrderMap(HashMap<String, Order> workingOrderMap) {
		this.workingOrderMap = workingOrderMap;
	}

	/**
	 * 成交更新
	 * 
	 * @param trade
	 */
	public void updateTrade(Trade trade) {

		if (RtConstant.DIRECTION_LONG.equals(trade.getDirection())) {// 多头
			if (RtConstant.OFFSET_OPEN.equals(trade.getOffset())) {// 开仓
				longTd += trade.getVolume();
			} else if (RtConstant.OFFSET_CLOSETODAY.equals(trade.getOffset())) {// 平今
				shortTd -= trade.getVolume();
			} else if (RtConstant.OFFSET_CLOSEYESTERDAY.equals(trade.getOffset())) {// 平昨
				shortYd -= trade.getVolume();
			} else if (RtConstant.OFFSET_CLOSE.equals(trade.getOffset())) {// 平仓
				if (RtConstant.EXCHANGE_SHFE.equals(exchange)) {// 上期所等同于平昨
					shortYd -= trade.getVolume();
				} else {
					// 非上期所,优先平今
					shortTd -= trade.getVolume();
					if (shortTd < 0) {
						shortYd += shortTd;
						shortTd = 0;
					}
				}
			}

		} else if (RtConstant.DIRECTION_SHORT.equals(trade.getDirection())) { // 空头
			// 开仓
			if (RtConstant.OFFSET_OPEN.equals(trade.getOffset())) {
				shortTd += trade.getVolume();
			} else if (RtConstant.OFFSET_CLOSETODAY.equals(trade.getOffset())) {// 平今
				longTd -= trade.getVolume();
			} else if (RtConstant.OFFSET_CLOSEYESTERDAY.equals(trade.getOffset())) {// 平昨
				longYd -= trade.getVolume();
			} else if (RtConstant.OFFSET_CLOSE.equals(trade.getOffset())) {// 平仓
				if (RtConstant.EXCHANGE_SHFE.equals(exchange)) {// 上期所等同于平昨
					longYd -= trade.getVolume();
				} else {// 非上期所,优先平今
					longTd -= trade.getVolume();
					if (longTd < 0) {
						longYd += longTd;
						longTd = 0;
					}
				}
			}
		}
		// 汇总今昨
		calculatePrice(trade);
		calculatePosition();
		calculatePnl();
	}

	/**
	 * 委托更新
	 * 
	 * @param order
	 */
	public void updateOrder(Order order) {
		// 将活动委托缓存下来
		if (RtConstant.STATUS_WORKING.contains(order.getStatus())) {
			workingOrderMap.put(order.getRtOrderID(), order);

			// 移除缓存中已经完成的委托
		} else {
			if (workingOrderMap.containsKey(order.getRtOrderID())) {
				workingOrderMap.remove(order.getRtOrderID());
			}
		}

		// 计算冻结
		calculateFrozen();
	}

	/**
	 * 发单更新
	 * 
	 * @param orderReq
	 */
	public void updateOrderReq(OrderReq orderReq, String rtOrderID) {
		// 基于请求创建委托对象
		Order order = new Order();
		order.setRtSymbol(orderReq.getRtSymbol());
		order.setSymbol(orderReq.getSymbol());
		order.setExchange(orderReq.getExchange());
		order.setOffset(orderReq.getOffset());
		order.setDirection(orderReq.getDirection());
		order.setTotalVolume(orderReq.getVolume());
		order.setStatus(RtConstant.STATUS_UNKNOWN);
		order.setGatewayID(orderReq.getGatewayID());
		order.setRtOrderID(rtOrderID);

		workingOrderMap.put(rtOrderID, order);

		calculateFrozen();

	}

	/**
	 * 价格更新
	 * 
	 * @param lastPrice
	 */
	public void updateLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
		calculatePnl();
	}

	/**
	 * 计算持仓盈亏
	 */
	public void calculatePnl() {
		longPnl = longPos * (lastPrice - longPrice) * contractSize;
		shortPnl = shortPos * (shortPrice - lastPrice) * contractSize;
	}

	/**
	 * 计算持仓均价（基于成交数据）
	 * 
	 * @param trade
	 */
	public void calculatePrice(Trade trade) {
		// 只有开仓会影响持仓均价
		if (RtConstant.OFFSET_OPEN.equals(trade.getOffset())) {
			double cost = 0;
			int newPos = 0;
			if (RtConstant.DIRECTION_LONG.equals(trade.getDirection())) {
				cost = longPrice * longPos;
				cost += trade.getVolume() * trade.getPrice();
				newPos = longPos + trade.getVolume();
				if (newPos > 0) {
					longPrice = cost / newPos;
				} else {
					longPrice = 0;
				}
			} else {
				cost = shortPrice * shortPos;
				cost += trade.getVolume() * trade.getPrice();
				newPos = shortPos + trade.getVolume();
				if (newPos > 0) {
					shortPrice = cost / newPos;
				} else {
					shortPrice = 0;
				}
			}
		}

	}

	public void calculatePosition() {
		longPos = longTd + longYd;
		shortPos = shortTd + shortYd;
	}

	/**
	 * 计算冻结
	 */
	public void calculateFrozen() {
		// 清空冻结数据
		longPosFrozen = 0;
		longYdFrozen = 0;
		longTdFrozen = 0;
		shortPosFrozen = 0;
		shortYdFrozen = 0;
		shortTdFrozen = 0;

		int frozenVolume = 0;

		// 遍历统计
		for (Order order : workingOrderMap.values()) {
			// 计算剩余冻结量
			frozenVolume = order.getTotalVolume() - order.getTradedVolume();
			if (RtConstant.DIRECTION_LONG.equals(order.getDirection())) {// 多头委托
				if (RtConstant.OFFSET_CLOSETODAY.equals(order.getOffset())) {// 平今
					shortTdFrozen += frozenVolume;
				} else if (RtConstant.OFFSET_CLOSEYESTERDAY.equals(order.getOffset())) {// 平昨
					shortYdFrozen += frozenVolume;
				} else if (RtConstant.OFFSET_CLOSE.equals(order.getOffset())) {// 平仓
					shortTdFrozen += frozenVolume;
					if (shortTdFrozen > shortTd) {
						shortYdFrozen += (shortTdFrozen - shortTd);
						shortTdFrozen = shortTd;
					}
				}
			} else if (RtConstant.DIRECTION_SHORT.equals(order.getDirection())) {// 空头委托
				if (RtConstant.OFFSET_CLOSETODAY.equals(order.getOffset())) { // 平今
					longTdFrozen += frozenVolume;
				} else if (RtConstant.OFFSET_CLOSEYESTERDAY.equals(order.getOffset())) { // 平昨
					longYdFrozen += frozenVolume;
				} else if (RtConstant.OFFSET_CLOSE.equals(order.getOffset())) {// 平仓
					longTdFrozen += frozenVolume;
					if (longTdFrozen > longTd) {
						longYdFrozen += (longTdFrozen - longTd);
						longTdFrozen = longTd;
					}
				}
			}
			// 汇总今昨冻结
			longPosFrozen = longYdFrozen + longTdFrozen;
			shortPosFrozen = shortYdFrozen + shortTdFrozen;
		}

	}

	/**
	 * 通过Position推送更新
	 * 
	 * @param position
	 */
	public void updatePosition(Position position) {

		if (RtConstant.DIRECTION_LONG.equals(position.getDirection())) {
			longPos = position.getPosition();
			longYd = position.getYdPosition();
			longTd = longPos - longYd;
			longPnl = position.getPositionProfit();
			longPrice = position.getPrice();

		} else if (RtConstant.DIRECTION_SHORT.equals(position.getDirection())) {
			shortPos = position.getPosition();
			shortYd = position.getYdPosition();
			shortTd = shortPos - shortYd;
			shortPnl = position.getPositionProfit();
			shortPrice = position.getPrice();
		}

	}

	@Override
	public String toString() {
		return "LocalPositionDetail [rtSymbol=" + rtSymbol + ", gatewayID=" + gatewayID + ", gatewayDisplayName="
				+ gatewayDisplayName + ", symbol=" + symbol + ", contractName=" + contractName + ", contractSize="
				+ contractSize + ", exchange=" + exchange + ", longPos=" + longPos + ", longYd=" + longYd + ", longTd="
				+ longTd + ", longPosFrozen=" + longPosFrozen + ", longYdFrozen=" + longYdFrozen + ", longTdFrozen="
				+ longTdFrozen + ", longPnl=" + longPnl + ", longPrice=" + longPrice + ", shortPos=" + shortPos
				+ ", shortYd=" + shortYd + ", shortTd=" + shortTd + ", shortPosFrozen=" + shortPosFrozen
				+ ", shortYdFrozen=" + shortYdFrozen + ", shortTdFrozen=" + shortTdFrozen + ", shortPnl=" + shortPnl
				+ ", shortPrice=" + shortPrice + ", lastPrice=" + lastPrice + ", workingOrderMap=" + workingOrderMap
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contractName == null) ? 0 : contractName.hashCode());
		result = prime * result + contractSize;
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + ((gatewayDisplayName == null) ? 0 : gatewayDisplayName.hashCode());
		result = prime * result + ((gatewayID == null) ? 0 : gatewayID.hashCode());
		long temp;
		temp = Double.doubleToLongBits(lastPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longPnl);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + longPos;
		result = prime * result + longPosFrozen;
		temp = Double.doubleToLongBits(longPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + longTd;
		result = prime * result + longTdFrozen;
		result = prime * result + longYd;
		result = prime * result + longYdFrozen;
		result = prime * result + ((rtSymbol == null) ? 0 : rtSymbol.hashCode());
		temp = Double.doubleToLongBits(shortPnl);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + shortPos;
		result = prime * result + shortPosFrozen;
		temp = Double.doubleToLongBits(shortPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + shortTd;
		result = prime * result + shortTdFrozen;
		result = prime * result + shortYd;
		result = prime * result + shortYdFrozen;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((workingOrderMap == null) ? 0 : workingOrderMap.hashCode());
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
		LocalPositionDetail other = (LocalPositionDetail) obj;
		if (contractName == null) {
			if (other.contractName != null)
				return false;
		} else if (!contractName.equals(other.contractName))
			return false;
		if (contractSize != other.contractSize)
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
		if (Double.doubleToLongBits(lastPrice) != Double.doubleToLongBits(other.lastPrice))
			return false;
		if (Double.doubleToLongBits(longPnl) != Double.doubleToLongBits(other.longPnl))
			return false;
		if (longPos != other.longPos)
			return false;
		if (longPosFrozen != other.longPosFrozen)
			return false;
		if (Double.doubleToLongBits(longPrice) != Double.doubleToLongBits(other.longPrice))
			return false;
		if (longTd != other.longTd)
			return false;
		if (longTdFrozen != other.longTdFrozen)
			return false;
		if (longYd != other.longYd)
			return false;
		if (longYdFrozen != other.longYdFrozen)
			return false;
		if (rtSymbol == null) {
			if (other.rtSymbol != null)
				return false;
		} else if (!rtSymbol.equals(other.rtSymbol))
			return false;
		if (Double.doubleToLongBits(shortPnl) != Double.doubleToLongBits(other.shortPnl))
			return false;
		if (shortPos != other.shortPos)
			return false;
		if (shortPosFrozen != other.shortPosFrozen)
			return false;
		if (Double.doubleToLongBits(shortPrice) != Double.doubleToLongBits(other.shortPrice))
			return false;
		if (shortTd != other.shortTd)
			return false;
		if (shortTdFrozen != other.shortTdFrozen)
			return false;
		if (shortYd != other.shortYd)
			return false;
		if (shortYdFrozen != other.shortYdFrozen)
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (workingOrderMap == null) {
			if (other.workingOrderMap != null)
				return false;
		} else if (!workingOrderMap.equals(other.workingOrderMap))
			return false;
		return true;
	}

}
