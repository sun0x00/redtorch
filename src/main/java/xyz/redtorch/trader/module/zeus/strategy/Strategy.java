package xyz.redtorch.trader.module.zeus.strategy;

import java.util.Map;

import xyz.redtorch.trader.engine.event.FastEventDynamicHandler;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.zeus.entity.StopOrder;

/**
 * @author sun0x00@gmail.com
 */
public interface Strategy extends FastEventDynamicHandler {
	
	/**
	 * 返回策略ID
	 * @return
	 */
	String getID();

	/**
	 * 返回策略名
	 * @return
	 */
	String getName();
	
	/**
	 * 返回引擎类型,区分实盘和回测
	 * @return
	 */
	int getEngineType();
	
	/**
	 * 初始化
	 */
	void init();

	/**
	 * 开始交易
	 */
	void startTrading();
	
	/**
	 * 停止交易
	 * @param isException 是否因异常触发
	 */
	void stopTrading(boolean isException);
	
	/**
	 * 是否已经初始化
	 * @return
	 */
	boolean isInitStatus();
	
	/**
	 * 是否处于交易状态
	 * @return
	 */
	boolean isTrading();
	
	/** 
	 * 在策略初始化时调用
	 * @throws Exception
	 */
	void onInit() throws Exception;

	/**
	 * 在开始交易时调用
	 * @throws Exception
	 */
	void onStartTrading() throws Exception;

	/**
	 * 在停止交易时调用
	 * @param isException
	 * @throws Exception
	 */
	void onStopTrading(boolean isException) throws Exception;
	
	/**
	 * 在有Tick数据时调用
	 * <br/>
	 * 注意,此处默认<b>不过滤</b>同一个策略使用多个接口订 阅同一个品种导致的同一个品种重复调用
	 * @param tick
	 * @throws Exception
	 */
	void onTick(Tick tick) throws Exception;
	
	/**
	 * 在有委托数据时调用
	 * @param order
	 * @throws Exception
	 */
	void onOrder(Order order) throws Exception;

	/**
	 * 在有交易数据时调用
	 * @param trade
	 * @throws Exception
	 */
	void onTrade(Trade trade) throws Exception;

	/**
	 * 在策略停止时调用
	 * @param StopOrder
	 * @throws Exception
	 */
	void onStopOrder(StopOrder StopOrder) throws Exception;

	/**
	 * 获取策略设置
	 * @return
	 */
	StrategySetting getStrategySetting();

	/**
	 * 获取日志便捷字符串
	 * @return
	 */
	String getLogStr();
	
	/**
	 * 获取未完成的停止单字典
	 * @return
	 */
	Map<String, StopOrder> getWorkingStopOrderMap();	
	
	/**
	 * 保存配置
	 */
	void saveStrategySetting();
	
	/**
	 * 设置变量值
	 */
	void setVarValue(String key,String value);
	
	/**
	 * 发单
	 * @param rtSymbol
	 * @param orderType
	 * @param priceType
	 * @param price
	 * @param volume
	 * @param gatewayID
	 * @return
	 */
	String sendOrder(String rtSymbol, String orderType, String priceType, double price, int volume, String gatewayID);
	
	/**
	 * 发送停止单
	 * @param rtSymbol
	 * @param orderType
	 * @param priceType
	 * @param price
	 * @param volume
	 * @param gatewayID
	 * @param strategy
	 * @return
	 */
	String sendStopOrder(String rtSymbol, String orderType, String priceType, double price, int volume,
			String gatewayID, Strategy strategy);

	/**
	 * 撤销停止单
	 * @param stopOrderID
	 */
	void cancelStopOrder(String stopOrderID);
	
	/**
	 * 撤单
	 * @param rtOrderID
	 */
	void cancelOrder(String rtOrderID);
	
	/**
	 * 撤销所有委托
	 */
	void cancelAll();
	
	/**
	 * 买开多
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	void buy(String rtSymbol, int volume, double price, String gatewayID);
	
	/**
	 * 卖平多
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	void sell(String rtSymbol, int volume, double price, String gatewayID);

	/**
	 * 卖平今多
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	void sellTd(String rtSymbol, int volume, double price, String gatewayID);
	
	/**
	 * 卖平昨多
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	void sellYd(String rtSymbol, int volume, double price, String gatewayID);
	
	/**
	 * 卖开空
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	void sellShort(String rtSymbol, int volume, double price, String gatewayID);
	
	/**
	 * 买平空
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	void buyToCover(String rtSymbol, int volume, double price, String gatewayID);
	
	/**
	 * 买平今空
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	void buyToCoverTd(String rtSymbol, int volume, double price, String gatewayID);
	
	/**
	 * 买平昨空
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	void buyToCoverYd(String rtSymbol, int volume, double price, String gatewayID);
	
	
	/**
	 * 处理Order
	 * @param order
	 */
	void processOrder(Order order);

	/**
	 * 处理Tick
	 * @param tick
	 */
	void processTick(Tick tick);

	/**
	 * 处理成交
	 * @param trade
	 */
	void processTrade(Trade trade);


	
	

}