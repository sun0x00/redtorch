package xyz.redtorch.trader.gateway;

import java.util.HashSet;

import xyz.redtorch.trader.engine.event.EventEngine;
import xyz.redtorch.trader.entity.Account;
import xyz.redtorch.trader.entity.CancelOrderReq;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Position;
import xyz.redtorch.trader.entity.SubscribeReq;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;

/**
 * @author sun0x00@gmail.com
 */
public interface Gateway {
	/**
	 * 获取ID
	 * @return
	 */
	String getGatewayID();
	
	/**
	 * 获取显示名称
	 * @return
	 */
	String getGatewayDisplayName();
	
	/**
	 * 返回已经按格式拼接好的用于日志打印的字符串信息
	 * @return
	 */
	String getGatewayLogInfo();
	/**
	 * 订阅
	 * @param subscribeReq
	 */
	void subscribe(SubscribeReq subscribeReq);
	
	/**
	 * 退订
	 * @param rtSymbol
	 */
	void unSubscribe(String rtSymbol);
	
	/**
	 * 连接
	 */
	void connect();
	/**
	 * 关闭
	 */
	void close();
	/**
	 * 发单
	 * @param orderReq 
	 */
	String sendOrder(OrderReq orderReq);
	/**
	 * 撤单
	 */
	void cancelOrder(CancelOrderReq cancelOrderReq);
	/**
	 * 查询账户
	 */
	void queryAccount();
	/**
	 * 查询持仓
	 */
	void queryPosition();
	
	/**
	 * 发送持仓事件
	 * @param position
	 */
	void emitPositon(Position position);
	
	/**
	 * 发送账户事件
	 * @param account
	 */
	void emitAccount(Account account);
	
	/**
	 * 发送合约事件
	 * @param contract
	 */
	void emitContract(Contract contract);
	
	/**
	 * 发送Tick事件
	 * @param tick
	 */
	void emitTick(Tick tick);

	/**
	 * 发送成交事件
	 * @param trade
	 */
	void emitTrade(Trade trade);
	
	/**
	 * 发送委托事件
	 * @param order
	 */
	void emitOrder(Order order);

	/**
	 * 获取事件引擎
	 * @return
	 */
	EventEngine getEventEngine();
	
	/**
	 * 获取配置 
	 * @return
	 */
	GatewaySetting getGatewaySetting();

	/**
	 * 发送错误日志
	 * @param logContent
	 */
	void emitErrorLog(String logContent);

	/**
	 * 发送日志
	 * @param logContent
	 */
	void emitInfoLog(String logContent);

	/**
	 * 发送警告日志
	 * @param logContent
	 */
	void emitWarnLog(String logContent);

	/**
	 * 发送debug日志
	 * @param logContent
	 */
	void emitDebugLog(String logContent);

	/**
	 * 获取已经订阅的合约符号
	 * @return
	 */
	HashSet<String> getSubscribedSymbols();
	
	/**
	 * 返回接口状态
	 * @return
	 */
	boolean isConnected();
	
	
}
