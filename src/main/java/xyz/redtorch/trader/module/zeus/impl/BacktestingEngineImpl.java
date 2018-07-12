package xyz.redtorch.trader.module.zeus.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.trader.base.BaseConfig;
import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.engine.event.FastEvent;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.zeus.BacktestingEngine;
import xyz.redtorch.trader.module.zeus.ZeusConstant;
import xyz.redtorch.trader.module.zeus.ZeusDataUtil;
import xyz.redtorch.trader.module.zeus.ZeusEngine;
import xyz.redtorch.trader.module.zeus.entity.ContractPositionDetail;
import xyz.redtorch.trader.module.zeus.entity.PositionDetail;
import xyz.redtorch.trader.module.zeus.strategy.Strategy;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting.TradeGatewaySetting;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting.ContractSetting;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting.gatewaySetting;
import xyz.redtorch.utils.CommonUtil;

/**
 * @author sun0x00@gmail.com
 */
public class BacktestingEngineImpl implements BacktestingEngine {

	private Logger log = LoggerFactory.getLogger(BacktestingEngineImpl.class);

	private String backtestingOutputDir = BaseConfig.rtConfig.getString("module.zeus.backtesting.output.dir");

	private ZeusDataUtil zeusDataUtil;
	// 模拟数据库存储持仓
	private Map<String, Map<String, PositionDetail>> posSimulationDBMap = new HashMap<>();
	// 模拟数据库存储同步变量
	private Map<String, StrategySetting> syncStrategySettingSimulationDBMap = new HashMap<>();
	// 合约最新Bar
	private Map<String, Bar> barMap = new HashMap<>();
	// 合约最新Tick
	private Map<String, Tick> tickMap = new HashMap<>();
	// 合约最后一笔数据时间
	private Map<String, DateTime> lastDateTimeMap = new HashMap<>();
	// 所有数据中最后一笔时间
	private DateTime lastDateTime = new DateTime();
	// 当前未撮合的限价单
	private Map<String, Order> workingLimitOrderMap = new HashMap<>();
	// 限价单
	private Map<String, Order> limitOrderMap = new HashMap<>();
	// 当前策略实例
	private Strategy strategy;
	// 成交
	private Map<String, Trade> tradeMap = new LinkedHashMap<>();
	// 成交计数
	private int tradeCount = 0;
	// 限价单委托计数
	private int limitOrderCount = 0;
	// 合约接口手续费率
	private Map<String, Double> rateMap = new HashMap<>();
	// 合约滑点
	private Map<String, Double> slippageMap = new HashMap<>();
	// 合约大小
	private Map<String, Integer> contractSizeMap = new HashMap<>();
	// 合约接口信息
	private Map<String, List<String>> rtSymbolMap = new LinkedHashMap<>();
	// 计算对冲平仓Trade
	private Map<String, Trade> settleTradeMap = new LinkedHashMap<>();
	

	// 交易结果 rtSymbol--gatewayID--BacktestingResult
	private Map<String, Map<String, BacktestingResult>> rtSymbolResultMap = new HashMap<>();
	// 按日计算结果 rtSymbol--gatewayID--date--DailyResult
	private Map<String, Map<String, Map<String, DailyResult>>> rtSymbolDailyResultMap = new LinkedHashMap<>();

	private StrategySetting strategySetting;

	private List<BacktestingSection> backestingSectionList;
	private int backtestingDataMode = 0;
	private Boolean reloadStrategyEveryday;

	public BacktestingEngineImpl(DataEngine dataEngine, String strategyID, 
			List<BacktestingSection> backestingSectionList, int backtestingDataMode, Boolean reloadStrategyEveryday) {
		zeusDataUtil = new ZeusDataUtilImpl(dataEngine);
		this.strategySetting = zeusDataUtil.loadStrategySetting(strategyID);
		this.backestingSectionList = backestingSectionList;
		this.backtestingDataMode = backtestingDataMode;
		this.reloadStrategyEveryday = reloadStrategyEveryday;

	}

	@Override
	public int getEngineType() {
		return ZeusConstant.ENGINE_TYPE_BACKTESTING;
	}

	@Override
	public String sendOrder(OrderReq orderReq, Strategy strategy) {

		String orderReqJsonString = JSON.toJSONString(orderReq);
		log.info("发送委托{}", orderReqJsonString);

		limitOrderCount += 1;
		String orderID = orderReq.getGatewayID() + "." + limitOrderCount;

		Order order = new Order();
		order.setRtSymbol(orderReq.getRtSymbol());
		order.setPrice(orderReq.getPrice());
		order.setTotalVolume(orderReq.getVolume());
		order.setOrderID(orderID);
		order.setRtOrderID(orderID);
		order.setOrderTime(lastDateTime.toString(RtConstant.T_FORMAT_FORMATTER));
		order.setGatewayID(orderReq.getGatewayID());
		order.setDirection(orderReq.getDirection());
		order.setOffset(orderReq.getOffset());

		workingLimitOrderMap.put(orderID, order);
		limitOrderMap.put(orderID, order);

		return orderID;

	}

	@Override
	public void cancelOrder(String rtOrderID) {
		if (workingLimitOrderMap.containsKey(rtOrderID)) {
			Order order = workingLimitOrderMap.get(rtOrderID);

			order.setStatus(RtConstant.STATUS_CANCELLED);
			order.setCancelTime(lastDateTime.toString(RtConstant.T_FORMAT_FORMATTER));

			strategy.processOrder(order);

			workingLimitOrderMap.remove(rtOrderID);
		}

	}

	@Override
	public List<Tick> loadTickDataByOffsetDay(String tradingDay, int offsetDay, String rtSymbol) {
		DateTime tradingDateTime = RtConstant.D_FORMAT_INT_FORMATTER.parseDateTime(tradingDay);
		DateTime endDateTime = tradingDateTime.minusDays(1);
		DateTime startDateTime = endDateTime.minusDays(offsetDay);

		return this.loadTickData(startDateTime, endDateTime, rtSymbol);
	}

