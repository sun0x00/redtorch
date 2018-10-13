package xyz.redtorch.core.service.extend.event;

import com.lmax.disruptor.RingBuffer;

public class FastEventProducerAbstract {
	protected final RingBuffer<FastEvent> ringBuffer;

	public FastEventProducerAbstract(RingBuffer<FastEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

}
