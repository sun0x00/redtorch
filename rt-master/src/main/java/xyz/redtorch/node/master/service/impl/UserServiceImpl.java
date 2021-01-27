package xyz.redtorch.node.master.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.node.master.dao.UserDao;
import xyz.redtorch.node.master.po.OperatorPo;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.FavoriteContractService;
import xyz.redtorch.node.master.service.OperatorService;
import xyz.redtorch.node.master.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Value("${rt.master.adminPasswordSha256}")
	private String adminPasswordSha256;
	@Value("${rt.master.operatorId}")
	private String masterOperatorId;
	@Autowired
	private UserDao userDao;
	@Autowired
	private FavoriteContractService favoriteContractService;
	@Autowired
	private OperatorService operatorService;


	@Override
	public UserPo auth(UserPo user) {

		if (user == null) {
			logger.error("用户审核错误,参数node缺失");
			throw new IllegalArgumentException("用户审核错误,参数user缺失");
		}
		if (StringUtils.isBlank(user.getUsername())) {
			logger.error("用户审核错误,参数username缺失");
			throw new IllegalArgumentException("用户审核错误,参数username缺失");
		}
		if (StringUtils.isBlank(user.getPassword())) {
			logger.error("用户审核错误,参数password缺失");
			throw new IllegalArgumentException("用户审核错误,参数password缺失");
		}

		// 获取密码的SHA-256散列值
		String passwordSha256hex = DigestUtils.sha256Hex(user.getPassword());

		if (user.getUsername().equals("admin")) {
			// 对admin用户使用配置文件密码校验
			if (adminPasswordSha256.equals(passwordSha256hex)) {
				UserPo resUser = new UserPo();
				resUser.setUsername("admin");
				logger.error("用户审核通过,用户名:{}", user.getUsername());
				resUser.setOperatorId(masterOperatorId);
				// 全部授权
				resUser.setCanChangeGatewayStatus(true);
				resUser.setCanChangeNodeToken(true);
				resUser.setCanChangeOperatorStatus(true);
				resUser.setCanReadGateway(true);
				resUser.setCanReadLog(true);
				resUser.setCanReadMarketDataRecording(true);
				resUser.setCanReadNode(true);
				resUser.setCanReadOperator(true);
				resUser.setCanReadUser(true);
				resUser.setCanWriteGateway(true);
				resUser.setCanWriteMarketDataRecording(true);
				resUser.setCanWriteNode(true);
				resUser.setCanWriteOperator(true);
				resUser.setCanWriteUser(true);
				return resUser;
			}
		} else {
			// 非admin用户查询数据库
			UserPo queriedUser = userDao.queryUserByUsername(user.getUsername());
			if (queriedUser != null && !StringUtils.isBlank(queriedUser.getPassword())) {
				if (queriedUser.getPassword().equals(passwordSha256hex)) {
					queriedUser.setRecentlyIpAddress(user.getRecentlyIpAddress());
					queriedUser.setRecentlyPort(user.getRecentlyPort());
					queriedUser.setRecentlySessionId(user.getRecentlySessionId());
					queriedUser.setRecentlyLoginTime(CommonUtils.DT_FORMAT_WITH_MS_FORMATTER.format(LocalDateTime.now()));
					if (queriedUser.getLoginTimes() == null) {
						queriedUser.setLoginTimes(1);
					} else {
						queriedUser.setLoginTimes(queriedUser.getLoginTimes() + 1);
					}
					userDao.upsertUserByUsername(queriedUser);
					queriedUser.setPassword(CommonConstant.SECURITY_MASK);
					return queriedUser;
				}
			}
		}
		return null;
	}

	@Override
	public String changePassword(UserPo user) {
		UserPo authedUser = this.auth(user);
		if (authedUser == null) {
			logger.error("用户{}修改密码失败,旧密码验证失败", user.getUsername());
			return "修改密码失败,旧密码验证失败";
		} else {
			if (StringUtils.isBlank(user.getNewPassword())) {
				logger.error("用户{}修改密码失败,新密码为空", user.getUsername());
				return "修改密码失败,新密码为空";
			}
			// 对新密码使用SHA-256加密
			authedUser.setPassword(DigestUtils.sha256Hex(user.getNewPassword()));
			// 更新用户
			userDao.upsertUserByUsername(authedUser);
			return null;
		}

	}

	@Override
	public List<UserPo> getUserList() {
		List<UserPo> userList = userDao.queryUserList();
		for (UserPo user : userList) {
			user.setPassword(CommonConstant.SECURITY_MASK);
		}
		return userList;
	}

	@Override
	public void deleteUserByUsername(String username) {
		if (StringUtils.isBlank(username)) {
			logger.error("根据用户名删除用户错误,参数username缺失");
			throw new IllegalArgumentException("根据用户名删除用户错误,参数username缺失");
		}

		UserPo user = userDao.queryUserByUsername(username);
		if (user != null) {
			userDao.deleteUserByUsername(username);
			favoriteContractService.deleteContractByUsername(username);
			if (StringUtils.isNotBlank(user.getOperatorId())) {
				OperatorPo operator = operatorService.getOperatorByOperatorId(masterOperatorId);
				if (operator == null || !operator.isAssociatedToUser()) {
					operatorService.deleteOperatorByOperatorId(user.getOperatorId());
				}
			}
		}

	}

	@Override
	public void updateUserDescriptionByUsername(UserPo user) {
		if (user == null) {
			logger.error("根据用户名更新用户描述错误,参数user缺失");
			throw new IllegalArgumentException("根据用户名更新用户描述错误,参数user缺失");
		}
		if (StringUtils.isBlank(user.getUsername())) {
			logger.error("根据用户名更新用户描述错误,参数username缺失");
			throw new IllegalArgumentException("根据用户名更新用户描述错误,参数username缺失");
		}
		String username = user.getUsername();
		UserPo queriedUser = userDao.queryUserByUsername(username);
		if (queriedUser != null) {
			queriedUser.setDescription(user.getDescription());
			userDao.upsertUserByUsername(queriedUser);
		} else {
			logger.warn("根据用户名更新用户描述警告,未查出用户,用户名:{}", username);
		}

	}

	@Override
	public void updateUserPasswordByUsername(UserPo user) {
		if (user == null) {
			logger.error("根据用户名更新密码错误,参数user缺失");
			throw new IllegalArgumentException("根据用户名更新密码错误,参数user缺失");
		}
		if (StringUtils.isBlank(user.getUsername())) {
			logger.error("根据用户名更新密码错误,参数username缺失");
			throw new IllegalArgumentException("根据用户名更新密码错误,参数username缺失");
		}
		if (StringUtils.isBlank(user.getNewPassword())) {
			logger.error("根据用户名更新密码错误,参数newPassword缺失");
			throw new IllegalArgumentException("根据用户名更新密码错误,参数newPassword缺失");
		}
		String username = user.getUsername();
		UserPo queriedUser = userDao.queryUserByUsername(username);
		if (queriedUser != null) {
			queriedUser.setPassword(DigestUtils.sha256Hex(user.getNewPassword()));
			userDao.upsertUserByUsername(queriedUser);
		} else {
			logger.warn("根据用户名更新密码警告,未查出用户,用户名:{}", username);
		}

	}

	@Override
	public void addUser(UserPo user) {
		if (user == null) {
			logger.error("增加用户错误,参数user缺失");
			throw new IllegalArgumentException("增加用户错误,参数user缺失");
		}
		if (StringUtils.isBlank(user.getUsername())) {
			logger.error("增加用户错误,参数username缺失");
			throw new IllegalArgumentException("增加用户错误,参数username缺失");
		}
		if (StringUtils.isBlank(user.getNewPassword())) {
			logger.error("增加用户错误,参数newPassword缺失");
			throw new IllegalArgumentException("增加用户错误,参数newPassword缺失");
		}

		String username = user.getUsername();
		UserPo queriedUser = userDao.queryUserByUsername(username);
		if (queriedUser != null) {
			throw new IllegalArgumentException("增加用户错误,用户已存在");
		} else {

			String operatorId = UUIDStringPoolUtils.getUUIDString();

			user.setPassword(DigestUtils.sha256Hex(user.getNewPassword()));
			user.setOperatorId(UUIDStringPoolUtils.getUUIDString());
			user.setOperatorId(operatorId);

			OperatorPo operator = new OperatorPo();
			operator.setUsername(username);
			operator.setAssociatedToUser(true);
			operator.setOperatorId(operatorId);

			userDao.upsertUserByUsername(user);
			operatorService.upsertOperatorByOperatorId(operator);
		}

	}

	@Override
	public void updateUserPermissionByUsername(UserPo user) {
		if (user == null) {
			logger.error("根据用户名更新权限错误,参数user缺失");
			throw new IllegalArgumentException("根据用户名更新权限错误,参数user缺失");
		}
		if (StringUtils.isBlank(user.getUsername())) {
			logger.error("根据用户名更新权限错误,参数username缺失");
			throw new IllegalArgumentException("根据用户名更新用户描述错误,参数username缺失");
		}

		String username = user.getUsername();
		UserPo queriedUser = userDao.queryUserByUsername(username);
		if (queriedUser != null) {
			queriedUser.setCanChangeGatewayStatus(user.isCanChangeGatewayStatus());
			queriedUser.setCanChangeNodeToken(user.isCanChangeNodeToken());
			queriedUser.setCanChangeOperatorStatus(user.isCanChangeOperatorStatus());
			queriedUser.setCanReadGateway(user.isCanReadGateway());
			queriedUser.setCanReadLog(user.isCanReadLog());
			queriedUser.setCanReadNode(user.isCanReadNode());
			queriedUser.setCanReadOperator(user.isCanReadOperator());
			queriedUser.setCanReadUser(user.isCanReadUser());
			queriedUser.setCanWriteGateway(user.isCanWriteGateway());
			queriedUser.setCanWriteNode(user.isCanWriteNode());
			queriedUser.setCanWriteOperator(user.isCanWriteOperator());
			queriedUser.setCanWriteUser(user.isCanWriteUser());
			queriedUser.setCanWriteMarketDataRecording(user.isCanWriteMarketDataRecording());
			queriedUser.setCanReadMarketDataRecording(user.isCanReadMarketDataRecording());
			userDao.upsertUserByUsername(queriedUser);
		} else {
			logger.warn("根据用户名更新权限警告,未查出用户,用户名:{}", username);
		}
	}

}
