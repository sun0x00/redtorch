package xyz.redtorch.core.zeus.impl;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.CancelOrderReq;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.service.CoreEngineService;
import xyz.redtorch.core.service.FastEventEngineService;
import xyz.redtorch.core.service.extend.event.EventConstant;
import xyz.redtorch.core.service.extend.event.FastEvent;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandler;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandlerAbstract;
import xyz.redtorch.core.zeus.ZeusTradingBaseService;
import xyz.redtorch.utils.CommonUtil;
import xyz.redtorch.core.zeus.ZeusMmapService;

@Service
public class ZeusMmapServiceImpl extends FastEventDynamicHandlerAbstract
		implements ZeusMmapService, FastEventDynamicHandler, InitializingBean {

	private Logger log = LoggerFactory.getLogger(ZeusMmapServiceImpl.class);

	@Autowired
	private FastEventEngineService fastEventEngineService;
	@Autowired
	private CoreEngineService coreEngineService;
	@Autowired
	private ZeusTradingBaseService zeusTradingBaseService;

	@Value("${chronicleQueueBasePath}")
	private String chronicleQueueBasePath;
	
	@Value("${rt.core.zeus.mmap.performance}")
	private String mmapPerformance;
	
	private SingleChronicleQueue queueTx;
	private SingleChronicleQueue queueRx;
	private ExcerptAppender queueTxEa;
	private ExcerptTailer queueRxEt;

	// 使用无大小限制的线程池,线程空闲60s会被释放
	private ExecutorService executor = Executors.newCachedThreadPool();

	@Override
	public void afterPropertiesSet() throws Exception {

		queueTx = SingleChronicleQueueBuilder.binary(chronicleQueueBasePath + File.separator + "channel0")
				.rollCycle(RollCycles.HOURLY).build();
		queueTxEa = queueTx.acquireAppender();

		queueRx = SingleChronicleQueueBuilder.binary(chronicleQueueBasePath + File.separator + "channel1")
				.rollCycle(RollCycles.HOURLY).build();
		queueRxEt = queueRx.createTailer().toEnd();

		fastEventEngineService.addHandler(this);
		subscribeEvent(EventConstant.EVENT_TICK);
		subscribeEvent(EventConstant.EVENT_TRADE);
		subscribeEvent(EventConstant.EVENT_ORDER);

		executor.execute(new RxTask());

		log.info("MMAP服务已经启动");
	}

	@Override
	public void onEvent(final FastEvent fastEvent, final long sequence, final boolean endOfBatch) throws Exception {

		if (!subscribedEventSet.contains(fastEvent.getEvent())) {
			return;
		}

		// 判断消息类型
		if (EventConstant.EVENT_TICK.equals(fastEvent.getEventType())) {
			try {
				Tick tick = fastEvent.getTick();
				onTick(tick);
			} catch (Exception e) {
				log.error("onTick发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_TRADE.equals(fastEvent.getEventType())) {
			try {
				Trade trade = fastEvent.getTrade();
				onTrade(trade);
			} catch (Exception e) {
				log.error("onTrade发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_ORDER.equals(fastEvent.getEventType())) {
			try {
				Order order = fastEvent.getOrder();
				onOrder(order);
			} catch (Exception e) {
				log.error("onOrder发生异常!!!", e);
			}
		} else {
			log.warn("未能识别的事件数据类型:" + JSON.toJSONString(fastEvent.getEvent()));
		}
	}

	private synchronized ExcerptAppender getQueueTxEa() {
		return queueTxEa;
	}

	private synchronized ExcerptTailer getQueueRxEt() {
		return queueRxEt;
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
	public void initStrategy(String strategyID) {
		log.info("初始化策略,策略ID:" + strategyID);

		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_COMMAND) //
				.writeInt(COMMAND_INIT_STARTEGY) //
				.writeUtf8(strategyID));
	}

	@Override
	public void startStrategy(String strategyID) {
		log.info("启动策略,策略ID:" + strategyID);

		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_COMMAND) //
				.writeInt(COMMAND_START_STARTEGY) //
				.writeUtf8(strategyID));
	}

	@Override
	public void stopStrategy(String strategyID) {
		log.info("停止策略,策略ID:" + strategyID);
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_COMMAND) //
				.writeInt(COMMAND_STOP_STARTEGY) //
				.writeUtf8(strategyID));
	}

	@Override
	public void reloadStrategy(String strategyID) {
		log.info("重新加载策略,策略ID:" + strategyID);
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_COMMAND) //
				.writeInt(COMMAND_RELOAD_STARTEGY) //
				.writeUtf8(strategyID));
	}

	@Override
	public void initAllStrategy() {
		log.info("初始化所有策略");
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_COMMAND) //
				.writeInt(COMMAND_INIT_ALL_STARTEGY));
	}

	@Override
	public void startAllStrategy() {
		log.info("启动所有策略");
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_COMMAND) //
				.writeInt(COMMAND_START_ALL_STARTEGY));
	}

	@Override
	public void stopAllStrategy() {
		log.info("停止所有策略");
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_COMMAND) //
				.writeInt(COMMAND_STOP_ALL_STARTEGY));
	}

	@Override
	public void reloadAllStrategy() {
		log.info("重新加载所有策略");
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_COMMAND) //
				.writeInt(COMMAND_RELOAD_ALL_STARTEGY));

	}

	@Override
	public void onTick(Tick tick) {
		
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_TICK)

				.writeUtf8(tick.getRtTickID()) //
				.writeUtf8(tick.getGatewayID()) //
				.writeUtf8(tick.getRtSymbol()) //

				.writeUtf8(tick.getGatewayDisplayName()) //
		
				.writeUtf8(tick.getSymbol()) //
				.writeUtf8(tick.getExchange()) //
				.writeUtf8(tick.getContractName()) //

				.writeUtf8(tick.getTradingDay()) //
				.writeUtf8(tick.getActionDay()) //
				.writeUtf8(tick.getActionTime()) //
				.writeLong(tick.getDateTime().getMillis()) //

				.writeInt(tick.getStatus()) //

				.writeDouble(tick.getLastPrice()) //
				.writeInt(tick.getLastVolume()) //
				.writeInt(tick.getVolume()) //
				.writeDouble(tick.getOpenInterest()) //

				.writeLong(tick.getPreOpenInterest()) //
				.writeDouble(tick.getPreClosePrice()) //
				.writeDouble(tick.getPreSettlePrice()) //

				.writeDouble(tick.getOpenPrice()) //
				.writeDouble(tick.getHighPrice()) //
				.writeDouble(tick.getLowPrice()) //

				.writeDouble(tick.getUpperLimit()) //
				.writeDouble(tick.getLowerLimit()) //

				.writeDouble(tick.getBidPrice1()) //
				.writeDouble(tick.getBidPrice2()) //
				.writeDouble(tick.getBidPrice3()) //
				.writeDouble(tick.getBidPrice4()) //
				.writeDouble(tick.getBidPrice5()) //
				.writeDouble(tick.getBidPrice6()) //
				.writeDouble(tick.getBidPrice7()) //
				.writeDouble(tick.getBidPrice8()) //
				.writeDouble(tick.getBidPrice9()) //
				.writeDouble(tick.getBidPrice10()) //

				.writeDouble(tick.getAskPrice1()) //
				.writeDouble(tick.getAskPrice2()) //
				.writeDouble(tick.getAskPrice3()) //
				.writeDouble(tick.getAskPrice4()) //
				.writeDouble(tick.getAskPrice5()) //
				.writeDouble(tick.getAskPrice6()) //
				.writeDouble(tick.getAskPrice7()) //
				.writeDouble(tick.getAskPrice8()) //
				.writeDouble(tick.getAskPrice9()) //
				.writeDouble(tick.getAskPrice10()) //

				.writeInt(tick.getBidVolume1()) //
				.writeInt(tick.getBidVolume2()) //
				.writeInt(tick.getBidVolume3()) //
				.writeInt(tick.getBidVolume4()) //
				.writeInt(tick.getBidVolume5()) //
				.writeInt(tick.getBidVolume6()) //
				.writeInt(tick.getBidVolume7()) //
				.writeInt(tick.getBidVolume8()) //
				.writeInt(tick.getBidVolume9()) //
				.writeInt(tick.getBidVolume10()) //

				.writeInt(tick.getAskVolume1()) //
				.writeInt(tick.getAskVolume2()) //
				.writeInt(tick.getAskVolume3()) //
				.writeInt(tick.getAskVolume4()) //
				.writeInt(tick.getAskVolume5()) //
				.writeInt(tick.getAskVolume6()) //
				.writeInt(tick.getAskVolume7()) //
				.writeInt(tick.getAskVolume8()) //
				.writeInt(tick.getAskVolume9()) //
				.writeInt(tick.getAskVolume10()));
	}

	@Override
	public void onOrder(Order order) {
		
		if (StringUtils.isBlank(order.getOriginalOrderID())
				&& zeusTradingBaseService.getOriginalOrderID(order.getRtOrderID()) != null) {
			order.setOriginalOrderID(zeusTradingBaseService.getOriginalOrderID(order.getRtOrderID()));
		}
		
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_ORDER) //

				.writeUtf8(order.getOriginalOrderID()) //

				.writeUtf8(order.getAccountID()) //
				.writeUtf8(order.getRtAccountID()) //

				.writeUtf8(order.getGatewayID()) //
				.writeUtf8(order.getGatewayDisplayName()) //

				.writeUtf8(order.getSymbol()) //
				.writeUtf8(order.getExchange()) //
				.writeUtf8(order.getRtSymbol()) //
				.writeUtf8(order.getContractName()) //

				.writeUtf8(order.getOrderID()) //
				.writeUtf8(order.getRtOrderID()) //

				.writeUtf8(order.getDirection()) //
				.writeUtf8(order.getOffset()) //
				.writeDouble(order.getPrice()) //
				.writeInt(order.getTotalVolume()) //
				.writeInt(order.getTradedVolume()) //
				.writeUtf8(order.getStatus()) //

				.writeUtf8(order.getTradingDay()) //

				.writeUtf8(order.getOrderDate()) //
				.writeUtf8(order.getOrderTime()) //
				.writeUtf8(order.getCancelTime()) //
				.writeUtf8(order.getActiveTime()) //
				.writeUtf8(order.getUpdateTime()) //

				.writeInt(order.getFrontID()) //
				.writeInt(order.getSessionID()));
	}

	@Override
	public void onTrade(Trade trade) {
		if (StringUtils.isBlank(trade.getOriginalOrderID())
				&& zeusTradingBaseService.getOriginalOrderID(trade.getRtOrderID()) != null) {
			trade.setOriginalOrderID(zeusTradingBaseService.getOriginalOrderID(trade.getRtOrderID()));
		}
		
		getQueueTxEa().writeBytes(b -> b
				// 写入数据类型
				.writeInt(DATA_TRADE) //
				.writeUtf8(trade.getOriginalOrderID()) //
				.writeUtf8(trade.getAccountID()) //
				.writeUtf8(trade.getRtAccountID()) //
				.writeUtf8(trade.getGatewayID()) //
				.writeUtf8(trade.getGatewayDisplayName()) //

				.writeUtf8(trade.getSymbol()) //
				.writeUtf8(trade.getExchange()) //
				.writeUtf8(trade.getRtSymbol()) //
				.writeUtf8(trade.getContractName()) //

				.writeUtf8(trade.getTradeID()) //
				.writeUtf8(trade.getRtTradeID()) //

				.writeUtf8(trade.getOrderID()) //
				.writeUtf8(trade.getRtOrderID()) //

				.writeUtf8(trade.getDirection()) //
				.writeUtf8(trade.getOffset()) //
				.writeDouble(trade.getPrice()) //

				.writeInt(trade.getVolume()) //
				.writeUtf8(trade.getTradingDay()) //
				.writeUtf8(trade.getTradeDate()) //
				.writeUtf8(trade.getTradeTime()));

	}

	private class RxTask implements Runnable {

		@Override
		public void run() {
			boolean performance = false;
			if("HIGH".equals(mmapPerformance)) {
				performance = true;
				log.info("MMAP RxTask已启动,高性能模式");
			}else {
				log.info("MMAP RxTask已启动,休眠模式");
			}
			int invalidReadCount = 0;
			boolean effectiveRead;
			while (!Thread.currentThread().isInterrupted()) {
				effectiveRead = getQueueRxEt().readBytes(in -> {
					
					int dataType = in.readInt();

					if (DATA_ORDERREQ == dataType) {
						OrderReq orderReq = new OrderReq();

						orderReq.setRtAccountID(in.readUtf8());
						orderReq.setSymbol(in.readUtf8());
						orderReq.setExchange(in.readUtf8());
						orderReq.setRtSymbol(in.readUtf8());

						orderReq.setPrice(in.readDouble());
						orderReq.setVolume(in.readInt());
						orderReq.setDirection(in.readUtf8());
						orderReq.setOffset(in.readUtf8());
						orderReq.setPriceType(in.readUtf8());

						orderReq.setOriginalOrderID(in.readUtf8());
						orderReq.setOperatorID(in.readUtf8());

						orderReq.setProductClass(in.readUtf8());
						orderReq.setCurrency(in.readUtf8());
						orderReq.setExpiry(in.readUtf8());
						orderReq.setStrikePrice(in.readDouble());
						orderReq.setOptionType(in.readUtf8());
						orderReq.setLastTradeDateOrContractMonth(in.readUtf8());
						orderReq.setMultiplier(in.readUtf8());
						
						Account account = coreEngineService.getAccount(orderReq.getRtAccountID());
						if (account != null) {
							orderReq.setAccountID(account.getAccountID());
							orderReq.setGatewayID(account.getGatewayID());
							orderReq.setGatewayDisplayName(account.getGatewayDisplayName());
							
							Contract contract = coreEngineService.getContract(orderReq.getRtSymbol(), account.getGatewayID());
							if(contract != null) {
								String symbol = contract.getSymbol();
								String exchange = contract.getExchange();
								orderReq.setSymbol(symbol);
								orderReq.setExchange(exchange);
								double priceTick = contract.getPriceTick();
								orderReq.setPrice(CommonUtil.rountToPriceTick(priceTick, orderReq.getPrice()));
								
								String rtOrderID = coreEngineService.sendOrder(orderReq);
								zeusTradingBaseService.registerOriginalOrderID(rtOrderID, orderReq.getOriginalOrderID());
							}else {
								log.error("发单错误,无法找到合约,{}", orderReq.toString());
							}

						} else {
							log.error("发单错误,无法找到账户,{}", orderReq.toString());
						}

					} else if (DATA_CANCEL_ORDER == dataType) {

						String originalOrderID = in.readUtf8();
						String operatorID = in.readUtf8();
						
						String rtOrderID = zeusTradingBaseService.getRtOrderID(originalOrderID);

						if (StringUtils.isBlank(originalOrderID)) {
							log.error("originalOrderID不可为空!");
						} else if (StringUtils.isBlank(operatorID)) {
							log.error("operatorID不可为空!");
						} else if (rtOrderID == null) {
							log.error("未能找到rtOrderID,originalOrderID:{}", originalOrderID);
						} else {
							Order order = coreEngineService.getOrder(rtOrderID);
							if (order != null) {
								if (!RtConstant.STATUS_FINISHED.contains(order.getStatus())) {

									CancelOrderReq cancelOrderReq = new CancelOrderReq();

									cancelOrderReq.setSymbol(order.getSymbol());
									cancelOrderReq.setExchange(order.getExchange());

									cancelOrderReq.setFrontID(order.getFrontID());
									cancelOrderReq.setSessionID(order.getSessionID());
									cancelOrderReq.setOperatorID(operatorID);
									cancelOrderReq.setOrderID(order.getOrderID());
									cancelOrderReq.setGatewayID(order.getGatewayID());

									coreEngineService.cancelOrder(cancelOrderReq);

								} else {
									log.warn("无法撤单,委托状态为完成,rtOrderID:{}", rtOrderID);
								}
							} else {
								log.warn("无法撤单,委托不存在,rtOrderID:{}", rtOrderID);
							}
						}
					}

				});
				
				// 如果非性能模式
				if(!performance) {
					if(effectiveRead) {
					// 有效读取
						// 重置计数器
						invalidReadCount = 0;
					}else {
					// 无效读取
						// 计数器累加
						invalidReadCount += 1;
						// 如果超过连续没有读到数据，休眠
						if(invalidReadCount>=3) {
							try {
								Thread.sleep(3);
							} catch (InterruptedException e) {
								// nop
							}
						}
					}
				}
				
			}
		}

	}

}
