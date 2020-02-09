package xyz.redtorch.node.master.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.common.mongo.MongoDBClient;
import xyz.redtorch.node.db.MongoDBClientService;
import xyz.redtorch.node.master.dao.UserDao;
import xyz.redtorch.node.master.po.UserPo;

@Service
public class UserDaoImpl implements UserDao, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

	private static final String USER_COLLECTION_NAME = "user_collection";

	@Autowired
	private MongoDBClientService mongoDBClientService;

	private MongoDBClient managementDBClient;
	private String managementDBName;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.managementDBClient = mongoDBClientService.getManagementDBClient();
		this.managementDBName = mongoDBClientService.getManagementDBName();
	}

	@Override
	public UserPo queryUserByUsername(String username) {
		if (StringUtils.isBlank(username)) {
			logger.error("根据用户名查询用户错误,参数username缺失");
			throw new IllegalArgumentException("根据用户名查询用户错误,参数username缺失");
		}

		try {
			Document filter = new Document();
			filter.append("username", username);
			List<Document> documentList = managementDBClient.find(managementDBName, USER_COLLECTION_NAME, filter);
			if (documentList == null || documentList.isEmpty()) {
				return null;
			}
			if (documentList.size() > 1) {
				logger.warn("根据用户名查询出多个用户,仅取一个,用户名：{}", username);
			}

			UserPo user = JSON.parseObject(JSON.toJSONString(documentList.get(0)), UserPo.class);

			return user;

		} catch (IllegalArgumentException e) {
			logger.error("根据用户名查询用户错误,用户名:{}", username, e);
			return null;
		}
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
			Document document = Document.parse(JSON.toJSONString(user));
			Document filter = new Document();
			filter.append("username", user.getUsername());
			managementDBClient.upsert(managementDBName, USER_COLLECTION_NAME, document, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据用户名更新或保存用户错误", e);
		}
	}

	@Override
	public List<UserPo> queryUserList() {
		List<Document> documentList = managementDBClient.find(managementDBName, USER_COLLECTION_NAME);
		List<UserPo> userList = new ArrayList<>();

		for (Document document : documentList) {
			try {
				UserPo user = JSON.parseObject(JSON.toJSONString(document), UserPo.class);
				userList.add(user);
			} catch (Exception e) {
				logger.error("查询网关列表,数据转换发生错误,Document-{}", document.toJson(), e);
			}
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
			Document filter = new Document();
			filter.append("username", username);
			this.managementDBClient.delete(managementDBName, USER_COLLECTION_NAME, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据用户名删除用户错误,节点ID:{}", username, e);
		}

	}

}
