package xyz.redtorch.trader.module.zeus.impl;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.engine.event.EventConstant;
import xyz.redtorch.trader.engine.event.FastEvent;
import xyz.redtorch.trader.engine.event.FastEventEngine;
import xyz.redtorch.trader.engine.main.MainEngine;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.CancelOrderReq;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.SubscribeReq;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.ModuleAbstract;
import xyz.redtorch.trader.module.zeus.ZeusConstant;
import xyz.redtorch.trader.module.zeus.ZeusEngine;
import xyz.redtorch.trader.module.zeus.ZeusDataUtil;
import xyz.redtorch.trader.module.zeus.ZeusUtil;
import xyz.redtorch.trader.module.zeus.entity.ContractPositionDetail;
import xyz.redtorch.trader.module.zeus.entity.PositionDetail;
import xyz.redtorch.trader.module.zeus.strategy.Strategy;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;
import xyz.redtorch.utils.CommonUtil;

/**
 * @author sun0x00@gmail.com
 */
public class TradingEngineImpl extends ModuleAbstract implements ZeusEngine {

	private Logger log = LoggerFactory.getLogger(TradingEngineImpl.class);

	private static final String moduleID = "MODULE_ZEUS";
	private static final String moduleDisplayName = "Zeus实盘引擎";
	private static final String logStr = "模块ID-[" + moduleID + "] 名称-[" + moduleDisplayName + "] >>> ";

	private ZeusDataUtil zeusDataUtil;

	// 使用无大小限制的线程池,线程空闲60s会被释放
	ExecutorService executor = Executors.newCachedThreadPool();

	Map<String, Strategy> strategyMap = new ConcurrentHashMap<>(); // 策略Map

	// 用于异步存储变量的队列(减少策略的IO等待时间,主要用于节省回测时间)
	LinkedBlockingQueue<Map<String, String>> syncVarMapSaveQueue = new LinkedBlockingQueue<>();
	LinkedBlockingQueue<PositionDetail> positionDetailSaveQueue = new LinkedBlockingQueue<>();

	public TradingEngineImpl(MainEngine mainEngine) {
		super(mainEngine);
		zeusDataUtil = new ZeusDataUtilImpl(mainEngine.getDataEngine());
		executor.execute(new SaveSyncVarMapTask());
		executor.execute(new SavePositionTask());
	}

	@Override
	public int getEngineType() {
		return ZeusConstant.ENGINE_TYPE_TRADING;
	}

	@Override
	public void onEvent(final FastEvent fastEvent, final long sequence, final boolean endOfBatch) throws Exception {

		String logContent;
		if (!subscribedEventSet.contains(fastEvent.getEvent())) {
			return;
		}
		// 判断消息类型
		if (EventConstant.EVENT_TICK.equals(fastEvent.getEventType())) {
			try {
				Tick tick = fastEvent.getTick();
				onTick(tick);
			} catch (Exception e) {
				logContent = logStr + "onTick发生异常!!!";
				emitErrorLog(logContent);
				log.error(logContent, e);
			}
		} else if (EventConstant.EVENT_TRADE.equals(fastEvent.getEventType())) {
			try {
				Trade trade = fastEvent.getTrade();
				onTrade(trade);
			} catch (Exception e) {
				logContent = logStr + "onTrade发生异常!!!";
				emitErrorLog(logContent);
				log.error(logContent, e);
			}
		} else if (EventConstant.EVENT_ORDER.equals(fastEvent.getEventType())) {
			try {
				Order order = fastEvent.getOrder();
				onOrder(order);
			} catch (Exception e) {
				logContent = logStr + "onOrder发生异常!!!";
				emitErrorLog(logContent);
				log.error(logContent, e);
			}
		} else {
			logContent = logStr + "未能识别的事件数据类型" + JSON.toJSONString(fastEvent.getEvent());
			emitWarnLog(logContent);
			log.warn(logContent);
		}
	}

	@Override
	public String getModuleID() {
		return moduleID;
	}

	@Override
	public String getModuleDisplayName() {
		return moduleDisplayName;
	}

