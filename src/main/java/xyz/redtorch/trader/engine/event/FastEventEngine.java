package xyz.redtorch.trader.engine.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import xyz.redtorch.trader.base.BaseConfig;

public class FastEventEngine {
	
	private static Logger log = LoggerFactory.getLogger(FastEventEngine.class); 
	
	private static ExecutorService executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);

	// BlockingWaitStrategy 低效
	// SleepingWaitStrategy 对生产者影响较小
	// YieldingWaitStrategy 高性能
	
    // Build a disruptor and start it.
    private static Disruptor<FastEvent> disruptor ;
    static{
    	if("slow".equals(BaseConfig.rtConfig.getString("engine.event.FastEventEngine.WaitStrategy"))) {
        	disruptor = new Disruptor<FastEvent>(
            		new FastEventFactory(), 65536, DaemonThreadFactory.INSTANCE,ProducerType.MULTI,new BlockingWaitStrategy());
    	}else {
        	disruptor = new Disruptor<FastEvent>(
            		new FastEventFactory(), 65536, DaemonThreadFactory.INSTANCE,ProducerType.MULTI,new YieldingWaitStrategy());
    	}
    }
    private static RingBuffer<FastEvent> ringBuffer = disruptor.start();
    private final static Map<EventHandler<FastEvent>, BatchEventProcessor<FastEvent>> handlerProcessorMap = new ConcurrentHashMap<>();
    

    
    public static synchronized BatchEventProcessor<FastEvent> addHandler(FastEventDynamicHandler handler) {
    	BatchEventProcessor<FastEvent> processor;
    	processor = new BatchEventProcessor<FastEvent>(ringBuffer, ringBuffer.newBarrier(), handler);
    	ringBuffer.addGatingSequences(processor.getSequence());
    	executor.execute(processor);
        handlerProcessorMap.put(handler, processor);
    	return processor;
    }
    
    public static void removeHandler(FastEventDynamicHandler handler) {
    	if(handlerProcessorMap.containsKey(handler)) {
        	BatchEventProcessor<FastEvent> processor = handlerProcessorMap.get(handler);
            // Remove a processor.
            // Stop the processor
        	processor.halt();
            // Wait for shutdown the complete
        	try {
    			handler.awaitShutdown();
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    			log.error("关闭时发生异常",e);
    		}
            // Remove the gating sequence from the ring buffer
            ringBuffer.removeGatingSequence(processor.getSequence());
            handlerProcessorMap.remove(handler);
    	}else {
    		log.warn("未找到Processor,无法移除");
    	}

    }
    
    public static RingBuffer<FastEvent> getRingBuffer() {
    	return ringBuffer;
    }
    
}
