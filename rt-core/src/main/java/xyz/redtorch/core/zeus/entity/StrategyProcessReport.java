package xyz.redtorch.core.zeus.entity;

import java.io.Serializable;
import java.util.Set;

import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

public class StrategyProcessReport implements Serializable {

	private static final long serialVersionUID = -5530681231590118370L;

	private boolean initStatus;
	private boolean trading;
	private StrategySetting strategySetting;
	private long reportTimestamp;
	private Set<SubscribeReq> subscribeReqSet;

	public boolean isInitStatus() {
		return initStatus;
	}

	public void setInitStatus(boolean initStatus) {
		this.initStatus = initStatus;
	}

	public boolean isTrading() {
		return trading;
	}

	public void setTrading(boolean trading) {
		this.trading = trading;
	}

	public StrategySetting getStrategySetting() {
		return strategySetting;
	}

	public void setStrategySetting(StrategySetting strategySetting) {
		this.strategySetting = strategySetting;
	}

	public long getReportTimestamp() {
		return reportTimestamp;
	}

	public void setReportTimestamp(long reportTimestamp) {
		this.reportTimestamp = reportTimestamp;
	}

	public Set<SubscribeReq> getSubscribeReqSet() {
		return subscribeReqSet;
	}

	public void setSubscribeReqSet(Set<SubscribeReq> subscribeReqSet) {
		this.subscribeReqSet = subscribeReqSet;
	}
}
