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
	 * @param content
	 */
	public static void emitLogBase(long timestmap, String event, String level, String threadName, String className, String content) {
		if (fastEventEngineService == null) {
			// 事件服务可能尚未启动
			// nop 丢弃
			return;
		}
		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setEvent(event);
			fastEvent.setEventType(EventConstant.EVENT_LOG);
			fastEvent.getLogData().setTimestamp(timestmap);
			fastEvent.getLogData().setLevel(level);
			fastEvent.getLogData().setThreadName(threadName);
			fastEvent.getLogData().setClassName(className);
			fastEvent.getLogData().setContent(content);
		} finally {
			ringBuffer.publish(sequence);
		}
	}

}
