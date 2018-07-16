package xyz.redtorch.trader.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import xyz.redtorch.trader.base.RtConstant;

/**
 * @author sun0x00@gmail.com
 */
public class LogData implements Serializable{
	private static final long serialVersionUID = 7122255887442856581L;
	
	private long logTimestamp = System.currentTimeMillis(); //日志创建时间
    private String logContent; //日志信息
    private String logLevel = RtConstant.LOG_INFO;  //日志级别

	public String getFormatDate() {
		SimpleDateFormat sdf = new SimpleDateFormat(RtConstant.DT_FORMAT_WITH_MS);
		return sdf.format(new Date(logTimestamp));
	}
	public long getLogTimestamp() {
		return logTimestamp;
	}
	public void setLogTimestamp(long logTimestamp) {
		this.logTimestamp = logTimestamp;
	}
	public String getLogContent() {
		return logContent;
	}
	public void setLogContent(String logContent) {
		this.logContent = logContent;
	}
	public String getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
	@Override
	public String toString() {
		return "LogData [logTimestamp=" + logTimestamp + ", logContent=" + logContent + ", logLevel=" + logLevel + "]";
	}
    
}
