package xyz.redtorch.web.service.impl;

import java.util.List;

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
	public String sendOrder(String gatewayID, String rtSymbol, double price, int volume, String priceType,
			String direction, String offset) {

		Contract contract = coreEngineService.getContract(rtSymbol, gatewayID);
		if (contract != null) {
			OrderReq orderReq = new OrderReq();
			orderReq.setSymbol(contract.getSymbol());
			orderReq.setExchange(contract.getExchange());
			orderReq.setRtSymbol(contract.getRtSymbol());
			orderReq.setPrice(CommonUtil.rountToPriceTick(contract.getPriceTick(), price));
			orderReq.setVolume(volume);
			orderReq.setGatewayID(gatewayID);
			orderReq.setDirection(direction);
			orderReq.setOffset(offset);
			orderReq.setPriceType(priceType);

			return coreEngineService.sendOrder(orderReq);
		} else {
			log.error("发单失败,未找到合约");
			return null;
		}

	}

	@Override
	public void cancelOrder(String rtOrderID) {

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

				coreEngineService.cancelOrder(cancelOrderReq);

			}
		} else {
			log.error("无法撤单,未能找到委托ID {}", rtOrderID);
		}
	}

	@Override
	public void cancelAllOrders() {

		for (Order order : coreEngineService.getWorkingOrders()) {
			System.out.println(order.getStatus());
			if (!RtConstant.STATUS_FINISHED.contains(order.getStatus())) {

				CancelOrderReq cancelOrderReq = new CancelOrderReq();

				cancelOrderReq.setSymbol(order.getSymbol());
				cancelOrderReq.setExchange(order.getExchange());

				cancelOrderReq.setFrontID(order.getFrontID());
				cancelOrderReq.setSessionID(order.getSessionID());
				cancelOrderReq.setOrderID(order.getOrderID());
				cancelOrderReq.setGatewayID(order.getGatewayID());

				coreEngineService.cancelOrder(cancelOrderReq);

			}
		}
	}

	@Override
	public boolean subscribe(String rtSymbol, String gatewayID) {
		if (StringUtils.isEmpty(rtSymbol)) {
			return false;
		}
		SubscribeReq subscribeReq = new SubscribeReq();
		subscribeReq.setGatewayID(gatewayID);
		subscribeReq.setRtSymbol(rtSymbol);
		return coreEngineService.subscribe(subscribeReq, "web-page-00");
	}

	@Override
	public boolean unsubscribe(String rtSymbol, String gatewayID) {
		SubscribeReq subscribeReq = new SubscribeReq();
		subscribeReq.setGatewayID(gatewayID);
		subscribeReq.setRtSymbol(rtSymbol);
		return coreEngineService.unsubscribe(rtSymbol, gatewayID, "web-page-00");
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
	public List<GatewaySetting> getGatewaySettings() {
		List<GatewaySetting> gatewaySettings = coreEngineService.queryGatewaySettings();
		if (gatewaySettings != null) {
			for (GatewaySetting gatewaySetting : gatewaySettings) {
				Gateway gateway = coreEngineService.getGateway(gatewaySetting.getGatewayID());

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
		coreEngineService.deleteGateway(gatewayID);
	}

	@Override
	public void changeGatewayConnectStatus(String gatewayID) {
		Gateway gateway = coreEngineService.getGateway(gatewayID);
		if (gateway != null) {
			if (gateway.isConnected()) {
				coreEngineService.disconnectGateway(gatewayID);
			} else {
				gateway.connect();
			}
		} else {
			coreEngineService.connectGateway(gatewayID);
		}

	}

	@Override
	public void saveOrUpdateGatewaySetting(GatewaySetting gatewaySetting) {
		if (StringUtils.isEmpty(gatewaySetting.getGatewayID())) {
			String[] tdAddressArray = gatewaySetting.getTdAddress().split("\\.");
			String tdAddressSuffix = tdAddressArray[tdAddressArray.length - 1].replaceAll(":", "\\.");
			gatewaySetting.setGatewayID(gatewaySetting.getBrokerID() + "." + gatewaySetting.getGatewayDisplayName()
					+ "." + tdAddressSuffix);
		} else {
			coreEngineService.deleteGateway(gatewaySetting.getGatewayID());
		}
		coreEngineService.saveGateway(gatewaySetting);
	}
	@Override
	public List<LogData> getLogDatas() {
		return coreEngineService.getLogDatas();
	}
}
