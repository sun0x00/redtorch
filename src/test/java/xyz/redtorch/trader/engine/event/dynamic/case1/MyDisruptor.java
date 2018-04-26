package xyz.redtorch.trader.engine.event.dynamic.case1;

import com.lmax.disruptor.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MyDisruptor {

    private final RingBuffer<Holder> ringBuffer;
    private final SequenceGroup sequenceGroup = new SequenceGroup();
    private final Map<EventHandler<Holder>, BatchEventProcessor<Holder>> handlersWithProcessors = new ConcurrentHashMap<>();

    public MyDisruptor() {
        ringBuffer = RingBuffer.createMultiProducer(Holder.EVENT_FACTORY, 4096);
    	ringBuffer.addGatingSequences(sequenceGroup);
    }

    public void addHandler(EventHandler<Holder> handler) {
        SequenceBarrier barrier = ringBuffer.newBarrier();
        BatchEventProcessor<Holder> processor = new BatchEventProcessor<>(ringBuffer, barrier, handler);
        processor.getSequence().set(barrier.getCursor());
        sequenceGroup.add(processor.getSequence());
        processor.getSequence().set(ringBuffer.getCursor());
        handlersWithProcessors.put(handler, processor);
        processor.run();
    }

    public void removeHandler(EventHandler<Holder> handler) {
        BatchEventProcessor<Holder> processor = handlersWithProcessors.remove(handler);
        processor.halt();
        sequenceGroup.remove(processor.getSequence());
    }

    public void publishValue(String value) {
        long nextSequence = ringBuffer.next();
        Holder holder = ringBuffer.get(nextSequence);
        holder.setValue(value);
        ringBuffer.publish(nextSequence);
    }

    public boolean hasHandlers() {
        return handlersWithProcessors.size() != 0;
    }
}
