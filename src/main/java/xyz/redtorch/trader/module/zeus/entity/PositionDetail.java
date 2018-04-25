package xyz.redtorch.trader.module.zeus.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Trade;

/**
 * @author sun0x00@gmail.com
 */
public class PositionDetail implements Serializable {

	private static final long serialVersionUID = 6210968936139034645L;

	private HashSet<String> WORKING_STATUS = new HashSet<String>() {
		private static final long serialVersionUID = 909683985291870766L;
		{
			add(RtConstant.STATUS_UNKNOWN);
			add(RtConstant.STATUS_NOTTRADED);
			add(RtConstant.STATUS_PARTTRADED);
		}
	};

	private String tradingDay;
	private String rtSymbol;
	private String gatewayID;
	private String strategyName;
	private String strategyID;

	private int contractSize;
	private String exchange;

	private double pnl;
	private double openContractValue;

	private int longPos;
	private double longPnl;
	private double longOpenContractValue;
	private double longPrice;

	private int longYd;
	private int longTd;
	private int longPosFrozen;
	private int longYdFrozen;
	private int longTdFrozen;
	private int longOpenFrozen;

	private int shortPos;
	private double shortPnl;
	private double shortOpenContractValue;
	private double shortPrice;
	private int shortYd;
	private int shortTd;
	private int shortPosFrozen;
	private int shortYdFrozen;
	private int shortTdFrozen;
	private int shortOpenFrozen;

	private double lastPrice;

	private HashMap<String, Order> workingOrderMap = new HashMap<>();

	public PositionDetail(String rtSymbol, String gatewayID, String tradingDay, String strategyName,
			String strategyID, String exchange, int contractSize) {
		this.rtSymbol = rtSymbol;
		this.gatewayID = gatewayID;
		this.tradingDay = tradingDay;
		this.strategyName = strategyName;
		this.strategyID = strategyID;
		this.exchange = exchange;
		this.contractSize = contractSize;
	}
	
	public PositionDetail() {}
	
	public HashSet<String> getWORKING_STATUS() {
		return WORKING_STATUS;
	}

	public String getTradingDay() {
		return tradingDay;
	}

	public void setTradingDay(String tradingDay) {
		this.tradingDay = tradingDay;
	}

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

	public String getStrategyName() {
		return strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public String getStrategyID() {
		return strategyID;
	}

	public void setStrategyID(String strategyID) {
		this.strategyID = strategyID;
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

	public double getPnl() {
		return pnl;
	}

	public void setPnl(double pnl) {
		this.pnl = pnl;
	}

	public double getOpenContractValue() {
		return openContractValue;
	}

	public void setOpenContractValue(double openContractValue) {
		this.openContractValue = openContractValue;
	}

	public int getLongPos() {
		return longPos;
	}

	public void setLongPos(int longPos) {
		this.longPos = longPos;
	}

	public double getLongPnl() {
		return longPnl;
	}

	public void setLongPnl(double longPnl) {
		this.longPnl = longPnl;
	}

	public double getLongOpenContractValue() {
		return longOpenContractValue;
	}

	public void setLongOpenContractValue(double longOpenContractValue) {
		this.longOpenContractValue = longOpenContractValue;
	}

	public double getLongPrice() {
		return longPrice;
	}

	public void setLongPrice(double longPrice) {
		this.longPrice = longPrice;
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

	public int getLongOpenFrozen() {
		return longOpenFrozen;
	}

	public void setLongOpenFrozen(int longOpenFrozen) {
		this.longOpenFrozen = longOpenFrozen;
	}

	public int getShortPos() {
		return shortPos;
	}

	public void setShortPos(int shortPos) {
		this.shortPos = shortPos;
	}

	public double getShortPnl() {
		return shortPnl;
	}

	public void setShortPnl(double shortPnl) {
		this.shortPnl = shortPnl;
	}

	public double getShortOpenContractValue() {
		return shortOpenContractValue;
	}

	public void setShortOpenContractValue(double shortOpenContractValue) {
		this.shortOpenContractValue = shortOpenContractValue;
	}

	public double getShortPrice() {
		return shortPrice;
	}

	public void setShortPrice(double shortPrice) {
		this.shortPrice = shortPrice;
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

	public int getShortOpenFrozen() {
		return shortOpenFrozen;
	}

	public void setShortOpenFrozen(int shortOpenFrozen) {
		this.shortOpenFrozen = shortOpenFrozen;
	}

	public double getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
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
			}else if (RtConstant.OFFSET_CLOSETODAY.equals(trade.getOffset())) {// 平今
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
			}else if (RtConstant.OFFSET_CLOSETODAY.equals(trade.getOffset())) {// 平今
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
		if (WORKING_STATUS.contains(order.getStatus())) {
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
		// orderReq如果存在则跳过
		if(workingOrderMap.containsKey(rtOrderID)) {
			return;
		}
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
        pnl = longPnl + shortPnl;

        longOpenContractValue = longPos * contractSize * longPrice;
        shortOpenContractValue = shortPos * contractSize * shortPrice;
        openContractValue = longOpenContractValue + shortOpenContractValue;
    }
	
	/**
	 * 计算持仓均价（基于成交数据）
	 * @param trade
	 */
	public void calculatePrice(Trade trade) {
        // 只有开仓会影响持仓均价
        if(RtConstant.OFFSET_OPEN.equals(trade.getOffset())) {
        	double cost = 0;
        	int newPos = 0;
            if(RtConstant.DIRECTION_LONG.equals(trade.getDirection())) {
            	cost =  longPrice *  longPos;
                cost += trade.getVolume() * trade.getPrice();
                newPos = longPos + trade.getVolume();
                if(newPos>0) {
                    longPrice = cost / newPos;
                } else {
                    longPrice = 0;
                }
            }else {
                cost = shortPrice * shortPos;
                cost += trade.getVolume() * trade.getPrice();
                newPos = shortPos + trade.getVolume();
                if(newPos>0) {
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
		shortOpenFrozen = 0;
		longOpenFrozen = 0;

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
				} else if (RtConstant.OFFSET_OPEN.equals(order.getOffset())) {// 开仓
					if (WORKING_STATUS.contains(order.getStatus())) {
						longOpenFrozen += frozenVolume;
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
				} else if (RtConstant.OFFSET_OPEN.equals(order.getOffset())) {// 开仓
					if (WORKING_STATUS.contains(order.getStatus())) {
						shortOpenFrozen += frozenVolume;
					}
				}
			}
			// 汇总今昨冻结
			longPosFrozen = longYdFrozen + longTdFrozen;
			shortPosFrozen = shortYdFrozen + shortTdFrozen;
		}

	}

}
