package xyz.redtorch.web.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.core.zeus.ZeusDataService;
import xyz.redtorch.core.zeus.ZeusMmapService;
import xyz.redtorch.core.zeus.ZeusTradingBaseService;
import xyz.redtorch.core.zeus.entity.StrategyProcessReport;
import xyz.redtorch.core.zeus.strategy.StrategySetting;
import xyz.redtorch.web.service.ZeusEngineWebService;

@Service
public class ZeusEngineWebServiceImpl implements ZeusEngineWebService {
	@Autowired
	private ZeusTradingBaseService zeusTradingBaseService;
	@Autowired
	private ZeusMmapService zeusMmapService;
	@Autowired
	private ZeusDataService zeusDataService;

	@Override
	public List<Map<String, Object>> getStrategyInfos() {

		List<Map<String, Object>> strategyInfos = new ArrayList<Map<String, Object>>();
		Map<String, StrategyProcessReport> reportMap = zeusTradingBaseService.getReportMap();
		List<StrategySetting> strategySettingList = zeusDataService.loadStrategySettings();

		for (StrategySetting strategySetting : strategySettingList) {
			Map<String, Object> strategyInfo = new HashMap<>();

			strategyInfo.put("strategyName", strategySetting.getStrategyName());
			strategyInfo.put("strategyID", strategySetting.getStrategyID());
			StrategyProcessReport report = reportMap.get(strategySetting.getStrategyID());
			if (report != null) {
				strategyInfo.put("isLoaded", true);
				strategyInfo.put("initStatus", report.isInitStatus());
				strategyInfo.put("trading", report.isTrading());

				strategyInfo.put("paramMap", report.getStrategySetting().getParamMap());
				strategyInfo.put("varMap", report.getStrategySetting().getVarMap());
				strategyInfos.add(strategyInfo);
			} else {
				strategyInfo.put("isLoaded", false);
				strategyInfo.put("initStatus", false);
				strategyInfo.put("trading", false);

				strategyInfo.put("paramMap", strategySetting.getParamMap());
				strategyInfo.put("varMap", strategySetting.getVarMap());
				strategyInfos.add(strategyInfo);
			}

		}
		return strategyInfos;
	}

	@Override
	public void initStrategy(String strategyID) {
		zeusMmapService.initStrategy(strategyID);
	}

	@Override
	public void sartStrategy(String strategyID) {
		zeusMmapService.startStrategy(strategyID);

	}

	@Override
	public void stopStrategy(String strategyID) {
		zeusMmapService.stopStrategy(strategyID);
	}

	@Override
	public void reloadStrategy(String strategyID) {
		zeusMmapService.reloadStrategy(strategyID);

	}

	@Override
	public void initAllStrategy() {
		zeusMmapService.initAllStrategy();
	}

	@Override
	public void startAllStrategy() {
		zeusMmapService.startAllStrategy();
	}

	@Override
	public void stopAllStrategy() {
		zeusMmapService.stopAllStrategy();
	}

	@Override
	public void reloadAllStrategy() {
		zeusMmapService.reloadAllStrategy();
	}
}
