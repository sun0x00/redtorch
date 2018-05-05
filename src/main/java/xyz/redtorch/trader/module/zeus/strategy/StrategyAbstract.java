package xyz.redtorch.trader.module.zeus.strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.engine.event.EventConstant;
import xyz.redtorch.trader.engine.event.FastEvent;
import xyz.redtorch.trader.engine.event.FastEventDynamicHandlerAbstract;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.zeus.ZeusConstant;
import xyz.redtorch.trader.module.zeus.ZeusEngine;
import xyz.redtorch.trader.module.zeus.entity.ContractPositionDetail;
import xyz.redtorch.trader.module.zeus.entity.StopOrder;
import xyz.redtorch.utils.CommonUtil;

/**
 * 策略基本实现抽象类
 * @author Administrator
 *
 */
public abstract class StrategyAbstract extends FastEventDynamicHandlerAbstract implements Strategy{
	private static final Logger log = LoggerFactory.getLogger(StrategyAbstract.class);

	protected String id; // 策略ID
	protected String name; // 策略名称
	protected String logStr; // 日志拼接字符串
	protected boolean initStatus = false; // 初始化状态
	protected boolean trading = false; // 交易开关

	protected ZeusEngine zeusEngine; // 策略引擎

	protected StrategySetting strategySetting; // 策略配置

	protected Map<String, ContractPositionDetail> contractPositionMap = new HashMap<>(); // 合约仓位维护

	protected Map<String, StopOrder> workingStopOrderMap = new HashMap<>(); // 本地停止单,停止单撤销后会被删除

	protected Map<String, Order> workingOrderMap = new HashMap<>(); // 委托单

	protected long stopOrderCount = 0L; // 停止单计数器

	protected HashSet<String> rtTradeIDSet = new HashSet<String>(); // 用于过滤可能重复的Trade推送

	/**
	 * 必须使用有参构造方法
	 * @param zeusEngine
	 * @param strategySetting
	 */
	public StrategyAbstract(ZeusEngine zeusEngine, StrategySetting strategySetting) {
		strategySetting.fixSetting();
		this.strategySetting = strategySetting;

		this.id = strategySetting.getStrategyID();
		this.name = strategySetting.getStrategyName();
		this.logStr = "策略-[" + name + "] ID-[" + id + "] >>> ";

		this.zeusEngine = zeusEngine;

	}

	@Override
	public void onEvent(final FastEvent fastEvent, final long sequence, final boolean endOfBatch) throws Exception {

		if (!subscribedEventSet.contains(fastEvent.getEvent())) {
			return;
		}
		// 判断消息类型
		if (EventConstant.EVENT_TICK.equals(fastEvent.getEventType())) {
			Tick tick = fastEvent.getTick();
			processTick(tick);

		} else if (EventConstant.EVENT_TRADE.equals(fastEvent.getEventType())) {
			Trade trade = fastEvent.getTrade();
			processTrade(trade);
		} else if (EventConstant.EVENT_ORDER.equals(fastEvent.getEventType())) {
			Order order = fastEvent.getOrder();
			processOrder(order);
		} else {
			log.warn("{} 未能识别的事件数据类型{}", logStr, JSON.toJSONString(fastEvent.getEvent()));
		}
	}

	/**
	 * 策略ID
	 * 
	 * @return
	 */
	@Override
	public String getID() {
		return id;
	}

	/**
	 * 获取策略名称
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * 快捷获取日志拼接字符串
	 * 
	 * @return
	 */
	@Override
	public String getLogStr() {
		return logStr;
	}

	@Override
	public boolean isInitStatus() {
		return initStatus;
	}

	@Override
	public boolean isTrading() {
		return trading;
	}

	@Override
	public int getEngineType() {
		return this.zeusEngine.getEngineType();
	}

	@Override
	public StrategySetting getStrategySetting() {
		return strategySetting;
	}


