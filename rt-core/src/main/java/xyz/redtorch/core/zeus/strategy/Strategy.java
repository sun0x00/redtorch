package xyz.redtorch.core.zeus.strategy;

import java.util.Map;

import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.zeus.entity.ContractPositionDetail;

/**
 * @author sun0x00@gmail.com
 */
public interface Strategy {

	/**
	 * 获取策略ID
	 * 
	 * @return
	 */
	String getID();

	/**
	 * 获取策略名称
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 返回引擎类型,区分实盘和回测
	 * 
	 * @return
	 */
	int getEngineType();

	/**
	 * 初始化
	 */
	void init();

	/**
	 * 重置策略,一般被回测引擎使用,用于实现连续回测
	 * 
	 * @param strategySetting
	 */
	void resetStrategy(StrategySetting strategySetting);

	/**
	 * 开始交易
	 */
	void startTrading();

	/**
	 * 
	 * @param finishedCorrectly 如果正常停止，为true，如果因异常导致停止，返回false
	 */
	void stopTrading(boolean finishedCorrectly);

	/**
	 * 销毁通知
	 */
	void destroy();

	/**
	 * 是否已经初始化
	 * 
	 * @return
	 */
	boolean isInitStatus();

	/**
	 * 是否处于交易状态
	 * 
	 * @return
	 */
	boolean isTrading();

	/**
	 * 在策略初始化时调用
	 * 
	 * @throws Exception
	 */
	void onInit() throws Exception;

	/**
	 * 在开始交易时调用
	 * 
	 * @throws Exception
	 */
	void onStartTrading() throws Exception;

	/**
	 * 在停止交易时调用
	 * 
	 * @param finishedCorrectly 如果正常停止，为true，如果因异常导致停止，返回false
	 * @throws Exception
	 */
	void onStopTrading(boolean finishedCorrectly) throws Exception;

	/**
	 * 在有Tick数据时调用 <br/>
	 * 注意,此处默认<b>不过滤</b>同一个策略使用多个网关订 阅同一个品种导致的同一个品种重复调用
	 * 
	 * @param tick
	 * @throws Exception
	 */
	void onTick(Tick tick) throws Exception;

	/**
	 * 在有委托数据时调用
	 * 
	 * @param order
	 * @throws Exception
	 */
	void onOrder(Order order) throws Exception;

	/**
	 * 在有交易数据时调用
	 * 
	 * @param trade
	 * @throws Exception
	 */
	void onTrade(Trade trade) throws Exception;

	/**
	 * 处理Order
	 * 
	 * @param order
	 */
	void processOrder(Order order);

	/**
	 * 处理Tick
	 * 
	 * @param tick
	 */
	void processTick(Tick tick);

	/**
	 * 处理Bar
	 * 
	 * @param bar
	 */
	void processBar(Bar bar);

	/**
	 * 处理成交
	 * 
	 * @param trade
	 */
	void processTrade(Trade trade);

	/**
	 * 获取日志拼接字符串
	 * 
	 * @return
	 */
	String getLogStr();

	/**
	 * 获取策略设置
	 * 
	 * @return
	 */
	StrategySetting getStrategySetting();

	/**
	 * 获取持仓结构
	 * 
	 * @return
	 */
	Map<String, ContractPositionDetail> getContractPositionMap();

	/**
	 * 保存配置
	 */
	void saveStrategySetting();

	/**
	 * 设置变量值
	 */
	void setVarValue(String key, String value);

	/**
	 * 发单
	 * 
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
	 * 撤单
	 * 
	 * @param rtOrderID
	 */
	void cancelOrder(String rtOrderID);

	/**
	 * 撤销所有委托
	 */
	void cancelAll();

	/**
	 * 买开多
	 * 
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	String buy(String rtSymbol, int volume, double price, String gatewayID);

	/**
	 * 卖平多
	 * 
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	String sell(String rtSymbol, int volume, double price, String gatewayID);

	/**
	 * 卖平今多
	 * 
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	String sellTd(String rtSymbol, int volume, double price, String gatewayID);

	/**
	 * 卖平昨多
	 * 
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	String sellYd(String rtSymbol, int volume, double price, String gatewayID);

	/**
	 * 卖开空
	 * 
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	String sellShort(String rtSymbol, int volume, double price, String gatewayID);

	/**
	 * 买平空
	 * 
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	String buyToCover(String rtSymbol, int volume, double price, String gatewayID);

	/**
	 * 买平今空
	 * 
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	String buyToCoverTd(String rtSymbol, int volume, double price, String gatewayID);

	/**
	 * 买平昨空
	 * 
	 * @param rtSymbol
	 * @param volume
	 * @param price
	 * @param gatewayID
	 */
	String buyToCoverYd(String rtSymbol, int volume, double price, String gatewayID);

}