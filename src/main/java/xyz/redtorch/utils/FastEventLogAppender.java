package xyz.redtorch.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.engine.event.EventConstant;



public class FastEventLogAppender extends AppenderBase<ILoggingEvent> {
	@Override
	protected void append(ILoggingEvent le) {

		if(le == null || le.getMessage() == null) {
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

		// 出于前端性能考虑，只转发错误级别日志（强烈建议不要修改或者可以关闭日志转发）
		if(RtConstant.LOG_ERROR.contains(rtLevel)) {
			String event = EventConstant.EVENT_LOG;
			StringBuffer sb = new StringBuffer();
			String loggerName = le.getLoggerName();
			sb.append(le.getThreadName()).append("\t").append(loggerName.split("\\.")[loggerName.split("\\.").length-1])
				.append("\t").append(le.getFormattedMessage()); 
			CommonUtil.emitLogBase(le.getTimeStamp(), event, rtLevel, sb.toString());
		}
		
	}

}
