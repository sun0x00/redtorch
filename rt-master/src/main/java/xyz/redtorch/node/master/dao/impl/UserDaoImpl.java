package xyz.redtorch.node.master.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.db.ZookeeperService;
import xyz.redtorch.node.master.dao.UserDao;
import xyz.redtorch.node.master.po.UserPo;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    private static final String USER_COLLECTION_NAME = "user";

    @Autowired
    private ZookeeperService zookeeperService;

    @Override
    public UserPo queryUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            logger.error("根据用户名查询用户错误,参数username缺失");
            throw new IllegalArgumentException("根据用户名查询用户错误,参数username缺失");
        }

        try {
            JSONObject jsonObject = zookeeperService.findById(USER_COLLECTION_NAME, username);
            if (jsonObject != null) {
                return jsonObject.toJavaObject(UserPo.class);
            }
        } catch (Exception e) {
            logger.error("根据用户名查询用户错误,数据转换发生错误", e);
        }
        return null;

    }

    @Override
    public void upsertUserByUsername(UserPo user) {
        if (user == null) {
            logger.error("根据用户名更新或保存用户错误,参数user缺失");
            throw new IllegalArgumentException("根据用户名更新或保存用户错误,参数user缺失");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            logger.error("根据用户名更新或保存用户错误,参数username缺失");
            throw new IllegalArgumentException("根据用户名更新或保存用户错误,参数username缺失");
        }

        try {
            zookeeperService.upsert(USER_COLLECTION_NAME, user.getUsername(), JSON.toJSONString(user));
        } catch (IllegalArgumentException e) {
            logger.error("根据用户名更新或保存用户错误", e);
        }
    }

    @Override
    public List<UserPo> queryUserList() {
        List<UserPo> userList = new ArrayList<>();
        try {
            List<JSONObject> jsonObjectList = zookeeperService.find(USER_COLLECTION_NAME);
            if (jsonObjectList != null) {
                for (JSONObject jsonObject : jsonObjectList) {
                    try {
                        userList.add(jsonObject.toJavaObject(UserPo.class));
                    } catch (Exception e) {
                        logger.error("查询用户列表,数据转换发生错误", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询用户列表发生错误", e);
        }

        return userList;
    }

    @Override
    public void deleteUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            logger.error("根据用户名删除用户错误,参数username缺失");
            throw new IllegalArgumentException("根据用户名删除用户错误,参数username缺失");
        }
        try {
            zookeeperService.deleteById(USER_COLLECTION_NAME, username);
        } catch (IllegalArgumentException e) {
            logger.error("根据用户名删除用户错误,节点ID:{}", username, e);
        }

    }

}
