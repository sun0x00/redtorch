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
	private String content; // 日志信息
	private String level = RtConstant.LOG_INFO; // 日志级别

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return "LogData [timestamp=" + timestamp + ", content=" + content + ", level=" + level + "]";
	}

}
