package xyz.redtorch.trader.engine.main;

import java.util.List;

import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.engine.event.FastEventDynamicHandler;
import xyz.redtorch.trader.entity.Account;
import xyz.redtorch.trader.entity.CancelOrderReq;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.LocalPositionDetail;
import xyz.redtorch.trader.entity.LogData;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Position;
import xyz.redtorch.trader.entity.SubscribeReq;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.gateway.Gateway;
import xyz.redtorch.trader.gateway.GatewaySetting;

/**
 * @author sun0x00@gmail.com
 */
public interface MainEngine extends FastEventDynamicHandler{
	
	/**
	 * 获取合约
	 * @param rtSymbol
	 * @return
	 */
	Contract getContract(String rtSymbol);
	
	/**
	 * 获取合约
	 * @param rtSymbol
	 * @param gatewayID
	 * @return
	 */
	Contract getContract(String rtSymbol, String gatewayID);

	/**
	 * 获取所有合约
	 * @return
	 */
	List<Contract> getContracts();
	
	/**
	 * 获取数据引擎
	 * @return
	 */
	DataEngine getDataEngine();

	/**
	 * 发单
	 * @param orderReq
	 * @return
	 */
	String sendOrder(OrderReq orderReq);

	/**
	 * 获取委托
	 * @param rtOrderID
	 * @return
	 */
	Order getOrder(String rtOrderID);

	/**
	 * 撤销委托
	 * @param cancelOrderReq
	 */
	void cancelOrder(CancelOrderReq cancelOrderReq);
	
	/**
	 * 订阅
	 * @param subscribeReq
	 * @param subscriberID
	 * @return
	 */
	boolean subscribe(SubscribeReq subscribeReq, String subscriberID);
	
	/**
	 * 尝试取消订阅
	 * @param rtSymbol
	 * @param gatewayID
	 * @param subscriberID
	 * @return
	 */
	boolean unsubscribe(String rtSymbol, String gatewayID, String subscriberID);
	
	/**
	 * 查询特定账户
	 * @param gatewayID
	 */
	void queryAccount(String gatewayID);
	
	/**
	 * 查询特定接口仓位
	 * @param gatewayID
	 */
	void queryPosition(String gatewayID);
	
	
	/**
	 * 连接接口
	 * @param gatewayID
	 */
	void connectGateway(String gatewayID);
	
	/**
	 * 断开接口
	 * @param gatewayID
	 */
	void disconnectGateway(String gatewayID);

	/**
	 * 查询已经加载的接口
	 * @param gatewayID
	 * @return
	 */
	Gateway getGateway(String gatewayID);
	/**
	 * 获取所有已经加载的接口列表
	 * @return
	 */
	List<Gateway> getGateways();

	/**
	 * 向DB中增加接口
	 * @param gatewaySetting
	 */
	void saveGateway(GatewaySetting gatewaySetting);
	
	/**
	 * 更新DB中的接口（如果已经加载会先断开）
	 * @param gatewaySetting
	 */
	void updateGateway(GatewaySetting gatewaySetting);
	
	/**
	 * 删除DB中的接口（如果已经加载会先断开）
	 * @param gatewayID
	 */
	void deleteGateway(String gatewayID);
	
	/**
	 * 从DB查询指定的接口
	 * @param gatewayID
	 * @return
	 */
	GatewaySetting queryGatewaySetting(String gatewayID);
	
	/**
	 * 查询DB中所有接口
	 * @return
	 */
	List<GatewaySetting> queryGatewaySettings();
	
	/**
	 * 扫描所有的接口实现类
	 * @return
	 */
	List<String> scanGatewayImpl();

	/**
	 * 获取持仓详细信息
	 * @param rtSymbol
	 * @param gatewayID
	 * @return
	 */
	LocalPositionDetail getLocalPositionDetail(String rtSymbol, String gatewayID);
	

	/**
	 * 获取所有持仓信息
	 * @return
	 */
	List<LocalPositionDetail> getLocalPositionDetails();

	
	/**
	 * 获取所有持仓
	 * @return
	 */
	List<Position> getPositions();
	
	/**
	 * 更新委托请求
	 * @param orderReq
	 * @param rtOrderID
	 */
	void updateOrderReq(OrderReq orderReq, String rtOrderID);

	/**
	 * 查询所有账户
	 * @return
	 */
	List<Account> getAccounts();

	/**
	 * 获取所有成交
	 * @return
	 */
	List<Trade> getTrades();

	/**
	 * 获取所有委托
	 * @return
	 */
	List<Order> getOrders();

	/**
	 * 获取所有活动委托
	 * @return
	 */
	List<Order> getWorkingOrders();

	/**
	 * 获取所有日志
	 * @return
	 */
	List<LogData> getLogDatas();

}
