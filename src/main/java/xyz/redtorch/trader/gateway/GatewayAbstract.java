package xyz.redtorch.trader.gateway;

import java.util.Date;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.RingBuffer;

import xyz.redtorch.trader.engine.event.EventConstant;
import xyz.redtorch.trader.engine.event.FastEvent;
import xyz.redtorch.trader.engine.event.FastEventEngine;
import xyz.redtorch.trader.entity.Account;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.Position;

/**
 * @author sun0x00@gmail.com
 */
public abstract class GatewayAbstract implements Gateway{
	
	private static Logger log = LoggerFactory.getLogger(GatewayAbstract.class);
	
	protected String gatewayID;
	protected String gatewayDisplayName;
	protected String gatewayLogInfo;

	protected GatewaySetting gatewaySetting;
	

	protected HashSet<String> subscribedSymbols = new HashSet<>();

	Timer timer = new Timer();
	
	public GatewayAbstract(GatewaySetting gatewaySetting){
		this.gatewaySetting = gatewaySetting;
		this.gatewayID = gatewaySetting.getGatewayID();
		this.gatewayDisplayName = gatewaySetting.getGatewayDisplayName();
		this.gatewayLogInfo = "接口ID-[" + gatewayID + "] 名称-[" + gatewayDisplayName+"] >>> ";
		log.info(gatewayLogInfo+"开始初始化");
		timer.schedule(new QueryTimerTask(), new Date(), 1000);

	}
	
