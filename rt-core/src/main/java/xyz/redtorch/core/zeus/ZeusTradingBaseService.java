package xyz.redtorch.core.zeus;

import java.util.Map;

import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.zeus.entity.StrategyProcessReport;

public interface ZeusTradingBaseService {
	/**
	 * 注册原始委托
	 * @param orderReq
	 */
	void registerOrderReq(OrderReq orderReq);
	
	/**
	 * 获取原始委托
	 * @param originalOrderID
	 * @return
	 */
	OrderReq getOrderReq(String originalOrderID);
	
	/**
	 * 注册原始OrderID
	 * 
	 * @param rtOrderID
	 * @param originalOrderID
	 */
	void registerOriginalOrderID(String rtOrderID, String originalOrderID);

	/**
	 * 获取原始OrderID
	 * 
	 * @param rtOrderID
	 * @return
	 */
	String getOriginalOrderID(String rtOrderID);

	/**
	 * 获取OrderID
	 * 
	 * @param originalOrderID
	 * @return
	 */
	String getRtOrderID(String originalOrderID);

	/**
	 * 策略状态报送
	 */
	void updateReport(StrategyProcessReport strategyProcessReport);

	/**
	 * 检查是否存在相同ID的策略在运行
	 * 
	 * @param strategyID
	 * @return
	 */
	boolean duplicationCheck(String strategyID);

	/**
	 * 获取所有存在的策略
	 * 
	 * @return
	 */
	Map<String, StrategyProcessReport> getReportMap();

}
