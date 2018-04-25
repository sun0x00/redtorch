package xyz.redtorch.web.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.engine.event.EventConstant;
import xyz.redtorch.trader.engine.event.EventData;
import xyz.redtorch.trader.engine.event.EventEngine;
import xyz.redtorch.trader.engine.event.EventListener;
import xyz.redtorch.trader.engine.main.MainEngine;
import xyz.redtorch.trader.engine.main.impl.MainEngineImpl;
import xyz.redtorch.trader.entity.Account;
import xyz.redtorch.trader.entity.CancelOrderReq;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.LocalPositionDetail;
import xyz.redtorch.trader.entity.LogData;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Position;
import xyz.redtorch.trader.entity.SubscribeReq;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.gateway.Gateway;
import xyz.redtorch.trader.gateway.GatewaySetting;
import xyz.redtorch.trader.module.zeus.ZeusEngine;
import xyz.redtorch.trader.module.zeus.impl.TradingEngineImpl;
import xyz.redtorch.trader.module.zeus.strategy.Strategy;
import xyz.redtorch.utils.CommonUtil;
import xyz.redtorch.web.service.TradingService;
import xyz.redtorch.web.socketio.SocketIOMessageEventHandler;

/**
 * @author sun0x00@gmail.com
 */
@Service
public class TradingServiceImpl implements TradingService{
	
	private Logger log = LoggerFactory.getLogger(TradingServiceImpl.class);
	
	@Autowired
	private SocketIOMessageEventHandler socketIOMessageEventHandler;
	
	LinkedBlockingQueue<EventData> eventDataQueue = new LinkedBlockingQueue<>();
	
	// 使用无大小限制的线程池,线程空闲60s会被释放
	ExecutorService executor = Executors.newCachedThreadPool();
	
	private MainEngine mainEngine = new MainEngineImpl();
	private ZeusEngine zeusEngine  =new TradingEngineImpl(mainEngine);
	
	public TradingServiceImpl() {
		EventTransferTask eventTransferTask = new EventTransferTask();
		EventEngine eventEngine = mainEngine.getEventEngine();
		eventEngine.registerListener(EventConstant.EVENT_TICK, eventTransferTask);
		eventEngine.registerListener(EventConstant.EVENT_TRADE, eventTransferTask);
		eventEngine.registerListener(EventConstant.EVENT_ORDER, eventTransferTask);
		eventEngine.registerListener(EventConstant.EVENT_POSITION, eventTransferTask);
		eventEngine.registerListener(EventConstant.EVENT_ACCOUNT, eventTransferTask);
		eventEngine.registerListener(EventConstant.EVENT_CONTRACT, eventTransferTask);
		eventEngine.registerListener(EventConstant.EVENT_ERROR, eventTransferTask);
		eventEngine.registerListener(EventConstant.EVENT_GATEWAY, eventTransferTask);
		eventEngine.registerListener(EventConstant.EVENT_LOG, eventTransferTask);

		// 这一步暂时没有实际意义,预留
		mainEngine.addModel(zeusEngine);

		executor.execute(mainEngine);
		executor.execute(zeusEngine);
		executor.execute(eventTransferTask);
	}
	