	@Override
	public String getLogStr() {
		return logStr;
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onShutdown() {
		stopAllStrategy();
		shutdownLatch.countDown();
	}

	@Override
	public String sendOrder(OrderReq orderReq, Strategy strategy) {
		String rtOrderID = mainEngine.sendOrder(orderReq);

		strategy.subscribeEvent(EventConstant.EVENT_ORDER + rtOrderID);
		subscribeEvent(EventConstant.EVENT_ORDER + rtOrderID);

		return rtOrderID;
	}

	@Override
	public void cancelOrder(String rtOrderID) {
		Order order = mainEngine.getOrder(rtOrderID);
		if (order != null) {
			if (!RtConstant.STATUS_FINISHED.contains(order.getStatus())) {

				CancelOrderReq cancelOrderReq = new CancelOrderReq();

				cancelOrderReq.setSymbol(order.getSymbol());
				cancelOrderReq.setExchange(order.getExchange());

				cancelOrderReq.setFrontID(order.getFrontID());
				cancelOrderReq.setSessionID(order.getSessionID());
				cancelOrderReq.setOrderID(order.getOrderID());
				cancelOrderReq.setGatewayID(order.getOrderID());
				mainEngine.cancelOrder(cancelOrderReq);

			} else {
				String logContent = logStr + "无法撤单,委托状态为完成,rtOrderID:" + rtOrderID;
				emitWarnLog(logContent);
				log.warn(logContent);
			}
		} else {
			String logContent = logStr + "无法撤单,委托不存在,rtOrderID:" + rtOrderID;
			emitWarnLog(logContent);
			log.warn(logContent);
		}

	}

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
	public void loadStartegy() {
		String logContent = logStr + "加载所有策略";
		emitInfoLog(logContent);
		log.info(logContent);
		scanAndLoadStartegy(null);
	}

	@Override
	public void loadStartegy(String strategyID) {

		String logContent = logStr + "加载指定策略,策略ID:" + strategyID;
		emitInfoLog(logContent);
		log.info(logContent);

		scanAndLoadStartegy(strategyID);
	}

	@Override
	public void scanAndLoadStartegy(String strategyID) {

		String logContent;

		Set<Class<?>> classes = CommonUtil.getClasses("xyz.redtorch.trader");
		if (classes == null) {
			logContent = logStr + "未能在包xyz.redtorch.trader下扫描到任何类!!!";
			emitErrorLog(logContent);
			log.error(logContent);
		} else {
			// 寻找Strategy的实现类,不包含抽象类
			Set<Class<?>> filteredClasses = CommonUtil.getImplementsByInterface(Strategy.class, classes, false);
			if (filteredClasses.isEmpty()) {
				logContent = logStr + "未能在包xyz.redtorch.trader下扫描到任何实现了Strategy接口的策略!!!";
				emitErrorLog(logContent);
				log.error(logContent);
			} else {
				for (Class<?> clazz : filteredClasses) {

					String classSimpleName = clazz.getSimpleName();
					String className = clazz.getSimpleName();
					File strategyConfigFile = ZeusUtil.getStartegyConfigFile(classSimpleName + "-Setting.json");

					logContent = logStr + "策略类" + className + "对应的配置文件临时路径" + strategyConfigFile.getAbsolutePath();
					emitInfoLog(logContent);
					log.info(logContent);

					if (!strategyConfigFile.exists() || strategyConfigFile.isDirectory()) {
						logContent = logStr + "未能找到策略类" + className + "对应的配置文件" + classSimpleName + "-Setting.json";
						emitErrorLog(logContent);
						log.error(logContent);
					} else {
						String configString = CommonUtil.readFileToString(strategyConfigFile.getAbsolutePath());
						if (StringUtils.isEmpty(configString)) {
							logContent = logStr + "读取策略类" + className + "对应的配置文件" + classSimpleName
									+ "-Setting.json发生异常";
							emitErrorLog(logContent);
							log.error(logContent);
						} else {
							StrategySetting strategySetting = null;
							try {
								strategySetting = JSON.parseObject(configString, StrategySetting.class);
								if (strategySetting == null) {
									logContent = logStr + "解析策略类" + className + "对应的配置文件" + classSimpleName
											+ "-Setting.json发生异常";
									emitErrorLog(logContent);
									log.error(logContent);
								}

								// 如果参数StrategyID不为空,表示加载指定策略,其它忽略
								if (!StringUtils.isEmpty(strategyID) && !strategyID.equals(strategySetting.getId())) {
									continue;
								}

								// 合成一些配置
								strategySetting.fixSetting();

								/////////////////////////////
								// 对配置文件进行基本检查
								////////////////////////////
								if (StringUtils.isEmpty(strategySetting.getId())) {
									logContent = logStr + "解析策略类" + className + "对应的配置文件" + classSimpleName
											+ "-Setting.json未能找到策略ID配置";
									emitErrorLog(logContent);
									log.error(logContent);
									continue;
								}
								if (StringUtils.isEmpty(strategySetting.getName())) {
									logContent = logStr + "解析策略类" + className + "对应的配置文件" + classSimpleName
											+ "-Setting.json未能找到策略Name配置";
									emitErrorLog(logContent);
									log.error(logContent);
									continue;
								}
								if (StringUtils.isEmpty(strategySetting.getTradingDay())) {
									logContent = logStr + "解析策略类" + className + "对应的配置文件" + classSimpleName
											+ "-Setting.json未能找到tradingDay配置";
									emitErrorLog(logContent);
									log.error(logContent);
									continue;
								}
								if (strategySetting.getGateways() == null || strategySetting.getGateways().isEmpty()) {
									logContent = logStr + "解析策略类" + className + "对应的配置文件" + classSimpleName
											+ "-Setting.json未能找到gateways配置";
									emitErrorLog(logContent);
									log.error(logContent);
									continue;
								}
								if (strategySetting.getContracts() == null
										|| strategySetting.getContracts().isEmpty()) {
									logContent = logStr + "解析策略类" + className + "对应的配置文件" + classSimpleName
											+ "-Setting.json未能找到contracts配置";
									emitErrorLog(logContent);
									log.error(logContent);
									continue;
								}

								boolean error = false;
								for (StrategySetting.ContractSetting contractSetting : strategySetting.getContracts()) {
									if (contractSetting.getTradeGateways() == null
											|| contractSetting.getTradeGateways().isEmpty()) {
										logContent = logStr + "解析策略类" + className + "对应的配置文件" + classSimpleName
												+ "-Setting.json未能找到合约" + contractSetting.getRtSymbol()
												+ "的tradeGateways配置";
										emitErrorLog(logContent);
										log.error(logContent);
										error = true;
										break;
									}

								}
								if (error) {
									continue;
								}
								////////////////////////////

							} catch (Exception e) {

								logContent = logStr + "解析策略类" + className + "对应的配置文件" + classSimpleName
										+ "-Setting.json发生异常";
								emitErrorLog(logContent);
								log.error(logContent, e);
							}

							if (strategySetting != null) {
								if (strategyMap.containsKey(strategySetting.getId())) {

									logContent = logStr + "策略:" + strategySetting.getName() + "ID:"
											+ strategySetting.getId() + "已经加载,不允许重复加载!";
									emitWarnLog(logContent);
									log.warn(logContent);

									continue;
								}

								try {
									Constructor<?> c = clazz.getConstructor(ZeusEngine.class, StrategySetting.class);
									Strategy strategy = (Strategy) c.newInstance(this, strategySetting);

									// 启动策略线程（不是初始化也不是启动交易,仅仅是启动线程）
									// 初始化之后就应该立即启动线程,便于通过事件结束run方法实现销毁
									FastEventEngine.addHandler(strategy);
									// Map缓存策略
									strategyMap.put(strategySetting.getId(), strategy);

									logContent = logStr + "策略:" + strategySetting.getName() + "ID:"
											+ strategySetting.getId() + "实现类" + className + "加载成功!";
									emitInfoLog(logContent);
									log.info(logContent);
								} catch (NoSuchMethodException | SecurityException | InstantiationException
										| IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e) {

									logContent = logStr + "反射创建策略类" + className + "实例发生异常";
									emitErrorLog(logContent);
									log.error(logContent, e);
								}
							}

						}

					}

				}
			}

		}

	}

	@Override
	public void unloadStrategy(String strategyID) {
		Strategy strategy = strategyMap.get(strategyID);
		if (strategy != null) {
			strategy.stopTrading(false);
			FastEventEngine.removeHandler(strategy);
			// 取消订阅合约
			for (StrategySetting.gatewaySetting gatewaySetting : strategy.getStrategySetting().getGateways()) {
				String gatewayID = gatewaySetting.getGatewayID();
				for (String rtSymbol : gatewaySetting.getSubscribeRtSymbols()) {
					String logContent = logStr + "卸载策略,ID:" + strategyID + ",取消订阅接口" + gatewayID + "合约" + rtSymbol;
					emitInfoLog(logContent);
					log.info(logContent);
					mainEngine.unsubscribe(rtSymbol, gatewayID, strategyID);
				}
			}
			strategyMap.remove(strategyID);
			String logContent = logStr + "成功卸载策略,ID:" + strategyID;
			emitInfoLog(logContent);
			log.info(logContent);
		} else {
			String logContent = logStr + "未找到策略,卸载失败,ID:" + strategyID;
			emitErrorLog(logContent);
			log.error(logContent);
		}
	}

	@Override
	public void initStrategy(String strategyID) {
		String logContent;
		Strategy strategy = strategyMap.get(strategyID);
		if (strategy != null) {
			if (strategy.isInitStatus()) {
				logContent = logStr + "初始化" + strategy.getLogStr() + "已经初始化,不允许重复初始化";
				emitWarnLog(logContent);
				log.warn(logContent);

				return;
			}

			StrategySetting strategySetting = strategy.getStrategySetting();

			String strategyName = strategySetting.getName();
			/******************** 初始化变量 ******************************/
			// 从文件中加载所有变量
			if (strategySetting.getVarMap() != null) {
				strategy.getVarMap().putAll(strategySetting.getVarMap());
			} else {
				logContent = logStr + "初始化" + strategy.getLogStr() + "配置文件中varMap为空";
				emitWarnLog(logContent);
				log.warn(logContent);
			}

			// 从MongoDB中加载所有存储的变量,如果和文件配置冲突,则覆盖
			Map<String, String> dbSyncVarMap = zeusDataUtil.loadStrategySyncVarMap(strategy.getID());
			if (dbSyncVarMap.isEmpty()) {
				logContent = logStr + "初始化" + strategy.getLogStr() + "数据库中varMap为空";
				emitWarnLog(logContent);
				log.warn(logContent);
			} else {
				// 过滤不在syncList中的kv
				Map<String, String> syncVarMap = new HashMap<>();
				for (String key : strategySetting.getSyncVarList()) {
					if (dbSyncVarMap.containsKey(key)) {
						syncVarMap.put(key, syncVarMap.get(key));
					}
				}
				strategy.getVarMap().putAll(syncVarMap);
			}

			logContent = logStr + "初始化" + strategy.getLogStr() + "初始化varMap完成";
			emitInfoLog(logContent);
			log.info(logContent);

			/********************** 初始化参数 ****************************/
			if (strategySetting.getParamMap() != null) {
				strategy.getParamMap().putAll(strategySetting.getParamMap());
			} else {
				logContent = logStr + "初始化" + strategy.getLogStr() + "配置文件中paramMap为空";
				emitWarnLog(logContent);
				log.warn(logContent);
			}

			logContent = logStr + "初始化" + strategy.getLogStr() + "初始化paramMap完成";
			emitInfoLog(logContent);
			log.info(logContent);

			/********************** 初始化持仓 ****************************/
			Map<String, ContractPositionDetail> contractPositionMap = strategy.getContractPositionMap();
			Set<String> contractGatewayKeySet = new HashSet<>(); // 用于后续判断数据库中读取的数据是否和配置匹配
			for (StrategySetting.ContractSetting contractSetting : strategySetting.getContracts()) {
				String rtSymbol = contractSetting.getRtSymbol();
				if (!contractPositionMap.containsKey(rtSymbol)) {
					contractPositionMap.put(rtSymbol, new ContractPositionDetail());
				}

				for (StrategySetting.TradeGatewaySetting tradeGatewaySetting : contractSetting.getTradeGateways()) {
					String contractGatewayKey = rtSymbol + tradeGatewaySetting.getGatewayID();
					contractGatewayKeySet.add(contractGatewayKey);
				}

			}

			String tradingDay = strategySetting.getTradingDay();

			List<PositionDetail> tdPositionDetailList = zeusDataUtil.loadStrategyPositionDetails(tradingDay, strategyID,
					strategyName);
			if (tdPositionDetailList.isEmpty()) {
				logContent = logStr + "初始化" + strategy.getLogStr() + "当日持仓数据记录为空,尝试读取前一交易日";
				emitWarnLog(logContent);
				log.warn(logContent);

				String preTradingDay = strategySetting.getPreTradingDay();
				if (StringUtils.isEmpty(preTradingDay)) {
					logContent = logStr + "初始化" + strategy.getLogStr() + "前一交易日配置为空";
					emitWarnLog(logContent);
					log.warn(logContent);
				} else {
					List<PositionDetail> ydPositionDetailList = zeusDataUtil.loadStrategyPositionDetails(preTradingDay,
							strategyID, strategyName);
					if (ydPositionDetailList.isEmpty()) {
						logContent = logStr + "初始化" + strategy.getLogStr() + "前一日持仓数据记录为空";
						emitWarnLog(logContent);
						log.warn(logContent);
					} else {

						List<PositionDetail> insertPositionDetailList = new ArrayList<>();
						for (PositionDetail ydPositionDetail : ydPositionDetailList) {
							String rtSymbol = ydPositionDetail.getRtSymbol();
							String gatewayID = ydPositionDetail.getGatewayID();

							if (contractPositionMap.containsKey(rtSymbol)) {
								ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);
								String tmpContractGatewayKey = rtSymbol + gatewayID;

								if (contractGatewayKeySet.contains(tmpContractGatewayKey)) {

									PositionDetail tdPositionDetail = new PositionDetail(rtSymbol, gatewayID,
											preTradingDay, strategyName, strategyID, ydPositionDetail.getExchange(),
											ydPositionDetail.getContractSize());
									tdPositionDetail
											.setLongYd(ydPositionDetail.getLongYd() + ydPositionDetail.getLongTd());
									tdPositionDetail
											.setShortYd(ydPositionDetail.getShortYd() + ydPositionDetail.getShortTd());
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
									logContent = logStr + "初始化" + strategy.getLogStr() + "从数据库中读取到合约" + rtSymbol
											+ "接口ID" + gatewayID + "组合与配置不匹配";
									emitErrorLog(logContent);
									log.error(logContent);
								}
							} else {
								logContent = logStr + "初始化" + strategy.getLogStr() + "从数据库中读取到配置中不存在的合约" + rtSymbol;
								emitErrorLog(logContent);
								log.error(logContent);
							}
						}

						// 存入数据库
						asyncSavePositionDetail(insertPositionDetailList);

					}
				}
			} else {
				List<PositionDetail> insertPositionDetailList = new ArrayList<>();
				for (PositionDetail positionDetail : tdPositionDetailList) {
					String rtSymbol = positionDetail.getRtSymbol();
					String gatewayID = positionDetail.getGatewayID();

					if (contractPositionMap.containsKey(rtSymbol)) {
						ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);
						String tmpContractGatewayKey = rtSymbol + gatewayID;

						if (contractGatewayKeySet.contains(tmpContractGatewayKey)) {
							positionDetail.setTradingDay(tradingDay);
							contractPositionDetail.getPositionDetailMap().put(gatewayID, positionDetail);
							contractPositionDetail.calculatePosition();
							insertPositionDetailList.add(positionDetail);
						} else {
							logContent = logStr + "初始化" + strategy.getLogStr() + "从数据库中读取到合约" + rtSymbol + "接口ID"
									+ gatewayID + "组合与配置不匹配";
							emitErrorLog(logContent);
							log.error(logContent);
						}
					} else {
						logContent = logStr + "初始化" + strategy.getLogStr() + "从数据库中读取到配置中不存在的合约" + rtSymbol;
						emitErrorLog(logContent);
						log.error(logContent);
					}
				}

			}

			logContent = logStr + "初始化" + strategy.getLogStr() + "初始化持仓完成";
			emitInfoLog(logContent);
			log.info(logContent);
			/************************ 订阅合约注册事件 *******************/
			// 通过配置订阅合约注册事件
			for (StrategySetting.gatewaySetting gatewaySetting : strategy.getStrategySetting().getGateways()) {
				String gatewayID = gatewaySetting.getGatewayID();

				for (String rtSymbol : gatewaySetting.getSubscribeRtSymbols()) {
					// 为策略注册Tick数据监听事件
					String event = EventConstant.EVENT_TICK + gatewayID + rtSymbol;
					strategy.subscribeEvent(event);

					logContent = logStr + "初始化" + strategy.getLogStr() + "注册事件监听" + event;
					emitInfoLog(logContent);
					log.info(logContent);

					// 订阅合约
					SubscribeReq subscribeReq = new SubscribeReq();
					subscribeReq.setRtSymbol(rtSymbol);
					subscribeReq.setGatewayID(gatewayID);
					mainEngine.subscribe(subscribeReq, strategyID);

					logContent = logStr + "初始化" + strategy.getLogStr() + "通过接口" + gatewayID + "订阅合约" + rtSymbol;
					emitInfoLog(logContent);
					log.info(logContent);
				}
			}
			logContent = logStr + "初始化" + strategy.getLogStr() + "订阅行情完成";
			emitInfoLog(logContent);
			log.info(logContent);
			/*********************************************************/
			strategy.init();
		} else {
			logContent = logStr + "未找到策略,初始化失败,策略ID:" + strategyID;
			emitErrorLog(logContent);
			log.error(logContent);
		}
	}

	@Override
	public void startStrategy(String strategyID) {

		String logContent = logStr + "启动策略,策略ID:" + strategyID;
		emitInfoLog(logContent);
		log.info(logContent);

		Strategy strategy = strategyMap.get(strategyID);
		if (strategy != null) {
			strategy.startTrading();
		} else {
			logContent = logStr + "未找到策略,启动失败,策略ID:" + strategyID;
			emitErrorLog(logContent);
			log.error(logContent);
		}
	}

	@Override
	public void stopStrategy(String strategyID) {
		String logContent = logStr + "停止策略,策略ID:" + strategyID;
		emitInfoLog(logContent);
		log.info(logContent);

		Strategy strategy = strategyMap.get(strategyID);
		if (strategy != null) {
			strategy.stopTrading(false);
		} else {
			logContent = logStr + "未找到策略,停止失败,策略ID:" + strategyID;
			emitErrorLog(logContent);
			log.error(logContent);
		}
	}

	@Override
	public void initAllStrategy() {

		String logContent = logStr + "初始化所有策略";
		emitInfoLog(logContent);
		log.info(logContent);

		for (Strategy strategy : strategyMap.values()) {
			initStrategy(strategy.getID());
		}
	}

	@Override
	public void startAllStrategy() {

		String logContent = logStr + "启动所有策略";
		emitInfoLog(logContent);
		log.info(logContent);

		for (Strategy strategy : strategyMap.values()) {
			strategy.startTrading();
		}
	}

	@Override
	public void stopAllStrategy() {

		String logContent = logStr + "停止所有策略";
		emitInfoLog(logContent);
		log.info(logContent);

		for (Strategy strategy : strategyMap.values()) {
			strategy.stopTrading(false);
		}
	}

	@Override
	public List<Tick> loadTickDataByOffsetDay(String tradingDay, int offsetDay, String rtSymbol) {
		DateTime tradingDateTime = RtConstant.D_FORMAT_INT_Formatter.parseDateTime(tradingDay);
		DateTime endDateTime = tradingDateTime.minusDays(1);
		DateTime startDateTime = endDateTime.minusDays(offsetDay);

		return this.loadTickData(startDateTime, endDateTime, rtSymbol);
	}

	@Override
	public List<Bar> loadBarDataByOffsetDay(String tradingDay, int offsetDay, String rtSymbol) {

		DateTime tradingDateTime = RtConstant.D_FORMAT_INT_Formatter.parseDateTime(tradingDay);
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
	public void asyncSaveSyncVarMap(String strategyID, String strategyName, Map<String, String> syncVarMap) {
		// 实现深度复制,避免引用被修改
		Map<String, String> saveSyncVarMap = SerializationUtils.clone(new HashMap<>(syncVarMap));
		saveSyncVarMap.put("strategyID", strategyID);
		saveSyncVarMap.put("strategyName", strategyName);
		syncVarMapSaveQueue.add(saveSyncVarMap);
	}

	@Override
	public void asyncSavePositionDetail(List<PositionDetail> positionDetailList) {

		List<PositionDetail> savePositionDetailList = new ArrayList<>();
		for (PositionDetail positionDetail : positionDetailList) {
			// 深度复制
			PositionDetail savePositionDetail = SerializationUtils.clone(positionDetail);
			savePositionDetailList.add(savePositionDetail);
		}
		positionDetailSaveQueue.addAll(savePositionDetailList);

	}

	private class SavePositionTask implements Runnable {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					PositionDetail positionDetail = positionDetailSaveQueue.take();
					zeusDataUtil.saveStrategyPositionDetail(positionDetail);
				} catch (InterruptedException e) {
					log.error("{} 保存持仓任务捕获到线程中断异常,线程停止!!!", logStr, e);
				}
			}
		}

	}

	private class SaveSyncVarMapTask implements Runnable {
		@Override
		public void run() {
			try {
				Map<String, String> syncVarMapWithNameAndID = syncVarMapSaveQueue.take();
				zeusDataUtil.saveStrategySyncVarMap(syncVarMapWithNameAndID);
			} catch (InterruptedException e) {
				log.error("{} 保存变量任务捕获到线程中断异常,线程停止!!!", logStr, e);
			}
		}

	}

	@Override
	public List<Strategy> getStragetyList() {
		return new ArrayList<Strategy>(strategyMap.values());
	}

	@Override
	public double getPriceTick(String rtSymbol, String gatewayID) {
		Contract contract = mainEngine.getContract(rtSymbol, gatewayID);
		return contract.getPriceTick();
	}

	@Override
	public Contract getContract(String rtSymbol) {
		return mainEngine.getContract(rtSymbol);
	}

	@Override
	public Contract getContract(String rtSymbol, String gatewayID) {
		return mainEngine.getContract(rtSymbol, gatewayID);
	}

	@Override
	public void emitErrorLog(String logContent) {
		String event = EventConstant.EVENT_LOG + "ZEUS|";
		CommonUtil.emitLogBase(event, RtConstant.LOG_ERROR, logContent);
	}

	@Override
	public void emitWarnLog(String logContent) {
		String event = EventConstant.EVENT_LOG + "ZEUS|";
		CommonUtil.emitLogBase(event, RtConstant.LOG_WARN, logContent);
	}

	@Override
	public void emitInfoLog(String logContent) {
		String event = EventConstant.EVENT_LOG + "ZEUS|";
		CommonUtil.emitLogBase(event, RtConstant.LOG_INFO, logContent);
	}

	@Override
	public void emitDebugLog(String logContent) {
		String event = EventConstant.EVENT_LOG + "ZEUS|";
		CommonUtil.emitLogBase(event, RtConstant.LOG_DEBUG, logContent);
	}

}
