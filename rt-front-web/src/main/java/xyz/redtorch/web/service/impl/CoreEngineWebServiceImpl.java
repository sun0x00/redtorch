package xyz.redtorch.web.service.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.CancelOrderReq;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.LocalPositionDetail;
import xyz.redtorch.core.entity.LogData;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.gateway.Gateway;
import xyz.redtorch.core.gateway.GatewaySetting;
import xyz.redtorch.core.service.CoreEngineService;
import xyz.redtorch.utils.CommonUtil;
import xyz.redtorch.web.service.CoreEngineWebService;

/**
 * @author sun0x00@gmail.com
 */
@Service
public class CoreEngineWebServiceImpl implements CoreEngineWebService {

	private Logger log = LoggerFactory.getLogger(CoreEngineWebServiceImpl.class);

	@Autowired
	private CoreEngineService coreEngineService;


	@Override
	public String sendOrder(OrderReq orderReq) {
		log.info("接收到委托请求,{}", orderReq.toString());

		if (StringUtils.isBlank(orderReq.getSymbol())) {
			log.error("发单失败,未提供合约代码");
			return null;
		}

		if (StringUtils.isBlank(orderReq.getRtAccountID())) {
			log.error("发单失败,未提供账户ID");
			return null;
		}

		Account account = coreEngineService.getAccount(orderReq.getRtAccountID());
		if (account == null) {
			log.error("发单失败,未能查询到账户,账户ID-[{}]", orderReq.getRtAccountID());
		}
		orderReq.setGatewayID(account.getGatewayID());
		orderReq.setGatewayDisplayName(account.getGatewayDisplayName());

		Contract contract = null;
		if(StringUtils.isBlank(orderReq.getExchange())){
			contract = coreEngineService.getContractByFuzzySymbol(orderReq.getSymbol()+"."+orderReq.getGatewayID());
		}else {
			contract = coreEngineService.getContract(orderReq.getSymbol()+"."+orderReq.getExchange(),orderReq.getGatewayID());
		}

		if (contract != null) {
			orderReq.setSymbol(contract.getSymbol());
			orderReq.setExchange(contract.getExchange());
			orderReq.setRtSymbol(contract.getRtSymbol());
			orderReq.setPrice(CommonUtil.rountToPriceTick(contract.getPriceTick(), orderReq.getPrice()));

			log.info("发送委托请求,{}", orderReq.toString());
			return coreEngineService.sendOrder(orderReq);
		} else {
			log.error("发单失败,未能搜寻到合约,合约代码-[{}],网关ID-[{}]", orderReq.getRtSymbol(), orderReq.getGatewayID());
			return null;
		}

	}

	@Override
	public void cancelOrder(String rtOrderID,String operatorID) {
		log.info("接收到撤单请求,委托ID-[{}]", rtOrderID);

		Order order = coreEngineService.getOrder(rtOrderID);
		if (order != null) {
			if (!RtConstant.STATUS_FINISHED.contains(order.getStatus())) {

				CancelOrderReq cancelOrderReq = new CancelOrderReq();

				cancelOrderReq.setSymbol(order.getSymbol());
				cancelOrderReq.setExchange(order.getExchange());

				cancelOrderReq.setFrontID(order.getFrontID());
				cancelOrderReq.setSessionID(order.getSessionID());
				cancelOrderReq.setOrderID(order.getOrderID());
				cancelOrderReq.setGatewayID(order.getGatewayID());
				cancelOrderReq.setOperatorID(operatorID);

				log.info("发送撤单请求,{}", cancelOrderReq.toString());
				coreEngineService.cancelOrder(cancelOrderReq);

			} else {
				log.error("无法撤单,委托状态为完成,{}", order.toString());
			}
		} else {
			log.error("无法撤单,未能找到委托,委托ID-{}", rtOrderID);
		}
	}

	@Override
	public void cancelAllOrders() {
		log.info("撤销所有活动委托");
		for (Order order : coreEngineService.getWorkingOrders()) {
			if (!RtConstant.STATUS_FINISHED.contains(order.getStatus())) {

				CancelOrderReq cancelOrderReq = new CancelOrderReq();

				cancelOrderReq.setSymbol(order.getSymbol());
				cancelOrderReq.setExchange(order.getExchange());

				cancelOrderReq.setFrontID(order.getFrontID());
				cancelOrderReq.setSessionID(order.getSessionID());
				cancelOrderReq.setOrderID(order.getOrderID());
				cancelOrderReq.setGatewayID(order.getGatewayID());

				log.info("发送撤单请求,{}", cancelOrderReq.toString());
				coreEngineService.cancelOrder(cancelOrderReq);

			} else {
				log.error("无法撤单,委托状态为完成,{}", order.toString());
			}
		}
	}

