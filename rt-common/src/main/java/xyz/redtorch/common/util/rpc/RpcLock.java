package xyz.redtorch.common.util.rpc;

public class RpcLock {
    private final Object lock = new Object();
    private final String transactionId;
    private final long waitMillisecond;
    private final long startTimestamp = System.currentTimeMillis();
    public RpcLock(String transactionId, long waitMillisecond){
        this.transactionId = transactionId;
        this.waitMillisecond = waitMillisecond;
    }
    public String getTransactionId(){
        return transactionId;
    }
    public Object getLock(){
        return lock;
    }
    public long getWaitMillisecond(){
        return waitMillisecond;
    }
    public long getStartTimestamp(){
        return startTimestamp;
    }
}
