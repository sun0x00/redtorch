package xyz.redtorch.common.service;

import java.time.LocalDateTime;
import java.util.List;

import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;

public interface MarketDataService {

	final String COLLECTION_NAME_PREFIX_TICK = "tick_";
	final String COLLECTION_NAME_BAR_1_MIN = "bar_1_min";
	final String COLLECTION_NAME_BAR_3_MIN = "bar_3_min";
	final String COLLECTION_NAME_BAR_5_MIN = "bar_5_min";
	final String COLLECTION_NAME_BAR_15_MIN = "bar_15_min";
	final String COLLECTION_NAME_BAR_1_DAY = "bar_1_day";
	final String COLLECTION_NAME_BAR_5_SEC = "bar_5_sec";


	List<BarField> queryBar5SecList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);
	
	List<BarField> queryBar1MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryBar3MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryBar5MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryBar15MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryBar1DayList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<TickField> queryTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);
	

	List<BarField> queryHistBar5SecList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);
	
	List<BarField> queryHistBar1MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryHistBar3MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryHistBar5MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryHistBar15MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryHistBar1DayList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<TickField> queryHistTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);
	
	
	List<BarField> queryTodayBar5SecList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);
	
	List<BarField> queryTodayBar1MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryTodayBar3MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryTodayBar5MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<BarField> queryTodayBar15MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);

	List<TickField> queryTodayTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol);
	

	List<BarField> queryBar5SecList(long startTimestamp, long endTimestamp, String unifiedSymbol);
	
	List<BarField> queryBar1MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryBar3MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryBar5MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryBar15MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryBar1DayList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<TickField> queryTickList(long startTimestamp, long endTimestamp, String unifiedSymbol);
	

	List<BarField> queryHistBar5SecList(long startTimestamp, long endTimestamp, String unifiedSymbol);
	
	List<BarField> queryHistBar1MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryHistBar3MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryHistBar5MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryHistBar15MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryHistBar1DayList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<TickField> queryHistTickList(long startTimestamp, long endTimestamp, String unifiedSymbol);
	
	
	List<BarField> queryTodayBar5SecList(long startTimestamp, long endTimestamp, String unifiedSymbol);
	
	List<BarField> queryTodayBar1MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryTodayBar3MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryTodayBar5MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<BarField> queryTodayBar15MinList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	List<TickField> queryTodayTickList(long startTimestamp, long endTimestamp, String unifiedSymbol);

	
	boolean upsertTick(String dbName, String collectionName, TickField tick);

	boolean upsertTick(String dbName, String collectionName, List<TickField> tickList);

	boolean upsertBar(String dbName, String collectionName, BarField bar);

	boolean upsertBar(String dbName, String collectionName, List<BarField> barList);
	
	
	boolean upsertBar5SecListToHistDB( List<BarField> barList);
	
	boolean upsertBar1MinListToHistDB( List<BarField> barList);

	boolean upsertBar3MinListToHistDB( List<BarField> barList);

	boolean upsertBar5MinListToHistDB( List<BarField> barList);

	boolean upsertBar15MinListToHistDB( List<BarField> barList);

	boolean upsertBar1DayListToHistDB( List<BarField> barList);

	boolean upsertTickListToHistDB( List<TickField> tickList,String unifiedSymbol);
	

	boolean upsertBar5SecListToTodayDB( List<BarField> barList);
	
	boolean upsertBar1MinListToTodayDB( List<BarField> barList);

	boolean upsertBar3MinListToTodayDB( List<BarField> barList);

	boolean upsertBar5MinListToTodayDB( List<BarField> barList);

	boolean upsertBar15MinListToTodayDB( List<BarField> barList);

	boolean upsertTickListToTodayDB( List<TickField> tickList,String unifiedSymbol);
	
	
	boolean upsertBar5SecToTodayDB(BarField bar);
	
	boolean upsertBar1MinToTodayDB(BarField bar);

	boolean upsertBar3MinToTodayDB(BarField bar);

	boolean upsertBar5MinToTodayDB(BarField bar);

	boolean upsertBar15MinToTodayDB(BarField bar);

	boolean upsertTickToTodayDB(TickField tick);
	
	
	/**
	 *  加载Tick数据,根据最后时间和数量向前推移
	 * @param endTimestamp
	 * @param limit
	 * @param unifiedSymbol
	 * @return
	 */
	List<TickField> queryTickListByEndTimestampAndLimit(long endTimestamp, int limit, String unifiedSymbol);

	/**
	 *  加载1分钟Bar数据,根据最后时间和数量向前推移
	 * @param endTimestamp
	 * @param limit
	 * @param unifiedSymbol
	 * @return
	 */
	List<BarField> queryBar1MinListByEndTimestampAndLimit(long endTimestamp, int limit, String unifiedSymbol);
	
}
