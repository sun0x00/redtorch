package xyz.redtorch.core.service.extend.event;

import java.util.List;
import java.util.Set;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

public interface FastEventDynamicHandler extends EventHandler<FastEvent>, LifecycleAware {
	void awaitShutdown() throws InterruptedException;

	List<String> getSubscribedEventList();

	Set<String> getSubscribedEventSet();

	void subscribeEvent(String event);

	void unsubscribeEvent(String event);
}
