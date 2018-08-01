package xyz.redtorch.web.service;

import java.util.List;

import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.LocalPositionDetail;
import xyz.redtorch.core.entity.LogData;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.gateway.GatewaySetting;

/**
 * @author sun0x00@gmail.com
 */
public interface CoreEngineWebService {
	boolean subscribe(String rtSymbol, String gatewayID);
	
	boolean unsubscribe(String rtSymbol, String gatewayID);

	List<Trade> getTrades();

	List<Order> getOrders();

	List<LocalPositionDetail> getLocalPositionDetails();
	
	List<Position> getPositions();

	List<Account> getAccounts();

	List<Contract> getContracts();
	
	List<GatewaySetting> getGatewaySettings();

	void deleteGateway(String gatewayID);

	void changeGatewayConnectStatus(String gatewayID);

	void saveOrUpdateGatewaySetting(GatewaySetting gatewaySetting);

	List<LogData> getLogDatas();

	String sendOrder(String gatewayID, String rtSymbol, double price, int volume, String priceType, String direction,
			String offset);

	void cancelOrder(String rtOrderID);

	void cancelAllOrders();


}
