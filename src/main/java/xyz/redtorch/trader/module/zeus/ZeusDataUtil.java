package xyz.redtorch.trader.module.zeus;

import java.util.List;

import org.joda.time.DateTime;

import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.module.zeus.entity.PositionDetail;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;

/**
 * @author sun0x00@gmail.com
 */
public interface ZeusDataUtil {
	
	List<Bar> loadBarDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);

	List<Tick> loadTickDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);
	
    List<PositionDetail> loadStrategyPositionDetails(String tradingDay, String strategyID, String strategyName);

	void saveStrategyPositionDetail(PositionDetail positionDetail);
	
	StrategySetting loadStrategySetting(String strategyID);

	void saveStrategySetting(StrategySetting strategySetting) ;

	List<StrategySetting> loadStrategySettings();
}
