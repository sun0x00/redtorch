package xyz.redtorch.core.service.extend.event;

import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.LogData;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;

public class FastEvent {

	private String eventType;
	private String event;

	// 提前new对象主要是为了性能考虑
	private Tick tick = new Tick();
	private Trade trade = new Trade();
	private Account account = new Account();
	private Bar bar = new Bar();
	private LogData logData = new LogData();
	private Contract contract = new Contract();
	private Position position = new Position();
	private Order order = new Order();
	private Object commonObj = null;

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public Tick getTick() {
		return tick;
	}

	public void setTick(Tick tick) {
		this.tick = tick;
	}

	public Trade getTrade() {
		return trade;
	}

	public void setTrade(Trade trade) {
		this.trade = trade;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Bar getBar() {
		return bar;
	}

	public void setBar(Bar bar) {
		this.bar = bar;
	}

	public LogData getLogData() {
		return logData;
	}

	public void setLogData(LogData logData) {
		this.logData = logData;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Object getCommonObj() {
		return commonObj;
	}

	public void setCommonObj(Object commonObj) {
		this.commonObj = commonObj;
	}
}
