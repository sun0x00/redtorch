package xyz.redtorch.core.zeus;

import java.util.List;

import org.joda.time.DateTime;

import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.zeus.entity.PositionDetail;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

/**
 * @author sun0x00@gmail.com
 */
public interface ZeusEngineService {

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
	void sendOrder(OrderReq orderReq);

	/** 
	 * 撤单
	 * @param originalOrderID
	 * @param operatorID
	 */
	void cancelOrder(String originalOrderID,String operatorID);

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
	 * 
	 * @param strategySetting
	 */
	void asyncSaveStrategySetting(StrategySetting strategySetting);

	/**
	 * 保存持仓到数据库
	 * 
	 * @param insertPositionDetailList
	 */
	void asyncSavePositionDetail(List<PositionDetail> positionDetailList);

	/**
	 * 获取合约最小变动价位
	 * 
	 * @param rtSymbol
	 * @param gatewayID
	 * @return
	 */
	double getPriceTick(String rtSymbol, String gatewayID);

	/**
	 * 获取合约
	 * 
	 * @param fuzzySymbol
	 * @return
	 */
	Contract getContractByFuzzySymbol(String fuzzySymbol);

	/**
	 * 获取合约
	 * 
	 * @param rtSymbol
	 * @param gatewayID
	 * @return
	 */
	Contract getContract(String rtSymbol, String gatewayID);

}
