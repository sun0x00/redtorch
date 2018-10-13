package xyz.redtorch.core.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.core.gateway.GatewaySetting;
import xyz.redtorch.core.service.MongoDBService;
import xyz.redtorch.core.service.CoreEngineDataService;
import xyz.redtorch.utils.MongoDBClient;

/**
 * @author sun0x00@gmail.com
 */
@Service
@PropertySource(value = { "classpath:rt-core.properties" })
public class CoreEngineDataServiceImpl implements CoreEngineDataService, InitializingBean {

	private Logger log = LoggerFactory.getLogger(CoreEngineDataServiceImpl.class);

	private final String gatewaySettingCollection = "GatewaySetting";

	private MongoDBClient defaultDBClient;

	@Autowired
	private MongoDBService mongoDBService;

	@Value("${rt.client.dbname}")
	private String clientDBName;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.defaultDBClient = mongoDBService.getDefaultDBClient();
	}

	@Override
	public GatewaySetting queryGatewaySetting(String gatewayID) {
		Document filter = new Document();
		filter.append("gatewayID", gatewayID);
		List<Document> documentList = defaultDBClient.find(clientDBName, gatewaySettingCollection, filter);
		if (documentList == null || documentList.isEmpty()) {
			return null;
		}
		Document document = documentList.get(0);

		try {
			GatewaySetting gatewaySetting = JSON.parseObject(document.toJson(), GatewaySetting.class);
			return gatewaySetting;
		} catch (Exception e) {
			log.error("查询网关数据数据转换发生错误Document-{}", document.toJson(), e);
			return null;
		}

	}

	@Override
	public List<GatewaySetting> queryGatewaySettings() {
		List<Document> documentList = defaultDBClient.find(clientDBName, gatewaySettingCollection);
		List<GatewaySetting> gatewaySettings = new ArrayList<>();

		for (Document document : documentList) {
			try {
				GatewaySetting gatewaySetting = JSON.parseObject(document.toJson(), GatewaySetting.class);
				gatewaySettings.add(gatewaySetting);
			} catch (Exception e) {
				log.error("查询网关数据转换发生错误", e);
			}
		}

		return gatewaySettings;
	}

	@Override
	public void deleteGatewaySetting(String gatewayID) {
		Document filter = new Document();
		filter.append("gatewayID", gatewayID);
		defaultDBClient.delete(clientDBName, gatewaySettingCollection, filter);
	}

	@Override
	public void saveGatewaySetting(GatewaySetting gatewaySetting) {
		try {
			Document document = Document.parse(JSON.toJSONString(gatewaySetting));
			defaultDBClient.insert(clientDBName, gatewaySettingCollection, document);
		} catch (IllegalArgumentException e) {
			log.error("保存网关设置到数据库发生错误", e);
		}

	}

}
