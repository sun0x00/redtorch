package xyz.redtorch.web.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.core.zeus.ZeusEngineService;
import xyz.redtorch.core.zeus.strategy.Strategy;
import xyz.redtorch.web.service.ZeusEngineWebService;

@Service
public class ZeusEngineWebServiceImpl implements ZeusEngineWebService {
	@Autowired
	private ZeusEngineService zeusEngineService;
	
	@Override
	public void loadStrategy() {
		zeusEngineService.loadStartegy();
	}

	@Override
	public List<Map<String, Object>> getStrategyInfos() {
		List<Map<String, Object>> strategyInfos = new ArrayList<Map<String, Object>>();
		List<Strategy> startegyList = zeusEngineService.getStragetyList();
		for (Strategy strategy : startegyList) {
			Map<String, Object> strategyInfo = new HashMap<>();

			strategyInfo.put("strategyName", strategy.getName());
			strategyInfo.put("strategyID", strategy.getID());
			strategyInfo.put("initStatus", strategy.isInitStatus());
			strategyInfo.put("trading", strategy.isTrading());

			strategyInfo.put("paramMap", strategy.getStrategySetting().getParamMap());
			strategyInfo.put("varMap", strategy.getStrategySetting().getVarMap());
			strategyInfos.add(strategyInfo);
		}
		return strategyInfos;
	}

	@Override
	public void initStrategy(String strategyID) {
		zeusEngineService.initStrategy(strategyID);
	}

	@Override
	public void sartStrategy(String strategyID) {
		zeusEngineService.startStrategy(strategyID);

	}

	@Override
	public void stopStrategy(String strategyID) {
		zeusEngineService.stopStrategy(strategyID);

	}

	@Override
	public void initAllStrategy() {
		zeusEngineService.initAllStrategy();
	}

	@Override
	public void startAllStrategy() {
		zeusEngineService.startAllStrategy();
	}

	@Override
	public void stopAllStrategy() {
		zeusEngineService.stopAllStrategy();
	}

	@Override
	public void reloadStrategy(String strategyID) {
		zeusEngineService.unloadStrategy(strategyID);
		zeusEngineService.loadStartegy(strategyID);
	}
}
