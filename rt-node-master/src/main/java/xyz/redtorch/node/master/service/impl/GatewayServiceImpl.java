package xyz.redtorch.node.master.service.impl;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.RtConstant;
import xyz.redtorch.node.master.dao.GatewayDao;
import xyz.redtorch.node.master.dao.NodeDao;
import xyz.redtorch.node.master.po.GatewayPo;
import xyz.redtorch.node.master.po.NodePo;
import xyz.redtorch.node.master.service.GatewayService;
import xyz.redtorch.node.master.service.MasterTradeCachesService;
import xyz.redtorch.pb.CoreEnum.ConnectStatusEnum;
import xyz.redtorch.pb.CoreEnum.GatewayAdapterTypeEnum;
import xyz.redtorch.pb.CoreEnum.GatewayTypeEnum;

@Service
public class GatewayServiceImpl implements GatewayService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(GatewayServiceImpl.class);

	@Autowired
	private GatewayDao gatewayDao;
	@Autowired
	private NodeDao nodeDao;
	@Autowired
	private MasterTradeCachesService masterTradeCachesService;

	private ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();

	@Override
	public void afterPropertiesSet() throws Exception {
		scheduledExecutorService.scheduleAtFixedRate(() -> {

			try {
				List<NodePo> nodeList = nodeDao.queryNodeList();
				Map<Integer, NodePo> nodeMap = new HashMap<>();

				for (NodePo node : nodeList) {
					nodeMap.put(node.getNodeId(), node);
				}

				List<GatewayPo> gatewayList = gatewayDao.queryGatewayList();

				for (GatewayPo gateway : gatewayList) {
					if (gateway.getStatus() == ConnectStatusEnum.CS_Connected_VALUE) {
						if (!nodeMap.containsKey(gateway.getTargetNodeId()) || nodeMap.get(gateway.getTargetNodeId()).getStatus() != NodePo.NODE_STATUS_CONNECTED) {
							gateway.setStatus(ConnectStatusEnum.CS_Connected_VALUE);
							gatewayDao.upsertGatewayByGatewayId(gateway);
						}
					}

				}
			} catch (Exception e) {
				logger.error("定时检查网关状态异常", e);
			}

		}, 5, 5, TimeUnit.SECONDS);

	}

	@Override
	public GatewayPo getGatewayByGatewayId(String gatewayId) {
		if (StringUtils.isBlank(gatewayId)) {
			logger.error("根据网关ID获取网关错误,参数gatewayId缺失");
			throw new IllegalArgumentException("根据网关ID获取网关错误,参数gatewayId缺失");
		}
		return gatewayDao.queryGatewayByGatewayId(gatewayId);
	}

	@Override
	public List<GatewayPo> getGatewayList() {
		return gatewayDao.queryGatewayList();
	}

	@Override
	public void upsertGatewayByGatewayId(GatewayPo gateway) {
		if (gateway == null) {
			logger.error("根据网关ID更新或保存网关错误,参数gateway缺失");
			throw new IllegalArgumentException("根据网关ID更新或保存网关错误,参数gateway缺失");
		}
		if (StringUtils.isBlank(gateway.getGatewayId())) {
			logger.error("根据网关ID更新或保存网关错误,参数gatewayId缺失");
			throw new IllegalArgumentException("根据网关ID更新或保存网关错误,参数gatewayId缺失");
		}

		if (gateway.getGatewayAdapterType() == null) {
			gateway.setGatewayAdapterType(GatewayAdapterTypeEnum.GAT_CTP_VALUE);
		}
		if (gateway.getGatewayDescription() == null) {
			gateway.setGatewayDescription("");
		}
		if (gateway.getGatewayName() == null) {
			gateway.setGatewayName("");
		}

		if (gateway.getGatewayType() == null) {
			gateway.setGatewayType(GatewayTypeEnum.GTE_TradeAndMarketData_VALUE);
		}

		if (gateway.getImplementClassName() == null) {
			gateway.setImplementClassName("");
		}

		if (gateway.getStatus() == null) {
			gateway.setStatus(ConnectStatusEnum.CS_Disconnected_VALUE);
		}

		if (gateway.getTargetNodeId() == null) {
			gateway.setTargetNodeId(0);
		}
		if (gateway.getVersion() == null) {
			gateway.setVersion(System.currentTimeMillis());
		}

		GatewayPo dbGateway = gatewayDao.queryGatewayByGatewayId(gateway.getGatewayId());

		if (dbGateway != null) {
			if (dbGateway.getCtpSetting() != null && gateway.getCtpSetting() != null) {
				if (RtConstant.SECURITY_MASK.equals(gateway.getCtpSetting().getAuthCode())) {
					gateway.getCtpSetting().setAuthCode(dbGateway.getCtpSetting().getAuthCode());
				}
				if (RtConstant.SECURITY_MASK.equals(gateway.getCtpSetting().getPassword())) {
					gateway.getCtpSetting().setPassword(dbGateway.getCtpSetting().getPassword());
				}
			}
		}

		gatewayDao.upsertGatewayByGatewayId(gateway);
	}

	@Override
	public void deleteGatewayByGatewayId(String gatewayId) {
		if (StringUtils.isBlank(gatewayId)) {
			logger.error("根据网关ID删除网关错误,参数gatewayId缺失");
			throw new IllegalArgumentException("根据网关ID删除网关错误,参数gatewayId缺失");
		}
		gatewayDao.deleteGatewayByGatewayId(gatewayId);
		masterTradeCachesService.clearAllCachesByGatewayId(gatewayId);
	}

	@Override
	public void connectGatewayByGatewayId(String gatewayId) {
		if (StringUtils.isBlank(gatewayId)) {
			logger.error("根据网关ID连接网关错误,参数gatewayId缺失");
			throw new IllegalArgumentException("根据网关ID连接网关错误,参数gatewayId缺失");
		}

		GatewayPo gateway = getGatewayByGatewayId(gatewayId);
		if (gateway == null) {
			logger.warn("根据网关ID连接网关,未找到网关记录,网关ID:{}", gatewayId);
			return;
		}
		if (gateway.getStatus() == ConnectStatusEnum.CS_Disconnecting_VALUE || gateway.getStatus() == ConnectStatusEnum.CS_Disconnected_VALUE || gateway.getStatus() == null
				|| gateway.getStatus() == ConnectStatusEnum.CS_Unkonwn_VALUE) {
			gateway.setStatus(ConnectStatusEnum.CS_Connecting_VALUE);
			upsertGatewayByGatewayId(gateway);
		} else {
			logger.info("根据网关ID连接网关,当前状态无需更改,网关ID:{},状态码:{}", gateway.getGatewayId(), gateway.getStatus());
		}
	}

	@Override
	public void disconnectGatewayByGatewayId(String gatewayId) {
		if (StringUtils.isBlank(gatewayId)) {
			logger.error("根据网关ID断开网关错误,参数gatewayId缺失");
			throw new IllegalArgumentException("根据网关ID断开网关错误,参数gatewayId缺失");
		}
		GatewayPo gateway = getGatewayByGatewayId(gatewayId);
		if (gateway == null) {
			logger.warn("根据网关ID断开网关,未找到网关记录,网关ID:{}", gatewayId);
			return;
		}
		if (gateway.getStatus() == ConnectStatusEnum.CS_Connecting_VALUE || gateway.getStatus() == ConnectStatusEnum.CS_Connected_VALUE || gateway.getStatus() == null
				|| gateway.getStatus() == ConnectStatusEnum.CS_Unkonwn_VALUE) {
			gateway.setStatus(ConnectStatusEnum.CS_Disconnecting_VALUE);
			upsertGatewayByGatewayId(gateway);
		} else {
			logger.info("根据网关ID断开网关,当前状态无需更改,网关ID:{},状态码:{}", gateway.getGatewayId(), gateway.getStatus());
		}
		masterTradeCachesService.clearAllCachesByGatewayId(gatewayId);
	}

	@Override
	public void disconnectAllGateways() {
		List<GatewayPo> gatewayList = gatewayDao.queryGatewayList();
		for (GatewayPo gateway : gatewayList) {
			if (gateway.getStatus() == ConnectStatusEnum.CS_Connecting_VALUE || gateway.getStatus() == ConnectStatusEnum.CS_Connected_VALUE || gateway.getStatus() == null) {
				gateway.setStatus(ConnectStatusEnum.CS_Disconnecting_VALUE);
				upsertGatewayByGatewayId(gateway);
			} else {
				logger.info("断开全部网关,当前状态无需更改,网关ID:{},状态码:{}", gateway.getGatewayId(), gateway.getStatus());
			}
			masterTradeCachesService.clearAllCachesByGatewayId(gateway.getGatewayId());
		}
	}

	@Override
	public void connectAllGateways() {
		List<GatewayPo> gatewayList = gatewayDao.queryGatewayList();
		for (GatewayPo gateway : gatewayList) {
			if (gateway.getStatus() == ConnectStatusEnum.CS_Disconnecting_VALUE || gateway.getStatus() == ConnectStatusEnum.CS_Disconnected_VALUE || gateway.getStatus() == null) {
				gateway.setStatus(ConnectStatusEnum.CS_Connecting_VALUE);
				upsertGatewayByGatewayId(gateway);
			} else {
				logger.info("连接全部网关,当前状态无需更改,网关ID:{},状态码:{}", gateway.getGatewayId(), gateway.getStatus());
			}
			masterTradeCachesService.clearAllCachesByGatewayId(gateway.getGatewayId());
		}
	}
}
