package xyz.redtorch.core.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.service.extend.event.EventConstant;

public class FastEventLogAppender extends AppenderBase<ILoggingEvent> {
	@Override
	protected void append(ILoggingEvent le) {

		if (le == null || le.getMessage() == null) {
			return;
		}

		String rtLevel = RtConstant.LOG_DEBUG;
		Level leLevel = le.getLevel();

		switch (leLevel.levelInt) {
		case Level.TRACE_INT:
			rtLevel = RtConstant.LOG_DEBUG;
			break;
		case Level.DEBUG_INT:
			rtLevel = RtConstant.LOG_DEBUG;
			break;
		case Level.INFO_INT:
			rtLevel = RtConstant.LOG_INFO;
			break;
		case Level.WARN_INT:
			rtLevel = RtConstant.LOG_WARN;
			break;
		case Level.ERROR_INT:
			rtLevel = RtConstant.LOG_ERROR;
			break;
		default:
			break;
		}
		
		// if (RtConstant.LOG_ERROR.contains(rtLevel)) { // 可以考虑之转发高级别日志
			String event = EventConstant.EVENT_LOG;
			String loggerName = le.getLoggerName();
			String threadName = le.getThreadName();
			String className = loggerName.split("\\.")[loggerName.split("\\.").length - 1];
			String content = le.getFormattedMessage();
			CoreUtil.emitLogBase(le.getTimeStamp(), event, rtLevel, threadName, className, content);
		// }

	}

}
