package xyz.redtorch.node.master.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.HaSessionManger;
import xyz.redtorch.node.master.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class HaSessionMangerImpl implements HaSessionManger, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(HaSessionMangerImpl.class);

    @Autowired
    private UserService userService;
    @Value("${rt.master.sessionTimeoutSeconds}")
    private int sessionTimeoutSeconds;

    private final Map<String,UserPo> authTokenToUserPoMap = new HashMap<>();
    private Map<String,Long> authTokenToLastUpdateTimestampMap = new HashMap<>();
    private long approximatelyTimestamp = System.currentTimeMillis();
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(()->{
            while (!Thread.currentThread().isInterrupted()){
                lock.lock();
                try {
                    approximatelyTimestamp = System.currentTimeMillis();
                    Map<String,Long> newAuthTokenToLastUpdateTimestampMap = new HashMap<>();
                    for(String authToken: authTokenToLastUpdateTimestampMap.keySet()){
                        long lastUpdateTimestamp = authTokenToLastUpdateTimestampMap.get(authToken);
                        if(approximatelyTimestamp - lastUpdateTimestamp < (long)sessionTimeoutSeconds*1000){
                            newAuthTokenToLastUpdateTimestampMap.put(authToken, lastUpdateTimestamp);
                        }else{
                            authTokenToUserPoMap.remove(authToken);
                        }
                    }
                    authTokenToLastUpdateTimestampMap = newAuthTokenToLastUpdateTimestampMap;
                } finally {
                    lock.unlock();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("捕获到中断",e);
                    break;
                }
            }
        }).start();
    }

    @Override
    public UserPo getUserPoByAuthToken(String authToken) {
        return authTokenToUserPoMap.get(authToken);
    }
    @Override
    public UserPo userAuth(UserPo userPo) {

        UserPo authedUser = userService.auth(userPo);
        if(authedUser == null){
            return null;
        }

        String authToken = UUIDStringPoolUtils.getUUIDString();
        authedUser.setRandomAuthToken(authToken);
        lock.lock();
        try{
            authTokenToUserPoMap.put(authToken,authedUser);
            authTokenToLastUpdateTimestampMap.put(authToken, approximatelyTimestamp);
        }finally {
            lock.unlock();
        }
        return authedUser;
    }

    @Override
    public void freshAuthToken(String authToken) {
        lock.lock();
        try{
            if(authToken!=null&&authTokenToUserPoMap.containsKey(authToken)){
                authTokenToLastUpdateTimestampMap.put(authToken, approximatelyTimestamp);
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void removeAuthToken(String authToken) {
        lock.lock();
        try{
            authTokenToLastUpdateTimestampMap.remove(authToken);
            authTokenToUserPoMap.remove(authToken);
        }finally {
            lock.unlock();
        }
    }
}
