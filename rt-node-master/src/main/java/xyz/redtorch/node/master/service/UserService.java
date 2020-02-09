package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.node.master.po.UserPo;

public interface UserService {
	UserPo userAuth(UserPo user);

	String userChangePassword(UserPo user);

	List<UserPo> getUserList();

	void deleteUserByUsername(String username);

	void updateUserDescriptionByUsername(UserPo user);

	void updateUserPasswordByUsername(UserPo user);

	void addUser(UserPo user);

	void updateUserPermissionByUsername(UserPo user);
}