	@Override
	public List<Bar> loadBarDataByOffsetDay(String tradingDay, int offsetDay, String rtSymbol) {

		DateTime tradingDateTime = RtConstant.D_FORMAT_INT_FORMATTER.parseDateTime(tradingDay);
		DateTime endDateTime = tradingDateTime.minusDays(1);
		DateTime startDateTime = endDateTime.minusDays(offsetDay);

		return this.loadBarData(startDateTime, endDateTime, rtSymbol);
	}

	@Override
	public List<Tick> loadTickData(DateTime startDateTime, DateTime endDateTime, String rtSymbol) {
		return zeusDataUtil.loadTickDataList(startDateTime, endDateTime, rtSymbol);
	}

	@Override
	public List<Bar> loadBarData(DateTime startDateTime, DateTime endDateTime, String rtSymbol) {
		return zeusDataUtil.loadBarDataList(startDateTime, endDateTime, rtSymbol);
	}

	@Override
	public void asyncSaveStrategySetting(StrategySetting strategySetting) {
		// 实现深度复制,避免引用被修改
		StrategySetting copyStrategySetting = SerializationUtils.clone(strategySetting);
		// 模拟存入数据库
		syncStrategySettingSimulationDBMap.put(strategySetting.getStrategyID(), copyStrategySetting);

	}

	@Override
	public void asyncSavePositionDetail(List<PositionDetail> positionDetailList) {
		for (PositionDetail positionDetail : positionDetailList) {

			// 深度复制
			PositionDetail savePositionDetail = SerializationUtils.clone(positionDetail);
			// 模拟存入数据库

			// 使用策略ID和交易日确定第一层Map
			String firstLevelKey = savePositionDetail.getStrategyID() + savePositionDetail.getTradingDay();
			Map<String, PositionDetail> secondLevelMap;
			if (posSimulationDBMap.containsKey(firstLevelKey)) {
				secondLevelMap = posSimulationDBMap.get(firstLevelKey);
			} else {
				secondLevelMap = new HashMap<>();
				posSimulationDBMap.put(firstLevelKey, secondLevelMap);
			}

			// 使用RtSymbol和接口ID确定第二层Map
			String secondLevelKey = savePositionDetail.getRtSymbol() + savePositionDetail.getGatewayID();
			secondLevelMap.put(secondLevelKey, savePositionDetail);

		}
	}

	@Override
	public List<Bar> loadBacktestingBarDataList(String startDate, String endDate, List<String> subscribeRtSymbolList) {
		log.info("加载Bar回测数据,合约{},开始日期{},结束日期{}", JSON.toJSONString(subscribeRtSymbolList), startDate, endDate);
		long startTime = System.currentTimeMillis();
		DateTime startDateTime = RtConstant.D_FORMAT_INT_FORMATTER.parseDateTime(startDate);
		DateTime endDateTime = RtConstant.D_FORMAT_INT_FORMATTER.parseDateTime(endDate);
		List<Bar> barList = new ArrayList<>();
		for (String rtSymbol : subscribeRtSymbolList) {
			List<Bar> tmpBarList = zeusDataUtil.loadBarDataList(startDateTime, endDateTime, rtSymbol);
			if (tmpBarList == null || tmpBarList.isEmpty()) {
				log.error("回测数据为空{}", rtSymbol);
			}
			barList.addAll(tmpBarList);
		}
		barList.sort((Bar b1, Bar b2) -> b1.getDateTime().compareTo(b2.getDateTime()));
		log.info("加载Bar回测数据结束,合约{},共{}条", JSON.toJSONString(subscribeRtSymbolList), barList.size(),
				System.currentTimeMillis() - startTime);
		return barList;
	}

	@Override
	public List<Tick> loadBacktestingTickDataList(String startDate, String endDate,
			List<String> subscribeRtSymbolList) {
		log.info("加载Tick回测数据,合约{},开始日期{},结束日期{}", JSON.toJSONString(subscribeRtSymbolList), startDate, endDate);
		long startTime = System.currentTimeMillis();
		DateTime startDateTime = RtConstant.D_FORMAT_INT_FORMATTER.parseDateTime(startDate);
		DateTime endDateTime = RtConstant.D_FORMAT_INT_FORMATTER.parseDateTime(endDate);
		List<Tick> tickList = new ArrayList<>();
		for (String rtSymbol : subscribeRtSymbolList) {
			tickList.addAll(zeusDataUtil.loadTickDataList(startDateTime, endDateTime, rtSymbol));
		}
		tickList.sort((Tick t1, Tick t2) -> t1.getDateTime().compareTo(t2.getDateTime()));
		log.info("加载Tick回测数据结束,合约{},共{}条,耗时{}ms", JSON.toJSONString(subscribeRtSymbolList), tickList.size(),
				System.currentTimeMillis() - startTime);
		return tickList;
	}

