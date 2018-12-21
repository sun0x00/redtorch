package xyz.redtorch.web.service;

import java.util.List;

import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.LocalPositionDetail;
import xyz.redtorch.core.entity.LogData;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.gateway.GatewaySetting;

/**
 * @author sun0x00@gmail.com
 */
public interface CoreEngineWebService {

	String sendOrder(OrderReq orderReq);

	boolean subscribe(SubscribeReq subscribeReq);

	boolean unsubscribe(String rtSymbol, String gatewayID);

	void cancelOrder(String rtOrderID,String operatorID);

	void cancelAllOrders();

	void deleteGateway(String gatewayID);

	void changeGatewayConnectStatus(String gatewayID);

	void saveOrUpdateGatewaySetting(GatewaySetting gatewaySetting);

	List<Trade> getTrades();

	List<Order> getOrders();

	List<LocalPositionDetail> getLocalPositionDetails();

	List<Position> getPositions();

	List<Account> getAccounts();

	List<Contract> getContracts();

	List<Tick> getTicks();

	List<GatewaySetting> getGatewaySettings();

	List<LogData> getLogDatas();

}
