package xyz.redtorch.trader.engine.event;

import xyz.redtorch.trader.entity.Account;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.LogData;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.Position;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;

public class FastEvent {

	private String eventType;
	private String event;
	
	private Tick tick = new Tick();
	private Trade trade = new Trade();
	private Account account = new Account();
	private Bar bar = new Bar();
	private LogData logData = new LogData();
	private Contract contract = new Contract();
	private Position position = new Position();
	private Order order = new Order();
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
	
}
