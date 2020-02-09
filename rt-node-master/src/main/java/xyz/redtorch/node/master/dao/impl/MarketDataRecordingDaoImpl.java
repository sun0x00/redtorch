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
import xyz.redtorch.node.master.dao.MarketDataRecordingDao;
import xyz.redtorch.node.master.po.ContractPo;

@Service
public class MarketDataRecordingDaoImpl implements MarketDataRecordingDao, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(MarketDataRecordingDaoImpl.class);

	private static final String MARKET_DATA_RECORDING_COLLECTION_NAME = "market_data_recording_collection";

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
	public List<ContractPo> queryContractList() {
		List<Document> documentList = managementDBClient.find(managementDBName, MARKET_DATA_RECORDING_COLLECTION_NAME);
		List<ContractPo> contractList = new ArrayList<>();

		for (Document document : documentList) {
			try {
				ContractPo contract = JSON.parseObject(JSON.toJSONString(document), ContractPo.class);
				contractList.add(contract);
			} catch (Exception e) {
				logger.error("查询网关列表,数据转换发生错误,Document-{}", document.toJson(), e);
			}
		}
		return contractList;
	}

	@Override
	public void upsertContractByUnifiedSymbol(ContractPo contract) {
		if (contract == null) {
			logger.error("根据统一合约标识更新或保存合约错误,参数contract缺失");
			throw new IllegalArgumentException("根据统一合约标识更新或保存合约错误,参数contract缺失");
		}
		if (StringUtils.isBlank(contract.getUnifiedSymbol())) {
			logger.error("根据统一合约标识更新或保存合约错误,参数unifiedSymbol缺失");
			throw new IllegalArgumentException("根据统一合约标识更新或保存合约错误,参数unifiedSymbol缺失");
		}

		try {
			Document document = Document.parse(JSON.toJSONString(contract));
			Document filter = new Document();
			filter.append("unifiedSymbol", contract.getUnifiedSymbol());
			managementDBClient.upsert(managementDBName, MARKET_DATA_RECORDING_COLLECTION_NAME, document, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据统一合约标识更新或保存合约错误,统一合约标识:{}", contract.getUnifiedSymbol(), e);
		}

	}

	@Override
	public void deleteContractByUnifiedSymbol(String unifiedSymbol) {
		if (StringUtils.isBlank(unifiedSymbol)) {
			logger.error("根据统一合约标识删除合约错误,参数unifiedSymbol缺失");
			throw new IllegalArgumentException("根据统一合约标识删除合约错误,参数unifiedSymbol缺失");
		}

		try {
			Document filter = new Document();
			filter.append("unifiedSymbol", unifiedSymbol);
			managementDBClient.delete(managementDBName, MARKET_DATA_RECORDING_COLLECTION_NAME, filter);
		} catch (IllegalArgumentException e) {
			logger.error("\"根据统一合约标识删除合约错误,统一合约标识:{}", unifiedSymbol, e);
		}
	}

}
