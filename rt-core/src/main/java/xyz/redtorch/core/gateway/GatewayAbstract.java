package xyz.redtorch.core.gateway;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.RingBuffer;

import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.service.FastEventEngineService;
import xyz.redtorch.core.service.extend.event.EventConstant;
import xyz.redtorch.core.service.extend.event.FastEvent;

/**
 * @author sun0x00@gmail.com
 */
public abstract class GatewayAbstract implements Gateway {

	private static Logger log = LoggerFactory.getLogger(GatewayAbstract.class);

	protected String gatewayID;
	protected String gatewayDisplayName;
	protected String gatewayLogInfo;

	protected GatewaySetting gatewaySetting;

	protected FastEventEngineService fastEventEngineService;

	public GatewayAbstract(FastEventEngineService fastEventEngineService, GatewaySetting gatewaySetting) {
		this.fastEventEngineService = fastEventEngineService;
		this.gatewaySetting = gatewaySetting;
		this.gatewayID = gatewaySetting.getGatewayID();
		this.gatewayDisplayName = gatewaySetting.getGatewayDisplayName();
		this.gatewayLogInfo = "网关ID-[" + gatewayID + "] 名称-[" + gatewayDisplayName + "] >>> ";
		log.info(gatewayLogInfo + "开始初始化");

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
	public void emitPosition(Position position) {

		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
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

		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
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

		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
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
	public void emitTick(String gatewayID, String gatewayDisplayName, String symbol, String exchange, String rtSymbol,
			String tickID, String tradingDay, String actionDay, String actionTime, DateTime dateTime, Integer status,
			double lastPrice, Integer lastVolume, Integer volume, double openInterest, long preOpenInterest,
			double preClosePrice, double preSettlePrice, double openPrice, double highPrice, double lowPrice,
			double upperLimit, double lowerLimit, double bidPrice1, double bidPrice2, double bidPrice3,
			double bidPrice4, double bidPrice5, double bidPrice6, double bidPrice7, double bidPrice8, double bidPrice9,
			double bidPrice10, double askPrice1, double askPrice2, double askPrice3, double askPrice4, double askPrice5,
			double askPrice6, double askPrice7, double askPrice8, double askPrice9, double askPrice10, int bidVolume1,
			int bidVolume2, int bidVolume3, int bidVolume4, int bidVolume5, int bidVolume6, int bidVolume7,
			int bidVolume8, int bidVolume9, int bidVolume10, int askVolume1, int askVolume2, int askVolume3,
			int askVolume4, int askVolume5, int askVolume6, int askVolume7, int askVolume8, int askVolume9,
			int askVolume10) {

		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTick().setAllValue(gatewayID, gatewayDisplayName, symbol, exchange, rtSymbol, tickID,
					tradingDay, actionDay, actionTime, dateTime, status, lastPrice, lastVolume, volume, openInterest,
					preOpenInterest, preClosePrice, preSettlePrice, openPrice, highPrice, lowPrice, upperLimit,
					lowerLimit, bidPrice1, bidPrice2, bidPrice3, bidPrice4, bidPrice5, bidPrice6, bidPrice7, bidPrice8,
					bidPrice9, bidPrice10, askPrice1, askPrice2, askPrice3, askPrice4, askPrice5, askPrice6, askPrice7,
					askPrice8, askPrice9, askPrice10, bidVolume1, bidVolume2, bidVolume3, bidVolume4, bidVolume5,
					bidVolume6, bidVolume7, bidVolume8, bidVolume9, bidVolume10, askVolume1, askVolume2, askVolume3,
					askVolume4, askVolume5, askVolume6, askVolume7, askVolume8, askVolume9, askVolume10);
			fastEvent.setEvent(EventConstant.EVENT_TICK);
			fastEvent.setEventType(EventConstant.EVENT_TICK);

		} finally {
			ringBuffer.publish(sequence);
		}

		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTick().setAllValue(gatewayID, gatewayDisplayName, symbol, exchange, rtSymbol, tickID,
					tradingDay, actionDay, actionTime, dateTime, status, lastPrice, lastVolume, volume, openInterest,
					preOpenInterest, preClosePrice, preSettlePrice, openPrice, highPrice, lowPrice, upperLimit,
					lowerLimit, bidPrice1, bidPrice2, bidPrice3, bidPrice4, bidPrice5, bidPrice6, bidPrice7, bidPrice8,
					bidPrice9, bidPrice10, askPrice1, askPrice2, askPrice3, askPrice4, askPrice5, askPrice6, askPrice7,
					askPrice8, askPrice9, askPrice10, bidVolume1, bidVolume2, bidVolume3, bidVolume4, bidVolume5,
					bidVolume6, bidVolume7, bidVolume8, bidVolume9, bidVolume10, askVolume1, askVolume2, askVolume3,
					askVolume4, askVolume5, askVolume6, askVolume7, askVolume8, askVolume9, askVolume10);
			fastEvent.setEvent(EventConstant.EVENT_TICK + gatewayID + rtSymbol);
			fastEvent.setEventType(EventConstant.EVENT_TICK);
		} finally {
			ringBuffer.publish(sequence);
		}

		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTick().setAllValue(gatewayID, gatewayDisplayName, symbol, exchange, rtSymbol, tickID,
					tradingDay, actionDay, actionTime, dateTime, status, lastPrice, lastVolume, volume, openInterest,
					preOpenInterest, preClosePrice, preSettlePrice, openPrice, highPrice, lowPrice, upperLimit,
					lowerLimit, bidPrice1, bidPrice2, bidPrice3, bidPrice4, bidPrice5, bidPrice6, bidPrice7, bidPrice8,
					bidPrice9, bidPrice10, askPrice1, askPrice2, askPrice3, askPrice4, askPrice5, askPrice6, askPrice7,
					askPrice8, askPrice9, askPrice10, bidVolume1, bidVolume2, bidVolume3, bidVolume4, bidVolume5,
					bidVolume6, bidVolume7, bidVolume8, bidVolume9, bidVolume10, askVolume1, askVolume2, askVolume3,
					askVolume4, askVolume5, askVolume6, askVolume7, askVolume8, askVolume9, askVolume10);
			fastEvent.setEvent(EventConstant.EVENT_TICK + rtSymbol);
			fastEvent.setEventType(EventConstant.EVENT_TICK);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitTick(Tick tick) {

		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setTick(tick);
			fastEvent.setEvent(EventConstant.EVENT_TICK);
			fastEvent.setEventType(EventConstant.EVENT_TICK);

		} finally {
			ringBuffer.publish(sequence);
		}

		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setTick(tick);
			fastEvent.setEvent(EventConstant.EVENT_TICK + gatewayID + tick.getRtSymbol());
			fastEvent.setEventType(EventConstant.EVENT_TICK);
		} finally {
			ringBuffer.publish(sequence);
		}

		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setTick(tick);
			fastEvent.setEvent(EventConstant.EVENT_TICK + tick.getRtSymbol());
			fastEvent.setEventType(EventConstant.EVENT_TICK);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitTrade(String gatewayID, String gatewayDisplayName, String accountID, String rtAccountID,
			String symbol, String exchange, String rtSymbol, String tradeID, String rtTradeID, String orderID,
			String rtOrderID, String originalOrderID, String direction, String offset, double price, int volume,
			String tradingDay, String tradeDate, String tradeTime, DateTime dateTime) {

		// 发送特定合约成交事件
		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTrade().setAllValue(gatewayID, gatewayDisplayName, accountID, rtAccountID, symbol, exchange,
					rtSymbol, tradeID, rtTradeID, orderID, rtOrderID, originalOrderID, direction, offset, price, volume,
					tradingDay, tradeDate, tradeTime, dateTime);
			fastEvent.setEvent(EventConstant.EVENT_TRADE);
			fastEvent.setEventType(EventConstant.EVENT_TRADE);

		} finally {
			ringBuffer.publish(sequence);
		}

		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getTrade().setAllValue(gatewayID, gatewayDisplayName, accountID, rtAccountID, symbol, exchange,
					rtSymbol, tradeID, rtTradeID, orderID, rtOrderID, originalOrderID, direction, offset, price, volume,
					tradingDay, tradeDate, tradeTime, dateTime);
			fastEvent.setEvent(EventConstant.EVENT_TRADE + originalOrderID);
			fastEvent.setEventType(EventConstant.EVENT_TRADE);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitTrade(Trade trade) {

		// 发送特定合约成交事件
		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setTrade(trade);
			fastEvent.setEvent(EventConstant.EVENT_TRADE);
			fastEvent.setEventType(EventConstant.EVENT_TRADE);

		} finally {
			ringBuffer.publish(sequence);
		}

		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setTrade(trade);
			fastEvent.setEvent(EventConstant.EVENT_TRADE + trade.getOriginalOrderID());
			fastEvent.setEventType(EventConstant.EVENT_TRADE);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitOrder(String originalOrderID, String gatewayID, String gatewayDisplayName, String accountID,
			String rtAccountID, String symbol, String exchange, String rtSymbol, String orderID, String rtOrderID,
			String direction, String offset, double price, int totalVolume, int tradedVolume, String status,
			String tradingDay, String orderDate, String orderTime, String cancelTime, String activeTime,
			String updateTime, int frontID, int sessionID) {

		// 发送带委托ID的事件
		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence

		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getOrder().setAllValue(originalOrderID, gatewayID, gatewayDisplayName, accountID, rtAccountID,
					symbol, exchange, rtSymbol, orderID, rtOrderID, direction, offset, price, totalVolume, tradedVolume,
					status, tradingDay, orderDate, orderTime, cancelTime, activeTime, updateTime, frontID, sessionID);
			fastEvent.setEvent(EventConstant.EVENT_ORDER);
			fastEvent.setEventType(EventConstant.EVENT_ORDER);

		} finally {
			ringBuffer.publish(sequence);
		}

		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.getOrder().setAllValue(originalOrderID, gatewayID, gatewayDisplayName, accountID, rtAccountID,
					symbol, exchange, rtSymbol, orderID, rtOrderID, direction, offset, price, totalVolume, tradedVolume,
					status, tradingDay, orderDate, orderTime, cancelTime, activeTime, updateTime, frontID, sessionID);
			fastEvent.setEvent(EventConstant.EVENT_ORDER + rtOrderID);
			fastEvent.setEventType(EventConstant.EVENT_ORDER);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitOrder(Order order) {

		// 发送带委托ID的事件
		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence

		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setOrder(order);
			fastEvent.setEvent(EventConstant.EVENT_ORDER);
			fastEvent.setEventType(EventConstant.EVENT_ORDER);

		} finally {
			ringBuffer.publish(sequence);
		}

		sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setOrder(order);
			fastEvent.setEvent(EventConstant.EVENT_ORDER + order.getRtOrderID());
			fastEvent.setEventType(EventConstant.EVENT_ORDER);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

}
