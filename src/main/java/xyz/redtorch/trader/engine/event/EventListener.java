package xyz.redtorch.trader.engine.event;
/**
 * @author sun0x00@gmail.com
 */
public interface EventListener extends Runnable{

	void onEvent(EventData eventData);
	
	void stop();
}
