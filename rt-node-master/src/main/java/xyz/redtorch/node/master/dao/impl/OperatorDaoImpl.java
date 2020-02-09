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
import xyz.redtorch.node.master.dao.OperatorDao;
import xyz.redtorch.node.master.po.OperatorPo;

@Service
public class OperatorDaoImpl implements OperatorDao, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(NodeDaoImpl.class);

	private static final String GATEWAY_COLLECTION_NAME = "operator_collection";

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
	public OperatorPo queryOperatorByOperatorId(String operatorId) {
		if (StringUtils.isBlank(operatorId)) {
			logger.error("根据操作员ID查询操作员错误,参数operatorId缺失");
			throw new IllegalArgumentException("根据操作员ID查询操作员错误,参数operatorId缺失");
		}

		Document filter = new Document();
		filter.append("operatorId", operatorId);
		List<Document> documentList = managementDBClient.find(managementDBName, GATEWAY_COLLECTION_NAME, filter);
		if (documentList == null || documentList.isEmpty()) {
			return null;
		}
		Document document = documentList.get(0);

		try {
			OperatorPo operator = JSON.parseObject(JSON.toJSONString(document), OperatorPo.class);
			return operator;
		} catch (Exception e) {
			logger.error("根据操作员ID查询操作员,数据转换发生错误Document-{}", document.toJson(), e);
			return null;
		}
	}

	@Override
	public List<OperatorPo> queryOperatorList() {
		List<Document> documentList = managementDBClient.find(managementDBName, GATEWAY_COLLECTION_NAME);
		List<OperatorPo> operatorList = new ArrayList<>();

		for (Document document : documentList) {
			try {
				OperatorPo operator = JSON.parseObject(JSON.toJSONString(document), OperatorPo.class);
				operatorList.add(operator);
			} catch (Exception e) {
				logger.error("查询操作员列表,数据转换发生错误,Document-{}", document.toJson(), e);
			}
		}

		return operatorList;
	}

	@Override
	public void upsertOperatorByOperatorId(OperatorPo operator) {
		if (operator == null) {
			logger.error("根据操作员ID更新或保存操作员错误,参数operator缺失");
			throw new IllegalArgumentException("根据操作员ID更新或保存操作员错误,参数operator缺失");
		}
		if (StringUtils.isBlank(operator.getOperatorId())) {
			logger.error("根据操作员ID更新或保存操作员错误,参数operatorId缺失");
			throw new IllegalArgumentException("根据操作员ID更新或保存操作员错误,参数operatorId缺失");
		}

		try {
			Document document = Document.parse(JSON.toJSONString(operator));
			Document filter = new Document();
			filter.append("operatorId", operator.getOperatorId());
			managementDBClient.upsert(managementDBName, GATEWAY_COLLECTION_NAME, document, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据操作员ID更新或保存操作员错误", e);
		}

	}

	@Override
	public void deleteOperatorByOperatorId(String operatorId) {
		if (StringUtils.isBlank(operatorId)) {
			logger.error("根据操作员ID删除操作员错误,参数operatorId缺失");
			throw new IllegalArgumentException("根据操作员ID删除操作员错误,参数operatorId缺失");
		}

		try {
			Document filter = new Document();
			filter.append("operatorId", operatorId);
			managementDBClient.delete(managementDBName, GATEWAY_COLLECTION_NAME, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据操作员ID删除操作员发生错误,操作员ID:{}", operatorId, e);
		}
	}

}
