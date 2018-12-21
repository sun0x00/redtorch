package xyz.redtorch.core.zeus.impl;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.service.CoreEngineService;
import xyz.redtorch.core.zeus.ZeusTradingBaseService;
import xyz.redtorch.core.zeus.ZeusConstant;
import xyz.redtorch.core.zeus.ZeusDataService;
import xyz.redtorch.core.zeus.ZeusEngineService;
import xyz.redtorch.core.zeus.ZeusMmapService;
import xyz.redtorch.core.zeus.entity.ContractPositionDetail;
import xyz.redtorch.core.zeus.entity.PositionDetail;
import xyz.redtorch.core.zeus.entity.StrategyProcessReport;
import xyz.redtorch.core.zeus.strategy.Strategy;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

/**
 * @author sun0x00@gmail.com
 */
// @Service // 在策略项目中使用@Import引入
public class TradingEngineServiceImpl implements ZeusEngineService, InitializingBean {

	private Logger log = LoggerFactory.getLogger(TradingEngineServiceImpl.class);

	@Autowired
	@Qualifier("coreEngineServiceRmiProxyFactory")
	private RmiProxyFactoryBean coreEngineServiceFactoryBean;

	@Autowired
	@Qualifier("zeusDataServiceRmiProxyFactory")
	private RmiProxyFactoryBean zeusDataServiceRmiProxyFactoryBean;

	@Autowired
	@Qualifier("zeusTradingBaseServiceRmiProxyFactory")
	private RmiProxyFactoryBean zeusTradingBaseServiceRmiProxyFactoryBean;

	@Autowired
	private CoreEngineService coreEngineService;
	@Autowired
	private ZeusDataService zeusDataService;
	@Autowired
	private ZeusTradingBaseService zeusTradingBaseService;

	private Set<String> originalOrderIDSet = new HashSet<>();
	private Set<String> subscribedTickKeySet = new HashSet<>();
	private Set<SubscribeReq> subscribeReqSet = new HashSet<>();

	@Value("${chronicleQueueBasePath}")
	private String chronicleQueueBasePath;

	@Value("${strategyID}")
	private String strategyID;

	// 用于异步存储配置的队列(减少策略的IO等待时间,主要用于节省回测时间)
	private Queue<StrategySetting> strategySettingSaveQueue = new ConcurrentLinkedQueue<>();
	private Queue<PositionDetail> positionDetailSaveQueue = new ConcurrentLinkedQueue<>();

	private Strategy strategy;

	private SingleChronicleQueue queueTx;
	private SingleChronicleQueue queueRx;
	private ExcerptAppender queueTxEa;
	private ExcerptTailer queueRxEt;

	// 使用无大小限制的线程池,线程空闲60s会被释放
	private ExecutorService executor = Executors.newCachedThreadPool();

	private synchronized ExcerptAppender getQueueTxEa() {
		return queueTxEa;
	}

	private synchronized ExcerptTailer getQueueRxEt() {
		return queueRxEt;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		coreEngineService = (CoreEngineService) coreEngineServiceFactoryBean.getObject();
		zeusDataService = (ZeusDataService) zeusDataServiceRmiProxyFactoryBean.getObject();
		zeusTradingBaseService = (ZeusTradingBaseService) zeusTradingBaseServiceRmiProxyFactoryBean.getObject();

		//
		queueTx = SingleChronicleQueueBuilder.binary(chronicleQueueBasePath + File.separator + "channel1")
				.rollCycle(RollCycles.HOURLY).build();
		queueTxEa = queueTx.acquireAppender();

		queueRx = SingleChronicleQueueBuilder.binary(chronicleQueueBasePath + File.separator + "channel0")
				.rollCycle(RollCycles.HOURLY).build();
		queueRxEt = queueRx.createTailer().toEnd();

		// 启动异步存储策略设置线程
		executor.execute(new SaveStrategySettingTask());
		// 启动异步存储仓位线程
		executor.execute(new SavePositionTask());
		// 启动数据接收线程
		executor.execute(new RxTask());

		executor.execute(new ReportTask());

		log.info("交易引擎服务已启动!");

		log.info("策略重复加载检查,请等待!");
		if (!zeusTradingBaseService.duplicationCheck(strategyID)) {
			loadStrategy();
		} else {
			log.error("策略加载失败，检查到重复启动,进程即将退出,策略ID:" + strategyID);
			System.exit(0);
		}

	}

