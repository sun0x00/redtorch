package xyz.redtorch.common.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import xyz.redtorch.common.service.FastEventService;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.NoticeField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;

//@Service
public class FastEventServiceImpl implements FastEventService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(FastEventServiceImpl.class);

	private static ExecutorService executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);

	private final Map<EventHandler<FastEvent>, BatchEventProcessor<FastEvent>> handlerProcessorMap = new ConcurrentHashMap<>();

	private Disruptor<FastEvent> disruptor;

	private RingBuffer<FastEvent> ringBuffer;

	@Value("${rt.common.service.fast-event-wait-strategy}")
	private String waitStrategy;

	@Override
	public void afterPropertiesSet() throws Exception {
		if ("BusySpinWaitStrategy".equals(waitStrategy)) {
			disruptor = new Disruptor<FastEvent>(new FastEventFactory(), 65536, DaemonThreadFactory.INSTANCE,
					ProducerType.MULTI, new BusySpinWaitStrategy());
		} else if ("SleepingWaitStrategy".equals(waitStrategy)) {
			disruptor = new Disruptor<FastEvent>(new FastEventFactory(), 65536, DaemonThreadFactory.INSTANCE,
					ProducerType.MULTI, new SleepingWaitStrategy());
		} else if ("BlockingWaitStrategy".equals(waitStrategy)) {
			disruptor = new Disruptor<FastEvent>(new FastEventFactory(), 65536, DaemonThreadFactory.INSTANCE,
					ProducerType.MULTI, new BlockingWaitStrategy());
		} else {
			disruptor = new Disruptor<FastEvent>(new FastEventFactory(), 65536, DaemonThreadFactory.INSTANCE,
					ProducerType.MULTI, new YieldingWaitStrategy());
		}
		ringBuffer = disruptor.start();
	}

	@Override
	public synchronized BatchEventProcessor<FastEvent> addHandler(FastEventDynamicHandler handler) {
		BatchEventProcessor<FastEvent> processor;
		processor = new BatchEventProcessor<FastEvent>(ringBuffer, ringBuffer.newBarrier(), handler);
		ringBuffer.addGatingSequences(processor.getSequence());
		executor.execute(processor);
		handlerProcessorMap.put(handler, processor);
		return processor;
	}

	@Override
	public void removeHandler(FastEventDynamicHandler handler) {
		if (handlerProcessorMap.containsKey(handler)) {
			BatchEventProcessor<FastEvent> processor = handlerProcessorMap.get(handler);
			// Remove a processor.
			// Stop the processor
			processor.halt();
			// Wait for shutdown the complete
			try {
				handler.awaitShutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error("关闭时发生异常", e);
			}
			// Remove the gating sequence from the ring buffer
			ringBuffer.removeGatingSequence(processor.getSequence());
			handlerProcessorMap.remove(handler);
		} else {
			logger.warn("未找到Processor,无法移除");
		}

	}

	@Override
	public RingBuffer<FastEvent> getRingBuffer() {
		return ringBuffer;
	}

	@Override
	public void emitEvent(FastEventType fastEventType, String event, Object obj) {
		RingBuffer<FastEvent> ringBuffer = getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setFastEventType(fastEventType);
			fastEvent.setEvent(event);
			fastEvent.setObj(obj);

		} finally {
			ringBuffer.publish(sequence);
		}
	}

	@Override
	public void emitPosition(PositionField position) {

		RingBuffer<FastEvent> ringBuffer = getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setObj(position);
			fastEvent.setEvent(position.getPositionId());
			fastEvent.setFastEventType(FastEventType.POSITION);

		} finally {
			ringBuffer.publish(sequence);
		}
	}

	@Override
	public void emitAccount(AccountField account) {
		// 发送事件

		RingBuffer<FastEvent> ringBuffer = getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setObj(account);
			fastEvent.setEvent(account.getAccountId());
			fastEvent.setFastEventType(FastEventType.ACCOUNT);
		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitContract(ContractField contract) {

		// 发送事件

		RingBuffer<FastEvent> ringBuffer = getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setObj(contract);
			fastEvent.setEvent(contract.getContractId());
			fastEvent.setFastEventType(FastEventType.CONTRACT);
		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitTick(TickField tick) {

		RingBuffer<FastEvent> ringBuffer = getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setObj(tick);
			fastEvent.setEvent(tick.getUnifiedSymbol()+"@"+tick.getGatewayId());
			fastEvent.setFastEventType(FastEventType.TICK);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitTrade(TradeField trade) {

		// 发送特定合约成交事件
		RingBuffer<FastEvent> ringBuffer = getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setObj(trade);
			fastEvent.setEvent(trade.getOriginOrderId());
			fastEvent.setFastEventType(FastEventType.TRADE);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitOrder(OrderField order) {

		// 发送带定单ID的事件
		RingBuffer<FastEvent> ringBuffer = getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence

		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setObj(order);
			fastEvent.setEvent(order.getOriginOrderId());
			fastEvent.setFastEventType(FastEventType.ORDER);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

	@Override
	public void emitNotice(NoticeField notice) {
		RingBuffer<FastEvent> ringBuffer = getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence

		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setObj(notice);
			fastEvent.setEvent(FastEventType.NOTICE.getDeclaringClass().getName());
			fastEvent.setFastEventType(FastEventType.NOTICE);

		} finally {
			ringBuffer.publish(sequence);
		}

	}

}