	@Override
	public void runBacktesting() {
		log.info("回测开始");
		// 设置回测模式
		if (backtestingDataMode != DATA_MODE_BAR) {
			backtestingDataMode = DATA_MODE_TICK;
		} else {
			backtestingDataMode = DATA_MODE_BAR;
		}

		// 是否每个交易日都重新初始化策略
		if (reloadStrategyEveryday != null && reloadStrategyEveryday) {
			reloadStrategyEveryday = true;
		} else {
			reloadStrategyEveryday = false;
		}
		// 加载策略类
		Class<?> strategyClass;
		try {
			strategyClass = Class.forName(strategySetting.getClassName());
		} catch (ClassNotFoundException e) {
			log.error("未找到策略类,回测结束", e);
			return;
		}

		for (BacktestingSection backtestingSection : backestingSectionList) {

			// 生成下一个回测段的合约代码
			String startDate = backtestingSection.getStartDate();
			String endDate = backtestingSection.getEndDate();
			
			Map<String, String> aliasMap = backtestingSection.getAliasMap();
			for (Entry<String, String> entry : aliasMap.entrySet()) {
				String alias = entry.getKey();
				String rtSymbol = entry.getValue();
				strategySetting.getContractByAlias(alias).setRtSymbol(rtSymbol);
			}
			
			Map<String, List<String>> subscribeRtSymbolsMap = backtestingSection.getGatewaySubscribeRtSymbolsMap();
			for (Entry<String, List<String>> entry : subscribeRtSymbolsMap.entrySet()) {
				String gatewayID = entry.getKey();
				List<String> rtSymbols = entry.getValue();
				strategySetting.getGatewaySetting(gatewayID).setSubscribeRtSymbols(rtSymbols);
			}
			
			strategySetting.fixSetting();

			// 保存手续费率,滑点,合约大小等设置
			for (ContractSetting contractSetting : strategySetting.getContracts()) {
				String rtSymbol = contractSetting.getRtSymbol();
				contractSizeMap.put(rtSymbol, contractSetting.getSize());
				slippageMap.put(rtSymbol, contractSetting.getBacktestingSlippage());
				List<String> gatewayIDList = new ArrayList<>();
				for (TradeGatewaySetting tradeGateway : contractSetting.getTradeGateways()) {
					String gatewayID = tradeGateway.getGatewayID();
					String key = gatewayID + "." + rtSymbol;
					rateMap.put(key, tradeGateway.getBacktestingRate());
					gatewayIDList.add(gatewayID);

				}
				rtSymbolMap.put(rtSymbol, gatewayIDList);

			}

			List<String> subscribeRtSymbolList = new ArrayList<>();

			for (gatewaySetting gateway : strategySetting.getGateways()) {
				subscribeRtSymbolList.addAll(gateway.getSubscribeRtSymbols());
			}

			Strategy strategy = null;

			String lastDay = null;
			if (DATA_MODE_TICK == backtestingDataMode) {

				List<Tick> tickDataList = loadBacktestingTickDataList(startDate, endDate, subscribeRtSymbolList);
				for (Tick tick : tickDataList) {
					String tradingDay = tick.getTradingDay();
					if (strategy == null) {
						// 第一次初始化策略
						strategySetting.setTradingDay(tradingDay);
						strategy = newInstance(strategyClass, strategySetting);
						initStrategy(strategy);
						strategy.init();
						strategy.startTrading();
					}
					if (lastDay == null) {
						// 第一次记录上一个交易日
						lastDay = tradingDay;
					} else if (!tradingDay.equals(lastDay)) {
						// 如果交易日发生了变动

						// 更新策略设置
						strategySetting.setPreTradingDay(lastDay);
						strategySetting.setTradingDay(tradingDay);

						// 是否每个交易日都重新实例化
						if (reloadStrategyEveryday) {
							strategy = newInstance(strategyClass, strategySetting);
							initStrategy(strategy);
							strategy.init();
							strategy.startTrading();
						} else {
							// 不重新实例化策略,不触发init事件
							strategy.resetStrategy(strategySetting);
							initStrategy(strategy);
							if (!strategy.isTrading()) {
								strategy.startTrading();
							}
						}

						lastDay = tradingDay;
					}

					tickMap.put(tick.getRtSymbol(), tick);
					lastDateTimeMap.put(tick.getRtSymbol(), tick.getDateTime());
					lastDateTime = tick.getDateTime();

					crossLimitOrder(tick.getRtSymbol());
					strategy.processTick(tick);

					updateDailyClose(tick.getDateTime().toString(RtConstant.D_FORMAT_INT_FORMATTER),
							tick.getLastPrice(), tick.getRtSymbol());

				}
			} else {
				List<Bar> barDataList = loadBacktestingBarDataList(startDate, endDate, subscribeRtSymbolList);

				// for (Bar bar : barDataList) {
				// System.out.println(bar.getDateTime().toString(RtConstant.DT_FORMAT_WITH_MS_FORMATTER)
				// + "===" + bar.getRtSymbol());
				// }
				for (Bar bar : barDataList) {
					String tradingDay = bar.getTradingDay();
					if (strategy == null) {
						strategySetting.setTradingDay(tradingDay);
						strategy = newInstance(strategyClass, strategySetting);
						initStrategy(strategy);
						strategy.init();
						strategy.startTrading();
					}
					if (lastDay == null) {
						lastDay = tradingDay;
					} else if (!tradingDay.equals(lastDay)) {

						strategySetting.setPreTradingDay(lastDay);
						strategySetting.setTradingDay(tradingDay);
						if (reloadStrategyEveryday) {
							strategy = newInstance(strategyClass, strategySetting);
							initStrategy(strategy);
							strategy.init();
							strategy.startTrading();
						} else {
							strategy.resetStrategy(strategySetting);
							initStrategy(strategy);
							if (!strategy.isTrading()) {
								strategy.startTrading();
							}
						}

						lastDay = tradingDay;
					}

					barMap.put(bar.getRtSymbol(), bar);
					lastDateTimeMap.put(bar.getRtSymbol(), bar.getDateTime());
					lastDateTime = bar.getDateTime();

					crossLimitOrder(bar.getRtSymbol());
					strategy.processBar(bar);

					updateDailyClose(bar.getDateTime().toString(RtConstant.D_FORMAT_INT_FORMATTER), bar.getClose(),
							bar.getRtSymbol());

				}
			}

		}

		log.info("计算交易结果");
		// 计算交易结果
		calculateBacktestingResult();
		calculateDailyResult();
		log.info("回测结束");
	}

	/**
	 * 更新每日收盘价
	 * 
	 * @param date
	 * @param lastPrice
	 * @param rtSymbol
	 */
	private void updateDailyClose(String date, double lastPrice, String rtSymbol) {
		if (strategy.getStrategySetting().getContractSetting(rtSymbol) == null) {
			return;
		}

		List<TradeGatewaySetting> gateways = strategy.getStrategySetting().getContractSetting(rtSymbol)
				.getTradeGateways();

		Map<String, Map<String, DailyResult>> gatewayDailyResultDict;
		if (rtSymbolDailyResultMap.containsKey(rtSymbol)) {
			gatewayDailyResultDict = rtSymbolDailyResultMap.get(rtSymbol);
		} else {
			gatewayDailyResultDict = new LinkedHashMap<>();
			for (TradeGatewaySetting gateway : gateways) {
				gatewayDailyResultDict.put(gateway.getGatewayID(), new LinkedHashMap<>());
			}
			rtSymbolDailyResultMap.put(rtSymbol, gatewayDailyResultDict);
		}

		for (TradeGatewaySetting gateway : gateways) {
			Map<String, DailyResult> dailyResultDict = gatewayDailyResultDict.get(gateway.getGatewayID());
			if (dailyResultDict.containsKey(date)) {
				dailyResultDict.get(date).setClosePrice(lastPrice);
			} else {
				dailyResultDict.put(date, new DailyResult(date, lastPrice, rtSymbol, gateway.getGatewayID()));
			}

		}

	}