	@Override
	public String sendOrder(String gatewayID, String rtSymbol, double price, int volume, String priceType,String direction,String offset) {
		
		Contract contract = mainEngine.getContract(rtSymbol, gatewayID);
		if(contract != null) {
			OrderReq orderReq = new OrderReq();
			orderReq.setSymbol(contract.getSymbol());
			orderReq.setExchange(contract.getExchange());
			orderReq.setRtSymbol(contract.getRtSymbol());
			orderReq.setPrice(CommonUtil.rountToPriceTick(contract.getPriceTick(), price));
			orderReq.setVolume(volume);
			orderReq.setGatewayID(gatewayID);
			orderReq.setDirection(direction);
			orderReq.setOffset(offset);
			orderReq.setPriceType(priceType);
			
			return mainEngine.sendOrder(orderReq);
		}else {
			log.error("发单失败,未找到合约");
			return null;
		}

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
		}else {
			log.error("无法撤单,未能找到委托ID {}", rtOrderID);
		}
	}
	
	@Override
	public void cancelAllOrders() {
		
		for(Order order:mainEngine.getWorkingOrders()) {
			System.out.println(order.getStatus());
			if (!RtConstant.STATUS_FINISHED.contains(order.getStatus())) {

				CancelOrderReq cancelOrderReq = new CancelOrderReq();

				cancelOrderReq.setSymbol(order.getSymbol());
				cancelOrderReq.setExchange(order.getExchange());

				cancelOrderReq.setFrontID(order.getFrontID());
				cancelOrderReq.setSessionID(order.getSessionID());
				cancelOrderReq.setOrderID(order.getOrderID());
				cancelOrderReq.setGatewayID(order.getGatewayID());

				mainEngine.cancelOrder(cancelOrderReq);
				
			}
		}
	}
	
	
	
	@Override
	public boolean subscribe(String rtSymbol, String gatewayID) {
		if(StringUtils.isEmpty(rtSymbol)) {
			return false;
		}
		SubscribeReq subscribeReq = new SubscribeReq();
		subscribeReq.setGatewayID(gatewayID);
		subscribeReq.setRtSymbol(rtSymbol);
		return mainEngine.subscribe(subscribeReq, "web-page-00");
	}

	@Override
	public boolean unsubscribe(String rtSymbol, String gatewayID) {
		SubscribeReq subscribeReq = new SubscribeReq();
		subscribeReq.setGatewayID(gatewayID);
		subscribeReq.setRtSymbol(rtSymbol);
		return mainEngine.unsubscribe(rtSymbol,gatewayID, "web-page-00");
	}
	
	@Override
	public List<Trade> getTrades(){
		return mainEngine.getTrades();
	}

	@Override
	public  List<Order> getOrders(){
		return mainEngine.getOrders();
	}

	@Override
	public  List<LocalPositionDetail> getLocalPositionDetails(){
		return mainEngine.getLocalPositionDetails();
	}
	@Override
	public List<Position> getPositions() {
		return mainEngine.getPositions();
	}
	@Override
	public  List<Account> getAccounts(){
		return mainEngine.getAccounts();
	}

	@Override
	public List<Contract> getContracts(){
		return mainEngine.getContracts();
	}
	@Override
	public List<GatewaySetting> getGatewaySettings() {
		List<GatewaySetting> gatewaySettings = mainEngine.queryGatewaySettings();
		if(gatewaySettings!=null) {
			for(GatewaySetting gatewaySetting:gatewaySettings) {
				Gateway gateway = mainEngine.getGateway(gatewaySetting.getGatewayID());

				gatewaySetting.setRuntimeStatus(false);
				if(gateway!=null) {
					if(gateway.isConnected()) {
						gatewaySetting.setRuntimeStatus(true);
					}
				}
			}
		}
		return  gatewaySettings;
	}
	@Override
	public void deleteGateway(String gatewayID) {
		mainEngine.deleteGateway(gatewayID);
	}
	@Override
	public void changeGatewayConnectStatus(String gatewayID) {
		Gateway gateway = mainEngine.getGateway(gatewayID);
		if(gateway != null) {
			if(gateway.isConnected()) {
				mainEngine.disconnectGateway(gatewayID);
			}else {
				gateway.connect();
			}
		}else {
			mainEngine.connectGateway(gatewayID);
		}
		
	}
	@Override
	public void saveOrUpdateGatewaySetting(GatewaySetting gatewaySetting) {
		if(StringUtils.isEmpty(gatewaySetting.getGatewayID())) {
			String[] tdAddressArray = gatewaySetting.getTdAddress().split("\\.");
			String tdAddressSuffix = tdAddressArray[tdAddressArray.length-1].replaceAll(":", "\\.");
			gatewaySetting.setGatewayID(gatewaySetting.getBrokerID()+"."+gatewaySetting.getGatewayDisplayName()+"."+tdAddressSuffix);
		}else {
			mainEngine.deleteGateway(gatewaySetting.getGatewayID());
		}
		mainEngine.saveGateway(gatewaySetting);
	}

	@Override
	public void zeusLoadStrategy() {
		zeusEngine.loadStartegy();
	}

	@Override
	public List<Map<String, Object>> zeusGetStrategyInfos() {
		List<Map<String, Object>>  strategyInfos = new ArrayList<Map<String,Object>>();
		List<Strategy> startegyList = zeusEngine.getStragetyList();
		for(Strategy strategy:startegyList) {
			Map<String, Object> strategyInfo = new HashMap<>();
			
			strategyInfo.put("strategyName", strategy.getName());
			strategyInfo.put("strategyID", strategy.getID());
			strategyInfo.put("initStatus", strategy.isInitStatus());
			strategyInfo.put("trading", strategy.isTrading());
			
			strategyInfo.put("paramMap", strategy.getParamMap());
			strategyInfo.put("varMap", strategy.getVarMap());
			strategyInfos.add(strategyInfo);
		}
		return strategyInfos;
	}

	@Override
	public void zeusInitStrategy(String strategyID) {
		zeusEngine.initStrategy(strategyID);
	}

	@Override
	public void zeusSartStrategy(String strategyID) {
		zeusEngine.startStrategy(strategyID);
		
	}

	@Override
	public void zeusStopStrategy(String strategyID) {
		zeusEngine.stopStrategy(strategyID);
		
	}

	@Override
	public void zeusInitAllStrategy() {
		zeusEngine.initAllStrategy();
	}

	@Override
	public void zeusSartAllStrategy() {
		zeusEngine.startAllStrategy();
	}

	@Override
	public void zeusStopAllStrategy() {
		zeusEngine.stopAllStrategy();
	}

	@Override
	public void zeusReloadStrategy(String strategyID) {
		zeusEngine.unloadStrategy(strategyID);
		zeusEngine.loadStartegy(strategyID);
	}
	

	@Override
	public List<LogData> getLogDatas() {
		return mainEngine.getLogDatas();
	}
	
	class EventTransferTask implements EventListener{

		@Override
		public void run() {while (!Thread.currentThread().isInterrupted()) {
			EventData ed = null;
			try {
				ed = eventDataQueue.take();
			} catch (InterruptedException e) {
				log.error("主引擎捕获到线程中断异常,线程停止!!!", e);
			}
			// 判断消息类型
			// 使用复杂的对比判断逻辑,便于扩展修改
			if (EventConstant.EVENT_TICK.equals(ed.getEventType())) {
				try {
					Tick tick = (Tick) ed.getEventObj();
					socketIOMessageEventHandler.sendEvent(ed.getEvent(), tick);
				} catch (Exception e) {
					log.error("向SocketIO转发Tick发生异常!!!", e);
				}
			} else if (EventConstant.EVENT_TRADE.equals(ed.getEventType())) {
				try {
					Trade trade = (Trade) ed.getEventObj();
					socketIOMessageEventHandler.sendEvent(ed.getEvent(), trade);
				} catch (Exception e) {
					log.error("向SocketIO转发Trade发生异常!!!", e);
				}
			} else if (EventConstant.EVENT_ORDER.equals(ed.getEventType())) {
				try {
					Order order = (Order) ed.getEventObj();
					socketIOMessageEventHandler.sendEvent(ed.getEvent(), order);
				} catch (Exception e) {
					log.error("向SocketIO转发Order发生异常!!!", e);
				}
			} else if (EventConstant.EVENT_CONTRACT.equals(ed.getEventType())) {
				try {
					Contract contract = (Contract) ed.getEventObj();
					socketIOMessageEventHandler.sendEvent(ed.getEvent(), contract);
				} catch (Exception e) {
					log.error("向SocketIO转发Contract发生异常!!!", e);
				}
			} else if (EventConstant.EVENT_POSITION.equals(ed.getEventType())) {
				try {
					Position position = (Position) ed.getEventObj();
					socketIOMessageEventHandler.sendEvent(ed.getEvent(), position);
				} catch (Exception e) {
					log.error("向SocketIO转发Position发生异常!!!", e);
				}
			} else if (EventConstant.EVENT_ACCOUNT.equals(ed.getEventType())) {
				try {
					Account account = (Account) ed.getEventObj();
					socketIOMessageEventHandler.sendEvent(ed.getEvent(), account);
				} catch (Exception e) {
					log.error("向SocketIO转发Account发生异常!!!", e);
				}
			} else if (EventConstant.EVENT_LOG.equals(ed.getEventType())) {
				try {
					LogData logData = (LogData) ed.getEventObj();
					socketIOMessageEventHandler.sendEvent(ed.getEvent(), logData);
				} catch (Exception e) {
					log.error("向SocketIO转发Order发生异常!!!", e);
				}
			} else if(EventConstant.EVENT_THREAD_STOP.equals(ed.getEventType())){
				// 弃用
				//Thread.currentThread().interrupt();
				break;
			} else {
				log.warn("主引擎未能识别的事件数据类型{}", JSON.toJSONString(ed));
			}
		}
			
		}

		@Override
		public void onEvent(EventData eventData) {
			if (eventData != null) {
				eventDataQueue.add(eventData);
			}
		}

		@Override
		public void stop() {
			mainEngine.getEventEngine().removeListener(null, this);
			// 通知其他线程
			EventData eventData = new EventData();
			eventData.setEvent(EventConstant.EVENT_THREAD_STOP);
			eventData.setEventType(EventConstant.EVENT_THREAD_STOP);
			eventDataQueue.add(eventData);
		}
		
	}

}
