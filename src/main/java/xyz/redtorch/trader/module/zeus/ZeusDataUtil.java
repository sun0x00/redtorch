package xyz.redtorch.trader.module.zeus;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.module.zeus.entity.PositionDetail;

/**
 * @author sun0x00@gmail.com
 */
public interface ZeusDataUtil {
	
	List<Bar> loadBarDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);

	List<Tick> loadTickDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);
	
	Map<String, String> loadStrategySyncVarMap(String strategyID);

	void saveStrategySyncVarMap(Map<String,String> syncVarMapWithNameAndID) ;

	List<PositionDetail> loadStrategyPositionDetails(String tradingDay, String strategyID, String strategyName);

	void saveStrategyPositionDetail(PositionDetail positionDetail);
}
