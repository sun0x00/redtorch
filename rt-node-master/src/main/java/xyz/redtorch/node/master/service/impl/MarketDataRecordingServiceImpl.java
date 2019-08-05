package xyz.redtorch.node.master.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.node.master.dao.MarketDataRecordingDao;
import xyz.redtorch.node.master.po.ContractPo;
import xyz.redtorch.node.master.service.MarketDataRecordingService;

@Service
public class MarketDataRecordingServiceImpl implements MarketDataRecordingService {

	private Logger logger = LoggerFactory.getLogger(MarketDataRecordingServiceImpl.class);

	@Autowired
	private MarketDataRecordingDao marketDataRecordingDao;

	@Override
	public List<ContractPo> getContractList() {
		return marketDataRecordingDao.queryContractList();
	}

	@Override
	public void deleteContractByUnifiedSymbol(String unifiedSymbol) {
		if (StringUtils.isBlank(unifiedSymbol)) {
			logger.error("根据统一标识删除合约错误，参数unifiedSymbol缺失");
			throw new IllegalArgumentException("根据统一标识删除合约错误，参数unifiedSymbol缺失");
		}
		marketDataRecordingDao.deleteContractByUnifiedSymbol(unifiedSymbol);

	}

	@Override
	public void addContractByUnifiedSymbol(ContractPo contract) {
		if (contract == null) {
			logger.error("根据统一标识新增合约错误，参数contract缺失");
			throw new IllegalArgumentException("根据统一标识新增合约错误，参数contract缺失");
		}
		if (StringUtils.isBlank(contract.getUnifiedSymbol())) {
			logger.error("根据统一标识新增合约错误，参数缺失");
			throw new IllegalArgumentException("根据统一标识新增合约错误，参数unifiedSymbol缺失");
		}
		marketDataRecordingDao.upsertContractByUnifiedSymbol(contract);

	}

}
