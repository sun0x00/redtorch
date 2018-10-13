package xyz.redtorch.core.gateway;

import org.joda.time.DateTime;

import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.CancelOrderReq;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;

/**
 * @author sun0x00@gmail.com
 */
public interface Gateway {

	final String TYPE_CTP = "CTP";
	final String TYPE_IB = "IB";

	/**
	 * 获取ID
	 * 
	 * @return
	 */
	String getGatewayID();

	/**
	 * 获取显示名称
	 * 
	 * @return
	 */
	String getGatewayDisplayName();

	/**
	 * 返回已经按格式拼接好的用于日志打印的字符串信息
	 * 
	 * @return
	 */
	String getGatewayLogInfo();

	/**
	 * 订阅
	 * 
	 * @param subscribeReq
	 */
	void subscribe(SubscribeReq subscribeReq);

	/**
	 * 退订
	 * 
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
	 * 
	 * @param orderReq
	 */
	String sendOrder(OrderReq orderReq);

	/**
	 * 撤单
	 */
	void cancelOrder(CancelOrderReq cancelOrderReq);

	/**
	 * 发送持仓事件
	 * 
	 * @param position
	 */
	void emitPosition(Position position);

	/**
	 * 发送账户事件
	 * 
	 * @param account
	 */
	void emitAccount(Account account);

	/**
	 * 发送合约事件
	 * 
	 * @param contract
	 */
	void emitContract(Contract contract);

	/**
	 * 发送Tick事件
	 * 
	 * @param gatewayID
	 * @param gatewayDisplayName
	 * @param symbol
	 * @param exchange
	 * @param rtSymbol
	 * @param tradingDay
	 * @param actionDay
	 * @param actionTime
	 * @param dateTime
	 * @param status
	 * @param lastPrice
	 * @param lastVolume
	 * @param volume
	 * @param openInterest
	 * @param preOpenInterest
	 * @param preClosePrice
	 * @param preSettlePrice
	 * @param openPrice
	 * @param highPrice
	 * @param lowPrice
	 * @param upperLimit
	 * @param lowerLimit
	 * @param bidPrice1
	 * @param bidPrice2
	 * @param bidPrice3
	 * @param bidPrice4
	 * @param bidPrice5
	 * @param bidPrice6
	 * @param bidPrice7
	 * @param bidPrice8
	 * @param bidPrice9
	 * @param bidPrice10
	 * @param askPrice1
	 * @param askPrice2
	 * @param askPrice3
	 * @param askPrice4
	 * @param askPrice5
	 * @param askPrice6
	 * @param askPrice7
	 * @param askPrice8
	 * @param askPrice9
	 * @param askPrice10
	 * @param bidVolume1
	 * @param bidVolume2
	 * @param bidVolume3
	 * @param bidVolume4
	 * @param bidVolume5
	 * @param bidVolume6
	 * @param bidVolume7
	 * @param bidVolume8
	 * @param bidVolume9
	 * @param bidVolume10
	 * @param askVolume1
	 * @param askVolume2
	 * @param askVolume3
	 * @param askVolume4
	 * @param askVolume5
	 * @param askVolume6
	 * @param askVolume7
	 * @param askVolume8
	 * @param askVolume9
	 * @param askVolume10
	 */
	public void emitTick(String gatewayID, String gatewayDisplayName, String symbol, String exchange, String rtSymbol,
			String tickID, String tradingDay, String actionDay, String actionTime, DateTime dateTime, Integer status,
			double lastPrice, Integer lastVolume, Integer volume, double openInterest, long preOpenInterest,
			double preClosePrice, double preSettlePrice, double openPrice, double highPrice, double lowPrice,
			double upperLimit, double lowerLimit, double bidPrice1, double bidPrice2, double bidPrice3,
			double bidPrice4, double bidPrice5, double bidPrice6, double bidPrice7, double bidPrice8, double bidPrice9,
			double bidPrice10, double askPrice1, double askPrice2, double askPrice3, double askPrice4, double askPrice5,
			double askPrice6, double askPrice7, double askPrice8, double askPrice9, double askPrice10, int bidVolume1,
			int bidVolume2, int bidVolume3, int bidVolume4, int bidVolume5, int bidVolume6, int bidVolume7,
			int bidVolume8, int bidVolume9, int bidVolume10, int askVolume1, int askVolume2, int askVolume3,
			int askVolume4, int askVolume5, int askVolume6, int askVolume7, int askVolume8, int askVolume9,
			int askVolume10);

	/**
	 * 发送Tick事件
	 * 
	 * @param tick
	 */
	public void emitTick(Tick tick);

	/**
	 * 
	 * @param gatewayID
	 * @param gatewayDisplayName
	 * @param accountID
	 * @param rtAccountID
	 * @param symbol
	 * @param exchange
	 * @param rtSymbol
	 * @param tradeID
	 * @param rtTradeID
	 * @param orderID
	 * @param rtOrderID
	 * @param originalOrderID
	 * @param direction
	 * @param offset
	 * @param price
	 * @param volume
	 * @param tradingDay
	 * @param tradeDate
	 * @param tradeTime
	 * @param dateTime
	 */
	void emitTrade(String gatewayID, String gatewayDisplayName, String accountID, String rtAccountID, String symbol,
			String exchange, String rtSymbol, String tradeID, String rtTradeID, String orderID, String rtOrderID,
			String originalOrderID, String direction, String offset, double price, int volume, String tradingDay,
			String tradeDate, String tradeTime, DateTime dateTime);

	/**
	 * 发送成交事件
	 * 
	 * @param trade
	 */
	void emitTrade(Trade trade);

	/**
	 * 
	 * @param originalOrderID
	 * @param gatewayID
	 * @param gatewayDisplayName
	 * @param accountID
	 * @param rtAccountID
	 * @param symbol
	 * @param exchange
	 * @param rtSymbol
	 * @param orderID
	 * @param rtOrderID
	 * @param direction
	 * @param offset
	 * @param price
	 * @param totalVolume
	 * @param tradedVolume
	 * @param status
	 * @param tradingDay
	 * @param orderDate
	 * @param orderTime
	 * @param cancelTime
	 * @param activeTime
	 * @param updateTime
	 * @param frontID
	 * @param sessionID
	 */
	void emitOrder(String originalOrderID, String gatewayID, String gatewayDisplayName, String accountID,
			String rtAccountID, String symbol, String exchange, String rtSymbol, String orderID, String rtOrderID,
			String direction, String offset, double price, int totalVolume, int tradedVolume, String status,
			String tradingDay, String orderDate, String orderTime, String cancelTime, String activeTime,
			String updateTime, int frontID, int sessionID);

	/**
	 * 发送委托事件
	 * 
	 * @param order
	 */
	void emitOrder(Order order);

	/**
	 * 获取配置
	 * 
	 * @return
	 */
	GatewaySetting getGatewaySetting();

	/**
	 * 返回网关状态
	 * 
	 * @return
	 */
	boolean isConnected();

}
