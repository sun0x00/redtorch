package xyz.redtorch.core.zeus.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.service.MongoDBService;
import xyz.redtorch.core.zeus.ZeusDataService;
import xyz.redtorch.core.zeus.entity.PositionDetail;
import xyz.redtorch.core.zeus.strategy.StrategySetting;
import xyz.redtorch.utils.MongoDBClient;
import xyz.redtorch.utils.MongoDBUtil;

/**
 * @author sun0x00@gmail.com
 */
@Service
@PropertySource(value = { "classpath:rt-core.properties" })
public class ZeusDataServiceImpl implements ZeusDataService, InitializingBean {

	private Logger log = LoggerFactory.getLogger(ZeusDataServiceImpl.class);

	private final String strategySettingCollection = "StrategySetting";
	private final String positionCollection = "StrategyPos.";

	@Autowired
	private MongoDBService mongoDBService;
	private MongoDBClient defaultDBClient;

	@Value("${rt.client.dbname}")
	private String clientDBName;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.defaultDBClient = mongoDBService.getDefaultDBClient();
	}

	@Override
	public List<StrategySetting> loadStrategySettings() {
		List<StrategySetting> strategySettingList = new ArrayList<>();
		List<Document> documentList = defaultDBClient.find(clientDBName, strategySettingCollection);
		if (!(documentList == null) && !documentList.isEmpty()) {
			for (Document document : documentList) {
				String strategyID = document.getString("strategyID");
				if (StringUtils.isEmpty(strategyID)) {
					log.error("查出的记录解析出错,未找到策略ID,跳过!");
					continue;
				}
				StrategySetting strategySetting = coverDocumentToStrategySetting(strategyID, document);
				if (strategySetting != null) {
					strategySettingList.add(strategySetting);
				}
			}
		} else {
			log.warn("未能查出任何策略配置记录!");
		}
		return strategySettingList;
	}

	@Override
	public StrategySetting loadStrategySetting(String strategyID) {
		Document filter = new Document();
		filter.append("strategyID", strategyID);
		List<Document> documentList = defaultDBClient.find(clientDBName, strategySettingCollection, filter);
		if (!(documentList == null) && !documentList.isEmpty()) {

			if (documentList.size() > 1) {
				log.error("根据策略ID[" + strategyID + "]查出" + documentList.size() + "个配置记录,仅选取第一个");
			}
			Document document = documentList.get(0);

			StrategySetting strategySetting = coverDocumentToStrategySetting(strategyID, document);

			return strategySetting;

		} else {
			log.warn("根据策略ID[" + strategyID + "]未能查出配置记录!");
			return null;
		}
	}

	public StrategySetting coverDocumentToStrategySetting(String strategyID, Document document) {
		try {
			StrategySetting strategySetting = JSON.parseObject(document.toJson(), StrategySetting.class);
			if (strategySetting == null) {
				log.error("根据策略ID[" + strategyID + "]查出的记录解析出错,JSON工具解析返回null!");
				return null;
			}

			// 合成一些配置
			strategySetting.fixSetting();

			/////////////////////////////
			// 对配置文件进行基本检查
			////////////////////////////
			if (StringUtils.isEmpty(strategySetting.getStrategyID())) {
				log.error("根据策略ID[" + strategyID + "]查出的记录解析出错,未找到策略ID!");
				return null;
			}
			if (StringUtils.isEmpty(strategySetting.getStrategyName())) {
				log.error("根据策略ID[" + strategyID + "]查出的记录解析出错,未找到策略名称!");
				return null;
			}
			if (StringUtils.isEmpty(strategySetting.getTradingDay())) {
				log.error("根据策略ID[" + strategyID + "]查出的记录解析出错,未找到tradingDay!");
				return null;
			}
			if (strategySetting.getSubscribeReqList() == null || strategySetting.getSubscribeReqList().isEmpty()) {
				log.error("根据策略ID[" + strategyID + "]查出的记录解析出错,未找到订阅记录!");
				return null;
			}

			if (strategySetting.getContracts() == null || strategySetting.getContracts().isEmpty()) {
				log.warn("根据策略ID[" + strategyID + "]查出的记录解析出错,未找到contracts!");
				return null;
			}

			return strategySetting;
		} catch (Exception e) {
			log.error("根据策略ID[" + strategyID + "]查出的记录解析出错!", e);
			return null;
		}

	}

	@Override
	public void saveStrategySetting(StrategySetting strategySetting) {
		Document document = Document.parse(JSON.toJSONString(strategySetting));

		Document filter = new Document();
		filter.put("strategyID", strategySetting.getStrategyID());

		// 不使用upsert,避免有些字段在策略被删除后仍然在数据库中存在
		// defaultDBClient.upsert(clientDBName, strategySettingCollection, document,
		// filter);

		defaultDBClient.delete(clientDBName, strategySettingCollection, filter);
		defaultDBClient.insert(clientDBName, strategySettingCollection, document);
	}

	@Override
	public List<PositionDetail> loadStrategyPositionDetails(String tradingDay, String strategyID, String strategyName) {
		Document filter = new Document();
		filter.put("strategyID", strategyID);
		filter.put("tradingDay", tradingDay);

		List<Document> documentList = defaultDBClient.find(clientDBName, positionCollection + strategyName, filter);
		List<PositionDetail> positionDetailList = new ArrayList<>();
		for (Document document : documentList) {
			PositionDetail positionDetail = new PositionDetail();
			try {
				MongoDBUtil.documentToBean(document, positionDetail);
				positionDetailList.add(positionDetail);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				log.error("查询持仓数据转换发生错误,Document-[{}]", document.toJson(), e);
			}
		}
		return positionDetailList;
	}

	@Override
	public void saveStrategyPositionDetail(PositionDetail positionDetail) {
		Document filter = new Document();
		filter.put("strategyID", positionDetail.getStrategyID());
		filter.put("rtSymbol", positionDetail.getRtSymbol());
		filter.put("rtAccountID", positionDetail.getRtAccountID());
		filter.put("tradingDay", positionDetail.getTradingDay());

		try {
			Document document = new Document();

			document = MongoDBUtil.beanToDocument(positionDetail);
			String strategyName = positionDetail.getStrategyName();
			defaultDBClient.upsert(clientDBName, positionCollection + strategyName, document, filter);
		} catch (Exception e) {
			log.error("保存持仓数据转换发生错误", e);
		}

	}

	@Override
	public List<Bar> loadBarDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol) {
		return mongoDBService.loadBarDataList(startDateTime, endDateTime, rtSymbol);
	}

	@Override
	public List<Tick> loadTickDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol) {
		return mongoDBService.loadTickDataList(startDateTime, endDateTime, rtSymbol);
	}

	@Override
	public List<Tick> loadTickDataListFromDailyDB(String rtSymbol) {
		return mongoDBService.loadTickDataListFromDailyDB(rtSymbol);
	}

}
