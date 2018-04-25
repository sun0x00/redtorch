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
import xyz.redtorch.trader.engine.event.EventData;
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

	LinkedBlockingQueue<EventData> eventDataQueue = new LinkedBlockingQueue<>();

	private static final String moduleID = "9ee63ae1-7924-44d2-8705-53500afe6135";
	private static final String moduleDisplayName = "Zeus实盘引擎";
	private static final String logStr = "Module:" + moduleDisplayName + " ID:" + moduleID;

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
	public void onEvent(EventData eventData) {
		if (eventData != null) {
			eventDataQueue.add(eventData);
		}
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			EventData ed = null;
			try {
				ed = eventDataQueue.take();
			} catch (InterruptedException e) {
				log.error("{} 捕获到线程中断异常,线程停止!!!", logStr, e);
			}
			// 判断消息类型
			if (EventConstant.EVENT_TICK.equals(ed.getEventType())) {
				try {
					Tick tick = (Tick) ed.getEventObj();
					onTick(tick);
				} catch (Exception e) {
					log.error("{} onTick发生异常!!!", logStr, e);
				}
			} else if (EventConstant.EVENT_TRADE.equals(ed.getEventType())) {
				try {
					Trade trade = (Trade) ed.getEventObj();
					onTrade(trade);
				} catch (Exception e) {
					log.error("{} onTrade发生异常!!!", logStr, e);
				}
			} else if (EventConstant.EVENT_ORDER.equals(ed.getEventType())) {
				try {
					Order order = (Order) ed.getEventObj();
					onOrder(order);
				} catch (Exception e) {
					log.error("{} onOrder发生异常!!!", logStr, e);
				}
			} else if(EventConstant.EVENT_THREAD_STOP.equals(ed.getEventType())){
				// 弃用
				//Thread.currentThread().interrupt();
				break;
			} else {
				log.warn("{} 未能识别的事件数据类型{}", logStr, JSON.toJSONString(ed));
			}
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
	public void stop() {
		stopAllStrategy();
		executor.shutdownNow();
		// 通知其他线程
		EventData eventData = new EventData();
		eventData.setEvent(EventConstant.EVENT_THREAD_STOP);
		eventData.setEventType(EventConstant.EVENT_THREAD_STOP);
		eventDataQueue.add(eventData);
	}

	@Override
	public String sendOrder(OrderReq orderReq,Strategy strategy) {
		String rtOrderID = mainEngine.sendOrder(orderReq);
		mainEventEngine.registerListener(EventConstant.EVENT_ORDER + rtOrderID, strategy);
		mainEventEngine.registerListener(EventConstant.EVENT_ORDER + rtOrderID, this);
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
			}
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
		scanAndLoadStartegy(null);
	}
	
	@Override
	public void loadStartegy(String strategyID) {
		scanAndLoadStartegy(strategyID);
	}
	
	@Override
	public void scanAndLoadStartegy(String strategyID) {
		Set<Class<?>> classes = CommonUtil.getClasses("xyz.redtorch.trader");
		if (classes == null) {
			log.error("{} 未能在包xyz.redtorch.trader下扫描到任何类");
		} else {
			// 寻找Strategy的实现类,不包含抽象类
			Set<Class<?>> filteredClasses = CommonUtil.getImplementsByInterface(Strategy.class, classes, false);
			if (filteredClasses.isEmpty()) {
				log.error("{} 未能在包xyz.redtorch.trader下扫描到任何实现了Strategy接口的策略", logStr);
			} else {
				for (Class<?> clazz : filteredClasses) {

					String classSimpleName = clazz.getSimpleName();
					String className = clazz.getSimpleName();
					File strategyConfigFile = ZeusUtil.getStartegyConfigFile(classSimpleName + "-Setting.json");
					if (!strategyConfigFile.exists() || strategyConfigFile.isDirectory()) {
						log.error("{} 未能找到策略{}对应的配置文件{}", logStr, className, classSimpleName + "-Setting.json");
					} else {
						String configString = CommonUtil.readFileToString(strategyConfigFile.getAbsolutePath());
						if (StringUtils.isEmpty(configString)) {
							log.error("{} 读取策略{}对应的配置文件{}发生异常", logStr, className, classSimpleName + "-Setting.json");
						} else {
							StrategySetting strategySetting = null;
							try {
								strategySetting = JSON.parseObject(configString, StrategySetting.class);
								if(strategySetting == null) {
									log.error("{} 解析策略{}对应的配置文件{}发生异常", logStr, className, classSimpleName + "-Setting.json");
									continue;
								}
								
								// 如果参数StrategyID不为空,表示加载指定策略,其它忽略
								if(!StringUtils.isEmpty(strategyID) && !strategyID.equals(strategySetting.getId())) {
									continue;
								}
								
								// 合成一些配置
								strategySetting.fixSetting();

								/////////////////////////////
								// 对配置文件进行基本检查
								////////////////////////////
								if (StringUtils.isEmpty(strategySetting.getId())) {
									log.error("{} 解析策略{}对应的配置文件{} 未能找到ID配置", logStr, className,
											classSimpleName + "-Setting.json");
									continue;
								}
								if (StringUtils.isEmpty(strategySetting.getName())) {
									log.error("{} 解析策略{}对应的配置文件{} 未能找到Name配置", logStr, className,
											classSimpleName + "-Setting.json");
									continue;
								}
								if (StringUtils.isEmpty(strategySetting.getTradingDay())) {
									log.error("{} 解析策略{}对应的配置文件{} 未能找到tradingDay配置", logStr, className,
											classSimpleName + "-Setting.json");
									continue;
								}
								if (strategySetting.getGateways() == null || strategySetting.getGateways().isEmpty()) {
									log.error("{} 解析策略{}对应的配置文件{} 未能找到gateways配置", logStr, className,
											classSimpleName + "-Setting.json");
									continue;
								}
								if (strategySetting.getContracts() == null
										|| strategySetting.getContracts().isEmpty()) {
									log.error("{} 解析策略{}对应的配置文件{} 未能找到contracts配置", logStr, className,
											classSimpleName + "-Setting.json");
									continue;
								}

								boolean error = false;
								for (StrategySetting.TradeContractSetting tradeContractSetting : strategySetting
										.getContracts()) {
									if (tradeContractSetting.getTradeGateways() == null
											|| tradeContractSetting.getTradeGateways().isEmpty()) {
										log.error("{} 解析策略{}对应的配置文件{} 未能找到合约{}的gateways配置", logStr, className,
												classSimpleName + "-Setting.json", tradeContractSetting.getRtSymbol());
										error = true;
										break;
									}

								}
								if (error) {
									continue;
								}
								////////////////////////////

							} catch (Exception e) {
								log.error("{} 解析策略{}对应的配置文件{}发生异常", logStr, className,
										classSimpleName + "-Setting.json", e);
							}

							if (strategySetting != null) {
								if(strategyMap.containsKey(strategySetting.getId())) {
									log.info("{} 策略-{} ID-{} 已经加载,不允许重复加载", logStr, strategySetting.getName(), strategySetting.getId(), className);
									continue;
								}

								try {
									Constructor<?> c = clazz.getConstructor(ZeusEngine.class, StrategySetting.class);
									Strategy strategy = (Strategy) c.newInstance(this, strategySetting);

									// 启动策略线程（不是初始化也不是启动交易,仅仅是启动线程）
									// 初始化之后就应该立即启动线程,便于通过事件结束run方法实现销毁
									executor.execute(strategy);
									// Map缓存策略
									strategyMap.put(strategySetting.getId(), strategy);

									log.info("{} 策略-{} ID-{} 实现-{} 加载成功", logStr, strategySetting.getName(), strategySetting.getId(), className);
								} catch (NoSuchMethodException | SecurityException | InstantiationException
										| IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e) {
									log.error("{} 反射创建策略{}实例发生异常", logStr, className, classSimpleName + "-Setting.json",
											e);
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
			mainEventEngine.removeListener(null, strategy);
			strategy.stop();
			// 取消订阅合约
			for (StrategySetting.TradeGatewaySetting tradeGatewaySetting : strategy.getStrategySetting()
					.getGateways()) {
				String gatewayID = tradeGatewaySetting.getGatewayID();
				for (String rtSymbol : tradeGatewaySetting.getSubscribeRtSymbols()) {
					mainEngine.unsubscribe(rtSymbol, gatewayID, strategyID);
					log.info("{}取消订阅,接口{}合约{}", strategy.getLogStr(), gatewayID, rtSymbol);
				}
			}
			strategyMap.remove(strategyID);
			log.error("{} 策略已卸载,strategyID:{}", logStr, strategyID);
		} else {
			log.error("{} 未找到策略,卸载失败,strategyID:{}", logStr, strategyID);
		}
	}

	@Override
	public void initStrategy(String strategyID) {

		Strategy strategy = strategyMap.get(strategyID);
		if (strategy != null) {
			if(strategy.isInitStatus()) {
				log.warn("{} 策略已经初始化,不允许重复初始化",strategy.getLogStr());
				return;
			}
			
			StrategySetting strategySetting = strategy.getStrategySetting();

			String strategyName = strategySetting.getName();
			/******************** 初始化变量 ******************************/
			// 从文件中加载所有变量
			if (strategySetting.getVarMap() != null) {
				strategy.getVarMap().putAll(strategySetting.getVarMap());
			} else {
				log.info("{}配置文件中varMap为空", strategy.getLogStr());
			}

			// 从MongoDB中加载所有存储的变量,如果和文件配置冲突,则覆盖
			Map<String, String> dbSyncVarMap = zeusDataUtil.loadStrategySyncVarMap(strategy.getID());
			if (dbSyncVarMap.isEmpty()) {
				log.info("{}数据库中varMap为空", strategy.getLogStr());
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

			/********************** 初始化参数 ****************************/
			if (strategySetting.getParamMap() != null) {
				strategy.getParamMap().putAll(strategySetting.getParamMap());
			} else {
				log.info("{}配置文件中paramMap为空", strategy.getLogStr());
			}

			/********************** 初始化持仓 ****************************/
			Map<String, ContractPositionDetail> contractPositionMap = strategy.getContractPositionMap();
			Set<String> contractGatewayKeySet = new HashSet<>(); // 用于后续判断数据库中读取的数据是否和配置匹配
			for (StrategySetting.TradeContractSetting tradeContractSetting : strategySetting.getContracts()) {
				String rtSymbol = tradeContractSetting.getRtSymbol();
				if (!contractPositionMap.containsKey(rtSymbol)) {
					contractPositionMap.put(rtSymbol, new ContractPositionDetail());
				}

				for (StrategySetting.ContractTradeGatewaySetting contractTradeGatewaySetting : tradeContractSetting
						.getTradeGateways()) {
					String contractGatewayKey = rtSymbol + contractTradeGatewaySetting.getGatewayID();
					contractGatewayKeySet.add(contractGatewayKey);
				}

			}

			String tradingDay = strategySetting.getTradingDay();

			List<PositionDetail> tdPositionDetailList = zeusDataUtil.loadStrategyPositionDetails(tradingDay, strategyID, strategyName);
			if (tdPositionDetailList.isEmpty()) {
				log.info("{} 当日持仓数据记录为空,尝试读取前一交易日", strategy.getLogStr());
				String preTradingDay = strategySetting.getPreTradingDay();
				if (StringUtils.isEmpty(preTradingDay)) {
					log.info("{} 前一交易日配置为空", strategy.getLogStr());
				} else {
					List<PositionDetail> ydPositionDetailList = zeusDataUtil.loadStrategyPositionDetails(preTradingDay,
							strategyID, strategyName);
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
									
									PositionDetail tdPositionDetail = new PositionDetail(rtSymbol, gatewayID, preTradingDay, strategyName, strategyID, ydPositionDetail.getExchange(), ydPositionDetail.getContractSize());
									tdPositionDetail.setLongYd(ydPositionDetail.getLongYd()+ydPositionDetail.getLongTd());
									tdPositionDetail.setShortYd(ydPositionDetail.getShortYd()+ydPositionDetail.getShortTd());
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
							log.error("{} 从数据库中读取到合约{}接口{}组合与配置不匹配", strategy.getLogStr(), rtSymbol, gatewayID);
						}
					} else {
						log.error("{} 从数据库中读取到配置中不存在的合约{}", strategy.getLogStr(), rtSymbol);
					}
				}

			}

			/************************ 订阅合约注册事件 *******************/
			// 通过配置订阅合约注册事件
			for (StrategySetting.TradeGatewaySetting tradeGatewaySetting : strategy.getStrategySetting()
					.getGateways()) {
				String gatewayID = tradeGatewaySetting.getGatewayID();

				for (String rtSymbol : tradeGatewaySetting.getSubscribeRtSymbols()) {
					// 为策略注册Tick数据监听事件
					String event = EventConstant.EVENT_TICK + gatewayID + rtSymbol;
					mainEventEngine.registerListener(event, strategy);
					log.info("{}注册事件监听{}", strategy.getLogStr(), event);

					// 订阅合约
					SubscribeReq subscribeReq = new SubscribeReq();
					subscribeReq.setRtSymbol(rtSymbol);
					subscribeReq.setGatewayID(gatewayID);
					mainEngine.subscribe(subscribeReq, strategyID);
					log.info("{}通过接口{}订阅合约{}", strategy.getLogStr(), gatewayID, rtSymbol);
				}
			}

			/*********************************************************/
			strategy.init();
		} else {
			log.error("{} 未找到策略,初始化失败,strategyID:{}", logStr, strategyID);
		}
	}

	@Override
	public void startStrategy(String strategyID) {
		Strategy strategy = strategyMap.get(strategyID);
		if (strategy != null) {
			strategy.startTrading();
		} else {
			log.error("{} 未找到策略,策略启动失败,strategyID:{}", logStr, strategyID);
		}
	}

	@Override
	public void stopStrategy(String strategyID) {
		Strategy strategy = strategyMap.get(strategyID);
		if (strategy != null) {
			strategy.stopTrading(false);
		} else {
			log.error("{} 未找到策略,策略停止失败,strategyID:{}", logStr, strategyID);
		}
	}

	@Override
	public void initAllStrategy() {
		for (Strategy strategy : strategyMap.values()) {
			initStrategy(strategy.getID());
		}
	}

	@Override
	public void startAllStrategy() {
		for (Strategy strategy : strategyMap.values()) {
			strategy.startTrading();
		}
	}

	@Override
	public void stopAllStrategy() {
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
		Map<String,String> saveSyncVarMap = SerializationUtils.clone(new HashMap<>(syncVarMap));
		saveSyncVarMap.put("strategyID", strategyID);
		saveSyncVarMap.put("strategyName", strategyName);
		syncVarMapSaveQueue.add(saveSyncVarMap);
	}

	@Override
	public void asyncSavePositionDetail(List<PositionDetail> positionDetailList) {
		
		List<PositionDetail> savePositionDetailList = new ArrayList<>();
		for(PositionDetail positionDetail:positionDetailList) {
			// 深度复制
			PositionDetail savePositionDetail = SerializationUtils.clone(positionDetail);
			savePositionDetailList.add(savePositionDetail);
		}
		positionDetailSaveQueue.addAll(savePositionDetailList);

	}
	
	private class SavePositionTask implements Runnable{
		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					PositionDetail positionDetail = positionDetailSaveQueue.take();
					zeusDataUtil.saveStrategyPositionDetail(positionDetail);
				} catch (InterruptedException e) {
					log.error("{} 保存持仓任务捕获到线程中断异常,线程停止!!!", logStr, e);
				}
			}
		}
		
	}
	private class SaveSyncVarMapTask implements Runnable{
		@Override
		public void run() {
			try {
				Map<String,String> syncVarMapWithNameAndID = syncVarMapSaveQueue.take();
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
	
}