	@Override
	public HashSet<String> getSubscribedSymbols() {
		return subscribedSymbols;
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
		
		RingBuffer<FastEvent> ringBuffer  = FastEventEngine.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setPosition(position);
			fastEvent.setEvent(EventConstant.EVENT_POSITION);
			fastEvent.setEventType(EventConstant.EVENT_POSITION);
			
		} finally {
			ringBuffer.publish(sequence);
		}
	}

	@Override
	public void emitAccount(Account account) {
		// 发送事件
		
		RingBuffer<FastEvent> ringBuffer  = FastEventEngine.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setAccount(account);
			fastEvent.setEvent(EventConstant.EVENT_ACCOUNT);
			fastEvent.setEventType(EventConstant.EVENT_ACCOUNT);
		} finally {
			ringBuffer.publish(sequence);
		}
		
	}

	@Override
	public void emitContract(Contract contract) {
		
		// 发送事件
		
		RingBuffer<FastEvent> ringBuffer  = FastEventEngine.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setContract(contract);
			fastEvent.setEvent(EventConstant.EVENT_CONTRACT);
			fastEvent.setEventType(EventConstant.EVENT_CONTRACT);
		} finally {
			ringBuffer.publish(sequence);
		}
				
	}

	@Override
	public void emitTick(String gatewayID, String symbol, String exchange, String rtSymbol, String tradingDay, String actionDay,
			String actionTime, DateTime dateTime, Integer status, Double lastPrice, Integer lastVolume, Integer volume,
			Double openInterest, Long preOpenInterest, Double preClosePrice, Double preSettlePrice, Double openPrice,
			Double highPrice, Double lowPrice, Double upperLimit, Double lowerLimit, Double bidPrice1, Double bidPrice2,
			Double bidPrice3, Double bidPrice4, Double bidPrice5, Double bidPrice6, Double bidPrice7, Double bidPrice8,
			Double bidPrice9, Double bidPrice10, Double askPrice1, Double askPrice2, Double askPrice3, Double askPrice4,
			Double askPrice5, Double askPrice6, Double askPrice7, Double askPrice8, Double askPrice9, Double askPrice10,
			Integer bidVolume1, Integer bidVolume2, Integer bidVolume3, Integer bidVolume4, Integer bidVolume5,
			Integer bidVolume6, Integer bidVolume7, Integer bidVolume8, Integer bidVolume9, Integer bidVolume10,
			Integer askVolume1, Integer askVolume2, Integer askVolume3, Integer askVolume4, Integer askVolume5,
			Integer askVolume6, Integer askVolume7, Integer askVolume8, Integer askVolume9, Integer askVolume10) {
		
		
		RingBuffer<FastEvent> ringBuffer  = FastEventEngine.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTick().setAllValue(gatewayID, symbol, exchange, rtSymbol, tradingDay, actionDay, actionTime, dateTime, status, lastPrice, lastVolume, volume, openInterest, preOpenInterest, preClosePrice, preSettlePrice, openPrice, highPrice, lowPrice, upperLimit, lowerLimit, bidPrice1, bidPrice2, bidPrice3, bidPrice4, bidPrice5, bidPrice6, bidPrice7, bidPrice8, bidPrice9, bidPrice10, askPrice1, askPrice2, askPrice3, askPrice4, askPrice5, askPrice6, askPrice7, askPrice8, askPrice9, askPrice10, bidVolume1, bidVolume2, bidVolume3, bidVolume4, bidVolume5, bidVolume6, bidVolume7, bidVolume8, bidVolume9, bidVolume10, askVolume1, askVolume2, askVolume3, askVolume4, askVolume5, askVolume6, askVolume7, askVolume8, askVolume9, askVolume10);
			fastEvent.setEvent(EventConstant.EVENT_TICK+gatewayID+rtSymbol);
			fastEvent.setEventType(EventConstant.EVENT_TICK);
		} finally {
			ringBuffer.publish(sequence);
		}
		
		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTick().setAllValue(gatewayID, symbol, exchange, rtSymbol, tradingDay, actionDay, actionTime, dateTime, status, lastPrice, lastVolume, volume, openInterest, preOpenInterest, preClosePrice, preSettlePrice, openPrice, highPrice, lowPrice, upperLimit, lowerLimit, bidPrice1, bidPrice2, bidPrice3, bidPrice4, bidPrice5, bidPrice6, bidPrice7, bidPrice8, bidPrice9, bidPrice10, askPrice1, askPrice2, askPrice3, askPrice4, askPrice5, askPrice6, askPrice7, askPrice8, askPrice9, askPrice10, bidVolume1, bidVolume2, bidVolume3, bidVolume4, bidVolume5, bidVolume6, bidVolume7, bidVolume8, bidVolume9, bidVolume10, askVolume1, askVolume2, askVolume3, askVolume4, askVolume5, askVolume6, askVolume7, askVolume8, askVolume9, askVolume10);
			fastEvent.setEvent(EventConstant.EVENT_TICK+rtSymbol);
			fastEvent.setEventType(EventConstant.EVENT_TICK);
			
		} finally {
			ringBuffer.publish(sequence);
		}
		
		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTick().setAllValue(gatewayID, symbol, exchange, rtSymbol, tradingDay, actionDay, actionTime, dateTime, status, lastPrice, lastVolume, volume, openInterest, preOpenInterest, preClosePrice, preSettlePrice, openPrice, highPrice, lowPrice, upperLimit, lowerLimit, bidPrice1, bidPrice2, bidPrice3, bidPrice4, bidPrice5, bidPrice6, bidPrice7, bidPrice8, bidPrice9, bidPrice10, askPrice1, askPrice2, askPrice3, askPrice4, askPrice5, askPrice6, askPrice7, askPrice8, askPrice9, askPrice10, bidVolume1, bidVolume2, bidVolume3, bidVolume4, bidVolume5, bidVolume6, bidVolume7, bidVolume8, bidVolume9, bidVolume10, askVolume1, askVolume2, askVolume3, askVolume4, askVolume5, askVolume6, askVolume7, askVolume8, askVolume9, askVolume10);
			fastEvent.setEvent(EventConstant.EVENT_TICK);
			fastEvent.setEventType(EventConstant.EVENT_TICK);
			
		} finally {
			ringBuffer.publish(sequence);
		}
	}

	@Override
	public void emitTrade(String gatewayID, String symbol, String exchange, String rtSymbol, String tradeID, String rtTradeID,
			String orderID, String rtOrderID, String direction, String offset, double price, int volume,
			String tradingDay, String tradeDate, String tradeTime, DateTime dateTime) {
		
		// 发送特定合约成交事件
		RingBuffer<FastEvent> ringBuffer  = FastEventEngine.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTrade().setAllValue(gatewayID, symbol, exchange, rtSymbol, tradeID, rtTradeID, orderID, rtOrderID, direction, offset, price, volume, tradingDay, tradeDate, tradeTime, dateTime);
			fastEvent.setEvent(EventConstant.EVENT_TRADE + rtOrderID);
			fastEvent.setEventType(EventConstant.EVENT_TRADE);
			
		} finally {
			ringBuffer.publish(sequence);
		}
		
		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTrade().setAllValue(gatewayID, symbol, exchange, rtSymbol, tradeID, rtTradeID, orderID, rtOrderID, direction, offset, price, volume, tradingDay, tradeDate, tradeTime, dateTime);
			fastEvent.setEvent(EventConstant.EVENT_TRADE);
			fastEvent.setEventType(EventConstant.EVENT_TRADE);
			
		} finally {
			ringBuffer.publish(sequence);
		}
		
	}

	@Override
	public void emitOrder(String gatewayID, String symbol, String exchange, String rtSymbol, String orderID, String rtOrderID,
			String direction, String offset, double price, int totalVolume, int tradedVolume, String status,
			String tradingDay, String orderDate, String orderTime, String cancelTime, String activeTime,
			String updateTime, int frontID, int sessionID) {
		
		// 发送带委托ID的事件
		RingBuffer<FastEvent> ringBuffer  = FastEventEngine.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getOrder().setAllValue(gatewayID, symbol, exchange, rtSymbol, orderID, rtOrderID, direction, offset, price, totalVolume, tradedVolume, status, tradingDay, orderDate, orderTime, cancelTime, activeTime, updateTime, frontID, sessionID);
			fastEvent.setEvent(EventConstant.EVENT_ORDER + rtOrderID);
			fastEvent.setEventType(EventConstant.EVENT_ORDER);
			
		} finally {
			ringBuffer.publish(sequence);
		}
		
		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getOrder().setAllValue(gatewayID, symbol, exchange, rtSymbol, orderID, rtOrderID, direction, offset, price, totalVolume, tradedVolume, status, tradingDay, orderDate, orderTime, cancelTime, activeTime, updateTime, frontID, sessionID);
			fastEvent.setEvent(EventConstant.EVENT_ORDER);
			fastEvent.setEventType(EventConstant.EVENT_ORDER);
			
		} finally {
			ringBuffer.publish(sequence);
		}

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
				log.error(gatewayLogInfo+"定时查询发生异常",e);
			}
	    }
	}
	
}
