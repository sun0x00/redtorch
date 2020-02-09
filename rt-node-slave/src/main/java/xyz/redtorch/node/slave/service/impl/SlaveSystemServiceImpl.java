package xyz.redtorch.node.slave.service.impl;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.common.service.FastEventService;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.gateway.GatewayApi;
import xyz.redtorch.node.slave.rpc.service.RpcClientApiService;
import xyz.redtorch.node.slave.service.SlaveSystemService;
import xyz.redtorch.node.slave.service.SlaveTradeCachesService;
import xyz.redtorch.pb.CoreEnum.ConnectStatusEnum;
import xyz.redtorch.pb.CoreEnum.GatewayTypeEnum;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.GatewaySettingField;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataRsp;

@Service
public class SlaveSystemServiceImpl implements SlaveSystemService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(SlaveSystemServiceImpl.class);

	@Autowired
	private SlaveTradeCachesService slaveTradeCachesService;
	@Autowired
	private FastEventService fastEventService;

	@Autowired
	private RpcClientApiService rpcClientApiService;

	private Map<String, GatewayApi> gatewayApiMap = new HashMap<>(100);

	private Set<String> authErrorGatewayVersionSet = new HashSet<>();

	private Map<String, ContractField> subscribedContractMap = new HashMap<>();

	private ExecutorService executor = Executors.newCachedThreadPool();

	@Value("${rt.rpc.client.node-id}")
	private Integer nodeId;

	@Value("${rt.node.slave.operatorId}")
	private String slaveOperatorId;

	@Value("${rt.node.slave.sync-runtime-data-wait-seconds}")
	private Long syncRuntimeDataWaitSeconds;

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("在xyz.redtorch包下扫描到了以下GatewayApi实现类,{}", JSON.toJSONString(this.scanGatewayApiImpl()));
		executor.execute(new SyncSlaveNodeRuntimeDataTask(syncRuntimeDataWaitSeconds));
	}

	private List<String> scanGatewayApiImpl() {
		List<String> gatewayClassNameList = new ArrayList<>();
		Set<Class<?>> classes = CommonUtils.getClasses("xyz.redtorch");
		if (classes == null) {
			logger.warn("未能在包xyz.redtorch下扫描到任何类");
		} else {
			// 寻找Gateway的实现类,不包含抽象类
			Set<Class<?>> filteredClasses = CommonUtils.getImplementsByInterface(GatewayApi.class, classes, false);
			if (filteredClasses.isEmpty()) {
				logger.warn("未能在包xyz.redtorch下扫描到任何Gateway网关的实现类");
			} else {
				for (Class<?> clazz : filteredClasses) {
					String className = clazz.getName();
					gatewayClassNameList.add(className);
				}
			}
		}
		return gatewayClassNameList;
	}

	@Override
	public List<String> getConnectedGatewayIdList() {
		List<String> resultList = new ArrayList<String>();

		for (Map.Entry<String, GatewayApi> entry : gatewayApiMap.entrySet()) {
			String gatewayId = entry.getKey();
			GatewayApi gatewayApi = entry.getValue();
			if (gatewayApi.isConnected()) {
				resultList.add(gatewayId);
			}
		}

		return resultList;
	}

	@Override
	public synchronized void connectGatewayApi(GatewaySettingField gatewaySetting) {

		String authErrorFlagKey = gatewaySetting.getGatewayId() + "@" + gatewaySetting.getVersion();
		if (authErrorGatewayVersionSet.contains(authErrorFlagKey)) {
			logger.info("拒绝连接网关,配置可能存在认证错误,网关ID:{},网关名称:{}", gatewaySetting.getGatewayId(), gatewaySetting.getGatewayName());
			return;
		}

		String gatewayId = gatewaySetting.getGatewayId();
		String gatewayName = gatewaySetting.getGatewayName();
		logger.info("连接网关,网关ID:{},网关名称:{}", gatewayId, gatewayName);
		if (gatewayApiMap.containsKey(gatewayId)) {
			logger.warn("网关已在缓存中存在,网关ID:{},网关名称:{}", gatewayId, gatewayName);
			GatewayApi gatewayApi = gatewayApiMap.get(gatewaySetting.getGatewayId());
			if (gatewayApi.isConnected()) {
				logger.error("连接网关错误,网关ID:{},网关名称:{},缓存中的网关处于连接状态", gatewayId, gatewayName);
				return;
			} else {
				logger.warn("缓存中的网关已经断开,再次调用网关断开并删除,网关ID:{},网关名称:{}", gatewayId, gatewayName);
				disconnectGatewayApi(gatewayId);
			}
		}

		String gatewayClassName = gatewaySetting.getImplementClassName();
		try {
			logger.info("使用反射创建网关实例,网关ID:{},网关名称:{}", gatewayId, gatewayName);
			Class<?> clazz = Class.forName(gatewayClassName);
			Constructor<?> c = clazz.getConstructor(FastEventService.class, GatewaySettingField.class);
			GatewayApi gatewayApi = (GatewayApi) c.newInstance(fastEventService, gatewaySetting);
			logger.info("调用网关连接,网关ID:{},网关名称:{}", gatewayId, gatewayName);
			gatewayApi.connect();
			logger.info("重新订阅合约,网关ID:{},网关名称:{}", gatewayId, gatewayName);
			// 重新订阅之前的合约
			for (ContractField contract : subscribedContractMap.values()) {
				gatewayApi.subscribe(contract);
			}

			gatewayApiMap.put(gatewayId, gatewayApi);
		} catch (Exception e) {
			logger.error("连接网关错误,创建网关实例发生异常,网关ID:{},Java实现类:{}", gatewayId, gatewayClassName, e);
		}
		logger.warn("连接网关完成,网关ID:{},网关名称:{}", gatewayId, gatewayName);
	}

	@Override
	public synchronized void disconnectGatewayApi(String gatewayId) {
		GatewayApi gatewayApi = getGatewayApi(gatewayId);
		if (gatewayApi != null) {
			gatewayApi.disconnect();
			gatewayApiMap.remove(gatewayId);
			slaveTradeCachesService.removeAllCachesByGatewayId(gatewayId);
		} else {
			logger.error("网关{}不存在,无法断开!", gatewayId);
		}
	}

	@Override
	public GatewayApi getGatewayApi(String gatewayId) {
		if (StringUtils.isEmpty(gatewayId)) {
			logger.error("获取网关,gatewayId不允许使用空字符串或null");
			return null;
		}
		return gatewayApiMap.get(gatewayId);
	}

	@Override
	public List<GatewayApi> getGatewayApiList() {
		return new ArrayList<>(gatewayApiMap.values());
	}

	class SyncSlaveNodeRuntimeDataTask implements Runnable {

		private long waitSeconds = 8;

		public SyncSlaveNodeRuntimeDataTask(long waitSeconds) {
			if (waitSeconds >= 8 && waitSeconds <= 300) {
				this.waitSeconds = waitSeconds;
			} else {
				logger.warn("使用默认等待时间配置");
			}

		}

		@Override
		public void run() {
			long startTime = System.currentTimeMillis();
			while (true) {
				if ((System.currentTimeMillis() - startTime) < waitSeconds * 1000) {
					try {
						Thread.sleep(50);
						continue;
					} catch (InterruptedException e) {
						logger.error("定时同步节点数据等待检测到线程中断", e);
						break;
					}
				}

				try {
					List<GatewayField> gatewayList = new ArrayList<>();

					for (GatewayApi gatewayApi : getGatewayApiList()) {
						String gatewayId = gatewayApi.getGatewayId();

						ConnectStatusEnum gatewayStatus = ConnectStatusEnum.CS_Disconnected;

						if (gatewayApi.isConnected()) {
							gatewayStatus = ConnectStatusEnum.CS_Connected;
						}
						boolean authErrorFlag = false;
						if (gatewayApi.getAuthErrorFlag()) {
							String authErrorFlagKey = gatewayApi.getGatewayId() + "@" + gatewayApi.getGatewaySetting().getVersion();
							authErrorGatewayVersionSet.add(authErrorFlagKey);
							authErrorFlag = true;
						}
						GatewayField gateway = GatewayField.newBuilder().setAuthErrorFlag(authErrorFlag).setGatewayId(gatewayId).setStatus(gatewayStatus).build();
						gatewayList.add(gateway);
					}

					RpcSyncSlaveNodeRuntimeDataRsp rpcSyncSlaveNodeRuntimeDataRsp = rpcClientApiService.syncSlaveNodeRuntimeData(gatewayList);

					if (rpcSyncSlaveNodeRuntimeDataRsp != null) {
						List<ContractField> newSubscribedContractList = rpcSyncSlaveNodeRuntimeDataRsp.getSubscribedContractList();

						Set<String> newSubscribedUnifiedSymbolSet = new HashSet<>();

						for (ContractField contract : newSubscribedContractList) {
							newSubscribedUnifiedSymbolSet.add(contract.getUnifiedSymbol());
						}

						List<ContractField> unsubscribeContractList = new ArrayList<>();
						for (Entry<String, ContractField> entry : subscribedContractMap.entrySet()) {
							String unifiedSymbol = entry.getKey();
							ContractField contract = entry.getValue();
							if (!newSubscribedUnifiedSymbolSet.contains(unifiedSymbol)) {
								unsubscribeContractList.add(contract);
							}
						}

						for (ContractField contract : unsubscribeContractList) {
							subscribedContractMap.remove(contract.getUnifiedSymbol());
							for (GatewayApi gatewayApi : gatewayApiMap.values()) {
								if (gatewayApi.getGateway().getGatewayType().equals(GatewayTypeEnum.GTE_MarketData)
										|| gatewayApi.getGateway().getGatewayType().equals(GatewayTypeEnum.GTE_TradeAndMarketData)) {
									try {
										gatewayApi.unsubscribe(contract);
									} catch (Exception e) {
										logger.error("{}取消订阅发生异常", gatewayApi.getLogInfo(), e);
									}
								}
							}
						}

						for (ContractField contract : newSubscribedContractList) {
							if (!subscribedContractMap.containsKey(contract.getUnifiedSymbol())) {
								subscribedContractMap.put(contract.getUnifiedSymbol(), contract);
								for (GatewayApi gatewayApi : gatewayApiMap.values()) {
									if (gatewayApi.getGateway().getGatewayType().equals(GatewayTypeEnum.GTE_MarketData)
											|| gatewayApi.getGateway().getGatewayType().equals(GatewayTypeEnum.GTE_TradeAndMarketData)) {
										try {
											gatewayApi.subscribe(contract);
										} catch (Exception e) {
											logger.error("{}订阅发生异常", gatewayApi.getLogInfo(), e);
										}
									}
								}
							}
						}

						List<GatewaySettingField> gatewaySettingList = rpcSyncSlaveNodeRuntimeDataRsp.getGatewaySettingList();
						if (gatewaySettingList != null) {

							Map<String, GatewaySettingField> gatewaySettingMap = new HashMap<>();
							for (GatewaySettingField gatewaySetting : gatewaySettingList) {
								gatewaySettingMap.put(gatewaySetting.getGatewayId(), gatewaySetting);
							}

							List<GatewayApi> gatewayApiList = getGatewayApiList();

							for (GatewayApi gatewayApi : gatewayApiList) {
								String gatewayId = gatewayApi.getGatewayId();
								GatewaySettingField gatewaySetting = gatewaySettingMap.get(gatewayId);
								if (gatewaySetting == null) {
									disconnectGatewayApi(gatewayId);
								} else {
									if (gatewaySetting.getStatus() == ConnectStatusEnum.CS_Disconnecting || gatewaySetting.getStatus() == ConnectStatusEnum.CS_Disconnected) {
										logger.info("网关{}状态变更,执行断开操作", gatewayId);
										disconnectGatewayApi(gatewayId);
									} else if (gatewaySetting.getVersion() != gatewayApi.getGatewaySetting().getVersion()) {
										logger.info("网关{}配置变更,执行断开操作", gatewayId);
										disconnectGatewayApi(gatewayId);
									}
								}
							}

							for (GatewaySettingField gatewaySetting : gatewaySettingList) {
								if (gatewaySetting.getStatus() == ConnectStatusEnum.CS_Connecting || gatewaySetting.getStatus() == ConnectStatusEnum.CS_Connected) {
									GatewayApi gatewayApi = getGatewayApi(gatewaySetting.getGatewayId());

									boolean shouldConnectFlag = false;

									if (gatewayApi == null) {
										shouldConnectFlag = true;
									} else if (!gatewayApi.isConnected()) {
										if (System.currentTimeMillis() - gatewayApi.getLastConnectBeginTimestamp() > 20 * 1000) {
											shouldConnectFlag = true;
										} else {
											logger.info("网关{}上次连接尚未超时,上次开始连接时间戳{}", gatewayApi.getGatewayId(), gatewayApi.getLastConnectBeginTimestamp());
										}
									}

									if (shouldConnectFlag) {
										boolean connectFlag = false;

										if (StringUtils.isBlank(gatewaySetting.getAutoConnectTimeRanges())) {
											connectFlag = true;
										} else {
											try {
												String[] timeRangeArray = gatewaySetting.getAutoConnectTimeRanges().split("#");
												for (String timeRange : timeRangeArray) {
													String[] timesArray = timeRange.split("-");
													int timeBegin = Integer.valueOf(timesArray[0]);
													int timeEnd = Integer.valueOf(timesArray[1]);
													LocalDateTime ldt = LocalDateTime.now();
													int timeNow = ldt.getMinute() + ldt.getHour() * 100;
													if (timeBegin <= timeNow && timeEnd >= timeNow) {
														connectFlag = true;
														break;
													}
												}
											} catch (Exception e) {
												logger.error("网关{}解析自动连接时间范围错误", gatewaySetting.getGatewayId(), e);
												connectFlag = false;
											}
										}
										if (connectFlag == true) {
											logger.info("网关{}执行连接操作", gatewaySetting.getGatewayId());
											connectGatewayApi(gatewaySetting);
										} else {
											logger.info("网关{}不在自动连接时间范围:{}", gatewaySetting.getGatewayId(), gatewaySetting.getAutoConnectTimeRanges());
										}
									}
								}

							}

						}
					} else {
						logger.warn("定时同步节点数据,未能获取到数据");
					}

				} catch (Exception e) {
					logger.error("定时同步节点数据发生异常", e);
				}

				logger.info("定时同步节点数据完成");

				startTime = System.currentTimeMillis();
			}
		}

	}

}
