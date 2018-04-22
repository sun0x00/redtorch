package xyz.redtorch.trader.module.zeus.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.module.zeus.ZeusDataUtil;
import xyz.redtorch.trader.module.zeus.entity.PositionDetail;
import xyz.redtorch.utils.MongoDBClient;
import xyz.redtorch.utils.MongoDBUtil;

/**
 * @author sun0x00@gmail.com
 */
public class ZeusDataUtilImpl implements ZeusDataUtil {
	
	private Logger log = LoggerFactory.getLogger(ZeusDataUtilImpl.class);
	
	private final String strategySyncVarCollection = "SyncVarCollection";
	private final String positionCollection = "strategyPos";
	
	private DataEngine dataEngine;
	
	private MongoDBClient defaultDBClient;
	private String defaultDBName = DataEngine.defaultDBName;
	
	public ZeusDataUtilImpl(DataEngine dataEngine) {
		this.defaultDBClient = dataEngine.getDefaultDBClient();
		this.dataEngine = dataEngine;
	}
	
	
	@Override
	public Map<String, String> loadStrategySyncVarMap(String strategyID) {
		Map<String, String> syncVarMap = new HashMap<>();
		Document filter = new Document();
		filter.append("strategyID", strategyID);
		List<Document> documentList = defaultDBClient.find(defaultDBName, strategySyncVarCollection, filter);
		if(!(documentList==null) && !documentList.isEmpty()) {
			Document document = documentList.get(0);
			for(Entry<String, Object> entrySet:document.entrySet()) {
				String key = entrySet.getKey();
				String value = (String)entrySet.getValue();
				// 剔除主键，ID和Name
				if(!"_id".equals(key)&&!"strategyID".equals(key)&&!"strategyName".equals(key)) {
					syncVarMap.put(key,value);
				}
			}
		}
		return syncVarMap;
	}
	
	@Override
	public void saveStrategySyncVarMap(Map<String,String> syncVarMapWithNameAndID) {
		Document document = new Document();
		document.putAll(syncVarMapWithNameAndID);
		
		Document filter = new Document();
		filter.put("strategyID", syncVarMapWithNameAndID.get("strategyID"));
		
		// 不使用upsert，避免有些字段在策略被删除后仍然在数据库中存在
		//defaultDBClient.upsert(defaultDBName, strategySyncVarCollection, document, filter);
		
		defaultDBClient.delete(defaultDBName, strategySyncVarCollection, filter);
		defaultDBClient.insert(defaultDBName, strategySyncVarCollection, document);
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
		filter.put("tradingDay", positionDetail.getTradingDay());
		
		Document document;
		try {
			document = MongoDBUtil.beanToDocument(positionDetail);
			String strategyName = positionDetail.getStrategyName();
			defaultDBClient.upsert(defaultDBName, positionCollection+strategyName,document, filter);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error("保持持仓数据转换发生错误",e);
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
