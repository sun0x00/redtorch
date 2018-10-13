package xyz.redtorch.web.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.LogData;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.service.FastEventEngineService;
import xyz.redtorch.core.service.extend.event.EventConstant;
import xyz.redtorch.core.service.extend.event.FastEvent;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandler;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandlerAbstract;
import xyz.redtorch.web.service.EventTransferService;
import xyz.redtorch.web.socketio.SocketIOMessageEventHandler;

@Service
public class EventTransferServiceImpl extends FastEventDynamicHandlerAbstract
		implements FastEventDynamicHandler, EventTransferService, InitializingBean {

	private Logger log = LoggerFactory.getLogger(EventTransferServiceImpl.class);

	@Autowired
	private SocketIOMessageEventHandler socketIOMessageEventHandler;
	@Autowired
	private FastEventEngineService fastEventEngineService;

	// 使用无大小限制的线程池,线程空闲60s会被释放
	private ExecutorService executor = Executors.newCachedThreadPool();

	@Override
	public void afterPropertiesSet() throws Exception {
		fastEventEngineService.addHandler(this);
		subscribeEvent(EventConstant.EVENT_TICK);
		subscribeEvent(EventConstant.EVENT_TICKS_CHANGED);
		subscribeEvent(EventConstant.EVENT_TRADE);
		subscribeEvent(EventConstant.EVENT_ORDER);
		subscribeEvent(EventConstant.EVENT_POSITION);
		subscribeEvent(EventConstant.EVENT_ACCOUNT);
		subscribeEvent(EventConstant.EVENT_CONTRACT);
		subscribeEvent(EventConstant.EVENT_ERROR);
		subscribeEvent(EventConstant.EVENT_GATEWAY);
		subscribeEvent(EventConstant.EVENT_LOG);
		subscribeEvent(EventConstant.EVENT_LOG + "ZEUS|");

		executor.execute(new SocketIOTransferTask());

	}

	private Queue<FastEvent> eventQueue = new ConcurrentLinkedQueue<>();

	@Override
	public void onEvent(final FastEvent fastEvent, final long sequence, final boolean endOfBatch) throws Exception {

		if (!subscribedEventSet.contains(fastEvent.getEvent())) {
			return;
		}
		eventQueue.add(fastEvent);
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onShutdown() {
		shutdownLatch.countDown();
	}

	private class SocketIOTransferTask implements Runnable {

		@Override
		public void run() {

			log.info("SocketIO转发任务启动");

			Map<String, Tick> tickMap = new LinkedHashMap<>();
			Map<String, Position> positionMap = new LinkedHashMap<>();
			Map<String, Order> orderMap = new LinkedHashMap<>();
			Map<String, Trade> tradeMap = new LinkedHashMap<>();
			Map<String, Contract> contractMap = new LinkedHashMap<>();
			Map<String, Account> accountMap = new LinkedHashMap<>();
			List<LogData> logDataList = new ArrayList<>();

			long tickLastSendTime = System.currentTimeMillis();
			long positionLastSendTime = System.currentTimeMillis();
			long orderLastSendTime = System.currentTimeMillis();
			long tradeLastSendTime = System.currentTimeMillis();
			long contractLastSendTime = System.currentTimeMillis();
			long accountLastSendTime = System.currentTimeMillis();
			long logDataLastSendTime = System.currentTimeMillis();

			long tickMinTimeInterval = 200;
			long positionMinTimeInterval = 200;
			long orderMinTimeInterval = 200;
			long tradeMinTimeInterval = 200;
			long contractMinTimeInterval = 200;
			long accountMinTimeInterval = 200;
			long logDataMinTimeInterval = 1000;

			long tickMaxTimeInterval = 500;
			long positionMaxTimeInterval = 500;
			long orderMaxTimeInterval = 500;
			long tradeMaxTimeInterval = 500;
			long contractMaxTimeInterval = 500;
			long accountMaxTimeInterval = 500;
			long logDataMaxTimeInterval = 1500;

			while (!Thread.currentThread().isInterrupted()) {

				FastEvent fastEvent = eventQueue.poll();
				if (fastEvent != null) {

					// 判断消息类型
					if (EventConstant.EVENT_TICK.equals(fastEvent.getEventType())) {
						try {
							// 不经缓存，直接发送
							socketIOMessageEventHandler.sendEventToPresetClient(EventConstant.EVENT_TICK,fastEvent.getTick());
							
							// 经过缓存，避免客户端压力过大
							Tick tick = fastEvent.getTick();
							FastEvent nextEvent = eventQueue.peek();
							if (nextEvent != null || (EventConstant.EVENT_TICK.equals(fastEvent.getEventType())
									&& System.currentTimeMillis() - tickLastSendTime < tickMaxTimeInterval)) {
								tickMap.put(tick.getRtTickID(), tick);
							} else if (System.currentTimeMillis() - tickLastSendTime < tickMinTimeInterval) {
								tickMap.put(tick.getRtTickID(), tick);
							} else {
								tickMap.put(tick.getRtTickID(), tick);
								socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_TICKS,
										new ArrayList<>(tickMap.values()));
								tickLastSendTime = System.currentTimeMillis();
								tickMap = new LinkedHashMap<>();
							}
						} catch (Exception e) {
							log.error("向SocketIO转发Tick发生异常!!!", e);
						}
					} else if (EventConstant.EVENT_TRADE.equals(fastEvent.getEventType())) {
						try {
							// 不经缓存，直接发送
							socketIOMessageEventHandler.sendEventToPresetClient(EventConstant.EVENT_TRADE,fastEvent.getTrade());
							
							Trade trade = fastEvent.getTrade();
							FastEvent nextEvent = eventQueue.peek();
							if (nextEvent != null || (EventConstant.EVENT_TRADE.equals(fastEvent.getEventType())
									&& System.currentTimeMillis() - tradeLastSendTime < tradeMaxTimeInterval)) {
								tradeMap.put(trade.getRtTradeID(), trade);
							} else if (System.currentTimeMillis() - tradeLastSendTime < tradeMinTimeInterval) {
								tradeMap.put(trade.getRtTradeID(), trade);
							} else {
								tradeMap.put(trade.getRtTradeID(), trade);
								socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_TRADES,
										new ArrayList<>(tradeMap.values()));
								tradeLastSendTime = System.currentTimeMillis();
								tradeMap = new LinkedHashMap<>();
							}
						} catch (Exception e) {
							log.error("向SocketIO转发Trade发生异常!!!", e);
						}
					} else if (EventConstant.EVENT_ORDER.equals(fastEvent.getEventType())) {
						try {
							// 不经缓存，直接发送
							socketIOMessageEventHandler.sendEventToPresetClient(EventConstant.EVENT_ORDER,fastEvent.getOrder());
							
							Order order = fastEvent.getOrder();
							FastEvent nextEvent = eventQueue.peek();
							if (nextEvent != null || (EventConstant.EVENT_ORDER.equals(fastEvent.getEventType())
									&& System.currentTimeMillis() - orderLastSendTime < orderMaxTimeInterval)) {
								orderMap.put(order.getRtOrderID(), order);
							} else if (System.currentTimeMillis() - orderLastSendTime < orderMinTimeInterval) {
								orderMap.put(order.getRtOrderID(), order);
							} else {
								orderMap.put(order.getRtOrderID(), order);
								socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_ORDERS,
										new ArrayList<>(orderMap.values()));
								orderLastSendTime = System.currentTimeMillis();
								orderMap = new LinkedHashMap<>();
							}
						} catch (Exception e) {
							log.error("向SocketIO转发Order发生异常!!!", e);
						}
					} else if (EventConstant.EVENT_CONTRACT.equals(fastEvent.getEventType())) {
						try {
							// 不经缓存，直接发送
							socketIOMessageEventHandler.sendEventToPresetClient(EventConstant.EVENT_CONTRACT,fastEvent.getContract());
							
							Contract contract = fastEvent.getContract();
							FastEvent nextEvent = eventQueue.peek();
							if (nextEvent != null || (EventConstant.EVENT_CONTRACT.equals(fastEvent.getEventType())
									&& System.currentTimeMillis() - contractLastSendTime < contractMaxTimeInterval)) {
								contractMap.put(contract.getRtContractID(), contract);
							} else if (System.currentTimeMillis() - contractLastSendTime < contractMinTimeInterval) {
								contractMap.put(contract.getRtContractID(), contract);
							} else {
								contractMap.put(contract.getRtContractID(), contract);
								socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_CONTRACTS,
										new ArrayList<>(contractMap.values()));
								contractLastSendTime = System.currentTimeMillis();
								contractMap = new LinkedHashMap<>();
							}
						} catch (Exception e) {
							log.error("向SocketIO转发Contract发生异常!!!", e);
						}
					} else if (EventConstant.EVENT_POSITION.equals(fastEvent.getEventType())) {
						try {
							// 不经缓存，直接发送
							socketIOMessageEventHandler.sendEventToPresetClient(EventConstant.EVENT_POSITION,fastEvent.getPosition());
							
							Position position = fastEvent.getPosition();
							FastEvent nextEvent = eventQueue.peek();
							if (nextEvent != null || (EventConstant.EVENT_POSITION.equals(fastEvent.getEventType())
									&& System.currentTimeMillis() - positionLastSendTime < positionMaxTimeInterval)) {
								positionMap.put(position.getRtPositionID(), position);
							} else if (System.currentTimeMillis() - positionLastSendTime < positionMinTimeInterval) {
								positionMap.put(position.getRtPositionID(), position);
							} else {
								positionMap.put(position.getRtPositionID(), position);
								socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_POSITIONS,
										new ArrayList<>(positionMap.values()));
								positionLastSendTime = System.currentTimeMillis();
								positionMap = new LinkedHashMap<>();
							}
						} catch (Exception e) {
							log.error("向SocketIO转发Position发生异常!!!", e);
						}
					} else if (EventConstant.EVENT_ACCOUNT.equals(fastEvent.getEventType())) {
						try {
							// 不经缓存，直接发送
							socketIOMessageEventHandler.sendEventToPresetClient(EventConstant.EVENT_ACCOUNT,fastEvent.getAccount());
							
							Account account = fastEvent.getAccount();
							FastEvent nextEvent = eventQueue.peek();
							if (nextEvent != null || (EventConstant.EVENT_ACCOUNT.equals(fastEvent.getEventType())
									&& System.currentTimeMillis() - accountLastSendTime < accountMaxTimeInterval)) {
								accountMap.put(account.getRtAccountID(), account);
							} else if (System.currentTimeMillis() - accountLastSendTime < accountMinTimeInterval) {
								accountMap.put(account.getRtAccountID(), account);
							} else {
								accountMap.put(account.getRtAccountID(), account);
								socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_ACCOUNTS,
										new ArrayList<>(accountMap.values()));
								accountLastSendTime = System.currentTimeMillis();
								accountMap = new LinkedHashMap<>();
							}
						} catch (Exception e) {
							log.error("向SocketIO转发Account发生异常!!!", e);
						}

					} else if (EventConstant.EVENT_LOG.equals(fastEvent.getEventType())) {

						try {
							// 不经缓存，直接发送
							socketIOMessageEventHandler.sendEventToPresetClient(EventConstant.EVENT_LOG,fastEvent.getLogData());
							
							LogData logData = fastEvent.getLogData();
							FastEvent nextEvent = eventQueue.peek();
							if (nextEvent != null || (EventConstant.EVENT_LOG.equals(fastEvent.getEventType())
									&& System.currentTimeMillis() - logDataLastSendTime < logDataMaxTimeInterval)) {
								logDataList.add(logData);
							} else if (System.currentTimeMillis() - logDataLastSendTime < logDataMinTimeInterval) {
								logDataList.add(logData);
							} else {
								logDataList.add(logData);
								// 发送所有日志
								if (socketIOMessageEventHandler != null) {
									socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_LOGS, logDataList);
									logDataLastSendTime = System.currentTimeMillis();
									logDataList = new ArrayList<>();
								} else {
									// nop 系统启动初期socketIOMessageEventHandler可能尚未注入
								}
							}
						} catch (Exception e) {
							log.error("向SocketIO转发LogData发生异常!!!", e);
						}
					} else if (EventConstant.EVENT_GATEWAY.equals(fastEvent.getEventType())) {
						socketIOMessageEventHandler.sendEventToPresetClient(EventConstant.EVENT_GATEWAY,fastEvent.getEvent());
						socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_GATEWAY, fastEvent.getEvent());
					} else if (EventConstant.EVENT_TICKS_CHANGED.equals(fastEvent.getEventType())) {
						socketIOMessageEventHandler.sendEventToPresetClient(EventConstant.EVENT_TICKS_CHANGED, fastEvent.getEvent());
						socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_TICKS_CHANGED, fastEvent.getEvent());
					} else {
						log.warn("未能识别的事件数据类型{}", JSON.toJSONString(fastEvent.getEvent()));
					}
				}

				if (System.currentTimeMillis() - tickLastSendTime > tickMaxTimeInterval && tickMap.size() > 0) {
					socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_TICKS, new ArrayList<>(tickMap.values()));
					tickLastSendTime = System.currentTimeMillis();
					tickMap = new LinkedHashMap<>();
				}

				if (System.currentTimeMillis() - positionLastSendTime > positionMaxTimeInterval
						&& positionMap.size() > 0) {
					socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_POSITIONS,
							new ArrayList<>(positionMap.values()));
					positionLastSendTime = System.currentTimeMillis();
					positionMap = new LinkedHashMap<>();
				}

				if (System.currentTimeMillis() - orderLastSendTime > orderMaxTimeInterval && orderMap.size() > 0) {
					socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_ORDERS,
							new ArrayList<>(orderMap.values()));
					orderLastSendTime = System.currentTimeMillis();
					orderMap = new LinkedHashMap<>();
				}

				if (System.currentTimeMillis() - tradeLastSendTime > tradeMaxTimeInterval && tradeMap.size() > 0) {
					socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_TRADES,
							new ArrayList<>(tradeMap.values()));
					tradeLastSendTime = System.currentTimeMillis();
					tradeMap = new LinkedHashMap<>();
				}

				if (System.currentTimeMillis() - contractLastSendTime > contractMaxTimeInterval
						&& contractMap.size() > 0) {
					socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_CONTRACTS,
							new ArrayList<>(contractMap.values()));
					contractLastSendTime = System.currentTimeMillis();
					contractMap = new LinkedHashMap<>();
				}

				if (System.currentTimeMillis() - accountLastSendTime > accountMaxTimeInterval
						&& accountMap.size() > 0) {
					socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_ACCOUNTS,
							new ArrayList<>(accountMap.values()));
					accountLastSendTime = System.currentTimeMillis();
					accountMap = new LinkedHashMap<>();
				}

				if (System.currentTimeMillis() - logDataLastSendTime > logDataMaxTimeInterval
						&& logDataList.size() > 0) {
					socketIOMessageEventHandler.sendEventToLoginClient(EventConstant.EVENT_LOGS, logDataList);
					logDataLastSendTime = System.currentTimeMillis();
					logDataList = new ArrayList<>();
				}

			}

		}

	}

}
