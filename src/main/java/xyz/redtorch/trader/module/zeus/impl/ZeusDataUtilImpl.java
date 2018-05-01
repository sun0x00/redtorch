package xyz.redtorch.trader.module.zeus.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.module.zeus.ZeusDataUtil;
import xyz.redtorch.trader.module.zeus.entity.PositionDetail;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;
import xyz.redtorch.utils.CommonUtil;
import xyz.redtorch.utils.MongoDBClient;
import xyz.redtorch.utils.MongoDBUtil;

/**
 * @author sun0x00@gmail.com
 */
public class ZeusDataUtilImpl implements ZeusDataUtil {
	
	private Logger log = LoggerFactory.getLogger(ZeusDataUtilImpl.class);
	
	private final String strategySettingCollection = "StrategySetting";
	private final String positionCollection = "StrategyPos";
	
	private DataEngine dataEngine;
	
	private MongoDBClient defaultDBClient;
	private String defaultDBName = DataEngine.DEFAULT_DB_NAME;
	
	private String logStr = "ZEUS DataUtil:";
	
	public ZeusDataUtilImpl(DataEngine dataEngine) {
		this.defaultDBClient = dataEngine.getDefaultDBClient();
		this.dataEngine = dataEngine;
	}
	
	@Override
	public List<StrategySetting> loadStrategySettings() {
		String logContent;
		List<StrategySetting> strategySettingList = new ArrayList<>();
		List<Document> documentList = defaultDBClient.find(defaultDBName, strategySettingCollection);
		if(!(documentList==null) && !documentList.isEmpty()) {
			for(Document document:documentList) {
				String strategyID = document.getString("strategyID");
				if (StringUtils.isEmpty(strategyID)) {
					logContent = logStr + "根据策略ID["+strategyID+"]查出的记录解析出错,未找到策略ID!";
					CommonUtil.emitErrorLog(logContent);
					log.error(logContent);
					continue;
				}
				StrategySetting strategySetting = coverDocumentToStrategySetting(strategyID, document); 
				if(strategySetting != null) {
					strategySettingList.add(strategySetting);
				}
			}
		}else {
			logContent = logStr + "未能查出任何策略配置记录!";
			CommonUtil.emitWarnLog(logContent);
			log.warn(logContent);
		}
		return strategySettingList;
	}
	
	
	@Override
	public StrategySetting loadStrategySetting(String strategyID) {
		String logContent;
		Document filter = new Document();
		filter.append("strategyID", strategyID);
		List<Document> documentList = defaultDBClient.find(defaultDBName, strategySettingCollection, filter);
		if(!(documentList==null) && !documentList.isEmpty()) {

			if(documentList.size()>2){
				logContent = logStr + "根据策略ID["+strategyID+"]查出"+documentList.size()+"个配置记录,仅选取第一个";
				CommonUtil.emitErrorLog(logContent);
				log.error(logContent);
			}
			Document document = documentList.get(0);
			
			StrategySetting strategySetting = coverDocumentToStrategySetting(strategyID, document); 
			
			return strategySetting;
			
		}else {
			logContent = logStr + "根据策略ID["+strategyID+"]未能查出配置记录!";
			CommonUtil.emitWarnLog(logContent);
			log.warn(logContent);
			return null;
		}
	}
	
