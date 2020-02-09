package xyz.redtorch.node.master.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.RtConstant;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.node.master.dao.UserDao;
import xyz.redtorch.node.master.po.OperatorPo;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.FavoriteContractService;
import xyz.redtorch.node.master.service.OperatorService;
import xyz.redtorch.node.master.service.UserService;
import xyz.redtorch.node.master.web.socket.WebSocketServerHandler;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Value("${rt.node.master.web.admin-password-sha-256}")
	private String adminPasswordSha256;
	@Value("${rt.node.master.operatorId}")
	private String operatorId;
	@Autowired
	private UserDao userDao;
	@Autowired
	private FavoriteContractService favoriteContractService;
	@Autowired
	private OperatorService operatorService;
	@Autowired
	private WebSocketServerHandler webSocketServerHandler;

	/*
	 * 预设的取值范围是[60000000,90000000] 从理论上，有3000万次登录之后才有很小的几率出现错误， 数字会不断累加直到下次重启
	 */
	private volatile int nodeId = 60000000;

	private ReentrantLock nodeIdLock = new ReentrantLock();

	@Override
	public UserPo userAuth(UserPo user) {

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
		String passwoedSha256hex = DigestUtils.sha256Hex(user.getPassword());

		if (user.getUsername().equals("admin")) {
			// 对admin用户使用配置文件密码校验
			if (adminPasswordSha256.equals(passwoedSha256hex)) {
				UserPo resUser = new UserPo();
				resUser.setUsername("admin");
				nodeIdLock.lock();
				try {
					do {
						nodeId++;
					} while (webSocketServerHandler.containsNodeId(nodeId));
				} finally {
					nodeIdLock.unlock();
				}
				logger.error("用户审核通过,用户名:{},分配节点ID:{}", user.getUsername(), nodeId);
				resUser.setRecentlyNodeId(nodeId);
				resUser.setOperatorId(operatorId);
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
			return null;
		} else {
			// 非admin用户查询数据库
			UserPo queriedUser = userDao.queryUserByUsername(user.getUsername());
			if (queriedUser == null || StringUtils.isBlank(queriedUser.getPassword())) {
				return null;
			} else {
				if (queriedUser.getPassword().equals(passwoedSha256hex)) {
					nodeIdLock.lock();
					try {
						do {
							nodeId++;
						} while (webSocketServerHandler.containsNodeId(nodeId));
					} finally {
						nodeIdLock.unlock();
					}
					queriedUser.setRecentlyIpAddress(user.getRecentlyIpAddress());
					queriedUser.setRecentlyPort(user.getRecentlyPort());
					queriedUser.setRecentlySessionId(user.getRecentlySessionId());
					queriedUser.setRecentlyNodeId(nodeId);
					queriedUser.setRecentlyLoginTime(CommonUtils.DT_FORMAT_WITH_MS_FORMATTER.format(LocalDateTime.now()));
					if (queriedUser.getLoginTimes() == null) {
						queriedUser.setLoginTimes(1);
					} else {
						queriedUser.setLoginTimes(queriedUser.getLoginTimes() + 1);
					}
					userDao.upsertUserByUsername(queriedUser);
					queriedUser.setPassword(RtConstant.SECURITY_MASK);
					return queriedUser;
				}
				return null;
			}
		}
	}

	@Override
	public String userChangePassword(UserPo user) {
		UserPo loggedinUser = this.userAuth(user);
		if (loggedinUser == null) {
			logger.error("用户{}修改密码失败,旧密码验证失败", user.getUsername());
			return "修改密码失败,旧密码验证失败";
		} else {
			if (StringUtils.isBlank(user.getNewPassword())) {
				logger.error("用户{}修改密码失败,新密码为空", user.getUsername());
				return "修改密码失败,新密码为空";
			}
			// 对新密码使用SHA-256加密
			loggedinUser.setPassword(DigestUtils.sha256Hex(user.getNewPassword()));
			// 更新用户
			userDao.upsertUserByUsername(loggedinUser);
			return null;
		}

	}

	@Override
	public List<UserPo> getUserList() {
		List<UserPo> userList = userDao.queryUserList();
		for (UserPo user : userList) {
			user.setPassword(RtConstant.SECURITY_MASK);
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
				OperatorPo operator = operatorService.getOperatorByOperatorId(operatorId);
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
			userDao.upsertUserByUsername(queriedUser);
		} else {
			logger.warn("根据用户名更新权限警告,未查出用户,用户名:{}", username);
		}
	}

}
