package xyz.redtorch.trader.module.zeus.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.zeus.ZeusConstant;
import xyz.redtorch.trader.module.zeus.ZeusEngine;
import xyz.redtorch.trader.module.zeus.entity.ContractPositionDetail;
import xyz.redtorch.trader.module.zeus.entity.PositionDetail;
import xyz.redtorch.trader.module.zeus.entity.StopOrder;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting.ContractSetting;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting.TradeGatewaySetting;
import xyz.redtorch.utils.CommonUtil;

/**
 * 策略基本实现抽象类
 * 
 * @author Administrator
 *
 */
public abstract class StrategyAbstract extends FastEventDynamicHandlerAbstract implements Strategy {
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
	 * 
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

		/**
		 * 初始化基本的持仓数据结构
		 */
		initContractPositionMap();

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
			log.warn(logStr + "策略尚未初始化,无法开始交易!");
			return;
		}

		if (trading) {
			log.warn(logStr + "策略正在运行,请勿重复操作!");
			return;
		}
		this.trading = true;
		try {
			onStartTrading();
			log.info(logStr + "开始交易!");
		} catch (Exception e) {
			stopTrading(true);
			log.error(logStr + "调用onStartTrading发生异常,停止策略!!!", e);
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
			log.warn(logStr + "策略已经停止,请勿重复操作!");
			return;
		}

		// 保存持仓
		savePosition();
		// 保存策略配置
		saveStrategySetting();
		this.trading = false;
		try {
			onStopTrading(isException);
		} catch (Exception e) {
			log.error(logStr + "策略停止后调用onStopTrading发生异常!",e);
		}
	}

	/**
	 * 初始化策略
	 */
	@Override
	public void init() {
		if (initStatus == true) {
			log.warn(logStr + "策略已经初始化,请勿重复操作!");
			return;
		}
		initStatus = true;
		try {
			onInit();
			log.info(logStr + "初始化!");
		} catch (Exception e) {
			initStatus = false;
			log.error(logStr + "调用onInit发生异常!",e);
		}
	}

	@Override
	public void saveStrategySetting() {
		zeusEngine.asyncSaveStrategySetting(strategySetting);
	}

	@Override
	public void setVarValue(String key, String value) {
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

		if (contractPositionMap.containsKey(rtSymbol)) {
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
			log.error(logStr + "无法撤单,RtOrderID为空");
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
				log.error(logStr + "调用onStopOrder发生异常!",e);
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
						log.error(logStr + "调用onStopOrder发生异常!",e);
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
		return sendOrder(rtSymbol, ZeusConstant.ORDER_SELLTODAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume,
				gatewayID);
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
		return sendOrder(rtSymbol, ZeusConstant.ORDER_COVERTODAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume,
				gatewayID);

	}

	@Override
	public String buyToCoverYd(String rtSymbol, int volume, double price, String gatewayID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_COVERYESTERDAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume,
				gatewayID);

	}

	// X分钟Bar生成器,由构造方法xMin参数决定是否实例化生效
	private Map<String, XMinBarGenerator> xMinBarGeneratorMap = new HashMap<>();
	private Map<String, BarGenerator> barGeneratorMap = new HashMap<>();

	/**
	 * 在一分钟Bar产生时调用 <br/>
	 * 注意,此处默认<b>过滤</b>同一个策略使用多个接口订阅同一个品种导致的同一个品种重复调用
	 * 
	 * @param bar
	 * @throws Exception
	 */
	public abstract void onBar(Bar bar) throws Exception;

	/**
	 * 在X分钟Bar产生时调用 <br/>
	 * 注意,此处默认<b>过滤</b>同一个策略使用多个接口订阅同一个品种导致的同一个品种重复调用
	 * 
	 * @param bar
	 * @throws Exception
	 */
	public abstract void onXMinBar(Bar bar) throws Exception;

	/**
	 * 保存持仓
	 */
	public void savePosition() {
		List<PositionDetail> positionDetailList = new ArrayList<>();
		for (ContractPositionDetail contractPositionDetail : contractPositionMap.values()) {
			positionDetailList.addAll(new ArrayList<>(contractPositionDetail.getPositionDetailMap().values()));
			zeusEngine.asyncSavePositionDetail(positionDetailList);
		}
	}

	/**
	 * 重置策略,一般被回测引擎使用,用于实现连续回测
	 * 
	 * @param strategySetting
	 */
	@Override
	public void resetStrategy(StrategySetting strategySetting) {
		// 清空活动的停止单
		this.workingStopOrderMap.clear();
		// 清空委托ID过滤集合,避免影响下一个交易日回测
		this.rtTradeIDSet.clear();
		// 清空持仓信息
		this.contractPositionMap.clear();

		// 强制校正新的配置数据
		strategySetting.fixSetting();
		this.strategySetting = strategySetting;

		// 初始化持仓
		initContractPositionMap();

	}

	/**
	 * 初始化持仓数据结构
	 */
	private void initContractPositionMap() {

		String tradingDay = strategySetting.getTradingDay();

		for (ContractSetting contractSetting : strategySetting.getContracts()) {
			String rtSymbol = contractSetting.getRtSymbol();
			String exchange = contractSetting.getExchange();
			int contractSize = contractSetting.getSize();

			ContractPositionDetail contractPositionDetail = new ContractPositionDetail(rtSymbol, tradingDay, name, id,
					exchange, contractSize);
			for (TradeGatewaySetting tradeGatewaySetting : contractSetting.getTradeGateways()) {
				String gatewayID = tradeGatewaySetting.getGatewayID();
				PositionDetail positionDetail = new PositionDetail(rtSymbol, tradeGatewaySetting.getGatewayID(),
						tradingDay, name, id, exchange, contractSize);
				contractPositionDetail.getPositionDetailMap().put(gatewayID, positionDetail);
			}
			contractPositionMap.put(rtSymbol, contractPositionDetail);
		}
	}

	/**
	 * 获取持仓结构
	 * 
	 * @return
	 */
	@Override
	public Map<String, ContractPositionDetail> getContractPositionMap() {
		return contractPositionMap;
	}

	/**
	 * 根据预设配置买开多
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void buyByPreset(String rtSymbol, double price) {

		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

		ContractSetting contractSetting = strategySetting.getContractSetting(rtSymbol);
		if (contractSetting != null) {
			List<TradeGatewaySetting> tradeGateways = contractSetting.getTradeGateways();
			if (tradeGateways != null && !tradeGateways.isEmpty()) {
				if (contractPositionDetail != null) {
					int longPos = contractPositionDetail.getLongPos();
					int fixedPos = contractSetting.getTradeFixedPos();
					if (longPos == fixedPos) {
						log.warn("合约{}的多头总持仓量已经达到预设值,指令终止!", rtSymbol);
						return;
					} else if (longPos > fixedPos) {
						log.error("合约{}的多头总持仓量{}已经超过预设值{},指令终止!!", rtSymbol, longPos, fixedPos);
						stopTrading(true);
						return;
					}
				}

				for (TradeGatewaySetting tradeGteway : tradeGateways) {
					String gatewayID = tradeGteway.getGatewayID();
					int gatewayFixedPos = tradeGteway.getTradeFixedPos();
					int tradePos = gatewayFixedPos;
					if (gatewayFixedPos > 0) {
						PositionDetail positionDetail = contractPositionDetail.getPositionDetailMap().get(gatewayID);

						if (positionDetail != null) {
							int gatewayLongPos = positionDetail.getLongPos();
							int gatewayLongOpenFrozenPos = positionDetail.getLongOpenFrozen();
							if (gatewayLongPos + gatewayLongOpenFrozenPos == gatewayFixedPos) {
								log.warn("合约{}接口{}的多头持仓量加开仓冻结量已经达到预设值,指令忽略!", rtSymbol, gatewayID);
								continue;
							} else if (gatewayLongPos > gatewayFixedPos) {
								log.error("合约{}接口{}的多头持仓量{}加开仓冻结量{}已经超过预设值{},指令忽略!", rtSymbol, gatewayID,
										gatewayLongPos, gatewayLongOpenFrozenPos, gatewayFixedPos);
								stopTrading(true);
								continue;
							} else {
								tradePos = gatewayFixedPos - (gatewayLongPos + gatewayLongOpenFrozenPos);
							}
						}

						buy(rtSymbol, tradePos, price, gatewayID);
					} else {
						log.error("合约{}接口{}配置中的仓位大小不正确", rtSymbol, gatewayID);
						stopTrading(true);
					}
				}
			} else {
				log.error("未找到合约{}配置中的接口配置", rtSymbol);
				stopTrading(true);
			}
		} else {
			log.error("未找到合约{}的配置", rtSymbol);
			stopTrading(true);
		}

	}

	/**
	 * 根据仓位通用卖平多逻辑
	 * 
	 * @param rtSymbol
	 * @param price
	 * @param offsetType
	 */
	private void commonSellByPosition(String rtSymbol, double price, int offsetType) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

		if (contractPositionDetail != null) {
			int longPos = contractPositionDetail.getLongPos();
			if (longPos == 0) {
				log.warn("合约{}的多头总持仓量为0,指令终止!", rtSymbol);
				return;
			} else if (longPos < 0) {
				log.error("合约{}的多头总持仓量{}小于0!", rtSymbol, longPos);
				stopTrading(true);
				return;
			}

			for (Entry<String, PositionDetail> entry : contractPositionDetail.getPositionDetailMap().entrySet()) {
				String gatewayID = entry.getKey();
				PositionDetail positionDetail = entry.getValue();
				if (positionDetail == null) {
					continue;
				}

				if (positionDetail.getLongPos() > 0) {
					if (offsetType >= 0) {
						if (positionDetail.getLongOpenFrozen() > 0) {
							log.warn("合约{}接口{}多头开仓冻结为{},这部分不会被处理", rtSymbol, gatewayID,
									positionDetail.getLongOpenFrozen());
						}
						if (positionDetail.getLongTd() > 0) {
							sellTd(rtSymbol, positionDetail.getLongTd(), price, gatewayID);
						}
					}
					if (offsetType <= 0) {
						if (positionDetail.getLongYd() > 0) {
							sellYd(rtSymbol, positionDetail.getLongYd(), price, gatewayID);
						}
					}
				} else {
					log.error("合约{}接口{}多头持仓大小不正确", rtSymbol, gatewayID);
					stopTrading(true);
				}
			}
		} else {
			log.error("未找到合约{}的持仓信息", rtSymbol);
		}
	}

	/**
	 * 根据仓位卖平多
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void sellByPosition(String rtSymbol, double price) {
		commonSellByPosition(rtSymbol, price, 0);
	}

	/**
	 * 根据仓位卖平今多
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void sellTdByPosition(String rtSymbol, double price) {
		commonSellByPosition(rtSymbol, price, 1);
	}

	/**
	 * 根据仓位卖平昨多
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void sellYdByPosition(String rtSymbol, double price) {
		commonSellByPosition(rtSymbol, price, -1);
	}

	/**
	 * 根据预设配置卖开空
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void sellShortByPreset(String rtSymbol, double price) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

		ContractSetting contractSetting = strategySetting.getContractSetting(rtSymbol);
		if (contractSetting != null) {
			List<TradeGatewaySetting> tradeGateways = contractSetting.getTradeGateways();
			if (tradeGateways != null && !tradeGateways.isEmpty()) {

				if (contractPositionDetail != null) {
					int shortPos = contractPositionDetail.getShortPos();
					int fixedPos = contractSetting.getTradeFixedPos();
					if (shortPos == fixedPos) {
						log.warn("合约{}的空头总持仓量已经达到预设值,指令终止!", rtSymbol);
						return;
					} else if (shortPos > fixedPos) {
						log.error("合约{}的空头总持仓量{}已经超过预设值{},指令终止!", rtSymbol);
						stopTrading(true);
						return;
					}
				}

				for (TradeGatewaySetting tradeGteway : tradeGateways) {
					String gatewayID = tradeGteway.getGatewayID();
					int gatewayFixedPos = tradeGteway.getTradeFixedPos();
					int tradePos = gatewayFixedPos;
					if (gatewayFixedPos > 0) {
						PositionDetail positionDetail = contractPositionDetail.getPositionDetailMap().get(gatewayID);

						if (positionDetail != null) {
							int gatewayShortPos = positionDetail.getShortPos();
							int gatewayShortOpenFrozenPos = positionDetail.getShortOpenFrozen();
							if (gatewayShortPos + gatewayShortOpenFrozenPos == gatewayFixedPos) {
								log.warn("合约{}接口{}的空头持仓量加开仓冻结量已经达到预设值,指令忽略!", rtSymbol, gatewayID);
								continue;
							} else if (gatewayShortPos > gatewayFixedPos) {
								log.error("合约{}接口{}的空头持仓量{}加开仓冻结量{}已经超过预设值{},指令忽略!", rtSymbol, gatewayID,
										gatewayShortPos, gatewayShortOpenFrozenPos, gatewayFixedPos);
								stopTrading(true);
								continue;
							} else {
								tradePos = gatewayFixedPos - (gatewayShortPos + gatewayShortOpenFrozenPos);
							}
						}

						sellShort(rtSymbol, tradePos, price, gatewayID);
					} else {
						log.error("合约{}接口{}配置中的仓位大小不正确", rtSymbol, gatewayID);
						stopTrading(true);
					}
				}
			} else {
				log.error("未找到合约{}配置中的接口配置", rtSymbol);
				stopTrading(true);
			}
		} else {
			log.error("未找到合约{}的配置", rtSymbol);
			stopTrading(true);
		}

	}

	/**
	 * 根据仓位通用买平空逻辑
	 * 
	 * @param rtSymbol
	 * @param price
	 * @param offsetType
	 */
	private void commonBuyToCoverByPosition(String rtSymbol, double price, int offsetType) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);
		if (contractPositionDetail != null) {
			int shortPos = contractPositionDetail.getShortPos();
			if (shortPos == 0) {
				log.warn("合约{}的空头总持仓量为0,指令终止!", rtSymbol);
				return;
			} else if (shortPos < 0) {
				log.error("合约{}的空头总持仓量{}小于0!", rtSymbol, shortPos);
				stopTrading(true);
				return;
			}

			for (Entry<String, PositionDetail> entry : contractPositionDetail.getPositionDetailMap().entrySet()) {
				String gatewayID = entry.getKey();
				PositionDetail positionDetail = entry.getValue();
				if (positionDetail == null) {
					continue;
				}

				if (positionDetail.getShortPos() > 0) {
					if (offsetType >= 0) {
						if (positionDetail.getShortOpenFrozen() > 0) {
							log.warn("合约{}接口{}空头开仓冻结为{},这部分不会被处理", rtSymbol, gatewayID,
									positionDetail.getShortOpenFrozen());
						}

						if (positionDetail.getShortTd() > 0) {
							buyToCoverTd(rtSymbol, positionDetail.getShortTd(), price, gatewayID);
						}
					}
					if (offsetType <= 0) {
						if (positionDetail.getShortYd() > 0) {
							buyToCoverYd(rtSymbol, positionDetail.getShortYd(), price, gatewayID);
						}
					}

				} else {
					log.error("合约{}接口{}空头持仓大小不正确", rtSymbol, gatewayID);
					stopTrading(true);
				}
			}
		} else {
			log.error("未找到合约{}的持仓信息", rtSymbol);
		}
	}

	/**
	 * 根据仓位买平空
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void buyToCoverByPosition(String rtSymbol, double price) {
		commonBuyToCoverByPosition(rtSymbol, price, 0);

	}

	/**
	 * 根据仓位买平今空
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void buyToCoverTdByPosition(String rtSymbol, double price) {
		commonBuyToCoverByPosition(rtSymbol, price, 1);

	}

	/**
	 * 根据仓位买平昨空
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void buyToCoverYdByPosition(String rtSymbol, double price) {
		commonBuyToCoverByPosition(rtSymbol, price, -1);
	}

	/**
	 * 根据仓位买多锁空
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void buyToLockByPosition(String rtSymbol, double price) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);
		if (contractPositionDetail != null) {
			int shortPos = contractPositionDetail.getShortPos();
			if (shortPos == 0) {
				log.warn("合约{}的空头总持仓量为0,指令终止!", rtSymbol);
				return;
			} else if (shortPos < 0) {
				log.error("合约{}的空头总持仓量{}小于0!", rtSymbol, shortPos);
				stopTrading(true);
				return;
			}

			for (Entry<String, PositionDetail> entry : contractPositionDetail.getPositionDetailMap().entrySet()) {
				String gatewayID = entry.getKey();
				PositionDetail positionDetail = entry.getValue();
				if (positionDetail == null) {
					continue;
				}
				if (positionDetail.getShortOpenFrozen() > 0) {
					log.warn("合约{}接口{}空头开仓冻结为{},这部分不会被处理", rtSymbol, gatewayID, positionDetail.getShortOpenFrozen());
				}
				if (positionDetail.getShortPos() > 0) {
					buy(rtSymbol, positionDetail.getShortPos(), price, gatewayID);
				} else {
					log.error("合约{}接口{}空头持仓大小不正确", rtSymbol, gatewayID);
					stopTrading(true);
				}
			}
		} else {
			log.error("未找到合约{}的持仓信息", rtSymbol);
		}
	}

	/**
	 * 根据仓位卖空锁多
	 * 
	 * @param rtSymbol
	 * @param price
	 */
	public void sellShortToLockByPosition(String rtSymbol, double price) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

		if (contractPositionDetail != null) {
			int longPos = contractPositionDetail.getLongPos();
			if (longPos == 0) {
				log.warn("合约{}的多头总持仓量为0,指令终止!", rtSymbol);
				return;
			} else if (longPos < 0) {
				log.error("合约{}的多头总持仓量{}小于0!", rtSymbol, longPos);
				stopTrading(true);
				return;
			}

			for (Entry<String, PositionDetail> entry : contractPositionDetail.getPositionDetailMap().entrySet()) {
				String gatewayID = entry.getKey();
				PositionDetail positionDetail = entry.getValue();
				if (positionDetail == null) {

					continue;
				}

				if (positionDetail.getLongPos() > 0) {
					if (positionDetail.getLongOpenFrozen() > 0) {
						log.warn("合约{}接口{}多头开仓冻结为{},这部分不会被处理", rtSymbol, gatewayID, positionDetail.getLongOpenFrozen());
					}
					sellShort(rtSymbol, positionDetail.getLongPos(), price, gatewayID);
				} else {
					log.error("合约{}接口{}多头持仓大小不正确", rtSymbol, gatewayID);
					stopTrading(true);
				}
			}
		} else {
			log.error("未找到合约{}的持仓信息", rtSymbol);
		}
	}

	@Override
	public void processTick(Tick tick) {
		try {
			// 处理停止单
			processStopOrder(tick);
			onTick(tick);
			// 基于合约的onBar和onMinBar
			String bgKey = tick.getRtSymbol();
			// 基于合约+接口的onBar和onMinBar,使用这个key会多次触发同一策略下同一品种的相同时间bar的事件
			// String bgKey = tick.getRtSymbol()+tick.getGatewayID();
			BarGenerator barGenerator;
			if (barGeneratorMap.containsKey(bgKey)) {
				barGenerator = barGeneratorMap.get(bgKey);
			} else {
				barGenerator = new BarGenerator(new CallBackXMinBar() {
					@Override
					public void call(Bar bar) {
						processBar(bar);
					}
				});
				barGeneratorMap.put(bgKey, barGenerator);
			}

			// 更新1分钟bar生成器
			barGenerator.updateTick(tick);
		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onTick发生异常,停止策略!!!", logStr, e);
		}
	}

	@Override
	public void processTrade(Trade trade) {
		try {
			// 过滤重复
			if (!rtTradeIDSet.contains(trade.getRtTradeID())) {
				if(contractPositionMap.containsKey(trade.getRtSymbol())) {
					ContractPositionDetail contractPositionDetail = contractPositionMap.get(trade.getRtSymbol());
					contractPositionDetail.updateTrade(trade);
					savePosition();
				}
				rtTradeIDSet.add(trade.getRtTradeID());

				onTrade(trade);
			}

		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onTrade发生异常,停止策略!!!", logStr, e);
		}
	}

	@Override
	public void processOrder(Order order) {
		try {
			workingOrderMap.put(order.getRtOrderID(), order);
			if (RtConstant.STATUS_FINISHED.contains(order.getStatus())) {
				workingOrderMap.remove(order.getRtOrderID());
			}
			ContractPositionDetail contractPositionDetail = contractPositionMap.get(order.getRtSymbol());
			contractPositionDetail.updateOrder(order);
			onOrder(order);
		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onOrder发生异常,停止策略!!!", logStr, e);
		}
	}

	/**
	 * 处理Bar
	 * 
	 * @param bar
	 */
	@Override
	public void processBar(Bar bar) {

		String bgKey = bar.getRtSymbol();
		// 调用onBar方法,此方法会在onTick->bg.updateTick执行之后再执行
		try {
			onBar(bar);
		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onBar发生异常,停止策略!!!", logStr, e);
		}
		// 判断是否需要调用xMinBarGenerate,设置xMin大于1分钟xMinBarGenerate会生效
		if (strategySetting.getxMin() > 1) {
			XMinBarGenerator xMinBarGenerator;
			if (xMinBarGeneratorMap.containsKey(bgKey)) {
				xMinBarGenerator = xMinBarGeneratorMap.get(bgKey);
			} else {
				xMinBarGenerator = new XMinBarGenerator(strategySetting.getxMin(), new CallBackXMinBar() {
					@Override
					public void call(Bar bar) {
						try {
							// 调用onXMinBar方法
							// 此方法会在onTick->bg.updateTick->onBar->xbg.updateBar执行之后再执行
							onXMinBar(bar);
						} catch (Exception e) {
							stopTrading(true);
							log.error("{} 调用onXMinBar发生异常,停止策略!!!", logStr, e);
						}
					}
				});
				xMinBarGeneratorMap.put(bgKey, xMinBarGenerator);
			}
			xMinBarGenerator.updateBar(bar);
		}
	}

	// ##############################################################################

	/**
	 * CallBack接口,用于注册Bar生成器回调事件
	 */
	public static interface CallBackXMinBar {
		void call(Bar bar);
	}

	/**
	 * 1分钟Bar生成器
	 */
	public static class BarGenerator {

		private Bar bar = null;
		private Tick lastTick = null;
		CallBackXMinBar callBackXMinBar;

		BarGenerator(CallBackXMinBar callBackXMinBar) {
			this.callBackXMinBar = callBackXMinBar;
		}

		/**
		 * 更新Tick数据
		 * 
		 * @param tick
		 */
		public void updateTick(Tick tick) {

			boolean newMinute = false;

			if (lastTick != null) {
				// 此处过滤用于一个策略在多个接口订阅了同一个合约的情况下,Tick到达顺序和实际产生顺序不一致或者重复的情况
				if (tick.getDateTime().getMillis() <= lastTick.getDateTime().getMillis()) {
					return;
				}
			}

			if (bar == null) {
				bar = new Bar();
				newMinute = true;
			} else if (bar.getDateTime().getMinuteOfDay() != tick.getDateTime().getMinuteOfDay()) {

				bar.setDateTime(bar.getDateTime().withSecondOfMinute(0).withMillisOfSecond(0));
				bar.setActionTime(bar.getDateTime().toString(RtConstant.T_FORMAT_WITH_MS_FORMATTER));

				// 回调OnBar方法
				callBackXMinBar.call(bar);

				bar = new Bar();
				newMinute = true;
			}

			if (newMinute) {
				bar.setGatewayID(tick.getGatewayID());
				bar.setExchange(tick.getExchange());
				bar.setRtSymbol(tick.getRtSymbol());
				bar.setSymbol(tick.getSymbol());

				bar.setTradingDay(tick.getTradingDay());
				;
				bar.setActionDay(tick.getActionDay());

				bar.setOpen(tick.getLastPrice());
				bar.setHigh(tick.getLastPrice());
				bar.setLow(tick.getLastPrice());

				bar.setDateTime(tick.getDateTime());
			} else {
				bar.setHigh(Math.max(bar.getHigh(), tick.getLastPrice()));
				bar.setLow(Math.min(bar.getLow(), tick.getLastPrice()));
			}

			bar.setClose(tick.getLastPrice());
			bar.setOpenInterest(tick.getOpenInterest());
			if (lastTick != null) {
				bar.setVolume(bar.getVolume() + (tick.getVolume() - lastTick.getVolume()));
			}

			lastTick = tick;
		}
	}

	/**
	 * X分钟Bar生成器,xMin在策略初始化时指定,当值大于1小于时生效,建议此数值不要大于120
	 */
	public static class XMinBarGenerator {

		private int xMin;
		private Bar xMinBar = null;
		CallBackXMinBar callBackXMinBar;

		XMinBarGenerator(int xMin, CallBackXMinBar callBackXMinBar) {
			this.callBackXMinBar = callBackXMinBar;
			this.xMin = xMin;
		}

		public void updateBar(Bar bar) {

			if (xMinBar == null) {
				xMinBar = new Bar();
				xMinBar.setGatewayID(bar.getGatewayID());
				xMinBar.setExchange(bar.getExchange());
				xMinBar.setRtSymbol(bar.getRtSymbol());
				xMinBar.setSymbol(bar.getSymbol());

				xMinBar.setTradingDay(bar.getTradingDay());
				xMinBar.setActionDay(bar.getActionDay());

				xMinBar.setOpen(bar.getOpen());
				xMinBar.setHigh(bar.getHigh());
				xMinBar.setLow(bar.getLow());

				xMinBar.setDateTime(bar.getDateTime());

			} else {
				xMinBar.setHigh(Math.max(xMinBar.getHigh(), bar.getHigh()));
				xMinBar.setLow(Math.min(xMinBar.getLow(), bar.getLow()));
			}

			if ((xMinBar.getDateTime().getMinuteOfDay() + 1) % xMin == 0) {
				bar.setDateTime(bar.getDateTime().withSecondOfMinute(0).withMillisOfSecond(0));
				bar.setActionTime(bar.getDateTime().toString(RtConstant.T_FORMAT_WITH_MS_FORMATTER));

				// 回调onXMinBar方法
				callBackXMinBar.call(xMinBar);

				xMinBar = null;
			}

		}
	}

}
