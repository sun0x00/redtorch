package xyz.redtorch.node.master.dao;

import xyz.redtorch.node.master.po.UserPo;

import java.util.List;

public interface UserDao {
    UserPo queryUserByUsername(String username);

    void upsertUserByUsername(UserPo user);

    List<UserPo> queryUserList();

    void deleteUserByUsername(String username);
}
