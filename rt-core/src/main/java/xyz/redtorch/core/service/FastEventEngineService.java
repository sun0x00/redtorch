package xyz.redtorch.core.service;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;

import xyz.redtorch.core.service.extend.event.FastEvent;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandler;

public interface FastEventEngineService {

	BatchEventProcessor<FastEvent> addHandler(FastEventDynamicHandler handler);

	void removeHandler(FastEventDynamicHandler handler);

	RingBuffer<FastEvent> getRingBuffer();

}