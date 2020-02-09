package xyz.redtorch.node.master.dao;

import java.util.List;

import xyz.redtorch.node.master.po.UserPo;

public interface UserDao {
	UserPo queryUserByUsername(String username);

	void upsertUserByUsername(UserPo user);

	List<UserPo> queryUserList();

	void deleteUserByUsername(String username);
}
