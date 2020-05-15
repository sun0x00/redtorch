package xyz.redtorch.node.master.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.service.MarketDataService;
import xyz.redtorch.common.util.bar.BarGenerator;
import xyz.redtorch.common.util.bar.CommonBarCallBack;
import xyz.redtorch.common.util.bar.XMinBarGenerator;
import xyz.redtorch.node.master.dao.MarketDataRecordingDao;
import xyz.redtorch.node.master.po.ContractPo;
import xyz.redtorch.node.master.service.MarketDataRecordingService;
import xyz.redtorch.node.master.service.MasterTradeCachesService;
import xyz.redtorch.node.master.util.BeanUtils;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

@Service
public class MarketDataRecordingServiceImpl implements MarketDataRecordingService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(MarketDataRecordingServiceImpl.class);

	@Autowired
	private MarketDataRecordingDao marketDataRecordingDao;

	@Autowired
	private MarketDataService marketDataService;

	@Autowired
	private MasterTradeCachesService masterTradeCachesService;

	@Value("rt.node.master.operatorId")
	private String masterOperatorId;

	private Map<String, ContractField> subscribedContractFieldMap = new HashMap<>();

	private ConcurrentLinkedQueue<TickField> tickQueue = new ConcurrentLinkedQueue<>();

	// X分钟Bar生成器,由构造方法xMin参数决定是否实例化生效
	private Map<String, XMinBarGenerator> xMin3BarGeneratorMap = new HashMap<>();
	private Map<String, XMinBarGenerator> xMin5BarGeneratorMap = new HashMap<>();
	private Map<String, XMinBarGenerator> xMin15BarGeneratorMap = new HashMap<>();
	private Map<String, BarGenerator> barGeneratorMap = new HashMap<>();
	
	private List<TickField> tickInsertList = new ArrayList<>();
	private List<BarField> bar1MinInsertList = new ArrayList<>();
	private List<BarField> bar3MinInsertList = new ArrayList<>();
	private List<BarField> bar5MinInsertList = new ArrayList<>();
	private List<BarField> bar15MinInsertList = new ArrayList<>();
	
	private Long tickLastInsertTimestamp = System.currentTimeMillis();
	private Long bar1MinLastInsertTimestamp = System.currentTimeMillis();
	private Long bar3MinLastInsertTimestamp = System.currentTimeMillis();
	private Long bar5MinLastInsertTimestamp = System.currentTimeMillis();
	private Long bar15MinLastInsertTimestamp = System.currentTimeMillis();

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

		if (StringUtils.isNotBlank(unifiedSymbol)) {
			ContractField contract = masterTradeCachesService.queryContractByUnifiedSymbol(masterOperatorId, unifiedSymbol);
			if (contract == null) {
				logger.error("根据统一标识新增合约错误,未找到合约");
				throw new IllegalArgumentException("根据统一标识新增合约错误,未找到合约");
			}

			ContractPo contractPo = BeanUtils.contractFieldToContractPo(contract);

			marketDataRecordingDao.upsertContractByUnifiedSymbol(contractPo);
		} else {
			logger.error("根根据统一标识新增合约错误,参数unifiedSymbol缺失");
			throw new IllegalArgumentException("根据统一标识新增合约错误,参数unifiedSymbol缺失");
		}

	}

	@Override
	public List<ContractField> getSubscribedContractFieldList() {
		return new ArrayList<>(subscribedContractFieldMap.values());
	}

	@Override
	public Set<String> getSubscribedUnifiedSymbolSet() {
		return subscribedContractFieldMap.keySet();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {

						List<ContractPo> contractPoList = getContractList();

						Map<String, ContractField> newSubscribedContractFieldMap = new HashMap<>();

						if (contractPoList != null && !contractPoList.isEmpty()) {
							for (ContractPo contractPo : contractPoList) {

								ContractField contractField = masterTradeCachesService.queryContractByUnifiedSymbol(masterOperatorId, contractPo.getUnifiedSymbol());
								if (contractField != null) {
									newSubscribedContractFieldMap.put(contractField.getUnifiedSymbol(), contractField);
								}
							}
						}
						subscribedContractFieldMap = newSubscribedContractFieldMap;

					} catch (Exception e) {
						logger.error("行情记录同步错误");
					}

					try {
						Thread.sleep(5 * 1000);
					} catch (InterruptedException e) {
						// NOP
					}

				}

			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {

				while (!Thread.currentThread().isInterrupted()) {
					TickField tick = tickQueue.poll();

					if (tick != null) {
						// 有可能会过慢

						tickInsertList.add(tick);
						
						String bgKey = tick.getUnifiedSymbol();
						// 基于合约+网关的onBar和onMinBar,使用这个key会多次触发同一策略下同一品种的相同时间bar的事件
						// String bgKey = tick.getUniqueSymbol()+tick.getGatewayID();
						BarGenerator barGenerator;
						if (barGeneratorMap.containsKey(bgKey)) {
							barGenerator = barGeneratorMap.get(bgKey);
						} else {
							barGenerator = new BarGenerator(new CommonBarCallBack() {
								@Override
								public void call(BarField bar) {

									bar1MinInsertList.add(bar);

									XMinBarGenerator xMin3BarGenerator;
									if (xMin3BarGeneratorMap.containsKey(bgKey)) {
										xMin3BarGenerator = xMin3BarGeneratorMap.get(bgKey);
									} else {
										xMin3BarGenerator = new XMinBarGenerator(3, new CommonBarCallBack() {
											@Override
											public void call(BarField bar) {
												bar3MinInsertList.add(bar);
											}
										});
										xMin3BarGeneratorMap.put(bgKey, xMin3BarGenerator);
									}
									xMin3BarGenerator.updateBar(bar);

									XMinBarGenerator xMin5BarGenerator;
									if (xMin5BarGeneratorMap.containsKey(bgKey)) {
										xMin5BarGenerator = xMin5BarGeneratorMap.get(bgKey);
									} else {
										xMin5BarGenerator = new XMinBarGenerator(5, new CommonBarCallBack() {
											@Override
											public void call(BarField bar) {
												bar5MinInsertList.add(bar);
											}
										});
										xMin5BarGeneratorMap.put(bgKey, xMin5BarGenerator);
									}
									xMin5BarGenerator.updateBar(bar);

									XMinBarGenerator xMin15BarGenerator;
									if (xMin15BarGeneratorMap.containsKey(bgKey)) {
										xMin15BarGenerator = xMin15BarGeneratorMap.get(bgKey);
									} else {
										xMin15BarGenerator = new XMinBarGenerator(15, new CommonBarCallBack() {
											@Override
											public void call(BarField bar) {
												bar15MinInsertList.add(bar);
											}
										});
										xMin15BarGeneratorMap.put(bgKey, xMin15BarGenerator);
									}
									xMin15BarGenerator.updateBar(bar);

								}
							});
							barGeneratorMap.put(bgKey, barGenerator);
						}

						// 更新1分钟bar生成器
						barGenerator.updateTick(tick);
						
						long currentTimestamp = System.currentTimeMillis();
						
						if(tickInsertList.size()>100 || (currentTimestamp-tickLastInsertTimestamp>1000&& !tickInsertList.isEmpty())) {
							marketDataService.upsertTickListToTodayDB(tickInsertList);
							tickInsertList = new ArrayList<>();
							tickLastInsertTimestamp = currentTimestamp;
						}
						
						if(bar1MinInsertList.size()>100 || (currentTimestamp-bar1MinLastInsertTimestamp>1000&& !bar1MinInsertList.isEmpty())) {
							marketDataService.upsertBar1MinListToTodayDB(bar1MinInsertList);
							bar1MinInsertList = new ArrayList<>();
							bar1MinLastInsertTimestamp = currentTimestamp;
						}
						
						if(bar3MinInsertList.size()>100 || (currentTimestamp-bar3MinLastInsertTimestamp>1000&& !bar3MinInsertList.isEmpty())) {
							marketDataService.upsertBar3MinListToTodayDB(bar3MinInsertList);
							bar3MinInsertList = new ArrayList<>();
							bar3MinLastInsertTimestamp = currentTimestamp;
						}
						
						if(bar5MinInsertList.size()>100 || (currentTimestamp-bar5MinLastInsertTimestamp>1000&& !bar5MinInsertList.isEmpty())) {
							marketDataService.upsertBar5MinListToTodayDB(bar5MinInsertList);
							bar5MinInsertList = new ArrayList<>();
							bar5MinLastInsertTimestamp = currentTimestamp;
						}
						
						if(bar15MinInsertList.size()>100 || (currentTimestamp-bar15MinLastInsertTimestamp>1000&& !bar15MinInsertList.isEmpty())) {
							marketDataService.upsertBar15MinListToTodayDB(bar15MinInsertList);
							bar15MinInsertList = new ArrayList<>();
							bar15MinLastInsertTimestamp = currentTimestamp;
						}

					}else {
						
						
						long currentTimestamp = System.currentTimeMillis();
						
						if(!tickInsertList.isEmpty()) {
							marketDataService.upsertTickListToTodayDB(tickInsertList);
							tickInsertList = new ArrayList<>();
							tickLastInsertTimestamp = currentTimestamp;
						}
						
						if(bar1MinInsertList.isEmpty()) {
							marketDataService.upsertBar1MinListToTodayDB(bar1MinInsertList);
							bar1MinInsertList = new ArrayList<>();
							bar1MinLastInsertTimestamp = currentTimestamp;
						}
						
						if(bar3MinInsertList.isEmpty()) {
							marketDataService.upsertBar3MinListToTodayDB(bar3MinInsertList);
							bar3MinInsertList = new ArrayList<>();
							bar3MinLastInsertTimestamp = currentTimestamp;
						}
						
						if(bar5MinInsertList.isEmpty()) {
							marketDataService.upsertBar5MinListToTodayDB(bar5MinInsertList);
							bar5MinInsertList = new ArrayList<>();
							bar5MinLastInsertTimestamp = currentTimestamp;
						}
						
						if(bar15MinInsertList.isEmpty()) {
							marketDataService.upsertBar15MinListToTodayDB(bar15MinInsertList);
							bar15MinInsertList = new ArrayList<>();
							bar15MinLastInsertTimestamp = currentTimestamp;
						}
						
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							// NOP
						}
					}

				}

			}
		}).start();

	}

	@Override
	public void processTick(TickField tick) {
		if (tick != null) {
			tickQueue.add(tick);
		}
	}

}
