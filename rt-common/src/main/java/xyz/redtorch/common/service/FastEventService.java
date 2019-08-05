package xyz.redtorch.common.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.RingBuffer;

import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.NoticeField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;

public interface FastEventService {

	BatchEventProcessor<FastEvent> addHandler(FastEventDynamicHandler handler);

	void removeHandler(FastEventDynamicHandler handler);

	RingBuffer<FastEvent> getRingBuffer();

	void emitEvent(FastEventType fastEventType, String event, Object obj);

	void emitPosition(PositionField position);

	void emitAccount(AccountField account);

	void emitContract(ContractField contract);

	void emitTick(TickField tick);

	void emitTrade(TradeField trade);

	void emitOrder(OrderField order);

	void emitNotice(NoticeField notice);

	public static enum FastEventType {
		TICK(0), POSITION(1), ACCOUNT(2), CONTRACT(3), TRADE(4), ORDER(5), NOTICE(6), LOG(7);

		private int value;

		private FastEventType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public static class FastEvent {
		FastEventType fastEventType;
		private String event;

		private Object obj = null;

		public FastEventType getFastEventType() {
			return fastEventType;
		}

		public void setFastEventType(FastEventType fastEventType) {
			this.fastEventType = fastEventType;
		}

		public String getEvent() {
			return event;
		}

		public void setEvent(String event) {
			this.event = event;
		}

		public Object getObj() {
			return obj;
		}

		public void setObj(Object obj) {
			this.obj = obj;
		}
	}

	public static interface FastEventDynamicHandler extends EventHandler<FastEvent>, LifecycleAware {

		void awaitShutdown() throws InterruptedException;

		List<String> getSubscribedEventList();

		Set<String> getSubscribedEventSet();

		void subscribeEvent(String event);

		void unsubscribeEvent(String event);

		void subscribeFastEventType(FastEventType fastEventType);

		void unsubscribeFastEventType(FastEventType fastEventType);

	}

	public static abstract class FastEventDynamicHandlerAbstract implements FastEventDynamicHandler {
		protected final CountDownLatch shutdownLatch = new CountDownLatch(1);
		protected List<String> subscribedEventList = new ArrayList<>();
		protected Set<String> subscribedEventSet = new HashSet<>();
		protected Set<FastEventType> subscribedFastEventTypeSet = new HashSet<>();

		@Override
		public void onStart() {

		}

		@Override
		public void onShutdown() {
			shutdownLatch.countDown();
		}

		@Override
		public void awaitShutdown() throws InterruptedException {
			shutdownLatch.await();
		}

		@Override
		public List<String> getSubscribedEventList() {
			return subscribedEventList;
		}

		@Override
		public Set<String> getSubscribedEventSet() {
			return subscribedEventSet;
		}

		@Override
		public void subscribeEvent(String event) {
			subscribedEventList.add(event);
			subscribedEventSet.add(event);
		}

		@Override
		public void unsubscribeEvent(String event) {
			subscribedEventList.remove(event);
			if (!subscribedEventList.contains(event)) {
				subscribedEventSet.remove(event);
			}

		}

		@Override
		public void subscribeFastEventType(FastEventType fastEventType) {
			subscribedFastEventTypeSet.add(fastEventType);
		}

		@Override
		public void unsubscribeFastEventType(FastEventType fastEventType) {
			subscribedFastEventTypeSet.remove(fastEventType);
		}

	}

	public static class FastEventFactory implements EventFactory<FastEvent> {

		@Override
		public FastEvent newInstance() {
			return new FastEvent();
		}

	}

	public static class FastEventProducerAbstract {
		protected final RingBuffer<FastEvent> ringBuffer;

		public FastEventProducerAbstract(RingBuffer<FastEvent> ringBuffer) {
			this.ringBuffer = ringBuffer;
		}

	}

}
