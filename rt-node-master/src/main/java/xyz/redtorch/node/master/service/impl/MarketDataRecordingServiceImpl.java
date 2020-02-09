package xyz.redtorch.node.master.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.node.master.dao.MarketDataRecordingDao;
import xyz.redtorch.node.master.po.ContractPo;
import xyz.redtorch.node.master.service.MarketDataRecordingService;
import xyz.redtorch.node.master.service.MasterTradeCachesService;
import xyz.redtorch.node.master.util.BeanUtils;
import xyz.redtorch.pb.CoreField.ContractField;

@Service
public class MarketDataRecordingServiceImpl implements MarketDataRecordingService {

	private static final Logger logger = LoggerFactory.getLogger(MarketDataRecordingServiceImpl.class);

	@Autowired
	private MarketDataRecordingDao marketDataRecordingDao;
	
	@Autowired
	private MasterTradeCachesService masterTradeCachesService;
	
	@Value("rt.node.master.operatorId")
	private String masterOperatorId;

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
	public void addContractByUnifiedSymbol(String unifiedSymbol) {
		
		if(StringUtils.isNotBlank(unifiedSymbol)) {
			ContractField contract = masterTradeCachesService.queryContractByUnifiedSymbol(masterOperatorId, unifiedSymbol);
			if(contract==null) {
				logger.error("根据统一标识新增合约错误,未找到合约");
				throw new IllegalArgumentException("根据统一标识新增合约错误,未找到合约");
			}
			
			ContractPo contractPo = BeanUtils.contractFieldToContractPo(contract);

			marketDataRecordingDao.upsertContractByUnifiedSymbol(contractPo);
		}else {
			logger.error("根根据统一标识新增合约错误,参数unifiedSymbol缺失");
			throw new IllegalArgumentException("根据统一标识新增合约错误,参数unifiedSymbol缺失");
		}
		
	}

}
