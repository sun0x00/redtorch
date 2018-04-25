package xyz.redtorch.web.service;

import java.util.List;
import java.util.Map;

import xyz.redtorch.trader.entity.Account;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.LocalPositionDetail;
import xyz.redtorch.trader.entity.LogData;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.Position;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.gateway.GatewaySetting;

/**
 * @author sun0x00@gmail.com
 */
public interface TradingService {
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

	void zeusLoadStrategy();

	List<Map<String, Object>> zeusGetStrategyInfos();
	
	void zeusInitStrategy(String strategyID);

	void zeusSartStrategy(String strategyID);
	
	void zeusStopStrategy(String strategyID);
	
	void zeusInitAllStrategy();

	void zeusSartAllStrategy();
	
	void zeusStopAllStrategy();

	void zeusReloadStrategy(String strategyID);

	List<LogData> getLogDatas();

	String sendOrder(String gatewayID, String rtSymbol, double price, int volume, String priceType, String direction,
			String offset);

	void cancelOrder(String rtOrderID);

	void cancelAllOrders();


}
