package xyz.redtorch.core.service.impl;

import java.lang.reflect.InvocationTargetException;
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

import xyz.redtorch.core.gateway.GatewaySetting;
import xyz.redtorch.core.service.MongoDBService;
import xyz.redtorch.core.service.TradeEngineDataService;
import xyz.redtorch.utils.MongoDBClient;
import xyz.redtorch.utils.MongoDBUtil;
/**
 * @author sun0x00@gmail.com
 */
@Service
@PropertySource(value = {"classpath:rt-core.properties"})
public class TradeEngineDataServiceImpl implements TradeEngineDataService,InitializingBean {

	private Logger log = LoggerFactory.getLogger(TradeEngineDataServiceImpl.class);
	
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
		if(documentList == null || documentList.isEmpty()) {
			return null;
		}
		Document document = documentList.get(0);

		GatewaySetting gatewaySetting = new GatewaySetting();
		try {
			gatewaySetting = MongoDBUtil.documentToBean(document, gatewaySetting);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			log.error("查询接口数据数据转换发生错误Document-",document.toJson(),e);
			return null;
		}
		
		return gatewaySetting;
	}

	@Override
	public List<GatewaySetting> queryGatewaySettings() {
		List<Document> documentList = defaultDBClient.find(clientDBName, gatewaySettingCollection);
		List<GatewaySetting> gatewaySettings = new ArrayList<>();
		
		for(Document document:documentList) {
			GatewaySetting gatewaySetting = new GatewaySetting();
			try {
				gatewaySetting = MongoDBUtil.documentToBean(document, gatewaySetting);
				gatewaySettings.add(gatewaySetting);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				log.error("查询接口数据转换发生错误",e);
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
			Document document = MongoDBUtil.beanToDocument(gatewaySetting);
			defaultDBClient.insert(clientDBName, gatewaySettingCollection, document);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error("保存接口设置到数据库发生错误",e);
		}
		
	}

}
