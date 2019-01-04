package xyz.redtorch.core.service;

import java.util.List;

import org.joda.time.DateTime;

//import xyz.redtorch.core.base.BaseConfig;
//import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.utils.MongoDBClient;

/**
 * @author sun0x00@gmail.com
 */
public interface MongoDBService {

	String dailyMarketDataTickCollection = "DailyMarketDataTick";

	List<Bar> loadBarDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);

	List<Tick> loadTickDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);
	
	MongoDBClient getMdDBClient();

	MongoDBClient getDefaultDBClient();

	boolean saveTickToDailyDB(Tick tick);
	
	List<Tick> loadTickDataListFromDailyDB(String rtSymbol);
}
