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
import xyz.redtorch.node.master.dao.GatewayDao;
import xyz.redtorch.node.master.po.GatewayPo;

@Service
public class GatewayDaoImpl implements GatewayDao, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(NodeDaoImpl.class);

	private static final String GATEWAY_COLLECTION_NAME = "gateway_collection";

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
	public GatewayPo queryGatewayByGatewayId(String gatewayId) {
		if (StringUtils.isBlank(gatewayId)) {
			logger.error("根据网关ID查询网关错误,参数gatewayId缺失");
			throw new IllegalArgumentException("根据网关ID查询网关错误,参数gatewayId缺失");
		}

		Document filter = new Document();
		filter.append("gatewayId", gatewayId);
		List<Document> documentList = managementDBClient.find(managementDBName, GATEWAY_COLLECTION_NAME, filter);
		if (documentList == null || documentList.isEmpty()) {
			return null;
		}
		Document document = documentList.get(0);

		try {
			GatewayPo gateway = JSON.parseObject(JSON.toJSONString(document), GatewayPo.class);
			return gateway;
		} catch (Exception e) {
			logger.error("根据网关ID查询网关,数据转换发生错误Document-{}", document.toJson(), e);
			return null;
		}
	}

	@Override
	public List<GatewayPo> queryGatewayList() {
		List<Document> documentList = managementDBClient.find(managementDBName, GATEWAY_COLLECTION_NAME);
		List<GatewayPo> gatewayList = new ArrayList<>();

		for (Document document : documentList) {
			try {
				GatewayPo gateway = JSON.parseObject(JSON.toJSONString(document), GatewayPo.class);
				gatewayList.add(gateway);
			} catch (Exception e) {
				logger.error("查询网关列表,数据转换发生错误,Document-{}", document.toJson(), e);
			}
		}

		return gatewayList;
	}

	@Override
	public void upsertGatewayByGatewayId(GatewayPo gateway) {
		if (gateway == null) {
			logger.error("根据网关ID更新或保存网关错误,参数gateway缺失");
			throw new IllegalArgumentException("根据网关ID更新或保存网关错误,参数gateway缺失");
		}
		if (StringUtils.isBlank(gateway.getGatewayId())) {
			logger.error("根据网关ID更新或保存网关错误,参数gatewayId缺失");
			throw new IllegalArgumentException("根据网关ID更新或保存网关错误,参数gatewayId缺失");
		}

		try {
			Document document = Document.parse(JSON.toJSONString(gateway));
			Document filter = new Document();
			filter.append("gatewayId", gateway.getGatewayId());
			managementDBClient.upsert(managementDBName, GATEWAY_COLLECTION_NAME, document, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据网关ID更新或保存网关错误", e);
		}

	}

	@Override
	public void deleteGatewayByGatewayId(String gatewayId) {
		if (StringUtils.isBlank(gatewayId)) {
			logger.error("根据网关ID删除网关错误,参数gatewayId缺失");
			throw new IllegalArgumentException("根据网关ID删除网关错误,参数gatewayId缺失");
		}

		try {
			Document filter = new Document();
			filter.append("gatewayId", gatewayId);
			managementDBClient.delete(managementDBName, GATEWAY_COLLECTION_NAME, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据网关ID删除网关发生错误,网关ID:{}", gatewayId, e);
		}
	}

}
