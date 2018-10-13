package xyz.redtorch.core.zeus.entity;

import java.io.Serializable;

public class ZeusLog implements Serializable {

	private static final long serialVersionUID = 1L;

	private long actionTimestamp;
	private String level;
	private String content;

	public long getActionTimestamp() {
		return actionTimestamp;
	}

	public void setActionTimestamp(long actionTimestamp) {
		this.actionTimestamp = actionTimestamp;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "ZeusLog [actionTimestamp=" + actionTimestamp + ", level=" + level + ", content=" + content + "]";
	}

}
