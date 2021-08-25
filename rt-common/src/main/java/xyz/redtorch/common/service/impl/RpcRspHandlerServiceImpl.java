package xyz.redtorch.common.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import xyz.redtorch.common.service.RpcRspHandlerService;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.common.util.rpc.RpcLock;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;


//@Service
public class RpcRspHandlerServiceImpl implements RpcRspHandlerService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RpcRspHandlerServiceImpl.class);

    private Map<String, RpcLock> waitRpcLockMap = new HashMap<>();
    private final ReentrantLock waitRpcLockMapLock = new ReentrantLock();

    private final Map<String, Object> rpcRspMap = new ConcurrentHashMap<>(100000);

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Value("${xyz.redtorch.common.service.impl.RpcRspHandlerServiceImpl.defaultRpcTimeoutSeconds}")
    private int defaultRpcTimeoutSeconds;

    @Override
    public void afterPropertiesSet() throws Exception {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                waitRpcLockMapLock.lock();
                try {
                    long now = System.currentTimeMillis();
                    Map<String, RpcLock> newWaitRpcLockMap = new HashMap<>();
                    for (String transactionId : waitRpcLockMap.keySet()) {
                        RpcLock rpcLock = waitRpcLockMap.get(transactionId);
                        if (now - rpcLock.getStartTimestamp() < rpcLock.getWaitMillisecond()) {
                            newWaitRpcLockMap.put(transactionId, rpcLock);
                        } else {
                            logger.error("业务ID:{},锁超时", transactionId);
                            notifyAndRemoveRpcLock(transactionId);
                        }
                    }
                    waitRpcLockMap = newWaitRpcLockMap;
                } finally {
                    waitRpcLockMapLock.unlock();
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    logger.error("捕获到中断", e);
                    break;
                }
            }
        });
    }

    @Override
    public Object getAndRemoveRpcRsp(String transactionId) {
        return rpcRspMap.remove(transactionId);
    }

    @Override
    public void onRpcRsp(String transactionId, Object object) {
        waitRpcLockMapLock.lock();
        try {
            if (waitRpcLockMap.containsKey(transactionId)) {
                rpcRspMap.put(transactionId, object);
                notifyAndRemoveRpcLock(transactionId);
            } else {
                logger.info("直接丢弃的回报,业务ID:{}", transactionId);
            }
        } finally {
            waitRpcLockMapLock.unlock();
        }

    }

    @Override
    public void registerRpcLock(RpcLock rpcLock) {
        waitRpcLockMapLock.lock();
        try {
            waitRpcLockMap.put(rpcLock.getTransactionId(), rpcLock);
        } finally {
            waitRpcLockMapLock.unlock();
        }
    }

    @Override
    public void unregisterLock(RpcLock rpcLock) {
        waitRpcLockMapLock.lock();
        try {
            waitRpcLockMap.remove(rpcLock.getTransactionId());
        } finally {
            waitRpcLockMapLock.unlock();
        }
    }

    @Override
    public void notifyAndRemoveRpcLock(String transactionId) {
        waitRpcLockMapLock.lock();
        try {
            RpcLock rpcLock = waitRpcLockMap.get(transactionId);
            if (rpcLock != null) {
                synchronized (rpcLock.getLock()) {
                    rpcLock.getLock().notify();
                }
                waitRpcLockMap.remove(transactionId);
            }
        } finally {
            waitRpcLockMapLock.unlock();
        }

    }

    public RpcLock getRpcLock(String transactionId, Integer timeoutSeconds) {
        Integer rpcTimeoutSeconds = timeoutSeconds;
        if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
            rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
        }
        if (StringUtils.isBlank(transactionId)) {
            transactionId = UUIDStringPoolUtils.getUUIDString();
        }

        RpcLock rpcLock = new RpcLock(transactionId, rpcTimeoutSeconds * 1000);
        registerRpcLock(rpcLock);
        return rpcLock;
    }

    public <T> T processCommonRsp(String transactionId, CommonRspField commonRsp, Object rsp, String logPartial) {
        if (commonRsp.getErrorId() == 0) {
            logger.info("{}完成,业务ID:{}", logPartial, transactionId);
            return (T) rsp;
        } else {
            logger.error("{}错误,业务ID:{},错误ID:{},错误信息{}", logPartial, transactionId, commonRsp.getErrorId(), commonRsp.getErrorMsg());
        }
        return null;
    }

    public boolean processBooleanRsp(String transactionId, CommonRspField commonRsp, String logPartial) {
        if (commonRsp.getErrorId() == 0) {
            logger.info("{}完成,业务ID:{}", logPartial, transactionId);
            return true;
        } else {
            logger.error("{}错误,业务ID:{},错误ID:{},错误信息{}", logPartial, transactionId, commonRsp.getErrorId(), commonRsp.getErrorMsg());
        }
        return false;
    }

    public <T> T processObjectRsp(String transactionId, RpcLock rpcLock, Class<T> cls, String logPartial) {

        synchronized (rpcLock.getLock()) {
            try {
                rpcLock.getLock().wait();
            } catch (InterruptedException e) {
                logger.error("{}错误,捕获到中断,业务ID:{}", logPartial, transactionId, e);
            }
        }

        Object obj = getAndRemoveRpcRsp(transactionId);
        if (obj == null) {
            logger.error("{}错误,业务ID:{},未获取到数据", logPartial, transactionId);
        } else {
            if (obj.getClass().isAssignableFrom(cls)) {
                return (T) obj;
            } else if (obj instanceof RpcExceptionRsp) {
                logger.error("{}错误,业务ID:{},远程错误回报:{}", logPartial, transactionId, ((RpcExceptionRsp) obj).getInfo());
            } else {
                logger.error("{}错误,业务ID:{}返回错误类型数据", logPartial, transactionId);
            }
        }
        return null;
    }

}
