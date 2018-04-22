package xyz.redtorch.trader.engine.event;
/**
 * @author sun0x00@gmail.com
 */
public class EventData {
	
	private String event;
	private Object eventObj;
	private String eventType;
	
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public Object getEventObj() {
		return eventObj;
	}
	public void setEventObj(Object eventObj) {
		this.eventObj = eventObj;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
	
}
