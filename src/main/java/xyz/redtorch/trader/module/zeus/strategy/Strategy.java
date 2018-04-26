package xyz.redtorch.trader.module.zeus.strategy;

import java.util.List;
import java.util.Map;

import xyz.redtorch.trader.engine.event.FastEventDynamicHandler;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.zeus.entity.ContractPositionDetail;
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
	 * 在一分钟Bar产生时调用
	 * <br/>
	 * 注意,此处默认<b>过滤</b>同一个策略使用多个接口订阅同一个品种导致的同一个品种重复调用
	 * @param bar
	 * @throws Exception
	 */
	void onBar(Bar bar) throws Exception;
	
	/**
	 * 在X分钟Bar产生时调用
	 * <br/>
	 * 注意,此处默认<b>过滤</b>同一个策略使用多个接口订阅同一个品种导致的同一个品种重复调用
	 * @param bar
	 * @throws Exception
	 */
	void onXMinBar(Bar bar) throws Exception;

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
	 * 获取策略持仓
	 * @return
	 */
	Map<String, ContractPositionDetail> getContractPositionMap();

	/**
	 * 获取日志便捷字符串
	 * @return
	 */
	String getLogStr();

	/**
	 * 获取存入数据库的变量列表
	 * @return
	 */
	List<String> getSyncVarList();

	/**
	 * 获取参数字典
	 * @return
	 */
	Map<String, String> getParamMap();

	/**
	 * 获取变量字典
	 * @return
	 */
	Map<String, String> getVarMap();
	
	/**
	 * 获取未完成的停止单字典
	 * @return
	 */
	Map<String, StopOrder> getWorkingStopOrderMap();	
	
	/**
	 * 保存持仓
	 */
	void savePosition();
	
	/**
	 * 重置策略,一般用于连续回测
	 * @param strategySetting
	 */
	void resetStrategy(StrategySetting strategySetting);

	String sendOrder(String rtSymbol, String orderType, String priceType, double price, int volume, String gatewayID);
	
	String sendStopOrder(String rtSymbol, String orderType, String priceType, double price, int volume,
			String gatewayID, Strategy strategy);

	void cancelStopOrder(String stopOrderID);
	
	void cancelOrder(String rtOrderID);
	
	void cancelAll();
	
	void buy(String rtSymbol, int volume, double price, String gatewayID);
	
	void sell(String rtSymbol, int volume, double price, String gatewayID);

	void sellTd(String rtSymbol, int volume, double price, String gatewayID);
	
	void sellYd(String rtSymbol, int volume, double price, String gatewayID);
	
	void sellShort(String rtSymbol, int volume, double price, String gatewayID);
	
	void buyToCover(String rtSymbol, int volume, double price, String gatewayID);
	
	void buyToCoverTd(String rtSymbol, int volume, double price, String gatewayID);
	
	void buyToCoverYd(String rtSymbol, int volume, double price, String gatewayID);
	
	void buyByPreset(String rtSymbol, double price);
	
	void sellByPosition(String rtSymbol, double price);
	
	void sellTdByPosition(String rtSymbol, double price);
	
	void sellYdByPosition(String rtSymbol, double price);
	
	void sellShortByPreset(String rtSymbol, double price);
	
	void buyToCoverByPosition(String rtSymbol, double price);
	
	void buyToCoverTdByPosition(String rtSymbol, double price);
	
	void buyToCoverYdByPosition(String rtSymbol, double price);
	
	void buyToLockByPosition(String rtSymbol, double price);
	
	void sellShortToLockByPosition(String rtSymbol, double price);
	
	void processOrder(Order order);

	void processTick(Tick tick);

	void processTrade(Trade trade);

	void processBar(Bar bar);

	
	

}