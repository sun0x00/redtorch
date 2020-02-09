package xyz.redtorch.common.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;

import xyz.redtorch.common.mongo.MongoDBClient;
import xyz.redtorch.common.service.MarketDataService;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
import xyz.redtorch.pb.CoreEnum.ProductClassEnum;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

public class MarketDataServiceBasicImpl implements MarketDataService {

	private static final Logger logger = LoggerFactory.getLogger(MarketDataServiceBasicImpl.class);

	private MongoDBClient todayMarketDataDBClient;
	private MongoDBClient histMarketDataDBClient;
	private String todayMarketDataDBName;
	private String histMarketDataDBName;

	public void initSetting(MongoDBClient todayMarketDataDBClient, String todayMarketDataDBName, MongoDBClient histMarketDataDBClient, String histMarketDataDBName) {
		this.todayMarketDataDBClient = todayMarketDataDBClient;
		this.todayMarketDataDBName = todayMarketDataDBName;
		this.histMarketDataDBClient = histMarketDataDBClient;
		this.histMarketDataDBName = histMarketDataDBName;
	}

	@Override
	public List<BarField> queryHistBar5SecList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryHistBar5SecList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryHistBar1MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryHistBar1MinList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryHistBar3MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryHistBar3MinList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryHistBar5MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryHistBar5MinList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryHistBar15MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryHistBar15MinList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryHistBar1DayList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryHistBar1DayList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<TickField> queryHistTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryHistTickList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryTodayBar5SecList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryTodayBar5SecList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryTodayBar1MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryTodayBar1MinList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryTodayBar3MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryTodayBar3MinList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryTodayBar5MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryTodayBar5MinList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryTodayBar15MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryTodayBar15MinList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<TickField> queryTodayTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		long startTimestamp = CommonUtils.localDateTimeToMills(startDateTime);
		long endTimestamp = CommonUtils.localDateTimeToMills(endDateTime);
		return queryTodayTickList(startTimestamp, endTimestamp, unifiedSymbol);
	}

	@Override
	public List<BarField> queryBar5SecList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar5SecList(startDateTime, endDateTime, unifiedSymbol);
		barList.addAll(this.queryTodayBar5SecList(startDateTime, endDateTime, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar1MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar1MinList(startDateTime, endDateTime, unifiedSymbol);
		barList.addAll(this.queryTodayBar1MinList(startDateTime, endDateTime, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar3MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar3MinList(startDateTime, endDateTime, unifiedSymbol);
		barList.addAll(this.queryTodayBar3MinList(startDateTime, endDateTime, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar5MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar5MinList(startDateTime, endDateTime, unifiedSymbol);
		barList.addAll(this.queryTodayBar5MinList(startDateTime, endDateTime, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar15MinList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar15MinList(startDateTime, endDateTime, unifiedSymbol);
		barList.addAll(this.queryTodayBar15MinList(startDateTime, endDateTime, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar1DayList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar1DayList(startDateTime, endDateTime, unifiedSymbol);
		return barList;
	}

	@Override
	public List<TickField> queryTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String unifiedSymbol) {
		List<TickField> tickList = this.queryHistTickList(startDateTime, endDateTime, unifiedSymbol);
		tickList.addAll(this.queryTodayTickList(startDateTime, endDateTime, unifiedSymbol));
		return tickList;
	}

	@Override
	public boolean upsertBar(String dbName, String collectionName, BarField bar) {

		Document barDocument = barToDocument(bar);

		Document filterDocument = new Document();
		filterDocument.put("unifiedSymbol", bar.getUnifiedSymbol());
		filterDocument.put("actionTimestamp", bar.getActionTimestamp());

		todayMarketDataDBClient.getDatabase(dbName).getCollection(collectionName).createIndex(Indexes.ascending("actionTimestamp", "unifiedSymbol"));
		return todayMarketDataDBClient.upsert(dbName, collectionName, barDocument, filterDocument);
	}

	@Override
	public boolean upsertBar(String dbName, String collectionName, List<BarField> barList) {

		if (barList == null || barList.isEmpty()) {
			logger.error("更新插入Bar集合错误,数据集合为空");
			return false;
		}

		List<WriteModel<Document>> writeModelList = new ArrayList<WriteModel<Document>>();

		long beginTime = System.currentTimeMillis();
		for (BarField bar : barList) {
			Document filterDocument = new Document();
			filterDocument.put("unifiedSymbol", bar.getUnifiedSymbol());
			filterDocument.put("actionTimestamp", bar.getActionTimestamp());

			Document barDocument = barToDocument(bar);
			ReplaceOptions replaceOptions = new ReplaceOptions();
			replaceOptions.upsert(true);

			ReplaceOneModel<Document> replaceOneModel = new ReplaceOneModel<Document>(filterDocument, barDocument, replaceOptions);
			writeModelList.add(replaceOneModel);
		}
		logger.info("更新插入Bar集合,数据库{},集合{},数据转换耗时{}ms,共{}条数据", dbName, collectionName, (System.currentTimeMillis() - beginTime), barList.size());
		beginTime = System.currentTimeMillis();
		todayMarketDataDBClient.getDatabase(dbName).getCollection(collectionName).createIndex(Indexes.ascending("actionTimestamp", "unifiedSymbol"));
		todayMarketDataDBClient.getDatabase(dbName).getCollection(collectionName).bulkWrite(writeModelList);
		logger.info("更新插入Bar集合,数据库{},集合{},数据库操作耗时{}ms,共{}条操作", dbName, collectionName, (System.currentTimeMillis() - beginTime), writeModelList.size());
		return true;
	}

	@Override
	public boolean upsertTick(String dbName, String collectionName, TickField tick) {
		Document tickDocument = tickToDocument(tick);
		Document filterDocument = new Document();
		filterDocument.put("unifiedSymbol", tick.getUnifiedSymbol());
		filterDocument.put("actionTimestamp", tick.getActionTimestamp());

		todayMarketDataDBClient.getDatabase(dbName).getCollection(collectionName).createIndex(Indexes.ascending("actionTimestamp"));
		return todayMarketDataDBClient.upsert(dbName, collectionName, tickDocument, filterDocument);
	}

	@Override
	public boolean upsertTick(String dbName, String collectionName, List<TickField> tickList) {

		if (tickList == null || tickList.isEmpty()) {
			logger.error("更新插入Tick集合错误,数据集合为空");
			return false;
		}

		List<WriteModel<Document>> writeModelList = new ArrayList<WriteModel<Document>>();

		long beginTime = System.currentTimeMillis();
		for (TickField tick : tickList) {
			Document filterDocument = new Document();
			filterDocument.put("unifiedSymbol", tick.getUnifiedSymbol());
			filterDocument.put("actionTimestamp", tick.getActionTimestamp());

			Document tickDocument = tickToDocument(tick);
			ReplaceOptions replaceOptions = new ReplaceOptions();
			replaceOptions.upsert(true);

			ReplaceOneModel<Document> replaceOneModel = new ReplaceOneModel<Document>(filterDocument, tickDocument, replaceOptions);
			writeModelList.add(replaceOneModel);
		}
		logger.info("更新插入Tick集合,数据库{},集合{},数据转换耗时{}ms,共{}条数据", dbName, collectionName, (System.currentTimeMillis() - beginTime), tickList.size());
		beginTime = System.currentTimeMillis();
		todayMarketDataDBClient.getDatabase(dbName).getCollection(collectionName).createIndex(Indexes.ascending("actionTimestamp"));
		todayMarketDataDBClient.getDatabase(dbName).getCollection(collectionName).bulkWrite(writeModelList);
		logger.info("更新插入Tick集合,数据库{},集合{},数据库操作耗时{}ms,共{}条操作", dbName, collectionName, (System.currentTimeMillis() - beginTime), writeModelList.size());
		return true;
	}

	public List<BarField> documentListToBarList(List<Document> documentList, String gatewayId) {
		List<BarField> barList = new ArrayList<>();
		if (documentList != null && !documentList.isEmpty()) {
			long beginTime = System.currentTimeMillis();
			for (Document document : documentList) {
				try {
					BarField.Builder barBuilder = BarField.newBuilder();

					ContractField.Builder contractBuilder = ContractField.newBuilder();

					String unifiedSymbol = document.getString("unifiedSymbol");
					String[] unifiedSymbolStrArr = unifiedSymbol.split("@");
					String symbol = unifiedSymbolStrArr[0];
					String exchangeStr = unifiedSymbolStrArr[1];
					String productClassStr = unifiedSymbolStrArr[2];

					contractBuilder.setUnifiedSymbol(unifiedSymbol);
					contractBuilder.setSymbol(symbol);
					contractBuilder.setExchange(ExchangeEnum.valueOf(exchangeStr));
					contractBuilder.setProductClass(ProductClassEnum.valueOf(productClassStr));

					barBuilder.setUnifiedSymbol(unifiedSymbol);
					barBuilder.setGatewayId(gatewayId);

					barBuilder.setActionDay(document.getString("actionDay"));
					barBuilder.setActionTime(document.getString("actionTime"));
					barBuilder.setActionTimestamp(document.getLong("actionTimestamp"));
					barBuilder.setClosePrice(document.getDouble("closePrice"));
					barBuilder.setHighPrice(document.getDouble("highPrice"));
					barBuilder.setLowPrice(document.getDouble("lowPrice"));
					barBuilder.setNumTrades(document.getLong("numTrades"));
					barBuilder.setNumTradesDelta(document.getLong("numTradesDelta"));
					barBuilder.setOpenInterest(document.getDouble("openInterest"));
					barBuilder.setOpenInterestDelta(document.getDouble("openInterestDelta"));
					barBuilder.setOpenPrice(document.getDouble("openPrice"));
					barBuilder.setTradingDay(document.getString("tradingDay"));
					barBuilder.setTurnover(document.getDouble("turnover"));
					barBuilder.setTurnoverDelta(document.getDouble("turnoverDelta"));
					barBuilder.setVolume(document.getLong("volume"));
					barBuilder.setVolumeDelta(document.getLong("volumeDelta"));

					barList.add(barBuilder.build());
				} catch (Exception e) {
					logger.error("数据转换错误", e);
				}
			}
			logger.info("MongoDB文档集合转为Bar对象集合耗时{}ms,共{}条数据", (System.currentTimeMillis() - beginTime), barList.size());
		} else {
			logger.warn("MongoDB文档集合转为Bar对象集合时传入的文档集合为空");
		}

		return barList;

	}

	public List<TickField> documentListToTickList(List<Document> documentList, String gatewayId) {
		List<TickField> tickList = new ArrayList<>();
		if (documentList != null && !documentList.isEmpty()) {
			long beginTime = System.currentTimeMillis();
			for (Document document : documentList) {
				try {
					TickField.Builder tickBuilder = TickField.newBuilder();
					ContractField.Builder contractBuilder = ContractField.newBuilder();

					String unifiedSymbol = document.getString("unifiedSymbol");
					String[] unifiedSymbolStrArr = unifiedSymbol.split("@");
					String symbol = unifiedSymbolStrArr[0];
					String exchangeStr = unifiedSymbolStrArr[1];
					String productClassStr = unifiedSymbolStrArr[2];

					contractBuilder.setUnifiedSymbol(unifiedSymbol);
					contractBuilder.setSymbol(symbol);
					contractBuilder.setExchange(ExchangeEnum.valueOf(exchangeStr));
					contractBuilder.setProductClass(ProductClassEnum.valueOf(productClassStr));

					tickBuilder.setUnifiedSymbol(unifiedSymbol);
					tickBuilder.setGatewayId(gatewayId);

					List<Double> askPriceList = new ArrayList<>();
					List<Integer> askVolumeList = new ArrayList<>();
					for (int i = 0; i < 5; i++) {
						askPriceList.add(document.getDouble("askPrice" + (i + 1)));
						askVolumeList.add(document.getInteger("askVolume" + (i + 1)));
					}
					tickBuilder.addAllAskPrice(askPriceList);
					tickBuilder.addAllAskVolume(askVolumeList);

					tickBuilder.setActionDay(document.getString("actionDay"));
					tickBuilder.setActionTime(document.getString("actionTime"));
					tickBuilder.setActionTimestamp(document.getLong("actionTimestamp"));
					tickBuilder.setAvgPrice(document.getDouble("avgPrice"));

					List<Double> bidPriceList = new ArrayList<>();
					List<Integer> bidVolumeList = new ArrayList<>();
					for (int i = 0; i < 5; i++) {
						bidPriceList.add(document.getDouble("bidPrice" + (i + 1)));
						bidVolumeList.add(document.getInteger("bidVolume" + (i + 1)));
					}
					tickBuilder.addAllBidPrice(bidPriceList);
					tickBuilder.addAllBidVolume(bidVolumeList);

					tickBuilder.setLastPrice(document.getDouble("lastPrice"));
					tickBuilder.setHighPrice(document.getDouble("highPrice"));
					tickBuilder.setIopv(document.getDouble("iopv"));
					tickBuilder.setLowPrice(document.getDouble("lowPrice"));
					tickBuilder.setLowerLimit(document.getDouble("lowerLimit"));
					tickBuilder.setNumTrades(document.getLong("numTrades"));
					tickBuilder.setNumTradesDelta(document.getLong("numTradesDelta"));
					tickBuilder.setOpenInterest(document.getDouble("openInterest"));
					tickBuilder.setOpenInterestDelta(document.getDouble("openInterestDelta"));
					tickBuilder.setOpenPrice(document.getDouble("openPrice"));
					tickBuilder.setPreClosePrice(document.getDouble("preClosePrice"));
					tickBuilder.setPreOpenInterest(document.getDouble("preOpenInterest"));
					tickBuilder.setPreSettlePrice(document.getDouble("preSettlePrice"));
					tickBuilder.setSettlePrice(document.getDouble("settlePrice"));
					tickBuilder.setStatus(document.getInteger("status"));
					tickBuilder.setTotalAskVol(document.getLong("totalAskVol"));
					tickBuilder.setTotalBidVol(document.getLong("totalBidVol"));
					tickBuilder.setTradingDay(document.getString("tradingDay"));
					tickBuilder.setTurnover(document.getDouble("turnover"));
					tickBuilder.setTurnoverDelta(document.getDouble("turnoverDelta"));
					tickBuilder.setUpperLimit(document.getDouble("upperLimit"));
					tickBuilder.setVolume(document.getLong("volume"));
					tickBuilder.setVolumeDelta(document.getLong("volumeDelta"));
					tickBuilder.setWeightedAvgAskPrice(document.getDouble("weightedAvgAskPrice"));
					tickBuilder.setWeightedAvgBidPrice(document.getDouble("weightedAvgBidPrice"));
					tickBuilder.setYieldToMaturity(document.getDouble("yieldToMaturity"));

					tickList.add(tickBuilder.build());
				} catch (Exception e) {
					logger.error("数据转换错误", e);
				}
			}
			logger.info("MongoDB文档集合转为Tick对象集合耗时{}ms,共{}条数据", (System.currentTimeMillis() - beginTime), tickList.size());
		} else {
			logger.warn("MongoDB文档集合转为Tick对象集合时传入的文档集合为空");
		}

		return tickList;
	}

	private static Document tickToDocument(TickField tick) {
		Document tickDocument = new Document();
		tickDocument.put("unifiedSymbol", tick.getUnifiedSymbol());

		for (int i = 0; i < 5; i++) {
			if (tick.getAskPriceList().size() > i) {
				tickDocument.put("askPrice" + (i + 1), tick.getAskPrice(i));
			}

			if (tick.getAskVolumeList().size() > i) {
				tickDocument.put("askVolume" + (i + 1), tick.getAskVolume(i));
			}
		}

		tickDocument.put("actionDay", tick.getActionDay());
		tickDocument.put("actionTime", tick.getActionTime());
		tickDocument.put("actionTimestamp", tick.getActionTimestamp());
		tickDocument.put("avgPrice", tick.getAvgPrice());

		for (int i = 0; i < 5; i++) {
			if (tick.getBidPriceList().size() > i) {
				tickDocument.put("bidPrice" + (i + 1), tick.getBidPrice(i));
			}

			if (tick.getBidVolumeList().size() > i) {
				tickDocument.put("bidVolume" + (i + 1), tick.getBidVolume(i));
			}
		}

		tickDocument.put("lastPrice", tick.getLastPrice());
		tickDocument.put("highPrice", tick.getHighPrice());
		tickDocument.put("iopv", tick.getIopv());
		tickDocument.put("lowPrice", tick.getLowPrice());
		tickDocument.put("lowerLimit", tick.getLowerLimit());
		tickDocument.put("numTrades", tick.getNumTrades());
		tickDocument.put("numTradesDelta", tick.getNumTradesDelta());
		tickDocument.put("openInterest", tick.getOpenInterest());
		tickDocument.put("openInterestDelta", tick.getOpenInterestDelta());
		tickDocument.put("openPrice", tick.getOpenPrice());
		tickDocument.put("preClosePrice", tick.getPreClosePrice());
		tickDocument.put("preOpenInterest", tick.getPreOpenInterest());
		tickDocument.put("preSettlePrice", tick.getPreSettlePrice());
		tickDocument.put("settlePrice", tick.getSettlePrice());
		tickDocument.put("status", tick.getStatus());
		tickDocument.put("totalAskVol", tick.getTotalAskVol());
		tickDocument.put("totalBidVol", tick.getTotalBidVol());
		tickDocument.put("tradingDay", tick.getTradingDay());
		tickDocument.put("turnover", tick.getTurnover());
		tickDocument.put("turnoverDelta", tick.getTurnoverDelta());
		tickDocument.put("upperLimit", tick.getUpperLimit());
		tickDocument.put("volume", tick.getVolume());
		tickDocument.put("volumeDelta", tick.getVolumeDelta());
		tickDocument.put("weightedAvgAskPrice", tick.getWeightedAvgAskPrice());
		tickDocument.put("weightedAvgBidPrice", tick.getWeightedAvgBidPrice());
		tickDocument.put("yieldToMaturity", tick.getYieldToMaturity());

		return tickDocument;
	}

	private static Document barToDocument(BarField bar) {
		Document barDocument = new Document();
		barDocument.put("unifiedSymbol", bar.getUnifiedSymbol());

		barDocument.put("actionDay", bar.getActionDay());
		barDocument.put("actionTime", bar.getActionTime());
		barDocument.put("actionTimestamp", bar.getActionTimestamp());
		barDocument.put("closePrice", bar.getClosePrice());
		barDocument.put("highPrice", bar.getHighPrice());
		barDocument.put("lowPrice", bar.getLowPrice());
		barDocument.put("numTrades", bar.getNumTrades());
		barDocument.put("numTradesDelta", bar.getNumTradesDelta());
		barDocument.put("openInterest", bar.getOpenInterest());
		barDocument.put("openInterestDelta", bar.getOpenInterestDelta());
		barDocument.put("openPrice", bar.getOpenPrice());
		barDocument.put("tradingDay", bar.getTradingDay());
		barDocument.put("turnover", bar.getTurnover());
		barDocument.put("turnoverDelta", bar.getTurnoverDelta());
		barDocument.put("volume", bar.getVolume());
		barDocument.put("volumeDelta", bar.getVolumeDelta());

		return barDocument;
	}

	@Override
	public boolean upsertBar5SecListToHistDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(histMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_5_SEC, barList);

		}
		return false;
	}

	@Override
	public boolean upsertBar1MinListToHistDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(histMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_1_MIN, barList);
		}
		return false;
	}

	@Override
	public boolean upsertBar3MinListToHistDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(histMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_3_MIN, barList);
		}
		return false;
	}

	@Override
	public boolean upsertBar5MinListToHistDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(histMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_5_MIN, barList);
		}
		return false;
	}

	@Override
	public boolean upsertBar15MinListToHistDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(histMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_15_MIN, barList);
		}
		return false;
	}

	@Override
	public boolean upsertBar1DayListToHistDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(histMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_1_DAY, barList);
		}
		return false;
	}

	@Override
	public boolean upsertTickListToHistDB(List<TickField> tickList, String unifiedSymbol) {
		if (!tickList.isEmpty()) {
			return upsertTick(histMarketDataDBName, MarketDataService.COLLECTION_NAME_PREFIX_TICK + unifiedSymbol, tickList);
		}
		return false;
	}

	@Override
	public boolean upsertBar5SecListToTodayDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(todayMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_5_SEC, barList);
		}
		return false;
	}

	@Override
	public boolean upsertBar1MinListToTodayDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(todayMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_1_MIN, barList);
		}
		return false;
	}

	@Override
	public boolean upsertBar3MinListToTodayDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(todayMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_3_MIN, barList);
		}
		return false;
	}

	@Override
	public boolean upsertBar5MinListToTodayDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(todayMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_5_MIN, barList);
		}
		return false;
	}

	@Override
	public boolean upsertBar15MinListToTodayDB(List<BarField> barList) {
		if (!barList.isEmpty()) {
			return upsertBar(todayMarketDataDBName, MarketDataService.COLLECTION_NAME_BAR_15_MIN, barList);
		}
		return false;
	}

	@Override
	public boolean upsertTickListToTodayDB(List<TickField> tickList, String unifiedSymbol) {
		if (!tickList.isEmpty()) {
			return upsertTick(todayMarketDataDBName, MarketDataService.COLLECTION_NAME_PREFIX_TICK + unifiedSymbol, tickList);
		}
		return false;
	}

	@Override
	public boolean upsertBar5SecToTodayDB(BarField bar) {
		return upsertBar(todayMarketDataDBName, COLLECTION_NAME_BAR_5_SEC, bar);
	}

	@Override
	public boolean upsertBar1MinToTodayDB(BarField bar) {
		return upsertBar(todayMarketDataDBName, COLLECTION_NAME_BAR_1_MIN, bar);
	}

	@Override
	public boolean upsertBar3MinToTodayDB(BarField bar) {
		return upsertBar(todayMarketDataDBName, COLLECTION_NAME_BAR_3_MIN, bar);
	}

	@Override
	public boolean upsertBar5MinToTodayDB(BarField bar) {
		return upsertBar(todayMarketDataDBName, COLLECTION_NAME_BAR_5_MIN, bar);
	}

	@Override
	public boolean upsertBar15MinToTodayDB(BarField bar) {
		return upsertBar(todayMarketDataDBName, COLLECTION_NAME_BAR_15_MIN, bar);
	}

	@Override
	public boolean upsertTickToTodayDB(TickField tick) {
		return upsertTick(todayMarketDataDBName, COLLECTION_NAME_PREFIX_TICK + tick.getUnifiedSymbol(), tick);
	}

	@Override
	public List<BarField> queryBar5SecList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar5SecList(startTimestamp, endTimestamp, unifiedSymbol);
		barList.addAll(this.queryTodayBar5SecList(startTimestamp, endTimestamp, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar1MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar1MinList(startTimestamp, endTimestamp, unifiedSymbol);
		barList.addAll(this.queryTodayBar1MinList(startTimestamp, endTimestamp, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar3MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar3MinList(startTimestamp, endTimestamp, unifiedSymbol);
		barList.addAll(this.queryTodayBar3MinList(startTimestamp, endTimestamp, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar5MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar5MinList(startTimestamp, endTimestamp, unifiedSymbol);
		barList.addAll(this.queryTodayBar5MinList(startTimestamp, endTimestamp, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar15MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar15MinList(startTimestamp, endTimestamp, unifiedSymbol);
		barList.addAll(this.queryTodayBar15MinList(startTimestamp, endTimestamp, unifiedSymbol));
		return barList;
	}

	@Override
	public List<BarField> queryBar1DayList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		List<BarField> barList = this.queryHistBar1DayList(startTimestamp, endTimestamp, unifiedSymbol);
		return barList;
	}

	@Override
	public List<TickField> queryTickList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		List<TickField> tickList = this.queryHistTickList(startTimestamp, endTimestamp, unifiedSymbol);
		tickList.addAll(this.queryTodayTickList(startTimestamp, endTimestamp, unifiedSymbol));
		return tickList;
	}

	@Override
	public List<BarField> queryHistBar5SecList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.histMarketDataDBClient.find(histMarketDataDBName, COLLECTION_NAME_BAR_5_SEC, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", histMarketDataDBName, COLLECTION_NAME_BAR_5_SEC, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询历史5秒钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryHistBar1MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.histMarketDataDBClient.find(histMarketDataDBName, COLLECTION_NAME_BAR_1_MIN, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", histMarketDataDBName, COLLECTION_NAME_BAR_1_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询历史1分钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryHistBar3MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.histMarketDataDBClient.find(histMarketDataDBName, COLLECTION_NAME_BAR_3_MIN, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", histMarketDataDBName, COLLECTION_NAME_BAR_3_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询历史3分钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryHistBar5MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.histMarketDataDBClient.find(histMarketDataDBName, COLLECTION_NAME_BAR_5_MIN, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", histMarketDataDBName, COLLECTION_NAME_BAR_5_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询历史5分钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryHistBar15MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.histMarketDataDBClient.find(histMarketDataDBName, COLLECTION_NAME_BAR_15_MIN, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", histMarketDataDBName, COLLECTION_NAME_BAR_15_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询历史15分钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryHistBar1DayList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.histMarketDataDBClient.find(histMarketDataDBName, COLLECTION_NAME_BAR_1_DAY, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", histMarketDataDBName, COLLECTION_NAME_BAR_1_DAY, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询历史1日Bar数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<TickField> queryHistTickList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			String collectionName = COLLECTION_NAME_PREFIX_TICK + unifiedSymbol;
			List<Document> documentList = this.histMarketDataDBClient.find(histMarketDataDBName, collectionName, filter, sortBO);
			logger.info("查询Tick数据,数据库{},集合{},操作耗时{}ms,共{}条数据", histMarketDataDBName, collectionName, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToTickList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询历史Tick数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryTodayBar5SecList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.todayMarketDataDBClient.find(todayMarketDataDBName, COLLECTION_NAME_BAR_5_SEC, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", todayMarketDataDBName, COLLECTION_NAME_BAR_5_SEC, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询历史5秒钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryTodayBar1MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.todayMarketDataDBClient.find(todayMarketDataDBName, COLLECTION_NAME_BAR_1_MIN, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", todayMarketDataDBName, COLLECTION_NAME_BAR_1_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询当日1分钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryTodayBar3MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.todayMarketDataDBClient.find(todayMarketDataDBName, COLLECTION_NAME_BAR_3_MIN, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", todayMarketDataDBName, COLLECTION_NAME_BAR_3_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询当日3分钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryTodayBar5MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.todayMarketDataDBClient.find(todayMarketDataDBName, COLLECTION_NAME_BAR_5_MIN, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", todayMarketDataDBName, COLLECTION_NAME_BAR_5_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询当日5分钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<BarField> queryTodayBar15MinList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			List<Document> documentList = this.todayMarketDataDBClient.find(todayMarketDataDBName, COLLECTION_NAME_BAR_15_MIN, filter, sortBO);
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", todayMarketDataDBName, COLLECTION_NAME_BAR_15_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询当日15分钟数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<TickField> queryTodayTickList(long startTimestamp, long endTimestamp, String unifiedSymbol) {
		try {
			Document filter = new Document();
			Document dateDocument = new Document();
			dateDocument.put("$gte", startTimestamp);
			dateDocument.put("$lte", endTimestamp);
			filter.put("actionTimestamp", dateDocument);
			filter.put("unifiedSymbol", unifiedSymbol);

			BasicDBObject sortBO = new BasicDBObject();
			sortBO.put("actionTimestamp", 1);
			long beginTime = System.currentTimeMillis();
			String collectionName = COLLECTION_NAME_PREFIX_TICK + unifiedSymbol;
			List<Document> documentList = this.todayMarketDataDBClient.find(todayMarketDataDBName, collectionName, filter, sortBO);
			logger.info("查询Tick数据,数据库{},集合{},操作耗时{}ms,共{}条数据", todayMarketDataDBName, collectionName, (System.currentTimeMillis() - beginTime), documentList.size());
			return documentListToTickList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("查询当日Tick数据发生错误", e);
		}
		return new ArrayList<>();
	}

	@Override
	public List<TickField> queryTickListByEndTimestampAndLimit(long endTimestamp, int limit, String unifiedSymbol) {
		List<TickField> tickList = new ArrayList<>();
		try {
			long beginTime = System.currentTimeMillis();
			
			List<Document> pipeline = new ArrayList<>();
			
			Document filter = new Document();
			filter.put("actionTimestamp", new Document("$lte", endTimestamp));
			filter.put("unifiedSymbol", unifiedSymbol);
			
			pipeline.add(new Document("$match",filter));
			pipeline.add(new Document("$sort", new Document("actionTimestamp",-1)));
			pipeline.add(new Document("$limit", limit));
			pipeline.add(new Document("$sort", new Document("actionTimestamp",1)));
			
			String collectionName = COLLECTION_NAME_PREFIX_TICK + unifiedSymbol;
			
			AggregateIterable<Document> doc = this.histMarketDataDBClient.getDatabase(histMarketDataDBName).getCollection(collectionName).aggregate(pipeline);
			MongoCursor<Document> mongoCursor = doc.iterator();
			
			List<Document> documentList = new ArrayList<Document>();
			while (mongoCursor.hasNext()) {
				documentList.add(mongoCursor.next());
			}
			
			logger.info("查询Tick数据,数据库{},集合{},操作耗时{}ms,共{}条数据", histMarketDataDBName, collectionName, (System.currentTimeMillis() - beginTime), documentList.size());
			tickList = documentListToTickList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
		} catch (Exception e) {
			logger.error("根据最后日期和数量限制查询历史Tick数据发生错误", e);
		}
		
		try {
			long beginTime = System.currentTimeMillis();
			
			List<Document> pipeline = new ArrayList<>();
			
			Document filter = new Document();
			filter.put("actionTimestamp", new Document("$lte", endTimestamp));
			filter.put("unifiedSymbol", unifiedSymbol);
			
			pipeline.add(new Document("$match",filter));
			pipeline.add(new Document("$sort", new Document("actionTimestamp",-1)));
			pipeline.add(new Document("$limit", limit));
			pipeline.add(new Document("$sort", new Document("actionTimestamp",1)));
			
			String collectionName = COLLECTION_NAME_PREFIX_TICK + unifiedSymbol;
			
			AggregateIterable<Document> doc = this.todayMarketDataDBClient.getDatabase(todayMarketDataDBName).getCollection(collectionName).aggregate(pipeline);
			MongoCursor<Document> mongoCursor = doc.iterator();
			
			List<Document> documentList = new ArrayList<Document>();
			while (mongoCursor.hasNext()) {
				documentList.add(mongoCursor.next());
			}
			
			logger.info("查询Tick数据,数据库{},集合{},操作耗时{}ms,共{}条数据", todayMarketDataDBName, collectionName, (System.currentTimeMillis() - beginTime), documentList.size());
			tickList.addAll(documentListToTickList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName()));
		} catch (Exception e) {
			logger.error("根据最后日期和数量限制查询当日Tick数据发生错误", e);
		}
		
		if(tickList.size()>limit) {
			tickList = tickList.subList(tickList.size()-limit, tickList.size());
		}
		
		return tickList;
	}

	@Override
	public List<BarField> queryBar1MinListByEndTimestampAndLimit(long endTimestamp, int limit, String unifiedSymbol) {
		List<BarField> barList = new ArrayList<>();
		try {
			long beginTime = System.currentTimeMillis();
			
			List<Document> pipeline = new ArrayList<>();
			
			Document filter = new Document();
			filter.put("actionTimestamp", new Document("$lte", endTimestamp));
			filter.put("unifiedSymbol", unifiedSymbol);
			
			pipeline.add(new Document("$match",filter));
			pipeline.add(new Document("$sort", new Document("actionTimestamp",-1)));
			pipeline.add(new Document("$limit", limit));
			pipeline.add(new Document("$sort", new Document("actionTimestamp",1)));
			
			AggregateIterable<Document> doc = this.histMarketDataDBClient.getDatabase(histMarketDataDBName).getCollection(COLLECTION_NAME_BAR_1_MIN).aggregate(pipeline);
			MongoCursor<Document> mongoCursor = doc.iterator();
			
			List<Document> documentList = new ArrayList<Document>();
			while (mongoCursor.hasNext()) {
				documentList.add(mongoCursor.next());
			}
			
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", histMarketDataDBName, COLLECTION_NAME_BAR_1_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			barList.addAll(documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName()));
		} catch (Exception e) {
			logger.error("根据最后日期和数量限制查询历史1分钟数据发生错误", e);
		}
		
		try {
			long beginTime = System.currentTimeMillis();
			
			List<Document> pipeline = new ArrayList<>();
			
			Document filter = new Document();
			filter.put("actionTimestamp", new Document("$lte", endTimestamp));
			filter.put("unifiedSymbol", unifiedSymbol);
			
			pipeline.add(new Document("$match",filter));
			pipeline.add(new Document("$sort", new Document("actionTimestamp",-1)));
			pipeline.add(new Document("$limit", limit));
			pipeline.add(new Document("$sort", new Document("actionTimestamp",1)));
			
			AggregateIterable<Document> doc = this.todayMarketDataDBClient.getDatabase(todayMarketDataDBName).getCollection(COLLECTION_NAME_BAR_1_MIN).aggregate(pipeline);
			MongoCursor<Document> mongoCursor = doc.iterator();
			
			List<Document> documentList = new ArrayList<Document>();
			while (mongoCursor.hasNext()) {
				documentList.add(mongoCursor.next());
			}
			
			logger.info("查询Bar数据,数据库{},集合{},操作耗时{}ms,共{}条数据", todayMarketDataDBName, COLLECTION_NAME_BAR_1_MIN, (System.currentTimeMillis() - beginTime), documentList.size());
			barList.addAll(documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName()));
		} catch (Exception e) {
			logger.error("根据最后日期和数量限制查询当日1分钟数据发生错误", e);
		}
		
		if(barList.size()>limit) {
			barList = barList.subList(barList.size()-limit, barList.size());
		}
		return barList;
	}

}
