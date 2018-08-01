package xyz.redtorch.core.utils;

import com.lmax.disruptor.RingBuffer;

import xyz.redtorch.core.service.FastEventEngineService;
import xyz.redtorch.core.service.extend.event.EventConstant;
import xyz.redtorch.core.service.extend.event.FastEvent;

public class CoreUtil {
	private static FastEventEngineService fastEventEngineService;
	
	public static void setFastEventEngineService(FastEventEngineService fastEventEngineService) {
		CoreUtil.fastEventEngineService = fastEventEngineService;
	}
	
	/**
	 * 发出日志事件
	 * 
	 * @param eventEngine
	 * @param logContent
	 */
	public static void emitLogBase(long timestmap, String event, String logLevel,String logContent) {
		if(fastEventEngineService == null) {
			// 事件服务可能尚未启动
			// nop 丢弃
			return;
		}
		RingBuffer<FastEvent> ringBuffer  = fastEventEngineService.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setEvent(event);
			fastEvent.setEventType(EventConstant.EVENT_LOG);
			fastEvent.getLogData().setLogTimestamp(timestmap);
			fastEvent.getLogData().setLogLevel(logLevel);
			fastEvent.getLogData().setLogContent(logContent);
		} finally {
			ringBuffer.publish(sequence);
		}
	}
	
}