	@Override
	public void startTrading() {
		if (!initStatus) {
			log.warn("{} 策略尚未初始化,无法开始交易!", logStr);
			return;
		}

		if (trading) {
			log.warn("{} 策略正在运行,请勿重复操作!", logStr);
			return;
		}
		this.trading = true;
		try {
			onStartTrading();
			log.info("{} 开始交易", logStr);
		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onStartTrading发生异常,停止策略!!!", logStr, e);
		}
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onShutdown() {
		shutdownLatch.countDown();
	}

	/**
	 * 停止交易
	 */
	@Override
	public void stopTrading(boolean isException) {
		if (!trading) {
			log.warn("{} 策略已经停止,请勿重复操作!", logStr);
			return;
		}
		// 保存策略配置
		saveStrategySetting();
		this.trading = false;
		try {
			onStopTrading(isException);
		} catch (Exception e) {
			log.error("{} 策略停止后调用onStopTrading发生异常!", logStr, e);
		}
	}

	/**
	 * 初始化策略
	 */
	@Override
	public void init() {
		if (initStatus == true) {
			log.warn("{} 策略已经初始化,请勿重复操作!", logStr);
			return;
		}
		initStatus = true;
		try {
			onInit();
			log.info("{} 初始化", logStr);
		} catch (Exception e) {
			initStatus = false;
			log.error("{} 调用onInit发生异常!", logStr, e);
		}
	}

	@Override
	public void saveStrategySetting() {
		zeusEngine.asyncSaveStrategySetting(strategySetting);
	}
	
	@Override
	public void setVarValue(String key,String value) {
		strategySetting.getVarMap().put(key, value);
		saveStrategySetting();
	}
	
	@Override
	public Map<String, StopOrder> getWorkingStopOrderMap() {
		return workingStopOrderMap;
	}

	@Override
	public String sendOrder(String rtSymbol, String orderType, String priceType, double price, int volume,
			String gatewayID) {

		String symbol;
		String exchange;
		double priceTick = 0;
		if (zeusEngine.getEngineType() == ZeusConstant.ENGINE_TYPE_BACKTESTING) {
			String[] rtSymbolArray = rtSymbol.split("\\.");
			symbol = rtSymbolArray[0];
			exchange = rtSymbolArray[1];
			priceTick = strategySetting.getContractSetting(rtSymbol).getBacktestingPriceTick();
		} else {
			Contract contract = zeusEngine.getContract(rtSymbol, gatewayID);
			symbol = contract.getSymbol();
			exchange = contract.getExchange();
			priceTick = contract.getPriceTick();

		}

		OrderReq orderReq = new OrderReq();

		orderReq.setSymbol(symbol);
		orderReq.setExchange(exchange);
		orderReq.setRtSymbol(rtSymbol);
		orderReq.setPrice(CommonUtil.rountToPriceTick(priceTick, price));
		orderReq.setVolume(volume);
		orderReq.setGatewayID(gatewayID);

		orderReq.setPriceType(priceType);

		if (ZeusConstant.ORDER_BUY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_LONG);
			orderReq.setOffset(RtConstant.OFFSET_OPEN);

		} else if (ZeusConstant.ORDER_SELL.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_SHORT);
			orderReq.setOffset(RtConstant.OFFSET_CLOSE);

		} else if (ZeusConstant.ORDER_SHORT.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_SHORT);
			orderReq.setOffset(RtConstant.OFFSET_OPEN);

		} else if (ZeusConstant.ORDER_COVER.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_LONG);
			orderReq.setOffset(RtConstant.OFFSET_CLOSE);

		} else if (ZeusConstant.ORDER_SELLTODAY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_SHORT);
			orderReq.setOffset(RtConstant.OFFSET_CLOSETODAY);

		} else if (ZeusConstant.ORDER_SELLYESTERDAY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_SHORT);
			orderReq.setOffset(RtConstant.OFFSET_CLOSEYESTERDAY);

		} else if (ZeusConstant.ORDER_COVERTODAY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_LONG);
			orderReq.setOffset(RtConstant.OFFSET_CLOSETODAY);

		} else if (ZeusConstant.ORDER_COVERYESTERDAY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_LONG);
			orderReq.setOffset(RtConstant.OFFSET_CLOSEYESTERDAY);
		}

		String rtOrderID = zeusEngine.sendOrder(orderReq, this);

		if(contractPositionMap.containsKey(rtSymbol)) {
			contractPositionMap.get(rtSymbol).updateOrderReq(orderReq, rtOrderID);
		}

		return rtOrderID;
	}

	@Override
	public String sendStopOrder(String rtSymbol, String orderType, String priceType, double price, int volume,
			String gatewayID, Strategy strategy) {

		String stopOrderID = ZeusConstant.STOPORDERPREFIX + stopOrderCount + "." + id + "." + gatewayID;

		StopOrder stopOrder = new StopOrder();
		stopOrder.setRtSymbol(rtSymbol);
		stopOrder.setOrderType(orderType);
		double priceTick = 0;

		if (zeusEngine.getEngineType() == ZeusConstant.ENGINE_TYPE_BACKTESTING) {
			priceTick = strategySetting.getContractSetting(rtSymbol).getBacktestingPriceTick();
		} else {
			priceTick = zeusEngine.getPriceTick(rtSymbol, gatewayID);
		}
		stopOrder.setPrice(CommonUtil.rountToPriceTick(priceTick, price));
		stopOrder.setVolume(volume);
		stopOrder.setStopOrderID(stopOrderID);
		stopOrder.setStatus(ZeusConstant.STOPORDER_WAITING);
		stopOrder.setGatewayID(gatewayID);
		stopOrder.setPriceType(priceType);

		if (ZeusConstant.ORDER_BUY.equals(orderType)) {

			stopOrder.setDirection(RtConstant.DIRECTION_LONG);
			stopOrder.setOffset(RtConstant.OFFSET_OPEN);

		} else if (ZeusConstant.ORDER_SELL.equals(orderType)) {

			stopOrder.setDirection(RtConstant.DIRECTION_SHORT);
			stopOrder.setOffset(RtConstant.OFFSET_CLOSE);

		} else if (ZeusConstant.ORDER_SHORT.equals(orderType)) {

			stopOrder.setDirection(RtConstant.DIRECTION_SHORT);
			stopOrder.setOffset(RtConstant.OFFSET_OPEN);

		} else if (ZeusConstant.ORDER_COVER.equals(orderType)) {

			stopOrder.setDirection(RtConstant.DIRECTION_LONG);
			stopOrder.setOffset(RtConstant.OFFSET_CLOSE);

		} else if (ZeusConstant.ORDER_SELLTODAY.equals(orderType)) {

			stopOrder.setDirection(RtConstant.DIRECTION_SHORT);
			stopOrder.setOffset(RtConstant.OFFSET_CLOSETODAY);

		} else if (ZeusConstant.ORDER_SELLYESTERDAY.equals(orderType)) {

			stopOrder.setDirection(RtConstant.DIRECTION_SHORT);
			stopOrder.setOffset(RtConstant.OFFSET_CLOSEYESTERDAY);

		} else if (ZeusConstant.ORDER_COVERTODAY.equals(orderType)) {

			stopOrder.setDirection(RtConstant.DIRECTION_LONG);
			stopOrder.setOffset(RtConstant.OFFSET_CLOSETODAY);

		} else if (ZeusConstant.ORDER_COVERYESTERDAY.equals(orderType)) {

			stopOrder.setDirection(RtConstant.DIRECTION_LONG);
			stopOrder.setOffset(RtConstant.OFFSET_CLOSEYESTERDAY);
		}

		workingStopOrderMap.put(stopOrderID, stopOrder);

		return stopOrderID;
	}

	@Override
	public void cancelOrder(String rtOrderID) {
		if (StringUtils.isEmpty(rtOrderID)) {
			return;
		}
		if (workingOrderMap.containsKey(rtOrderID)) {
			zeusEngine.cancelOrder(rtOrderID);
			workingOrderMap.remove(rtOrderID);
		}
	}

	@Override
	public void cancelStopOrder(String stopOrderID) {
		if (workingStopOrderMap.containsKey(stopOrderID)) {
			StopOrder stopOrder = workingStopOrderMap.get(stopOrderID);

			stopOrder.setStatus(ZeusConstant.STOPORDER_CANCELLED);

			workingStopOrderMap.remove(stopOrderID);

			try {
				onStopOrder(stopOrder);
			} catch (Exception e) {
				log.error("{} 通知策略StopOrder发生异常!!!", logStr, e);
				stopTrading(true);
			}

		}
	}

	@Override
	public void cancelAll() {

		for (Entry<String, Order> entry : workingOrderMap.entrySet()) {
			String rtOrderID = entry.getKey();
			Order order = entry.getValue();
			if (!RtConstant.STATUS_FINISHED.contains(order.getStatus())) {
				cancelOrder(rtOrderID);
			}

		}

		for (Entry<String, StopOrder> entry : workingStopOrderMap.entrySet()) {
			String stopOrderID = entry.getKey();
			StopOrder stopOrder = entry.getValue();
			if (!ZeusConstant.STOPORDER_CANCELLED.equals(stopOrder.getStatus())) {
				cancelStopOrder(stopOrderID);
			}

		}
	}

	/**
	 * 处理停止单
	 * 
	 * @param tick
	 */
	protected void processStopOrder(Tick tick) {
		if (!trading) {
			return;
		}
		String rtSymbol = tick.getRtSymbol();

		for (StopOrder stopOrder : workingStopOrderMap.values()) {

			if (stopOrder.getRtSymbol().equals(rtSymbol)) {
				// 多头停止单触发
				boolean longTriggered = RtConstant.DIRECTION_LONG.equals(stopOrder.getDirection())
						&& tick.getLastPrice() >= stopOrder.getPrice();
				// 空头停止单触发
				boolean shortTriggered = RtConstant.DIRECTION_SHORT.equals(stopOrder.getDirection())
						&& tick.getLastPrice() <= stopOrder.getPrice();

				if (longTriggered || shortTriggered) {
					double price = 0;
					// 涨跌停价格报单
					if (RtConstant.DIRECTION_LONG.equals(stopOrder.getDirection())) {
						price = tick.getUpperLimit();
					} else {
						price = tick.getLowerLimit();
					}

					sendOrder(rtSymbol, stopOrder.getOrderType(), stopOrder.getPriceType(), price,
							stopOrder.getVolume(), stopOrder.getGatewayID());

					stopOrder.setStatus(ZeusConstant.STOPORDER_TRIGGERED);

					workingStopOrderMap.remove(stopOrder.getStopOrderID());

					try {
						onStopOrder(stopOrder);
					} catch (Exception e) {
						log.error("{} 通知策略StopOrder发生异常!!!", logStr, e);
						stopTrading(true);
					}
				}

			}
		}

	}

	@Override
	public String buy(String rtSymbol, int volume, double price, String gatewayID) {

		return sendOrder(rtSymbol, ZeusConstant.ORDER_BUY, RtConstant.PRICETYPE_LIMITPRICE, price, volume, gatewayID);

	}

	@Override
	public String sell(String rtSymbol, int volume, double price, String gatewayID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_SELL, RtConstant.PRICETYPE_LIMITPRICE, price, volume, gatewayID);
	}

	@Override
	public String sellTd(String rtSymbol, int volume, double price, String gatewayID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_SELLTODAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume, gatewayID);
	}

	@Override
	public String sellYd(String rtSymbol, int volume, double price, String gatewayID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_SELLYESTERDAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume,
				gatewayID);
	}

	@Override
	public String sellShort(String rtSymbol, int volume, double price, String gatewayID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_SHORT, RtConstant.PRICETYPE_LIMITPRICE, price, volume, gatewayID);
	}

	@Override
	public String buyToCover(String rtSymbol, int volume, double price, String gatewayID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_COVER, RtConstant.PRICETYPE_LIMITPRICE, price, volume, gatewayID);

	}

	@Override
	public String buyToCoverTd(String rtSymbol, int volume, double price, String gatewayID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_COVERTODAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume, gatewayID);

	}

	@Override
	public String buyToCoverYd(String rtSymbol, int volume, double price, String gatewayID) {
		return  sendOrder(rtSymbol, ZeusConstant.ORDER_COVERYESTERDAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume,
				gatewayID);

	}
}
