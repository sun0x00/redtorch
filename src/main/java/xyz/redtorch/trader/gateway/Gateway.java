package xyz.redtorch.trader.gateway;

import java.util.HashSet;

import org.joda.time.DateTime;

import xyz.redtorch.trader.entity.Account;
import xyz.redtorch.trader.entity.CancelOrderReq;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Position;
import xyz.redtorch.trader.entity.SubscribeReq;

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
	void emitTick(String gatewayID, String symbol, String exchange, String rtSymbol, String tradingDay, String actionDay,
			String actionTime, DateTime dateTime, Integer status, Double lastPrice, Integer lastVolume, Integer volume,
			Double openInterest, Long preOpenInterest, Double preClosePrice, Double preSettlePrice, Double openPrice,
			Double highPrice, Double lowPrice, Double upperLimit, Double lowerLimit, Double bidPrice1, Double bidPrice2,
			Double bidPrice3, Double bidPrice4, Double bidPrice5, Double bidPrice6, Double bidPrice7, Double bidPrice8,
			Double bidPrice9, Double bidPrice10, Double askPrice1, Double askPrice2, Double askPrice3, Double askPrice4,
			Double askPrice5, Double askPrice6, Double askPrice7, Double askPrice8, Double askPrice9, Double askPrice10,
			Integer bidVolume1, Integer bidVolume2, Integer bidVolume3, Integer bidVolume4, Integer bidVolume5,
			Integer bidVolume6, Integer bidVolume7, Integer bidVolume8, Integer bidVolume9, Integer bidVolume10,
			Integer askVolume1, Integer askVolume2, Integer askVolume3, Integer askVolume4, Integer askVolume5,
			Integer askVolume6, Integer askVolume7, Integer askVolume8, Integer askVolume9, Integer askVolume10);

	/**
	 * 发送成交事件
	 * @param trade
	 */
	void emitTrade(String gatewayID, String symbol, String exchange, String rtSymbol, String tradeID, String rtTradeID,
			String orderID, String rtOrderID, String direction, String offset, double price, int volume,
			String tradingDay, String tradeDate, String tradeTime, DateTime dateTime);
	
	/**
	 * 发送委托事件
	 * @param order
	 */
	void emitOrder(String gatewayID, String symbol, String exchange, String rtSymbol, String orderID, String rtOrderID,
			String direction, String offset, double price, int totalVolume, int tradedVolume, String status,
			String tradingDay, String orderDate, String orderTime, String cancelTime, String activeTime,
			String updateTime, int frontID, int sessionID);
	
	/**
	 * 获取配置 
	 * @return
	 */
	GatewaySetting getGatewaySetting();

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
