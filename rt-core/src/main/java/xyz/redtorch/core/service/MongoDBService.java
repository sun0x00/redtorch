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

//	public final static String MINUTE_DB_NAME = BaseConfig.rtConfig.getString("mongodb.instance.md.dbname.minute");
//	public final static String TICK_DB_NAME = BaseConfig.rtConfig.getString("mongodb.instance.md.dbname.tick");
//	public final static String DAILY_DB_NAME = BaseConfig.rtConfig.getString("mongodb.instance.md.dbname.daily");
//	public final static String DEFAULT_DB_NAME = RtConstant.RED_TORCH_DB_NAME+"_"+BaseConfig.rtConfig.getString("rt.client.id");

	List<Bar> loadBarDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);

	List<Tick> loadTickDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol);

	MongoDBClient getMdDBClient();

	MongoDBClient getDefaultDBClient();
}
