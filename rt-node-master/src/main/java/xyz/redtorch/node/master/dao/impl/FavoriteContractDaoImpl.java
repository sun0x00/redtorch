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
import xyz.redtorch.node.master.dao.FavoriteContractDao;
import xyz.redtorch.node.master.po.ContractPo;

@Service
public class FavoriteContractDaoImpl implements FavoriteContractDao, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(FavoriteContractDaoImpl.class);

	private static final String FAVORITE_CONTRACT_COLLECTION_NAME = "favorite_contract_collection";

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
		List<Document> documentList = managementDBClient.find(managementDBName, FAVORITE_CONTRACT_COLLECTION_NAME);
		List<ContractPo> contractList = new ArrayList<>();

		for (Document document : documentList) {
			try {
				ContractPo contract = JSON.parseObject(JSON.toJSONString(document), ContractPo.class);
				contractList.add(contract);
			} catch (Exception e) {
				logger.error("查询合约列表,数据转换发生错误,Document-{}", document.toJson(), e);
			}
		}

		return contractList;
	}

	@Override
	public List<ContractPo> queryContractListByUsername(String username) {
		if (StringUtils.isBlank(username)) {
			logger.error("根据用户名查询常用合约列表错误,参数username缺失");
			throw new IllegalArgumentException("根据用户名查询常用合约列表错误,参数username缺失");
		}

		Document filter = new Document();
		filter.append("username", username);

		List<Document> documentList = managementDBClient.find(managementDBName, FAVORITE_CONTRACT_COLLECTION_NAME, filter);
		List<ContractPo> contractList = new ArrayList<>();

		for (Document document : documentList) {
			try {
				ContractPo contract = JSON.parseObject(JSON.toJSONString(document), ContractPo.class);
				contractList.add(contract);
			} catch (Exception e) {
				logger.error("根据用户名查询常用合约列表错误,数据转换发生错误,Document-{}", document.toJson(), e);
			}
		}

		return contractList;
	}

	@Override
	public void upsertContractByUsernameAndUnifiedSymbol(String username, ContractPo contract) {
		if (StringUtils.isBlank(username)) {
			logger.error("根据用户名和合约统一标识更新或保存合约错误,参数username缺失");
			throw new IllegalArgumentException("根据用户名和合约统一标识更新或保存合约错误,参数username缺失");
		}
		if (contract == null) {
			logger.error("根据用户名和合约统一标识更新或保存合约错误,参数contract缺失");
			throw new IllegalArgumentException("根据用户名和合约统一标识更新或保存合约错误,参数contract缺失");
		}
		if (StringUtils.isBlank(contract.getUnifiedSymbol())) {
			logger.error("根据用户名和合约统一标识更新或保存合约错误,参数unifiedSymbol缺失");
			throw new IllegalArgumentException("根据用户名和合约统一标识更新或保存合约错误,参数unifiedSymbol缺失");
		}

		try {
			Document document = Document.parse(JSON.toJSONString(contract));
			document.append("username", username);
			Document filter = new Document();
			filter.append("username", username);
			filter.append("unifiedSymbol", contract.getUnifiedSymbol());
			managementDBClient.upsert(managementDBName, FAVORITE_CONTRACT_COLLECTION_NAME, document, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据用户名和合约统一标识更新或保存合约错误", e);
		}

	}

	@Override
	public void deleteContractByUsername(String username) {
		if (StringUtils.isBlank(username)) {
			logger.error("根据用户名删除合约错误,参数username缺失");
			throw new IllegalArgumentException("根据用户名删除合约错误,参数username缺失");
		}

		try {
			Document filter = new Document();
			filter.append("username", username);
			managementDBClient.delete(managementDBName, FAVORITE_CONTRACT_COLLECTION_NAME, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据用户名删除合约错误,用户名:{}", username, e);
		}
	}

	@Override
	public void deleteContractByUsernameAndUnifiedSymbol(String username, String unifiedSymbol) {
		if (StringUtils.isBlank(unifiedSymbol)) {
			logger.error("根据用户名和合约统一标识删除合约错误,参数unifiedSymbol缺失");
			throw new IllegalArgumentException("根据用户名和合约统一标识删除合约错误,参数unifiedSymbol缺失");
		}

		if (StringUtils.isBlank(username)) {
			logger.error("根据用户名和合约统一标识删除合约错误,参数username缺失");
			throw new IllegalArgumentException("根据用户名和合约统一标识删除合约错误,参数username缺失");
		}

		try {
			Document filter = new Document();
			filter.append("username", username);
			filter.append("unifiedSymbol", unifiedSymbol);
			managementDBClient.delete(managementDBName, FAVORITE_CONTRACT_COLLECTION_NAME, filter);
		} catch (IllegalArgumentException e) {
			logger.error("根据用户名和合约统一标识删除合约错误,用户名:{}", username, e);
		}
	}
}
