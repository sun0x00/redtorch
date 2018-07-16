package xyz.redtorch.trader.module.zeus.entity;

import java.io.Serializable;
import java.util.HashMap;

import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Trade;

/**
 * @author sun0x00@gmail.com
 */
public class ContractPositionDetail implements Serializable{

	private static final long serialVersionUID = 7704834003824888658L;
	private String tradingDay;
	private String rtSymbol;
	private String strategyName;
	private String strategyID;

	private String exchange;
	private int contractSize;

	private boolean isLocked = false; // 总锁仓标记
	private boolean ydIsLocked = false; // 昨日锁仓标记
	private boolean tdIsLocked = false; // 今日锁仓标记
	private boolean mixLocked = false; // 混合锁仓标记

	private int pos; // 总仓位
	private int ydPos; // 昨仓
	private int tdPos; // 今仓
	private double pnl; // 总收益
	private double openContractValue; // 总合约价值

	private int longPos; // 多头仓位
	private int longYd; // 多头昨日仓位
	private int longTd; // 多头今日仓位
	private int longPosFrozen; // 多头仓位冻结（平仓未成交）
	private int longYdFrozen; // 多头昨日仓位冻结（平仓未成交）
	private int longTdFrozen; // 多头今日仓位冻结（平仓未成交）
	private int longOpenFrozen; // 多头开仓冻结（开仓未成交）
	private double longPnl; // 多头收益
	private double longOpenContractValue; // 多头合约价值

	private int shortPos; // 空头仓位
	private int shortYd; // 空头昨日仓位
	private int shortTd; // 空头今日仓位
	private int shortPosFrozen; // 空头仓位冻结（平仓未成交）
	private int shortYdFrozen; // 空头昨日仓位冻结（平仓未成交）
	private int shortTdFrozen; // 空头今日仓位冻结（平仓未成交）
	private int shortOpenFrozen; // 空头开仓冻结（开仓未成交）
	private double shortPnl; // 空头收益
	private double shortOpenContractValue; // 空头合约价值

	private HashMap<String, PositionDetail> positionDetailMap = new HashMap<>(); // 各接口持仓详细

	/**
	 * 有参构造方法,需要传入必要信息
	 * @param rtSymbol
	 * @param gatewayID
	 * @param tradeDay
	 * @param strategyName
	 * @param strategyID
	 * @param exchange
	 * @param contractSize
	 */
	public ContractPositionDetail(String rtSymbol, String tradingDay, String strategyName,
			String strategyID, String exchange, int contractSize) {
		this.rtSymbol = rtSymbol;
		this.tradingDay = tradingDay;
		this.strategyName = strategyName;
		this.strategyID = strategyID;
		this.exchange = exchange;
		this.contractSize = contractSize;
	}
	
