package xyz.redtorch.core.event;

import xyz.redtorch.core.service.extend.event.FastEvent;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandlerAbstract;

public class FastEventDynamicHandlerTest extends FastEventDynamicHandlerAbstract
{
	String id = "";
	FastEventDynamicHandlerTest(String id){
		this.id = id+"=====";
	}
    @Override
	public void onEvent(final FastEvent fastEvent, final long sequence, final boolean endOfBatch) throws Exception {
		System.out.println(this.id+fastEvent.getEvent());
	}
    
	@Override
	public void onStart() {

	}

	@Override
	public void onShutdown() {
		shutdownLatch.countDown();
	}
}