package xyz.redtorch.trader.engine.event;

import com.lmax.disruptor.EventFactory;

public class FastEventFactory implements EventFactory<FastEvent>{

	@Override
	public FastEvent newInstance() {
		return new FastEvent();
	}

}
