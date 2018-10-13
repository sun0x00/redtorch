package xyz.redtorch.core.zeus;

import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;

public interface ZeusMmapService {

	final int DATA_ORDER = 0x01;
	final int DATA_TRADE = 0x02;
	final int DATA_TICK = 0x03;
	final int DATA_ORDERREQ = 0x04;
	final int DATA_COMMAND = 0x05;
	final int DATA_CANCEL_ORDER = 0x06;

	final int COMMAND_INIT_STARTEGY = 0x01;
	final int COMMAND_START_STARTEGY = 0x02;
	final int COMMAND_STOP_STARTEGY = 0x03;
	final int COMMAND_RELOAD_STARTEGY = 0x04;
	final int COMMAND_INIT_ALL_STARTEGY = 0x05;
	final int COMMAND_START_ALL_STARTEGY = 0x06;
	final int COMMAND_STOP_ALL_STARTEGY = 0x07;
	final int COMMAND_RELOAD_ALL_STARTEGY = 0x08;

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
	 * 重新加载策略
	 * 
	 * @param strategyID
	 */
	void reloadStrategy(String strategyID);

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
	 * 重新加载所有策略
	 */
	void reloadAllStrategy();

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

}
