package xyz.redtorch.core.service;

import java.util.List;

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
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandler;

/**
 * @author sun0x00@gmail.com
 */
public interface CoreEngineService extends FastEventDynamicHandler {

	/**
	 * 根据模糊化的Symbol获取合约
	 * 
	 * @param fuzzySymbol
	 * @return
	 */
	Contract getContractByFuzzySymbol(String fuzzySymbol);

	/**
	 * 获取合约
	 * 
	 * @param rtSymbol
	 * @param gatewayID
	 * @return
	 */
	Contract getContract(String rtSymbol, String gatewayID);

	/**
	 * 获取所有合约
	 * 
	 * @return
	 */
	List<Contract> getContracts();

	/**
	 * 获取数据引擎
	 * 
	 * @return
	 */
	MongoDBService getDataEngine();

	/**
	 * 发单
	 * 
	 * @param orderReq
	 * @return
	 */
	String sendOrder(OrderReq orderReq);

	/**
	 * 获取委托
	 * 
	 * @param rtOrderID
	 * @return
	 */
	Order getOrder(String rtOrderID);

	/**
	 * 撤销委托
	 * 
	 * @param cancelOrderReq
	 */
	void cancelOrder(CancelOrderReq cancelOrderReq);

	/**
	 * 订阅
	 * 
	 * @param subscribeReq
	 * @param subscriberID
	 * @return
	 */
	boolean subscribe(SubscribeReq subscribeReq, String subscriberID);

	/**
	 * 尝试取消订阅
	 * 
	 * @param rtSymbol
	 * @param gatewayID
	 * @param subscriberID
	 * @return
	 */
	boolean unsubscribe(String rtSymbol, String gatewayID, String subscriberID);

	/**
	 * 连接网关
	 * 
	 * @param gatewayID
	 */
	void connectGateway(String gatewayID);

	/**
	 * 断开网关
	 * 
	 * @param gatewayID
	 */
	void disconnectGateway(String gatewayID);

	/**
	 * 查询已经加载的网关
	 * 
	 * @param gatewayID
	 * @return
	 */
	Gateway getGateway(String gatewayID);

	/**
	 * 获取所有已经加载的网关列表
	 * 
	 * @return
	 */
	List<Gateway> getGateways();

	/**
	 * 向DB中增加网关
	 * 
	 * @param GatewaySetting
	 */
	void saveGateway(GatewaySetting gatewaySetting);

	/**
	 * 更新DB中的网关（如果已经加载会先断开）
	 * 
	 * @param GatewaySetting
	 */
	void updateGateway(GatewaySetting gatewaySetting);

	/**
	 * 删除DB中的网关（如果已经加载会先断开）
	 * 
	 * @param gatewayID
	 */
	void deleteGateway(String gatewayID);

	/**
	 * 从DB查询指定的网关
	 * 
	 * @param gatewayID
	 * @return
	 */
	GatewaySetting queryGatewaySetting(String gatewayID);

	/**
	 * 查询DB中所有网关
	 * 
	 * @return
	 */
	List<GatewaySetting> queryGatewaySettings();

	/**
	 * 扫描所有的网关实现类
	 * 
	 * @return
	 */
	List<String> scanGatewayImpl();

	/**
	 * 获取所有持仓信息
	 * 
	 * @return
	 */
	List<LocalPositionDetail> getLocalPositionDetails();

	/**
	 * 获取所有持仓
	 * 
	 * @return
	 */
	List<Position> getPositions();

	/**
	 * 更新委托请求
	 * 
	 * @param orderReq
	 * @param rtOrderID
	 */
	void updateOrderReq(OrderReq orderReq, String rtOrderID);

	/**
	 * 查询所有账户
	 * 
	 * @return
	 */
	List<Account> getAccounts();

	/**
	 * 根据ID查询账户
	 * 
	 * @param rtAccountID
	 * @return
	 */
	public Account getAccount(String rtAccountID);

	/**
	 * 获取所有成交
	 * 
	 * @return
	 */
	List<Trade> getTrades();

	/**
	 * 获取所有委托
	 * 
	 * @return
	 */
	List<Order> getOrders();

	/**
	 * 获取所有活动委托
	 * 
	 * @return
	 */
	List<Order> getWorkingOrders();

	/**
	 * 获取所有日志
	 * 
	 * @return
	 */
	List<LogData> getLogDatas();

	/**
	 * 获取缓存Tick
	 * 
	 * @return
	 */
	List<Tick> getTicks();

}
