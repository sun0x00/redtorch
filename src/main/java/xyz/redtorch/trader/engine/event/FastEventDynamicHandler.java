package xyz.redtorch.trader.engine.event;

import java.util.List;
import java.util.Set;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

public interface FastEventDynamicHandler extends EventHandler<FastEvent>, LifecycleAware{
	void awaitShutdown()throws InterruptedException;
	
	public List<String> getSubscribedEventList();
	
	public Set<String> getSubscribedEventSet();
	
	public void subscribeEvent(String event);
	
	public void unsubscribeEvent(String event);
}