	/**
	 * 无参构造方法
	 */
	public ContractPositionDetail() {
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

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public int getContractSize() {
		return contractSize;
	}

	public void setContractSize(int contractSize) {
		this.contractSize = contractSize;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public boolean isYdIsLocked() {
		return ydIsLocked;
	}

	public void setYdIsLocked(boolean ydIsLocked) {
		this.ydIsLocked = ydIsLocked;
	}

	public boolean isTdIsLocked() {
		return tdIsLocked;
	}

	public void setTdIsLocked(boolean tdIsLocked) {
		this.tdIsLocked = tdIsLocked;
	}

	public boolean isMixLocked() {
		return mixLocked;
	}

	public void setMixLocked(boolean mixLocked) {
		this.mixLocked = mixLocked;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public int getYdPos() {
		return ydPos;
	}

	public void setYdPos(int ydPos) {
		this.ydPos = ydPos;
	}

	public int getTdPos() {
		return tdPos;
	}

	public void setTdPos(int tdPos) {
		this.tdPos = tdPos;
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

	public int getShortOpenFrozen() {
		return shortOpenFrozen;
	}

	public void setShortOpenFrozen(int shortOpenFrozen) {
		this.shortOpenFrozen = shortOpenFrozen;
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

	public HashMap<String, PositionDetail> getPositionDetailMap() {
		return positionDetailMap;
	}

	public void setPositionDetailMap(HashMap<String, PositionDetail> positionDetailMap) {
		this.positionDetailMap = positionDetailMap;
	}

	/**
	 * 计算持仓情况
	 */
	public void calculatePosition() {

		int tmpLongPos = 0;
		int tmpLongYd = 0;
		int tmpLongTd = 0;
		int tmpLongPosFrozen = 0;
		int tmpLongYdFrozen = 0;
		int tmpLongTdFrozen = 0;
		int tmpLongOpenFrozen = 0;
		float tmpLongPnl = 0;
		float tmpLongOpenContractValue = 0;

		int tmpShortPos = 0;
		int tmpShortYd = 0;
		int tmpShortTd = 0;
		int tmpShortPosFrozen = 0;
		int tmpShortYdFrozen = 0;
		int tmpShortTdFrozen = 0;
		int tmpShortOpenFrozen = 0;
		float tmpShortPnl = 0;
		float tmpShortOpenContractValue = 0;

		for (String gatewayName : positionDetailMap.keySet()) {
			PositionDetail positionDetail = positionDetailMap.get(gatewayName);
			positionDetail.calculatePosition();
			positionDetail.calculatePnl();

			tmpLongPos += positionDetail.getLongPos();
			tmpLongYd += positionDetail.getLongYd();
			tmpLongTd += positionDetail.getLongTd();
			tmpLongPosFrozen += positionDetail.getLongPosFrozen();
			tmpLongYdFrozen += positionDetail.getLongYdFrozen();
			tmpLongTdFrozen += positionDetail.getLongTdFrozen();
			tmpLongOpenFrozen += positionDetail.getLongOpenFrozen();
			tmpLongPnl += positionDetail.getLongPnl();
			tmpLongOpenContractValue += positionDetail.getLongOpenContractValue();

			tmpShortPos += positionDetail.getShortPos();
			tmpShortYd += positionDetail.getShortYd();
			tmpShortTd += positionDetail.getShortTd();
			tmpShortPosFrozen += positionDetail.getShortPosFrozen();
			tmpShortYdFrozen += positionDetail.getShortYdFrozen();
			tmpShortTdFrozen += positionDetail.getShortTdFrozen();
			tmpShortOpenFrozen += positionDetail.getShortOpenFrozen();
			tmpShortPnl += positionDetail.getShortPnl();
			tmpShortOpenContractValue += positionDetail.getShortOpenContractValue();
		}
		longPos = tmpLongPos;
		longYd = tmpLongYd;
		longTd = tmpLongTd;
		longPosFrozen = tmpLongPosFrozen;
		longYdFrozen = tmpLongYdFrozen;
		longTdFrozen = tmpLongTdFrozen;
		longOpenFrozen = tmpLongOpenFrozen;

		shortPos = tmpShortPos;
		shortYd = tmpShortYd;
		shortTd = tmpShortTd;
		shortPosFrozen = tmpShortPosFrozen;
		shortYdFrozen = tmpShortYdFrozen;
		shortTdFrozen = tmpShortTdFrozen;
		shortOpenFrozen = tmpShortOpenFrozen;
		tdPos = longTd - shortTd;
		ydPos = longYd - shortYd;

		longPnl = tmpLongPnl;
		shortPnl = tmpShortPnl;
		pnl = longPnl + shortPnl;
		longOpenContractValue = tmpLongOpenContractValue;
		shortOpenContractValue = tmpShortOpenContractValue;
		openContractValue = longOpenContractValue + shortOpenContractValue;

		pos = longPos - shortPos;

		if (pos == 0 && longPos != 0) {
			isLocked = false;
		} else {
			isLocked = false;
		}

		if (tdPos == 0 && longTd != 0) {
			tdIsLocked = true;
		} else {
			tdIsLocked = false;
		}

		if (ydPos == 0 && longYd != 0) {
			ydIsLocked = true;
		} else {
			ydIsLocked = false;
		}

		if (isLocked && !tdIsLocked && !ydIsLocked && longPos != 0) {
			mixLocked = true;
		} else {
			mixLocked = false;
		}
	}
	
	/**
	 * 更新委托请求
	 * @param orderReq
	 * @param rtOrderID
	 */
	public void updateOrderReq(OrderReq orderReq, String rtOrderID) {
		String gatewayID = orderReq.getGatewayID();
		PositionDetail positionDetail;
		if (positionDetailMap.containsKey(gatewayID)) {
			positionDetail = positionDetailMap.get(gatewayID);
		} else {
			positionDetail = new PositionDetail(rtSymbol, gatewayID, tradingDay, strategyName, strategyID, exchange,
					contractSize);
			positionDetailMap.put(gatewayID, positionDetail);
		}

		positionDetail.updateOrderReq(orderReq, rtOrderID);

		calculatePosition();

	}

	/**
	 * 更新委托
	 * @param order
	 */
	public void updateOrder(Order order) {
		String gatewayID = order.getGatewayID();
		PositionDetail positionDetail;
		if (positionDetailMap.containsKey(gatewayID)) {
			positionDetail = positionDetailMap.get(gatewayID);
		} else {
			positionDetail = new PositionDetail(rtSymbol, gatewayID, tradingDay, strategyName, strategyID, exchange,
					contractSize);
			positionDetailMap.put(gatewayID, positionDetail);
		}

		positionDetail.updateOrder(order);

		calculatePosition();

	}
	
	/**
	 * 更新成交
	 * @param trade
	 */
	public void updateTrade(Trade trade) {
		String gatewayID = trade.getGatewayID();
		PositionDetail positionDetail;
		if (positionDetailMap.containsKey(gatewayID)) {
			positionDetail = positionDetailMap.get(gatewayID);
		} else {
			positionDetail = new PositionDetail(rtSymbol, gatewayID, tradingDay, strategyName, strategyID, exchange,
					contractSize);
			positionDetailMap.put(gatewayID, positionDetail);
		}

		positionDetail.updateTrade(trade);

		calculatePosition();

	}
	
	/**
	 * 更新合约最后价格
	 * @param lastPrice
	 */
	public void updateLastPrice(double lastPrice) {
		for(PositionDetail positionDetail:positionDetailMap.values()) {
			positionDetail.updateLastPrice(lastPrice);
		}
		
		calculatePosition();
	}

	@Override
	public String toString() {
		return "ContractPositionDetail [tradingDay=" + tradingDay + ", rtSymbol=" + rtSymbol + ", strategyName="
				+ strategyName + ", strategyID=" + strategyID + ", exchange=" + exchange + ", contractSize="
				+ contractSize + ", isLocked=" + isLocked + ", ydIsLocked=" + ydIsLocked + ", tdIsLocked=" + tdIsLocked
				+ ", mixLocked=" + mixLocked + ", pos=" + pos + ", ydPos=" + ydPos + ", tdPos=" + tdPos + ", pnl=" + pnl
				+ ", openContractValue=" + openContractValue + ", longPos=" + longPos + ", longYd=" + longYd
				+ ", longTd=" + longTd + ", longPosFrozen=" + longPosFrozen + ", longYdFrozen=" + longYdFrozen
				+ ", longTdFrozen=" + longTdFrozen + ", longOpenFrozen=" + longOpenFrozen + ", longPnl=" + longPnl
				+ ", longOpenContractValue=" + longOpenContractValue + ", shortPos=" + shortPos + ", shortYd=" + shortYd
				+ ", shortTd=" + shortTd + ", shortPosFrozen=" + shortPosFrozen + ", shortYdFrozen=" + shortYdFrozen
				+ ", shortTdFrozen=" + shortTdFrozen + ", shortOpenFrozen=" + shortOpenFrozen + ", shortPnl=" + shortPnl
				+ ", shortOpenContractValue=" + shortOpenContractValue + ", positionDetailMap=" + positionDetailMap
				+ "]";
	}
}
