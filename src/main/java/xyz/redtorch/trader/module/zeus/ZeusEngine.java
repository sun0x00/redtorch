package xyz.redtorch.trader.module.zeus;

import java.util.List;

import org.joda.time.DateTime;

import xyz.redtorch.trader.engine.event.FastEventDynamicHandler;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.zeus.entity.PositionDetail;
import xyz.redtorch.trader.module.zeus.strategy.Strategy;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;

/**
 * @author sun0x00@gmail.com
 */
public interface ZeusEngine extends FastEventDynamicHandler {

	/**
	 * 获取引擎类型,区分实盘模拟盘
	 * 
	 * @return
	 */
	int getEngineType();

	/**
	 * 
	 * @param orderReq
	 * @return
	 */
	String sendOrder(OrderReq orderReq,Strategy strategy);

	/**
	 * 撤单
	 * 
	 * @param rtOrderID
	 */
	void cancelOrder(String rtOrderID);

	/**
	 * Tick推送
	 * 
	 * @param tick
	 */
	void onTick(Tick tick);

	/**
	 * 委托回报
	 * 
	 * @param order
	 */
	void onOrder(Order order);

	/**
	 * 成交回报
	 * 
	 * @param trade
	 */
	void onTrade(Trade trade);
	
	/**
	 * 创建策略实例
	 * @param strategyID
	 */
	void createStrategyClassInstance(StrategySetting strategySetting);

	/**
	 * 卸载策略
	 * 
	 * @param strategyID
	 */
	void unloadStrategy(String strategyID);

	/**
	 * 加载所有策略
	 */
	void loadStartegy();
	

	/**
	 * 加载指定策略
	 * @param strategyID
	 */
	void loadStartegy(String strategyID);

	/**
	 * 初始化策略
	 * 
	 * @param strategyID
	 */
	void initStrategy(String strategyID);

	/**
	 * 启动策略
	 * 
	 * @param strategyID
	 */
	void startStrategy(String strategyID);

	/**
	 * 停止策略
	 * 
	 * @param strategyID
	 */
	void stopStrategy(String strategyID);

	/**
	 * 初始化所有策略
	 */
	void initAllStrategy();

	/**
	 * 启动所有策略
	 */
	void startAllStrategy();

	/**
	 * 停止所有策略
	 */
	void stopAllStrategy();

	/**
	 * 加载Tick数据,根据交易日向前推移,不包含交易日当天
	 * 
	 * @param offsetDay
	 * @return
	 */
	List<Tick> loadTickDataByOffsetDay(String tradingDay, int offsetDay, String rtSymbol);

	/**
	 * 加载Bar数据,根据交易日向前推移,不包含交易日当天
	 * 
	 * @param offsetDay
	 * @return
	 */
	List<Bar> loadBarDataByOffsetDay(String tradingDay, int offsetDay, String rtSymbol);

	/**
	 * 加载Tick数据,包含开始日期和结束日期
	 * 
	 * @param startDatetime
	 * @param endDateTime
	 * @return
	 */
	List<Tick> loadTickData(DateTime startDateTime, DateTime endDateTime, String rtSymbol);

	/**
	 * 加载Bar数据,包含开始日期和结束日期
	 * 
	 * @param startDatetime
	 * @param endDateTime
	 * @return
	 */
	List<Bar> loadBarData(DateTime startDateTime, DateTime endDateTime, String rtSymbol);

	/**
	 * 保存配置到数据库
	 * @param strategySetting
	 */
	void asyncSaveStrategySetting(StrategySetting strategySetting);
	
	/**
	 * 保存持仓到数据库
	 * @param insertPositionDetailList
	 */
	void asyncSavePositionDetail(List<PositionDetail> positionDetailList);

	/**
	 * 获取策略列表
	 * @return
	 */
	List<Strategy> getStragetyList();

	/**
	 * 获取合约最小变动价位
	 * @param rtSymbol
	 * @param gatewayID
	 * @return
	 */
	double getPriceTick(String rtSymbol, String gatewayID);
	
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
	 * 发出debug日志事件
	 * 
	 * @param eventEngine
	 * @param logContent
	 */
	void emitDebugLog(String logContent);
	
	/**
	 * 发出error日志事件
	 * 
	 * @param eventEngine
	 * @param logContent
	 */
	void emitErrorLog(String logContent);
	
	/**
	 * 发出warn日志事件
	 * 
	 * @param eventEngine
	 * @param logContent
	 */
	void emitWarnLog(String logContent);
	
	/**
	 * 发出info日志事件
	 * 
	 * @param eventEngine
	 * @param logContent
	 */
	void emitInfoLog(String logContent);
}
