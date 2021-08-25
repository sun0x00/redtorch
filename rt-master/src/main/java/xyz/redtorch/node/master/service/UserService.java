package xyz.redtorch.node.master.service;

import xyz.redtorch.node.master.po.UserPo;

import java.util.List;

public interface UserService {
    UserPo auth(UserPo user);

    String changePassword(UserPo user);

    List<UserPo> getUserList();

    void deleteUserByUsername(String username);

    void updateUserDescriptionByUsername(UserPo user);

    void updateUserPasswordByUsername(UserPo user);

    void addUser(UserPo user);

    void updateUserPermissionByUsername(UserPo user);

    UserPo getUserByUsername(String username);
}
