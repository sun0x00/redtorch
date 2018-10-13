package xyz.redtorch.core.zeus.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.service.CoreEngineService;
import xyz.redtorch.core.zeus.ZeusTradingBaseService;
import xyz.redtorch.core.zeus.entity.StrategyProcessReport;

@Service
public class ZeusTradingBaseServiceImpl implements ZeusTradingBaseService, InitializingBean {

	private final static Logger log = LoggerFactory.getLogger(ZeusTradingBaseServiceImpl.class);

	private Map<String, String> originalOrderIDMap = new HashMap<>();
	private Map<String, String> rtOrderIDMap = new HashMap<>();
	private Map<String, StrategyProcessReport> strategyProcessReportMap = new ConcurrentHashMap<>();

	// 使用无大小限制的线程池,线程空闲60s会被释放
	private ExecutorService executor = Executors.newCachedThreadPool();

	@Autowired
	private CoreEngineService coreEngineService;

	@Override
	public void afterPropertiesSet() throws Exception {
		executor.execute(new ReportExpirationCheckTask());
	}

	@Override
	public void registerOriginalOrderID(String rtOrderID, String originalOrderID) {
		if (StringUtils.isNotBlank(rtOrderID) && StringUtils.isNotBlank(originalOrderID)) {
			originalOrderIDMap.put(rtOrderID, originalOrderID);
			rtOrderIDMap.put(originalOrderID, rtOrderID);
		}
	}

	@Override
	public String getOriginalOrderID(String rtOrderID) {
		return originalOrderIDMap.get(rtOrderID);
	}

	@Override
	public String getRtOrderID(String originalOrderID) {
		return rtOrderIDMap.get(originalOrderID);
	}

	@Override
	public void updateReport(StrategyProcessReport strategyProcessReport) {
		strategyProcessReportMap.put(strategyProcessReport.getStrategySetting().getStrategyID(), strategyProcessReport);
	}

	@Override
	public boolean duplicationCheck(String strategyID) {
		log.info("策略重复启动检查,策略ID:" + strategyID);
		// 强制等待
		try {
			Thread.sleep(6 * 1000);
		} catch (InterruptedException e) {
			// nop
		}
		if (strategyProcessReportMap.containsKey(strategyID)) {
			log.info("策略重复启动检查发现重复,策略ID:" + strategyID);
			return true;
		} else {
			log.info("策略重复启动检查未发现重复,策略ID:" + strategyID);
			return false;
		}

	}

	private class ReportExpirationCheckTask implements Runnable {
		@Override
		public void run() {

			log.info("策略报告过期检查已启动");

			while (!Thread.currentThread().isInterrupted()) {
				Iterator<Map.Entry<String, StrategyProcessReport>> it = strategyProcessReportMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, StrategyProcessReport> entry = it.next();
					StrategyProcessReport strategyProcessReport = entry.getValue();
					if (System.currentTimeMillis() - strategyProcessReport.getReportTimestamp() > 4 * 1000) {
						log.info("策略报告过期检查程序移除过期报告,策略ID:" + entry.getKey());
						for (SubscribeReq subscribeReq : strategyProcessReport.getSubscribeReqSet()) {
							coreEngineService.unsubscribe(subscribeReq.getRtSymbol(), subscribeReq.getGatewayID(),
									strategyProcessReport.getStrategySetting().getStrategyID());
						}
						it.remove();// 使用迭代器的remove()方法删除元素
					}
				}
				// 定时检查
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// nop
				}
			}
		}
	}

	@Override
	public Map<String, StrategyProcessReport> getReportMap() {
		return strategyProcessReportMap;
	}
}
