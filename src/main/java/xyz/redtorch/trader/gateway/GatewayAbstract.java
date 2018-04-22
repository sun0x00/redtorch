package xyz.redtorch.trader.gateway;

import java.util.Date;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.trader.engine.event.EventConstant;
import xyz.redtorch.trader.engine.event.EventData;
import xyz.redtorch.trader.engine.event.EventEngine;
import xyz.redtorch.trader.entity.Account;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.Position;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.utils.CommonUtil;

/**
 * @author sun0x00@gmail.com
 */
public abstract class GatewayAbstract implements Gateway{
	
	private static Logger log = LoggerFactory.getLogger(GatewayAbstract.class);
	
	protected String gatewayID;
	protected String gatewayDisplayName;
	protected String gatewayLogInfo;

	protected GatewaySetting gatewaySetting;
	
	public EventEngine eventEngine;

	protected HashSet<String> subscribedSymbols = new HashSet<>();

	Timer timer = new Timer();
	
	public GatewayAbstract(GatewaySetting gatewaySetting, EventEngine eventEngine){
		this.eventEngine = eventEngine;
		this.gatewaySetting = gatewaySetting;
		this.gatewayID = gatewaySetting.getGatewayID();
		this.gatewayDisplayName = gatewaySetting.getGatewayDisplayName();
		this.gatewayLogInfo = "GatewayID:" + gatewayID + " GatewayDisplayName:" + gatewayDisplayName+" ";
		log.info(gatewayLogInfo+"初始化");
		timer.schedule(new QueryTimerTask(), new Date(), 1000);

	}
	
	@Override
	public HashSet<String> getSubscribedSymbols() {
		return subscribedSymbols;
	}

	@Override
	public EventEngine getEventEngine() {
		return eventEngine;
	}

	@Override
	public GatewaySetting getGatewaySetting() {
		return gatewaySetting;
	}
	
	@Override
	public String getGatewayID() {
		return gatewayID;
	}

	@Override
	public String getGatewayDisplayName() {
		return gatewayDisplayName;
	}

	@Override
	public String getGatewayLogInfo() {
		return this.gatewayLogInfo;
	}

	@Override
	public void emitPositon(Position position) {
		String event = EventConstant.EVENT_POSITION;
		EventData eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_POSITION);
		eventData.setEventObj(position);
		// 发送事件
		eventEngine.emit(event, eventData);
		
	}

	@Override
	public void emitAccount(Account account) {
		String event = EventConstant.EVENT_ACCOUNT;
		EventData eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_ACCOUNT);
		eventData.setEventObj(account);
		// 发送事件
		eventEngine.emit(event, eventData);
		
	}

	@Override
	public void emitContract(Contract contract) {
		String event = EventConstant.EVENT_CONTRACT;
		EventData eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_CONTRACT);
		eventData.setEventObj(contract);
		// 发送事件
		eventEngine.emit(event, eventData);
		
	}

	@Override
	public void emitTick(Tick tick) {
		String event = EventConstant.EVENT_TICK+tick.getGatewayID()+tick.getRtSymbol();
		EventData eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_TICK);
		eventData.setEventObj(tick);
		eventEngine.emit(event, eventData);
		
		event = EventConstant.EVENT_TICK+tick.getRtSymbol();
		eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_TICK);
		eventData.setEventObj(tick);
		eventEngine.emit(event, eventData);
		
		event = EventConstant.EVENT_TICK;
		eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_TICK);
		eventData.setEventObj(tick);
		eventEngine.emit(event, eventData);
	}

	@Override
	public void emitTrade(Trade trade) {
		// 发送带委托ID的事件
		String event = EventConstant.EVENT_TRADE + trade.getRtOrderID();
		EventData eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_TRADE);
		eventData.setEventObj(trade);
		eventEngine.emit(event, eventData);
		
		event = EventConstant.EVENT_TRADE;
		eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_TRADE);
		eventData.setEventObj(trade);
		eventEngine.emit(event, eventData);
		
	}

	@Override
	public void emitOrder(Order order) {
		// 发送带委托ID的事件
		String event = EventConstant.EVENT_ERROR + order.getRtOrderID();
		EventData eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_ORDER);
		eventData.setEventObj(order);
		eventEngine.emit(event, eventData);
		
		event = EventConstant.EVENT_ORDER;
		eventData = new EventData();
		eventData.setEvent(event);
		eventData.setEventType(EventConstant.EVENT_ORDER);
		eventData.setEventObj(order);
		eventEngine.emit(event, eventData);
	}

	@Override
	public void emitErrorLog(String logContent) {
		CommonUtil.emitErrorLog(eventEngine, logContent);
	}
	
	@Override
	public void emitInfoLog(String logContent) {
		CommonUtil.emitInfoLog(eventEngine, logContent);
	}
	
	@Override
	public void emitWarnLog(String logContent) {
		CommonUtil.emitWarnLog(eventEngine, logContent);
	}
	
	@Override
	public void emitDebugLog(String logContent) {
		CommonUtil.emitDebugLog(eventEngine, logContent);
	}
	
	class QueryTimerTask extends TimerTask{

	    @Override
	    public void run() {
	    	try {
		    	if(isConnected()) {
			        queryAccount();
		    	}
			    Thread.sleep(1250);
			    if(isConnected()) {
				    queryPosition();
			    }
			    Thread.sleep(1250);
	    	}catch (Exception e) {
				log.error("{} 定时查询发生异常",gatewayLogInfo,e);
			}
	    }
	}
	
}
