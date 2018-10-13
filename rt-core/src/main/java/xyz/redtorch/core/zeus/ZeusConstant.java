package xyz.redtorch.core.zeus;

/**
 * Zeus引擎常量
 * 
 * @author sun0x00@gmail.com
 *
 */
public class ZeusConstant {
	// 引擎类型,区分策略环境
	public static final int ENGINE_TYPE_BACKTESTING = 0;
	public static final int ENGINE_TYPE_TRADING = 1;
	// 委托类型
	public static final String ORDER_BUY = "BUY"; // 买开
	public static final String ORDER_SELL = "SELL"; // 卖平
	public static final String ORDER_SELLTODAY = "SELLTODAY"; // 卖平今
	public static final String ORDER_SELLYESTERDAY = "SELLYESTERDAY"; // 卖平昨
	public static final String ORDER_SHORT = "SHORT"; // 卖开
	public static final String ORDER_COVER = "COVER"; // 买平
	public static final String ORDER_COVERTODAY = "COVERTODAY"; // 买平今
	public static final String ORDER_COVERYESTERDAY = "COVERYESTERDAY"; // 买平昨
	// 本地停止单状态
	public static final String STOPORDER_WAITING = "WAITING"; // 等待中
	public static final String STOPORDER_CANCELLED = "CANCELLED"; // 已撤销
	public static final String STOPORDER_TRIGGERED = "TRIGGERED"; // 已触发
	// 本地停止单前缀
	public static final String STOPORDERPREFIX = "Z_STOP_ORDER."; //
}
