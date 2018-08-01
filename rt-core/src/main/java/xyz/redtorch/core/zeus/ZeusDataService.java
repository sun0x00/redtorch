package xyz.redtorch.core.zeus;

import java.util.List;

import org.joda.time.DateTime;

import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.zeus.entity.PositionDetail;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

/**
 * @author sun0x00@gmail.com
 */
public interface ZeusDataService {
	
	List<Bar> loadBarDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);

	List<Tick> loadTickDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);
	
    List<PositionDetail> loadStrategyPositionDetails(String tradingDay, String strategyID, String strategyName);

	void saveStrategyPositionDetail(PositionDetail positionDetail);
	
	StrategySetting loadStrategySetting(String strategyID);

	void saveStrategySetting(StrategySetting strategySetting) ;

	List<StrategySetting> loadStrategySettings();
}
