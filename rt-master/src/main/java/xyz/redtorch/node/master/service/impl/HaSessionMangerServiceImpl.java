package xyz.redtorch.node.master.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.node.db.ZookeeperService;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.HaSessionMangerService;
import xyz.redtorch.node.master.service.UserService;

import java.util.*;

@Service
public class HaSessionMangerServiceImpl implements HaSessionMangerService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(HaSessionMangerServiceImpl.class);
    private static final String SESSION_COLLECTION = "session";
    private final Map<String, UserPo> authTokenToUserPoMap = new HashMap<>(5000);

    @Autowired
    private UserService userService;
    @Value("${rt.master.sessionTimeoutSeconds}")
    private int sessionTimeoutSeconds;
    @Autowired
    private ZookeeperService zookeeperService;

    // 记录近似时间戳,用于避免频繁获取系统时间
    private long approximatelyTimestamp = System.currentTimeMillis();

    @Override
    public void afterPropertiesSet() throws Exception {

        // 线程用于检查会话授权令牌是否过期
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {

                // 更新近似时间戳
                approximatelyTimestamp = System.currentTimeMillis();

                Set<String> deleteTokenSet = new HashSet<>();
                // 处理本地缓存,删除过期会话
                for (UserPo userPo : authTokenToUserPoMap.values()) {
                    long lastRefreshTimestamp = userPo.getTokenRefreshTimestamp();
                    // 如果超时,则加入删除集合
                    if (approximatelyTimestamp - lastRefreshTimestamp >= (long) sessionTimeoutSeconds * 1000) {
                        deleteTokenSet.add(userPo.getRandomAuthToken());
                    }
                }

                // 执行删除
                for (String token : deleteTokenSet) {
                    authTokenToUserPoMap.remove(token);
                }

                // 对于未过期的会话更新到Zookeeper
                for (UserPo userPo : authTokenToUserPoMap.values()) {
                    upsertSessionToZookeeper(userPo.getRandomAuthToken(), userPo);
                }

                // 删除Zookeeper中过期的会话
                List<UserPo> userPoList = getZookeeperSessionList();
                if (userPoList != null) {
                    for (UserPo userPo : userPoList) {
                        long lastUpdateTimestamp = userPo.getTokenRefreshTimestamp();
                        if (approximatelyTimestamp - lastUpdateTimestamp >= (long) sessionTimeoutSeconds * 1000) {
                            // 如果过期则删除
                            deleteZookeeperSessionByAuthToken(userPo.getRandomAuthToken());
                        } else {
                            if (!"admin".equals(userPo.getUsername())&&userService.getUserByUsername(userPo.getUsername()) == null) {
                                // 如果数据库中不存在则删除
                                deleteZookeeperSessionByAuthToken(userPo.getRandomAuthToken());
                            }
                        }
                    }
                }

                // 循环检测,间隔5秒
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.info("捕获到中断", e);
                    break;
                }
            }
        }).start();
    }


    @Override
    public UserPo getUserPoByAuthToken(String authToken) {
        // 先从本地查找会话
        UserPo userPo = authTokenToUserPoMap.get(authToken);
        if (userPo == null) {
            /// 如果本地不存在从Zookeeper查询
            userPo = getZookeeperSessionByAuthToken(authToken);
            if (userPo != null) {
                // 如果Zookeeper中存在会话则存入本地
                authTokenToUserPoMap.put(authToken, userPo);
            }
        }
        return userPo;
    }

    @Override
    public UserPo userAuth(UserPo userPo) {
        // 验证用户
        UserPo authedUserPo = userService.auth(userPo);
        if (authedUserPo == null) {
            // 验证失败返回空
            return null;
        }

        // 生成临时令牌
        String authToken = UUIDStringPoolUtils.getUUIDString();
        authedUserPo.setRandomAuthToken(authToken);
        // 更新会话刷新时间
        authedUserPo.setTokenRefreshTimestamp(approximatelyTimestamp);

        // 存储会话
        authTokenToUserPoMap.put(authToken, authedUserPo);
        upsertSessionToZookeeper(authToken, authedUserPo);

        // 返回已验证用户
        return authedUserPo;
    }

    @Override
    public void refreshAuthToken(String authToken) {
        if (authToken != null) {
            if (authTokenToUserPoMap.containsKey(authToken)) {
                // 如果本地存在会话,则更新刷新时间戳
                authTokenToUserPoMap.get(authToken).setTokenRefreshTimestamp(approximatelyTimestamp);
            } else {
                UserPo userPo = getZookeeperSessionByAuthToken(authToken);
                if (userPo != null) {
                    // 更新从Zookeeper中查到的会话的时间戳
                    userPo.setTokenRefreshTimestamp(approximatelyTimestamp);
                    authTokenToUserPoMap.put(authToken, userPo);
                }
            }
        }
    }

    @Override
    public void removeSessionByAuthToken(String authToken) {
        authTokenToUserPoMap.remove(authToken);
        deleteZookeeperSessionByAuthToken(authToken);
    }

    @Override
    public void removeSessionByUsername(String username) {
        List<String> deleteTokenList = new ArrayList<>();
        // 查找需要删除的令牌集合
        for (UserPo userPo : authTokenToUserPoMap.values()) {
            if (username.equals(userPo.getUsername())) {
                deleteTokenList.add(userPo.getRandomAuthToken());
            }
        }
        // 删除本地会话
        for (String token : deleteTokenList) {
            authTokenToUserPoMap.remove(token);
        }
        // 删除Zookeeper中的会话
        List<UserPo> userPoList = getZookeeperSessionList();
        if (userPoList != null) {
            for (UserPo userPo : userPoList) {
                if (username.equals(userPo.getUsername())) {
                    deleteZookeeperSessionByAuthToken(userPo.getRandomAuthToken());
                }
            }
        }
    }

    private void upsertSessionToZookeeper(String token, UserPo userPo) {
        try {
            zookeeperService.upsert(SESSION_COLLECTION, token, JSON.toJSONString(userPo));
        } catch (Exception e) {
            logger.error("向Zookeeper更新或插入会话发生错误", e);
        }
    }

    private UserPo getZookeeperSessionByAuthToken(String authToken) {
        try {
            JSONObject jsonObject = zookeeperService.findById(SESSION_COLLECTION, authToken);
            if (jsonObject != null) {
                return jsonObject.toJavaObject(UserPo.class);
            }
        } catch (Exception e) {
            logger.error("从Zookeeper查找会话发生错误", e);
        }
        return null;
    }

    private List<UserPo> getZookeeperSessionList() {
        try {
            List<UserPo> userPoList = new ArrayList<>();
            List<JSONObject> jsonObjectList = zookeeperService.find(SESSION_COLLECTION);
            if (jsonObjectList != null) {
                for (JSONObject jsonObject : jsonObjectList) {
                    userPoList.add(jsonObject.toJavaObject(UserPo.class));
                }
            }
            return userPoList;
        } catch (Exception e) {
            logger.error("从Zookeeper查找会话列表发生错误", e);
        }
        return null;
    }

    private boolean deleteZookeeperSessionByAuthToken(String authToken) {
        try {
            return zookeeperService.deleteById(SESSION_COLLECTION, authToken);
        } catch (Exception e) {
            logger.error("从Zookeeper删除会话发生错误", e);
        }
        return false;
    }
}