	public boolean subscribe(SubscribeReq subscribeReq) {

		if (subscribeReq == null || StringUtils.isBlank(subscribeReq.getSymbol())) {
			return false;
		}

		Contract contract;
		if (StringUtils.isBlank(subscribeReq.getGatewayID())) {
			// 不存在GatewayID字段

			log.warn("订阅行情未提供网关ID,合约代码-[{}],尝试搜寻合约", subscribeReq.getSymbol());

			if (StringUtils.isNotBlank(subscribeReq.getExchange())) {
				// 存在交易所字段
				contract = coreEngineService
						.getContractByFuzzySymbol(subscribeReq.getSymbol() + "." + subscribeReq.getExchange());
			} else {
				contract = coreEngineService.getContractByFuzzySymbol(subscribeReq.getSymbol());
			}

			if (contract == null) {
				log.warn("订阅行情失败,合约代码-[{}],未搜寻到合约", subscribeReq.getSymbol());
				return false;
			} else {
				subscribeReq.setRtSymbol(contract.getRtSymbol());
				subscribeReq.setSymbol(contract.getSymbol());
				subscribeReq.setGatewayID(contract.getGatewayID());
				subscribeReq.setExchange(contract.getExchange());
			}

		} else if (StringUtils.isBlank(subscribeReq.getExchange())) {
			// 存在GatewayID字段，不存在交易所字段
			contract = coreEngineService
					.getContractByFuzzySymbol(subscribeReq.getSymbol() + "." + subscribeReq.getGatewayID());
			if (contract == null) {
				log.warn("订阅行情失败,合约代码-[{}],未搜寻到合约", subscribeReq.getSymbol());
				return false;
			} else {
				subscribeReq.setRtSymbol(contract.getRtSymbol());
				subscribeReq.setSymbol(contract.getSymbol());
				subscribeReq.setGatewayID(contract.getGatewayID());
				subscribeReq.setExchange(contract.getExchange());
			}

		} else {
			// 存在GatewayID字段，存在交易所字段
			subscribeReq.setRtSymbol(subscribeReq.getSymbol() + "." + subscribeReq.getExchange());
		}

		return coreEngineService.subscribe(subscribeReq, "WEB_API");
	}

	@Override
	public boolean unsubscribe(String rtSymbol, String gatewayID) {
		log.info("取消订阅行情,合约代码-[{}],网关ID-[{}]", rtSymbol, gatewayID);
		SubscribeReq subscribeReq = new SubscribeReq();
		subscribeReq.setGatewayID(gatewayID);
		subscribeReq.setRtSymbol(rtSymbol);
		return coreEngineService.unsubscribe(rtSymbol, gatewayID, "WEB_API");
	}

	@Override
	public List<GatewaySetting> getGatewaySettings() {
		List<GatewaySetting> gatewaySettings = coreEngineService.queryGatewaySettings();
		if (gatewaySettings != null) {
			for (GatewaySetting gatewaySetting : gatewaySettings) {
				Gateway gateway = coreEngineService.getGateway(gatewaySetting.getGatewayID());
				if (gatewaySetting.getCtpSetting() != null) {
					gatewaySetting.getCtpSetting().setPassword("*********");
				}
				gatewaySetting.setRuntimeStatus(false);
				if (gateway != null) {
					if (gateway.isConnected()) {
						gatewaySetting.setRuntimeStatus(true);
					}
				}
			}
		}
		return gatewaySettings;
	}

	@Override
	public void deleteGateway(String gatewayID) {
		log.info("删除网关,网关ID-[{}]", gatewayID);
		coreEngineService.deleteGateway(gatewayID);
	}

	@Override
	public void changeGatewayConnectStatus(String gatewayID) {
		log.info("变更网关连接状态,网关ID-[{}]", gatewayID);
		Gateway gateway = coreEngineService.getGateway(gatewayID);
		if (gateway != null) {
			log.info("断开网关,网关ID-[{}]", gatewayID);
			coreEngineService.disconnectGateway(gatewayID);
		} else {
			log.info("连接网关,网关ID-[{}]", gatewayID);
			coreEngineService.connectGateway(gatewayID);
		}

	}

	@Override
	public void saveOrUpdateGatewaySetting(GatewaySetting gatewaySetting) {
		if (StringUtils.isEmpty(gatewaySetting.getGatewayID())) {
			gatewaySetting.setGatewayID(UUID.randomUUID().toString().replace("-", "").toLowerCase());
		} else {
			if (gatewaySetting.getCtpSetting() != null && gatewaySetting.getCtpSetting().getPassword() != null
					&& gatewaySetting.getCtpSetting().getPassword().equals("*********")) {

				GatewaySetting dbGatewaySetting = coreEngineService.queryGatewaySetting(gatewaySetting.getGatewayID());
				if (dbGatewaySetting != null && dbGatewaySetting.getCtpSetting() != null)

					gatewaySetting.getCtpSetting().setPassword(dbGatewaySetting.getCtpSetting().getPassword());
			}
			coreEngineService.deleteGateway(gatewaySetting.getGatewayID());
		}
		coreEngineService.saveGateway(gatewaySetting);
	}

	@Override
	public List<Trade> getTrades() {
		return coreEngineService.getTrades();
	}

	@Override
	public List<Order> getOrders() {
		return coreEngineService.getOrders();
	}

	@Override
	public List<LocalPositionDetail> getLocalPositionDetails() {
		return coreEngineService.getLocalPositionDetails();
	}

	@Override
	public List<Position> getPositions() {
		return coreEngineService.getPositions();
	}

	@Override
	public List<Account> getAccounts() {
		return coreEngineService.getAccounts();
	}

	@Override
	public List<Contract> getContracts() {
		return coreEngineService.getContracts();
	}

	@Override
	public List<Tick> getTicks() {
		return coreEngineService.getTicks();
	}

	@Override
	public List<LogData> getLogDatas() {
		return coreEngineService.getLogDatas();
	}

}