	public StrategySetting coverDocumentToStrategySetting(String strategyID, Document document) {
		String logContent;
		try {
			StrategySetting strategySetting = JSON.parseObject(document.toJson(), StrategySetting.class);
			if (strategySetting == null) {
				logContent = logStr + "根据策略ID["+strategyID+"]查出的记录解析出错,JSON工具解析返回null!";
				CommonUtil.emitErrorLog(logContent);
				log.error(logContent);
				return null;
			}
			
			// 合成一些配置
			strategySetting.fixSetting();

			/////////////////////////////
			// 对配置文件进行基本检查
			////////////////////////////
			if (StringUtils.isEmpty(strategySetting.getStrategyID())) {
				logContent = logStr + "根据策略ID["+strategyID+"]查出的记录解析出错,未找到策略ID!";
				CommonUtil.emitErrorLog(logContent);
				log.error(logContent);
				return null;
			}
			if (StringUtils.isEmpty(strategySetting.getStrategyName())) {
				logContent = logStr + "根据策略ID["+strategyID+"]查出的记录解析出错,未找到策略名称!";
				CommonUtil.emitErrorLog(logContent);
				log.error(logContent);
				return null;
			}
			if (StringUtils.isEmpty(strategySetting.getTradingDay())) {
				logContent = logStr + "根据策略ID["+strategyID+"]查出的记录解析出错,未找到tradingDay!";
				CommonUtil.emitErrorLog(logContent);
				log.error(logContent);
				return null;
			}
			if (strategySetting.getGateways() == null || strategySetting.getGateways().isEmpty()) {
				logContent = logStr + "根据策略ID["+strategyID+"]查出的记录解析出错,未找到gateways!";
				CommonUtil.emitErrorLog(logContent);
				log.error(logContent);
				return null;
			}
			if (strategySetting.getContracts() == null
					|| strategySetting.getContracts().isEmpty()) {
				logContent = logStr + "根据策略ID["+strategyID+"]查出的记录解析出错,未找到contracts!";
				CommonUtil.emitErrorLog(logContent);
				log.error(logContent);
				return null;
			}

			boolean error = false;
			for (StrategySetting.ContractSetting contractSetting : strategySetting.getContracts()) {
				if (contractSetting.getTradeGateways() == null
						|| contractSetting.getTradeGateways().isEmpty()) {

					logContent = logStr + "根据策略ID["+strategyID+"]查出的记录解析出错,未找到合约"+ contractSetting.getRtSymbol() +"的tradeGateways配置";
					CommonUtil.emitErrorLog(logContent);
					log.error(logContent);
					error = true;
					break;
				}

			}
			if (error) {
				return null;
			}

			return strategySetting;
		} catch (Exception e) {
			logContent = logStr + "根据策略ID["+strategyID+"]查出的记录解析出错!";
			CommonUtil.emitErrorLog(logContent);
			log.error(logContent,e);
			return null;
		}
		
	}
	
	@Override
	public void saveStrategySetting(StrategySetting strategySetting) {
		Document document =Document.parse(JSON.toJSONString(strategySetting));
		
		Document filter = new Document();
		filter.put("strategyID", strategySetting.getStrategyID());
		
		// 不使用upsert,避免有些字段在策略被删除后仍然在数据库中存在
		// defaultDBClient.upsert(defaultDBName, strategySettingCollection, document, filter);
		
		defaultDBClient.delete(defaultDBName, strategySettingCollection, filter);
		defaultDBClient.insert(defaultDBName, strategySettingCollection, document);
	}

	@Override
	public List<PositionDetail> loadStrategyPositionDetails(String tradingDay, String strategyID, String strategyName) {
		Document filter = new Document();
		filter.put("strategyID", strategyID);
		filter.put("tradingDay", tradingDay);
		
		List<Document> documentList = defaultDBClient.find(defaultDBName, positionCollection+strategyName, filter);
		List<PositionDetail> positionDetailList = new ArrayList<>();
		for(Document document :documentList) {
			PositionDetail positionDetail = new PositionDetail();
			try {
				MongoDBUtil.documentToBean(document, positionDetail);
				positionDetailList.add(positionDetail);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				log.error("查询持仓数据转换发生错误,Document-",document.toJson(),e);
			}
		}
		return positionDetailList;
	}

	@Override
	public void saveStrategyPositionDetail(PositionDetail positionDetail) {
		Document filter = new Document();
		filter.put("strategyID", positionDetail.getStrategyID());
		filter.put("rtSymbol", positionDetail.getRtSymbol());
		filter.put("gatewayID", positionDetail.getGatewayID());
		filter.put("tradingDay", positionDetail.getTradingDay());
		
		try {
			Document document = new Document();		
			
			document = MongoDBUtil.beanToDocument(positionDetail);
			String strategyName = positionDetail.getStrategyName();
			System.out.println(document.toJson());
			defaultDBClient.upsert(defaultDBName, positionCollection+strategyName,document, filter);
		} catch (Exception  e) {
			log.error("保存持仓数据转换发生错误",e);
		}
		
	}


	@Override
	public List<Bar> loadBarDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol) {
		return dataEngine.loadBarDataList(startDateTime, endDateTime, rtSymbol);
	}


	@Override
	public List<Tick> loadTickDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol) {
		return dataEngine.loadTickDataList(startDateTime, endDateTime, rtSymbol);
	}
}
