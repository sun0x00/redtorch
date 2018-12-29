package xyz.redtorch.core.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import xyz.redtorch.core.base.RtConstant;

/**
 * @author sun0x00@gmail.com
 */
public class LogData implements Serializable {
	private static final long serialVersionUID = 7122255887442856581L;

	private long timestamp = System.currentTimeMillis(); // 日志创建时间
	private String level = RtConstant.LOG_INFO; // 日志级别
	private String threadName; //　线程信息
	private String className; // 类名
	private String content; // 日志信息

	public String getFormatDateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat(RtConstant.DT_FORMAT_WITH_MS);
		return sdf.format(new Date(timestamp));
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "LogData [timestamp=" + timestamp + ", level=" + level + ", threadName=" + threadName + ", className="
				+ className + ", content=" + content + "]";
	}

}