	@Override
	public int getEngineType() {
		return ZeusConstant.ENGINE_TYPE_TRADING;
	}

	@Override
	public void sendOrder(OrderReq orderReq) {
		originalOrderIDSet.add(orderReq.getOriginalOrderID());

		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(ZeusMmapService.DATA_ORDERREQ)

				.writeUtf8(orderReq.getRtAccountID()) //
				.writeUtf8(orderReq.getSymbol()) //
				.writeUtf8(orderReq.getExchange()) //
				.writeUtf8(orderReq.getRtSymbol()) //

				.writeDouble(orderReq.getPrice()) //
				.writeInt(orderReq.getVolume()) //
				.writeUtf8(orderReq.getDirection()) //
				.writeUtf8(orderReq.getOffset()) //
				.writeUtf8(orderReq.getPriceType()) //

				.writeUtf8(orderReq.getOriginalOrderID()) //
				.writeUtf8(orderReq.getOperatorID()) //

				.writeUtf8(orderReq.getProductClass()) //
				.writeUtf8(orderReq.getCurrency()) //
				.writeUtf8(orderReq.getExpiry()) //
				.writeDouble(orderReq.getStrikePrice()) //
				.writeUtf8(orderReq.getOptionType()) //
				.writeUtf8(orderReq.getLastTradeDateOrContractMonth()) //
				.writeUtf8(orderReq.getMultiplier()));
	}

	@Override
	public void cancelOrder(String originalOrderID,String operatorID) {
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(ZeusMmapService.DATA_CANCEL_ORDER) //
				.writeUtf8(originalOrderID) //
				.writeUtf8(operatorID));

	}

	private void loadStrategy() {

		StrategySetting strategySetting = zeusDataService.loadStrategySetting(strategyID);
		if (strategySetting == null) {
			log.error("未找到策略配置,加载失败,策略ID:" + strategyID);
		} else {
			strategy = createStrategyClassInstance(strategySetting);
		}

	}

	private void initStrategy() {

		if (strategy == null) {
			log.error("策略初始化失败,可能尚未加载,策略ID:" + strategyID);
			return;
		} else if (strategy != null) {
			if (strategy.isInitStatus()) {
				log.warn(strategy.getLogStr() + "已经初始化,不允许重复初始化");
				return;
			}
			StrategySetting strategySetting = strategy.getStrategySetting();

			String strategyName = strategySetting.getStrategyName();

			/********************** 初始化持仓 ****************************/
			Map<String, ContractPositionDetail> contractPositionMap = strategy.getContractPositionMap();
			for (StrategySetting.ContractSetting contractSetting : strategySetting.getContracts()) {
				String rtSymbol = contractSetting.getRtSymbol();
				if (!contractPositionMap.containsKey(rtSymbol)) {
					contractPositionMap.put(rtSymbol, new ContractPositionDetail());
				}
			}

			String tradingDay = strategySetting.getTradingDay();

			List<PositionDetail> tdPositionDetailList = zeusDataService.loadStrategyPositionDetails(tradingDay,
					strategyID, strategyName);
			if (tdPositionDetailList.isEmpty()) {
				log.warn(strategy.getLogStr() + "当日持仓数据记录为空,尝试读取前一交易日");

				String preTradingDay = strategySetting.getPreTradingDay();
				if (StringUtils.isEmpty(preTradingDay)) {
					log.warn(strategy.getLogStr() + "前一交易日配置为空");
				} else {
					List<PositionDetail> ydPositionDetailList = zeusDataService
							.loadStrategyPositionDetails(preTradingDay, strategyID, strategyName);
					if (ydPositionDetailList.isEmpty()) {
						log.warn(strategy.getLogStr() + "前一日持仓数据记录为空");
					} else {

						List<PositionDetail> insertPositionDetailList = new ArrayList<>();
						for (PositionDetail ydPositionDetail : ydPositionDetailList) {
							String rtSymbol = ydPositionDetail.getRtSymbol();
							String rtAccountID = ydPositionDetail.getRtAccountID();

							if (contractPositionMap.containsKey(rtSymbol)) {
								ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

								PositionDetail tdPositionDetail = new PositionDetail(rtSymbol, rtAccountID,
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
	
								contractPositionDetail.getPositionDetailMap().put(rtAccountID, tdPositionDetail);
								contractPositionDetail.calculatePosition();
								insertPositionDetailList.add(tdPositionDetail);
							} else {
								log.error(strategy.getLogStr() + "从数据库中读取到配置中不存在的合约" + rtSymbol);
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
					String rtAccountID = positionDetail.getRtAccountID();

					if (contractPositionMap.containsKey(rtSymbol)) {
						ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

						positionDetail.setTradingDay(tradingDay);
						contractPositionDetail.getPositionDetailMap().put(rtAccountID, positionDetail);
						contractPositionDetail.calculatePosition();
						insertPositionDetailList.add(positionDetail);
			
					} else {
						log.error(strategy.getLogStr() + "从数据库中读取到配置中不存在的合约" + rtSymbol);
					}
				}

			}

			log.info(strategy.getLogStr() + "初始化持仓完成");
			/////////////////////////// 订阅行情/////////////////
			// 通过配置订阅合约注册事件
			for (StrategySetting.GatewaySetting GatewaySetting : strategy.getStrategySetting().getGateways()) {
				String gatewayID = GatewaySetting.getGatewayID();

				for (String rtSymbol : GatewaySetting.getSubscribeRtSymbols()) {
					// 为策略注册Tick数据监听事件
					String subscribedTickKey = rtSymbol + "." + gatewayID;
					subscribedTickKeySet.add(subscribedTickKey);

					// 订阅合约
					SubscribeReq subscribeReq = new SubscribeReq();
					subscribeReq.setRtSymbol(rtSymbol);
					subscribeReq.setGatewayID(gatewayID);
					subscribeReqSet.add(subscribeReq);
					coreEngineService.subscribe(subscribeReq, strategyID);

					log.info(strategy.getLogStr() + "通过网关" + gatewayID + "订阅合约" + rtSymbol);
				}
			}
			log.info(strategy.getLogStr() + "订阅行情完成");
			//////////////////////////////////////////////
			strategy.init();
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
		return zeusDataService.loadTickDataList(startDateTime, endDateTime, rtSymbol);
	}

	@Override
	public List<Bar> loadBarData(DateTime startDateTime, DateTime endDateTime, String rtSymbol) {
		return zeusDataService.loadBarDataList(startDateTime, endDateTime, rtSymbol);
	}

	@Override
	public void asyncSaveStrategySetting(StrategySetting strategySetting) {
		// 实现深度复制,避免引用被修改
		StrategySetting copyStrategySetting = SerializationUtils.clone(strategySetting);
		strategySettingSaveQueue.add(copyStrategySetting);
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
					PositionDetail positionDetail = positionDetailSaveQueue.poll();
					if (positionDetail == null) {
						Thread.sleep(10);
					} else {
						zeusDataService.saveStrategyPositionDetail(positionDetail);
					}
				} catch (InterruptedException e) {
					log.error("保存持仓任务捕获到线程中断异常,线程停止!!!", e);
				}
			}
		}

	}

	private class SaveStrategySettingTask implements Runnable {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					StrategySetting strategySetting = strategySettingSaveQueue.poll();
					if (strategySetting == null) {
						Thread.sleep(10);
					} else {
						zeusDataService.saveStrategySetting(strategySetting);
					}
				} catch (InterruptedException e) {
					log.error("保存配置任务捕获到线程中断异常,线程停止!!!", e);
				}
			}
		}

	}

	private class RxTask implements Runnable {
		@Override
		public void run() {
			log.info("交易引擎MMAP RxTask已启动");
			while (!Thread.currentThread().isInterrupted()) {
				getQueueRxEt().readBytes(in -> {
					int dataType = in.readInt();

					if (ZeusMmapService.DATA_ORDER == dataType) {
						String originalOrderID = in.readUtf8();
						if (originalOrderIDSet.contains(originalOrderID)) {
							Order order = new Order();
							order.setOriginalOrderID(originalOrderID);

							order.setAccountID(in.readUtf8());
							order.setRtAccountID(in.readUtf8());
							
							order.setGatewayID(in.readUtf8());

							order.setSymbol(in.readUtf8());
							order.setExchange(in.readUtf8());
							order.setRtSymbol(in.readUtf8());

							order.setOrderID(in.readUtf8());
							order.setRtOrderID(in.readUtf8());

							order.setDirection(in.readUtf8());
							order.setOffset(in.readUtf8());
							order.setPrice(in.readDouble());
							order.setTotalVolume(in.readInt());
							order.setTradedVolume(in.readInt());
							order.setStatus(in.readUtf8());

							order.setTradingDay(in.readUtf8());

							order.setOrderDate(in.readUtf8());
							order.setOrderTime(in.readUtf8());
							order.setCancelTime(in.readUtf8());
							order.setActiveTime(in.readUtf8());
							order.setUpdateTime(in.readUtf8());

							order.setFrontID(in.readInt());
							order.setSessionID(in.readInt());
							if (strategy != null) {
								strategy.processOrder(order);
							}
						}

					} else if (ZeusMmapService.DATA_TRADE == dataType) {

						String originalOrderID = in.readUtf8();
						if (originalOrderIDSet.contains(originalOrderID)) {

							Trade trade = new Trade();

							trade.setOriginalOrderID(originalOrderID);
							
							trade.setAccountID(in.readUtf8());
							trade.setRtAccountID(in.readUtf8());
							
							trade.setGatewayID(in.readUtf8());

							trade.setSymbol(in.readUtf8());
							trade.setExchange(in.readUtf8());
							trade.setRtSymbol(in.readUtf8());

							trade.setTradeID(in.readUtf8());
							trade.setRtTradeID(in.readUtf8());

							trade.setOrderID(in.readUtf8());
							trade.setRtOrderID(in.readUtf8());

							trade.setDirection(in.readUtf8());
							trade.setOffset(in.readUtf8());
							trade.setPrice(in.readDouble());
							trade.setVolume(in.readInt());

							trade.setTradingDay(in.readUtf8());
							trade.setTradeDate(in.readUtf8());
							trade.setTradeTime(in.readUtf8());
							if (strategy != null) {
								strategy.processTrade(trade);
							}
						}
					} else if (ZeusMmapService.DATA_TICK == dataType) {
						String rtTickID = in.readUtf8();
						String gatewayID = in.readUtf8();
						String rtSymbol = in.readUtf8();

						if (subscribedTickKeySet.contains(rtTickID)) {
							Tick tick = new Tick();

							tick.setGatewayID(gatewayID);
							tick.setRtSymbol(rtSymbol);
							tick.setRtTickID(rtTickID);

							tick.setSymbol(in.readUtf8());
							tick.setExchange(in.readUtf8());

							tick.setTradingDay(in.readUtf8());
							tick.setActionDay(in.readUtf8());
							tick.setActionTime(in.readUtf8());
							tick.setDateTime(new DateTime(in.readLong()));

							tick.setStatus(in.readInt());

							tick.setLastPrice(in.readDouble());
							tick.setLastVolume(in.readInt());
							tick.setVolume(in.readInt());
							tick.setOpenInterest(in.readDouble());

							tick.setPreOpenInterest(in.readLong());
							tick.setPreClosePrice(in.readDouble());
							tick.setPreSettlePrice(in.readDouble());

							tick.setOpenPrice(in.readDouble());
							tick.setHighPrice(in.readDouble());
							tick.setLowPrice(in.readDouble());

							tick.setUpperLimit(in.readDouble());
							tick.setLowerLimit(in.readDouble());

							tick.setBidPrice1(in.readDouble());
							tick.setBidPrice2(in.readDouble());
							tick.setBidPrice3(in.readDouble());
							tick.setBidPrice4(in.readDouble());
							tick.setBidPrice5(in.readDouble());
							tick.setBidPrice6(in.readDouble());
							tick.setBidPrice7(in.readDouble());
							tick.setBidPrice8(in.readDouble());
							tick.setBidPrice9(in.readDouble());
							tick.setBidPrice10(in.readDouble());

							tick.setAskPrice1(in.readDouble());
							tick.setAskPrice2(in.readDouble());
							tick.setAskPrice3(in.readDouble());
							tick.setAskPrice4(in.readDouble());
							tick.setAskPrice5(in.readDouble());
							tick.setAskPrice6(in.readDouble());
							tick.setAskPrice7(in.readDouble());
							tick.setAskPrice8(in.readDouble());
							tick.setAskPrice9(in.readDouble());
							tick.setAskPrice10(in.readDouble());

							tick.setBidVolume1(in.readInt());
							tick.setBidVolume2(in.readInt());
							tick.setBidVolume3(in.readInt());
							tick.setBidVolume4(in.readInt());
							tick.setBidVolume5(in.readInt());
							tick.setBidVolume6(in.readInt());
							tick.setBidVolume7(in.readInt());
							tick.setBidVolume8(in.readInt());
							tick.setBidVolume9(in.readInt());
							tick.setBidVolume10(in.readInt());

							tick.setAskVolume1(in.readInt());
							tick.setAskVolume2(in.readInt());
							tick.setAskVolume3(in.readInt());
							tick.setAskVolume4(in.readInt());
							tick.setAskVolume5(in.readInt());
							tick.setAskVolume6(in.readInt());
							tick.setAskVolume7(in.readInt());
							tick.setAskVolume8(in.readInt());
							tick.setAskVolume9(in.readInt());
							tick.setAskVolume10(in.readInt());

							if (strategy != null) {
								strategy.processTick(tick);
							}
						}

					} else if (ZeusMmapService.DATA_COMMAND == dataType) {
						int command = in.readInt();
						if (ZeusMmapService.COMMAND_INIT_STARTEGY == command) {
							String commandStrategyID = in.readUtf8();
							if (strategyID.equals(commandStrategyID)) {
								if (strategy != null || !strategy.isInitStatus()) {
									initStrategy();
								}
							}
						} else if (ZeusMmapService.COMMAND_START_STARTEGY == command) {
							String commandStrategyID = in.readUtf8();
							if (strategyID.equals(commandStrategyID)) {
								if (strategy != null || !strategy.isTrading()) {
									strategy.startTrading();
								}
							}
						} else if (ZeusMmapService.COMMAND_STOP_STARTEGY == command) {
							String commandStrategyID = in.readUtf8();
							if (strategyID.equals(commandStrategyID)) {
								if (strategy != null || strategy.isTrading()) {
									strategy.stopTrading(false);
								}
							}

						} else if (ZeusMmapService.COMMAND_RELOAD_STARTEGY == command) {
							String commandStrategyID = in.readUtf8();
							if (strategyID.equals(commandStrategyID)) {
								if (strategy != null) {
									if (strategy.isTrading()) {
										strategy.stopTrading(false);
									}

									strategy.saveStrategySetting();
									strategy.destroy();
									strategy = null;

									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										// nop
									}
									for (SubscribeReq subscribeReq : subscribeReqSet) {
										coreEngineService.unsubscribe(subscribeReq.getRtSymbol(),
												subscribeReq.getGatewayID(), strategyID);
									}
									loadStrategy();
								}
							}

						} else if (ZeusMmapService.COMMAND_INIT_ALL_STARTEGY == command) {
							if (strategy != null || !strategy.isInitStatus()) {
								initStrategy();
							}
						} else if (ZeusMmapService.COMMAND_START_ALL_STARTEGY == command) {
							if (strategy != null || !strategy.isTrading()) {
								strategy.startTrading();
							}
						} else if (ZeusMmapService.COMMAND_STOP_ALL_STARTEGY == command) {
							if (strategy != null || strategy.isTrading()) {
								strategy.stopTrading(false);
							}

						} else if (ZeusMmapService.COMMAND_RELOAD_ALL_STARTEGY == command) {
							if (strategy != null) {
								if (strategy.isTrading()) {
									strategy.stopTrading(false);
								}
								strategy.saveStrategySetting();
								strategy.destroy();
								strategy = null;

								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									// nop
								}
								for (SubscribeReq subscribeReq : subscribeReqSet) {
									coreEngineService.unsubscribe(subscribeReq.getRtSymbol(),
											subscribeReq.getGatewayID(), strategyID);
								}

								loadStrategy();
							}
						}

					}

				});
			}
		}

	}

	@Override
	public double getPriceTick(String rtSymbol, String gatewayID) {
		Contract contract = coreEngineService.getContract(rtSymbol, gatewayID);
		return contract.getPriceTick();
	}

	@Override
	public Contract getContractByFuzzySymbol(String fuzzySymbol) {
		return coreEngineService.getContractByFuzzySymbol(fuzzySymbol);
	}

	@Override
	public Contract getContract(String rtSymbol, String gatewayID) {
		return coreEngineService.getContract(rtSymbol, gatewayID);
	}

	private Strategy createStrategyClassInstance(StrategySetting strategySetting) {

		if (strategy != null) {
			log.error("策略:" + strategySetting.getStrategyName() + " ID:" + strategySetting.getStrategyID() + "实现类"
					+ strategySetting.getClassName() + "已经加载，请勿重复加载！");
			return null;
		}

		try {
			Class<?> clazz = Class.forName(strategySetting.getClassName());
			Constructor<?> c = clazz.getConstructor(ZeusEngineService.class, StrategySetting.class);
			Strategy strategy = (Strategy) c.newInstance(this, strategySetting);

			log.info("策略:" + strategySetting.getStrategyName() + " ID:" + strategySetting.getStrategyID() + "实现类"
					+ strategySetting.getClassName() + "加载成功！");
			return strategy;
		} catch (Exception e) {
			log.error("反射创建策略类" + strategySetting.getClassName() + "实例发生异常", e);
			return null;
		}

	}

	private class ReportTask implements Runnable {

		@Override
		public void run() {
			log.info("交易引擎ReportTask已启动");
			StrategyProcessReport report = new StrategyProcessReport();
			while (!Thread.currentThread().isInterrupted()) {
				if (strategy != null) {
					report.setInitStatus(strategy.isInitStatus());
					report.setTrading(strategy.isTrading());
					report.setSubscribeReqSet(subscribeReqSet);
					report.setStrategySetting(strategy.getStrategySetting());
					report.setReportTimestamp(System.currentTimeMillis());
					zeusTradingBaseService.updateReport(report);
					// 定时报送
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// nop
					}
				}
			}
		}

	}

}