	/**
	 * 撮合限价单
	 * 
	 * @param rtSymbol
	 */
	private void crossLimitOrder(String rtSymbol) {
		double buyCrossPrice;
		double sellCrossPrice;
		// double buyBestCrossPrice;
		// double sellBestCrossPrice;
		if (backtestingDataMode == DATA_MODE_BAR) {
			buyCrossPrice = barMap.get(rtSymbol).getLow(); // 若买入方向限价单价格高于该价格,则会成交
			sellCrossPrice = barMap.get(rtSymbol).getHigh(); // 若卖出方向限价单价格低于该价格,则会成交
			// buyBestCrossPrice = barMap.get(rtSymbol).getOpen(); // 在当前时间点前发出的买入委托可能的最优成交价
			// sellBestCrossPrice = barMap.get(rtSymbol).getOpen(); //
			// 在当前时间点前发出的卖出委托可能的最优成交价
		} else {
			buyCrossPrice = tickMap.get(rtSymbol).getAskPrice1(); // 若买入方向限价单价格高于该价格,则会成交
			sellCrossPrice = tickMap.get(rtSymbol).getBidPrice1(); // 若卖出方向限价单价格低于该价格,则会成交
			// buyBestCrossPrice = tickMap.get(rtSymbol).getAskPrice1(); //
			// 在当前时间点前发出的买入委托可能的最优成交价
			// sellBestCrossPrice = tickMap.get(rtSymbol).getBidPrice1(); //
			// 在当前时间点前发出的卖出委托可能的最优成交价

		}

		// 遍历限价单字典中的所有限价单
		Iterator<Map.Entry<String, Order>> it = workingLimitOrderMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Order> entry = it.next();
			// String orderID = entry.getKey();
			Order order = entry.getValue();
			if (rtSymbol.equals(order.getRtSymbol())) {
				if (StringUtils.isEmpty(order.getStatus())) {
					order.setStatus(RtConstant.STATUS_NOTTRADED);
					strategy.processOrder(order);
				}

				// 判断是否会成交,排除涨跌停
				boolean buyCross = order.getDirection().equals(RtConstant.DIRECTION_LONG)
						&& order.getPrice() >= buyCrossPrice && buyCrossPrice > 0;
				boolean sellCross = order.getDirection().equals(RtConstant.DIRECTION_SHORT)
						&& order.getPrice() <= sellCrossPrice && sellCrossPrice > 0;

				// 如果发生了成交
				if (buyCross || sellCross) {
					this.tradeCount += 1;
					String tradeID = order.getGatewayID() + "." + tradeCount;
					Trade trade = new Trade();
					trade.setRtSymbol(rtSymbol);
					trade.setTradeID(tradeID);
					trade.setRtTradeID(tradeID);
					trade.setOrderID(order.getOrderID());
					trade.setRtOrderID(order.getRtOrderID());
					trade.setDirection(order.getDirection());
					trade.setOffset(order.getOffset());
					trade.setGatewayID(order.getGatewayID());
					// 弃用最优价逻辑,实盘很难达成最优价条件
					// if(buyCross) {
					// trade.setPrice(Math.min(order.getPrice(), buyBestCrossPrice));
					// }else {
					// trade.setPrice(Math.max(order.getPrice(), sellBestCrossPrice));
					// }

					trade.setPrice(order.getPrice());
					trade.setVolume(order.getTotalVolume());
					trade.setTradingDay(order.getTradingDay());
					trade.setTradeTime(lastDateTimeMap.get(rtSymbol).toString(RtConstant.T_FORMAT));
					trade.setDateTime(lastDateTimeMap.get(rtSymbol));

					strategy.processTrade(trade);

					this.tradeMap.put(tradeID, trade);

					order.setTradedVolume(order.getTotalVolume());
					order.setStatus(RtConstant.STATUS_ALLTRADED);

					strategy.processOrder(order);

					// 使用迭代器删除这个活动限价单
					it.remove();
				}
			}
		}

	}

	/**
	 * 创建策略实例
	 * 
	 * @param clazz
	 * @param strategySetting
	 * @return
	 */
	private Strategy newInstance(Class<?> clazz, StrategySetting strategySetting) {
		try {
			Constructor<?> c = clazz.getConstructor(ZeusEngine.class, StrategySetting.class);
			Strategy strategy = (Strategy) c.newInstance(this, strategySetting);
			this.strategy = strategy;
			return strategy;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			log.error("反射创建策略{}实例发生异常", clazz.getName(), e);
		}
		return null;
	}

	/**
	 * 初始化策略
	 * 
	 * @param strategy
	 */
	private void initStrategy(Strategy strategy) {

		StrategySetting strategySetting = strategy.getStrategySetting();

		// 从模拟数据库中加载StrategySetting
		StrategySetting simStrategySetting = null;
		if (syncStrategySettingSimulationDBMap.containsKey(strategySetting.getStrategyID())) {
			simStrategySetting = syncStrategySettingSimulationDBMap.get(strategySetting.getStrategyID());
		}
		if (simStrategySetting==null) {
			log.info("{}模拟数据库中StrategySetting为空", strategy.getLogStr());
		} else {
			strategySetting.setVarMap(simStrategySetting.getVarMap());
		}

		String tradingDay = strategySetting.getTradingDay();
		String preTradingDay = strategySetting.getPreTradingDay();

		// 获取策略的持仓Map
		Map<String, ContractPositionDetail> contractPositionMap = strategy.getContractPositionMap();
		Set<String> contractGatewayKeySet = new HashSet<>(); // 用于后续判断数据库中读取的数据是否和配置匹配

		for (StrategySetting.ContractSetting contractSetting : strategySetting.getContracts()) {
			String rtSymbol = contractSetting.getRtSymbol();
			if (!contractPositionMap.containsKey(rtSymbol)) {
				contractPositionMap.put(rtSymbol, new ContractPositionDetail());
			}

			for (StrategySetting.TradeGatewaySetting gatewaySetting : contractSetting.getTradeGateways()) {
				String contractGatewayKey = rtSymbol + gatewaySetting.getGatewayID();
				contractGatewayKeySet.add(contractGatewayKey);
			}

		}

		// 这里不需要检查当日,只需要直接读取前一个交易日
		if (StringUtils.isEmpty(preTradingDay)) {
			log.info("{} 前一交易日配置为空", strategy.getLogStr());
		} else {
			String firstLevelKey = strategy.getID() + preTradingDay;

			// 从模拟数据库中读取昨日持仓
			List<PositionDetail> ydPositionDetailList = new ArrayList<>();
			if (posSimulationDBMap.containsKey(firstLevelKey)) {
				Map<String, PositionDetail> secondLevelMap = posSimulationDBMap.get(firstLevelKey);
				ydPositionDetailList.addAll(new ArrayList<>(secondLevelMap.values()));
			}

			if (ydPositionDetailList.isEmpty()) {
				log.info("{} 前一日持仓数据记录为空", strategy.getLogStr());
			} else {

				List<PositionDetail> insertPositionDetailList = new ArrayList<>();
				for (PositionDetail ydPositionDetail : ydPositionDetailList) {
					String rtSymbol = ydPositionDetail.getRtSymbol();
					String gatewayID = ydPositionDetail.getGatewayID();

					if (contractPositionMap.containsKey(rtSymbol)) {
						ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);
						String tmpContractGatewayKey = rtSymbol + gatewayID;

						if (contractGatewayKeySet.contains(tmpContractGatewayKey)) {

							PositionDetail tdPositionDetail = new PositionDetail(rtSymbol, gatewayID, preTradingDay,
									strategy.getStrategySetting().getStrategyName(), strategy.getStrategySetting().getStrategyID(),
									ydPositionDetail.getExchange(), ydPositionDetail.getContractSize());
							tdPositionDetail.setLongYd(ydPositionDetail.getLongYd() + ydPositionDetail.getLongTd());
							tdPositionDetail.setShortYd(ydPositionDetail.getShortYd() + ydPositionDetail.getShortTd());
							tdPositionDetail.setLastPrice(ydPositionDetail.getLastPrice());
							tdPositionDetail.setLongPrice(ydPositionDetail.getLongPrice());
							tdPositionDetail.setShortPrice(ydPositionDetail.getShortPrice());
							tdPositionDetail.setTradingDay(tradingDay);

							tdPositionDetail.calculatePosition();
							tdPositionDetail.calculatePnl();

							contractPositionDetail.getPositionDetailMap().put(gatewayID, tdPositionDetail);
							contractPositionDetail.calculatePosition();
							insertPositionDetailList.add(tdPositionDetail);
						} else {
							log.error("{} 从数据库中读取到合约{}接口{}组合与配置不匹配", strategy.getLogStr(), rtSymbol, gatewayID);
						}
					} else {
						log.error("{} 从数据库中读取到配置中不存在的合约{}", strategy.getLogStr(), rtSymbol);
					}
				}

				// 存入数据库
				asyncSavePositionDetail(insertPositionDetailList);

			}
		}
	}

	/**
	 * 计算交易结果
	 */
	private void calculateBacktestingResult() {
		rtSymbolResultMap = new HashMap<>();

		for (Entry<String, List<String>> entry : rtSymbolMap.entrySet()) {
			String rtSymbol = entry.getKey();
			List<String> gatewayList = entry.getValue();

			Map<String, BacktestingResult> gatewayResultMap = new HashMap<>();
			// 按接口计算

			for (String gatewayID : gatewayList) {

				List<TradingResult> resultList = new LinkedList<>();
				List<Trade> longTradeList = new LinkedList<>();
				List<Trade> shortTradeList = new LinkedList<>();
				List<DateTime> tradeTimeList = new LinkedList<>();
				List<Integer> posList = new LinkedList<>();

				for (Trade trade : tradeMap.values()) {
					// 不是同一个合约同一个接口的,跳过
					if (!trade.getRtSymbol().equals(rtSymbol) || !trade.getGatewayID().equals(gatewayID)) {
						continue;
					}
//					System.out.println("################");
//					System.out.println(trade.getRtSymbol());
//					System.out.println(trade.getGatewayID());
//					System.out.println(trade.getDirection());
//					System.out.println(trade.getDateTime().toString());
//					System.out.println("################");
					Trade cpTrade = SerializationUtils.clone(trade);
		

					if (cpTrade.getDirection().equals(RtConstant.DIRECTION_LONG)) {
						// 如果还没有空头交易
						if (shortTradeList.isEmpty()) {
							longTradeList.add(cpTrade);
						} else {
							// 当前多头交易为平空
							while (true) {
								Trade entryTrade = shortTradeList.get(0);
								Trade exitTrade = cpTrade;

								// 清算开平仓交易
								int closedVolume = Math.min(exitTrade.getVolume(), entryTrade.getVolume());
								TradingResult tradingResult = new TradingResult(entryTrade.getPrice(),
										entryTrade.getDateTime(), exitTrade.getPrice(), exitTrade.getDateTime(),
										-closedVolume, rateMap.get(gatewayID + "." + rtSymbol),
										slippageMap.get(rtSymbol), contractSizeMap.get(rtSymbol), rtSymbol, gatewayID);
								resultList.add(tradingResult);

								posList.add(0);
								posList.add(1);

								tradeTimeList.add(tradingResult.getEntryDateTime());
								tradeTimeList.add(tradingResult.getExitDateTime());

								// 计算未清算部分
								entryTrade.setVolume(entryTrade.getVolume() - closedVolume);
								exitTrade.setVolume(exitTrade.getVolume() - closedVolume);

								// 如果开仓交易已经清算完成
								if (entryTrade.getVolume() == 0) {
									shortTradeList.remove(0);
								}

								// 如果平仓交易已经清算,推出循环
								if (exitTrade.getVolume() == 0) {
									break;
								}

								// 如果平仓交易未清算完成
								if (exitTrade.getVolume() != 0) {
									// 开仓交易已经全部清算完成,则平仓交易剩余的部分等于新的反向开仓交易,添加到队列中
									if (shortTradeList.isEmpty()) {
										longTradeList.add(exitTrade);
										break;
									} else {
										// 进入下一轮循环
									}
								}
							}
						}
					} else {
						// 如果还没有多头交易
						if (longTradeList.isEmpty()) {
							shortTradeList.add(cpTrade);
						} else {
							// 当前空头交易为平多
							while (true) {
								Trade entryTrade = longTradeList.get(0);
								Trade exitTrade = cpTrade;

								// 清算开平仓交易
								int closedVolume = Math.min(exitTrade.getVolume(), entryTrade.getVolume());
								TradingResult tradingResult = new TradingResult(entryTrade.getPrice(),
										entryTrade.getDateTime(), exitTrade.getPrice(), exitTrade.getDateTime(),
										closedVolume, rateMap.get(gatewayID + "." + rtSymbol),
										slippageMap.get(rtSymbol), contractSizeMap.get(rtSymbol), rtSymbol, gatewayID);
								resultList.add(tradingResult);

								posList.add(1);
								posList.add(0);

								tradeTimeList.add(tradingResult.getEntryDateTime());
								tradeTimeList.add(tradingResult.getExitDateTime());

								// 计算未清算部分
								entryTrade.setVolume(entryTrade.getVolume() - closedVolume);
								exitTrade.setVolume(exitTrade.getVolume() - closedVolume);

								// 如果开仓交易已经清算完成
								if (entryTrade.getVolume() == 0) {
									longTradeList.remove(0);
								}

								// 如果平仓交易已经清算,推出循环
								if (exitTrade.getVolume() == 0) {
									break;
								}

								// 如果平仓交易未清算完成
								if (exitTrade.getVolume() != 0) {
									// 开仓交易已经全部清算完成,则平仓交易剩余的部分等于新的反向开仓交易,添加到队列中
									if (longTradeList.isEmpty()) {
										shortTradeList.add(exitTrade);
										break;
									} else {
										// 进入下一轮循环
									}
								}
							}
						}
					}

				}
				
				// 最后交易日尚未平仓的交易,以最后价格平仓
				double endPrice;
				String endTradingDay;
				if (backtestingDataMode == DATA_MODE_BAR) {
					endPrice = barMap.get(rtSymbol).getClose();
					endTradingDay = barMap.get(rtSymbol).getTradingDay();
				} else {
					endPrice = tickMap.get(rtSymbol).getLastPrice();
					endTradingDay = tickMap.get(rtSymbol).getTradingDay();
				}

				for (Trade longTrade : longTradeList) {
					
					Trade settleTrade = new Trade();
					settleTrade.setDirection(RtConstant.DIRECTION_SHORT);
					settleTrade.setDateTime(lastDateTimeMap.get(rtSymbol));
					settleTrade.setExchange(longTrade.getExchange());
					settleTrade.setGatewayID(longTrade.getGatewayID());
					settleTrade.setOffset(RtConstant.OFFSET_CLOSE);
					settleTrade.setOrderID("Settle-"+longTrade.getOrderID());
					settleTrade.setPrice(endPrice);
					settleTrade.setRtOrderID("Settle-"+longTrade.getRtOrderID());
					settleTrade.setRtSymbol(rtSymbol);
					settleTrade.setRtTradeID("Settle-"+longTrade.getRtTradeID());
					settleTrade.setSymbol(longTrade.getSymbol());
					settleTrade.setTradeDate(lastDateTimeMap.get(rtSymbol).toString(RtConstant.D_FORMAT_INT_FORMATTER));
					settleTrade.setTradeID("Settle-"+longTrade.getTradeID());
					settleTrade.setTradeTime(lastDateTimeMap.get(rtSymbol).toString(RtConstant.T_FORMAT_FORMATTER));
					settleTrade.setTradingDay(endTradingDay);
					settleTrade.setVolume(longTrade.getVolume());
					
					settleTradeMap.put(settleTrade.getRtTradeID(),settleTrade);
					
					TradingResult tradingResult = new TradingResult(longTrade.getPrice(), longTrade.getDateTime(),
							endPrice, lastDateTimeMap.get(rtSymbol), longTrade.getVolume(),
							rateMap.get(gatewayID + "." + rtSymbol), slippageMap.get(rtSymbol),
							contractSizeMap.get(rtSymbol), rtSymbol, gatewayID);

					resultList.add(tradingResult);

				}

				for (Trade shortTrade : shortTradeList) {
					
					Trade settleTrade = new Trade();
					settleTrade.setDirection(RtConstant.DIRECTION_LONG);
					settleTrade.setDateTime(lastDateTimeMap.get(rtSymbol));
					settleTrade.setExchange(shortTrade.getExchange());
					settleTrade.setGatewayID(shortTrade.getGatewayID());
					settleTrade.setOffset(RtConstant.OFFSET_CLOSE);
					settleTrade.setOrderID("Settle-"+shortTrade.getOrderID());
					settleTrade.setPrice(endPrice);
					settleTrade.setRtOrderID("Settle-"+shortTrade.getRtOrderID());
					settleTrade.setRtSymbol(rtSymbol);
					settleTrade.setRtTradeID("Settle-"+shortTrade.getRtTradeID());
					settleTrade.setSymbol(shortTrade.getSymbol());
					settleTrade.setTradeDate(lastDateTimeMap.get(rtSymbol).toString(RtConstant.D_FORMAT_INT_FORMATTER));
					settleTrade.setTradeID("Settle-"+shortTrade.getTradeID());
					settleTrade.setTradeTime(lastDateTimeMap.get(rtSymbol).toString(RtConstant.T_FORMAT_FORMATTER));
					settleTrade.setTradingDay(endTradingDay);
					settleTrade.setVolume(shortTrade.getVolume());
					
					settleTradeMap.put(settleTrade.getRtTradeID(),settleTrade);
					
					TradingResult tradingResult = new TradingResult(shortTrade.getPrice(), shortTrade.getDateTime(),
							endPrice, lastDateTimeMap.get(rtSymbol), -shortTrade.getVolume(),
							rateMap.get(gatewayID + "." + rtSymbol), slippageMap.get(rtSymbol),
							contractSizeMap.get(rtSymbol), rtSymbol, gatewayID);

					resultList.add(tradingResult);


				}
				
				// 检查是否存在交易结果
				if (resultList.isEmpty()) {
					log.warn("无交易结果,合约{}接口{}", rtSymbol, gatewayID);
					continue;
				}
				// 然后基于每笔交易的结果,我们可以计算具体的盈亏曲线和最大回撤等
				double capital = 0; // 资金
				double maxCapital = 0; // 资金最高净值
				double drawdown = 0; // 回撤

				int totalResult = 0; // 总成交数量
				double totalTurnover = 0; // 总成交金额（合约面值）
				double totalCommission = 0; // 总手续费
				double totalSlippage = 0; // 总滑点

				List<DateTime> timeList = new ArrayList<>(); // 时间序列
				List<Double> pnlList = new ArrayList<>(); // 每笔盈亏序列
				List<Double> capitalList = new ArrayList<>(); // 盈亏汇总的时间序列
				List<Double> drawdownList = new ArrayList<>(); // 回撤的时间序列

				int winningResultCount = 0; // 盈利次数
				int losingResultCount = 0; // 亏损次数
				double totalWinning = 0; // 总盈利金额
				double totalLosing = 0; // 总亏损金额

				for (TradingResult result : resultList) {
//					System.out.println("=========================");
//					System.out.println(result.getRtSymbol());
//					System.out.println(result.getGatewayID());
//					System.out.println(result.getEntryDateTime().toString());
//					System.out.println(result.getExitDateTime().toString());
//					System.out.println(result.getVolume());
//					System.out.println("=========================");
					capital += result.getPnl();
					maxCapital = Math.max(capital, maxCapital);
					drawdown = capital - maxCapital;

					pnlList.add(result.getPnl());
					timeList.add(result.getExitDateTime()); // 交易的时间戳使用平仓时间
					capitalList.add(capital);
					drawdownList.add(drawdown);

					totalResult += 1;
					totalTurnover += result.getTurnover();
					totalCommission += result.getCommission();
					totalSlippage += result.getSlippge();

					if (result.getPnl() >= 0) {
						winningResultCount += 1;
						totalWinning += result.getPnl();
					} else {
						losingResultCount += 1;
						totalLosing += result.getPnl();
					}
				}

				// 计算盈亏相关数据
				double winningRate = winningResultCount / totalResult * 100; // 胜率

				double averageWinning = 0; // 这里把数据都初始化为0
				double averageLosing = 0;
				double profitLossRatio = 0;

				if (winningResultCount > 0) {
					averageWinning = totalWinning / winningResultCount; // 平均每笔盈利
				} else if (losingResultCount > 0) {
					averageLosing = totalLosing / losingResultCount; // 平均每笔亏损
				} else if (averageLosing > 0) {
					profitLossRatio = -averageWinning / averageLosing; // 盈亏比
				}

				List<Object> lines = new ArrayList<>();
				List<Object> header = new ArrayList<>();
				List<Object> line = new ArrayList<>();

				BacktestingResult backtestingResult = new BacktestingResult();

				backtestingResult.setRtSymbol(rtSymbol);
				header.add("rtSymbol");
				line.add(rtSymbol);

				backtestingResult.setGatewayID(gatewayID);
				header.add("gatewayID");
				line.add(gatewayID);

				backtestingResult.setCapital(capital);
				header.add("capital");
				line.add(capital);

				backtestingResult.setMaxCapital(maxCapital);
				header.add("maxCapital");
				line.add(maxCapital);

				backtestingResult.setDrawdown(drawdown);
				header.add("drawdown");
				line.add(drawdown);

				backtestingResult.setTotalResult(totalResult);
				header.add("totalResult");
				line.add(totalResult);

				backtestingResult.setTotalTurnover(totalTurnover);
				header.add("totalTurnover");
				line.add(totalTurnover);

				backtestingResult.setTotalCommission(totalCommission);
				header.add("totalCommission");
				line.add(totalCommission);

				backtestingResult.setTotalSlippage(totalSlippage);
				header.add("totalSlippage");
				line.add(totalSlippage);

				backtestingResult.setTimeList(timeList);
				header.add("timeList");
				line.add(timeList.toString());

				backtestingResult.setPnlList(pnlList);
				header.add("pnlList");
				line.add(pnlList);

				backtestingResult.setCapitalList(capitalList);
				header.add("capitalList");
				line.add(JSON.toJSONString(pnlList));

				backtestingResult.setDrawdown(drawdown);
				header.add("drawdown");
				line.add(drawdown);

				backtestingResult.setWinningRate(winningRate);
				header.add("winningRate");
				line.add(winningRate);

				backtestingResult.setAverageWinning(averageWinning);
				header.add("averageWinning");
				line.add(averageWinning);

				backtestingResult.setAverageLosing(averageLosing);
				header.add("averageLosing");
				line.add(averageLosing);

				backtestingResult.setProfitLossRatio(profitLossRatio);
				header.add("profitLossRatio");
				line.add(profitLossRatio);

				backtestingResult.setPosList(posList);
				header.add("posList");
				line.add(JSON.toJSONString(posList));

				backtestingResult.setTradeTimeList(tradeTimeList);
				header.add("tradeTimeList");
				line.add(tradeTimeList.toString());

				backtestingResult.setResultList(resultList);

				gatewayResultMap.put(gatewayID, backtestingResult);

				lines.add(header);
				lines.add(line);

				String filePath = backtestingOutputDir + File.separator + "BacktestingResult" + File.separator
						+ "SN__" + strategy.getName() + File.separator + "SID__" + strategy.getID() + File.separator
						+ "GID__" + gatewayID + "__C__" + rtSymbol + ".csv";
				try {
					FileUtils.forceMkdirParent(new File(filePath));
				} catch (IOException ioe) {
					log.error("创建文件夹发生错误", ioe);
				}
				log.info("写入结果到文件{}", filePath);
				try (CSVPrinter printer = CommonUtil.getCSVPrinter(filePath)) {
					printer.printRecords(lines);
				} catch (Exception e) {
					log.error("写入结果到CSV发生错误", e);
				}

			}

			rtSymbolResultMap.put(rtSymbol, gatewayResultMap);
		}
	}

	/**
	 * 按日计算交易结果
	 */
	private void calculateDailyResult() {

		for (Trade trade : tradeMap.values()) {
			String date = trade.getDateTime().toString(RtConstant.D_FORMAT_INT_FORMATTER);

			DailyResult dailyResult = rtSymbolDailyResultMap.get(trade.getRtSymbol()).get(trade.getGatewayID())
					.get(date);
			dailyResult.addTrade(trade);
		}
		for (Trade trade : settleTradeMap.values()) {
			String date = trade.getDateTime().toString(RtConstant.D_FORMAT_INT_FORMATTER);

			DailyResult dailyResult = rtSymbolDailyResultMap.get(trade.getRtSymbol()).get(trade.getGatewayID())
					.get(date);
			dailyResult.addTrade(trade);
		}

		
		
		for (Entry<String, Map<String, Map<String, DailyResult>>> entry : rtSymbolDailyResultMap.entrySet()) {
			String rtSymbol = entry.getKey();
			Map<String, Map<String, DailyResult>> gatewayDailyResultMap = entry.getValue();

			for (Entry<String, Map<String, DailyResult>> gatewayEntry : gatewayDailyResultMap.entrySet()) {
				String gatewayID = gatewayEntry.getKey();
				Map<String, DailyResult> dateDailyResultMap = gatewayEntry.getValue();
				// 遍历每日结果
				double previousClose = 0;
				int openPosition = 0;
				List<Object> lines = new ArrayList<>();
				List<Object> header = new ArrayList<>();
				header.add("rtSymbol");
				header.add("gatewayID");
				header.add("date");
				header.add("netPnl");
				header.add("totalPnl");
				header.add("positionPnl");
				header.add("tradingPnl");
				header.add("slippage");
				header.add("commission");
				header.add("tradeCount");
				header.add("turnover");
				header.add("closePosition");
				header.add("openPosition");
				header.add("closePrice");
				header.add("previousClose");
				lines.add(header);
				for (DailyResult dailyResult : dateDailyResultMap.values()) {
					dailyResult.setPreviousClose(previousClose);
					previousClose = dailyResult.getClosePrice();

					dailyResult.calculatePnl(openPosition, contractSizeMap.get(rtSymbol),
							rateMap.get(gatewayID + "." + rtSymbol), slippageMap.get(rtSymbol));
					openPosition = dailyResult.getClosePosition();
					List<Object> line = new ArrayList<>();
					line.add(dailyResult.getRtSymbol());
					line.add(dailyResult.getGatewayID());
					line.add(dailyResult.getDate());
					line.add(dailyResult.getNetPnl());
					line.add(dailyResult.getTotalPnl());
					line.add(dailyResult.getPositionPnl());
					line.add(dailyResult.getTradingPnl());
					line.add(dailyResult.getTotalSlippage());
					line.add(dailyResult.getCommission());
					line.add(dailyResult.getTradeCount());
					line.add(dailyResult.getTurnover());
					line.add(dailyResult.getClosePosition());
					line.add(dailyResult.getOpenPosition());
					line.add(dailyResult.getClosePrice());
					line.add(dailyResult.getPreviousClose());

					lines.add(line);

				}

				String filePath = backtestingOutputDir + File.separator + "DailyResult" + File.separator + "SN__"
						+ strategy.getName() + File.separator + "SID__" + strategy.getID() + File.separator + "GID__"
						+ gatewayID + "__C__" + rtSymbol + ".csv";
				try {
					FileUtils.forceMkdirParent(new File(filePath));
				} catch (IOException ioe) {
					log.error("创建文件夹发生错误", ioe);
				}
				log.info("写入结果到文件{}", filePath);
				try (CSVPrinter printer = CommonUtil.getCSVPrinter(filePath)) {
					printer.printRecords(lines);
				} catch (Exception e) {
					log.error("写入结果到CSV发生错误", e);
				}
			}
		}
	}

	@Override
	public void emitErrorLog(String logContent) {
		log.error("E_LOG|ZEUS--"+logContent);
	}

	@Override
	public void emitWarnLog(String logContent) {
		log.warn("E_LOG|ZEUS--"+logContent);
	}

	@Override
	public void emitInfoLog(String logContent) {
		log.info("E_LOG|ZEUS--"+logContent);
	}

	@Override
	public void emitDebugLog(String logContent) {
		log.debug("E_LOG|ZEUS--"+logContent);
	}
	
	///////////////////////////// ↓↓↓↓↓↓↓回测不需要实现的方法↓↓↓↓↓↓↓////////////////////////////
	@Override
	public void onTick(Tick tick) {
	}

	@Override
	public void onOrder(Order order) {
	}

	@Override
	public void onTrade(Trade trade) {
	}

	@Override
	public void createStrategyClassInstance(StrategySetting strategySetting) {
	}

	@Override
	public void unloadStrategy(String strategyID) {
	}

	@Override
	public void loadStartegy() {
	}

	@Override
	public void loadStartegy(String strategyID) {
	}

	@Override
	public void initStrategy(String strategyID) {
	}

	@Override
	public void startStrategy(String strategyID) {
	}

	@Override
	public void stopStrategy(String strategyID) {
	}

	@Override
	public void initAllStrategy() {
	}

	@Override
	public void startAllStrategy() {
	}

	@Override
	public void stopAllStrategy() {
	}

	@Override
	public List<Strategy> getStragetyList() {
		return null;
	}

	@Override
	public double getPriceTick(String rtSymbol, String gatewayID) {
		return 0;
	}

	@Override
	public Contract getContract(String rtSymbol) {
		return null;
	}

	@Override
	public Contract getContract(String rtSymbol, String gatewayID) {
		return null;
	}

	@Override
	public void awaitShutdown() throws InterruptedException {
	}

	@Override
	public void onEvent(FastEvent event, long sequence, boolean endOfBatch) throws Exception {
		
	}

	@Override
	public void onStart() {
		
	}

	@Override
	public void onShutdown() {
		
	}
	@Override
	public List<String> getSubscribedEventList() {
		return null;
	}

	@Override
	public Set<String> getSubscribedEventSet() {
		return null;
	}

	@Override
	public void subscribeEvent(String event) {
		
	}

	@Override
	public void unsubscribeEvent(String event) {
		
	}
	
	///////////////////////////// ↑↑↑↑↑↑↑回测不需要实现的方法↑↑↑↑↑↑↑////////////////////////////




}
