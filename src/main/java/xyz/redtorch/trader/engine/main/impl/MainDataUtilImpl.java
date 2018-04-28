package xyz.redtorch.trader.engine.main.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.engine.main.MainDataUtil;
import xyz.redtorch.trader.gateway.GatewaySetting;
import xyz.redtorch.utils.MongoDBClient;
import xyz.redtorch.utils.MongoDBUtil;
/**
 * @author sun0x00@gmail.com
 */
public class MainDataUtilImpl implements MainDataUtil {
	
	private final String gatewaySettingCollection = "GatewaySetting";
	
	private MongoDBClient defaultDBClient;
	
	private String defaultDBName = DataEngine.DEFAULT_DB_NAME;

	
	private Logger log = LoggerFactory.getLogger(MainDataUtilImpl.class);
	
	public MainDataUtilImpl(DataEngine dataEngine) {
		this.defaultDBClient = dataEngine.getDefaultDBClient();
	}
	
	@Override
	public GatewaySetting queryGatewaySetting(String gatewayID) {
		Document filter = new Document(); 
		filter.append("gatewayID", gatewayID);
		List<Document> documentList = defaultDBClient.find(defaultDBName, gatewaySettingCollection, filter);
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
		List<Document> documentList = defaultDBClient.find(defaultDBName, gatewaySettingCollection);
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
		defaultDBClient.delete(defaultDBName, gatewaySettingCollection, filter);
	}

	@Override
	public void saveGatewaySetting(GatewaySetting gatewaySetting) {
		try {
			Document document = MongoDBUtil.beanToDocument(gatewaySetting);
			defaultDBClient.insert(defaultDBName, gatewaySettingCollection, document);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error("保存接口设置到数据库发生错误",e);
		}
		
	}

}
