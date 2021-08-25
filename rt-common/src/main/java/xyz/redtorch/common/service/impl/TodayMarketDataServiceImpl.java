package xyz.redtorch.common.service.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.redtorch.common.service.MongoDBService;
import xyz.redtorch.common.service.TodayMarketDataService;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TodayMarketDataServiceImpl implements TodayMarketDataService {

    private static final Logger logger = LoggerFactory.getLogger(TodayMarketDataServiceImpl.class);

    private final MongoDBService mongoDBService;

    public TodayMarketDataServiceImpl(@Autowired MongoDBService mongoDBService){
        this.mongoDBService = mongoDBService;
    }


    @Override
    public List<BarField> queryBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriodEnum) {
        try {
            Document filter = new Document();
            Document dateDocument = new Document();
            dateDocument.put("$gte", startTimestamp);
            dateDocument.put("$lte", endTimestamp);
            filter.put("actionTimestamp", dateDocument);
            filter.put("uniformSymbol", uniformSymbol);
            filter.put("period", barPeriodEnum.getNumber());

            BasicDBObject sortBO = new BasicDBObject();
            sortBO.put("actionTimestamp", 1);
            long beginTime = System.currentTimeMillis();
            List<Document> documentList = mongoDBService.getClient().find(mongoDBService.getDBName(), "tbl_bar", filter, sortBO);
            logger.info("查询Bar数据,操作耗时{}ms,共{}条数据", (System.currentTimeMillis() - beginTime), documentList.size());
            return documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
        } catch (Exception e) {
            logger.error("查询历史1分钟数据发生错误", e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<TickField> queryTickList(long startTimestamp, long endTimestamp, String uniformSymbol) {
        try {
            Document filter = new Document();
            Document dateDocument = new Document();
            dateDocument.put("$gte", startTimestamp);
            dateDocument.put("$lte", endTimestamp);
            filter.put("actionTimestamp", dateDocument);
            filter.put("uniformSymbol", uniformSymbol);

            BasicDBObject sortBO = new BasicDBObject();
            sortBO.put("actionTimestamp", 1);
            long beginTime = System.currentTimeMillis();
            List<Document> documentList = mongoDBService.getClient().find(mongoDBService.getDBName(), "tbl_tick", filter, sortBO);
            logger.info("查询Tick数据,操作耗时{}ms,共{}条数据", (System.currentTimeMillis() - beginTime), documentList.size());
            return documentListToTickList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName());
        } catch (Exception e) {
            logger.error("查询当日Tick数据发生错误", e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<BarField> queryBarList(LocalDateTime startDateTime, LocalDateTime endDateTime, String uniformSymbol, BarPeriodEnum barPeriodEnum) {
        return queryBarList(CommonUtils.localDateTimeToMills(startDateTime), CommonUtils.localDateTimeToMills(endDateTime), uniformSymbol, barPeriodEnum);
    }

    @Override
    public List<TickField> queryTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String uniformSymbol) {
        return queryTickList(CommonUtils.localDateTimeToMills(startDateTime), CommonUtils.localDateTimeToMills(endDateTime), uniformSymbol);
    }

    @Override
    public List<TickField> queryTickListByEndTimestampAndLimit(long endTimestamp, int limit, String uniformSymbol) {
        List<TickField> tickList = new ArrayList<>();

        try {
            long beginTime = System.currentTimeMillis();

            Document filter = new Document();
            filter.put("actionTimestamp", new Document("$lte", endTimestamp));
            filter.put("uniformSymbol", uniformSymbol);

            FindIterable<Document> docs = mongoDBService.getClient().getDatabase(mongoDBService.getDBName()).getCollection("tbl_tick").find(filter).sort(new Document("actionTimestamp", -1)).limit(limit);
            MongoCursor<Document> mongoCursor = docs.iterator();

            List<Document> documentList = new ArrayList<Document>();
            while (mongoCursor.hasNext()) {
                documentList.add(mongoCursor.next());
            }
            Collections.reverse(documentList);

            logger.info("查询Tick数据,操作耗时{}ms,共{}条数据", (System.currentTimeMillis() - beginTime), documentList.size());
            tickList.addAll(documentListToTickList(documentList, MarketDataDBTypeEnum.MDDT_TD.getValueDescriptor().getName()));
        } catch (Exception e) {
            logger.error("根据最后日期和数量限制查询当日Tick数据发生错误", e);
        }

        if (tickList.size() > limit) {
            tickList = tickList.subList(tickList.size() - limit, tickList.size());
        }

        return tickList;
    }

    @Override
    public List<BarField> queryBarListByEndTimestampAndLimit(long endTimestamp, int limit, String uniformSymbol, BarPeriodEnum barPeriodEnum) {
        List<BarField> barList = new ArrayList<>();
        try {
            long beginTime = System.currentTimeMillis();

            Document filter = new Document();
            filter.put("actionTimestamp", new Document("$lte", endTimestamp));
            filter.put("uniformSymbol", uniformSymbol);
            filter.put("period", barPeriodEnum.getNumber());

            FindIterable<Document> docs = mongoDBService.getClient().getDatabase(mongoDBService.getDBName()).getCollection("tbl_bar").find(filter).sort(new Document("actionTimestamp", -1)).limit(limit);
            MongoCursor<Document> mongoCursor = docs.iterator();

            List<Document> documentList = new ArrayList<>();
            while (mongoCursor.hasNext()) {
                documentList.add(mongoCursor.next());
            }
            Collections.reverse(documentList);

            logger.info("查询Bar数据,操作耗时{}ms,共{}条数据", (System.currentTimeMillis() - beginTime), documentList.size());
            barList.addAll(documentListToBarList(documentList, MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName()));
        } catch (Exception e) {
            logger.error("根据最后日期和数量限制查询Bar数据发生错误", e);
        }

        if (barList.size() > limit) {
            barList = barList.subList(barList.size() - limit, barList.size());
        }
        return barList;
    }
    
    @Override
    public boolean upsertBar(BarField bar) {
        Document barDocument = barToDocument(bar);
        Document filterDocument = new Document();
        filterDocument.put("uniformSymbol", bar.getUniformSymbol());
        filterDocument.put("actionTimestamp", bar.getActionTimestamp());

        mongoDBService.getClient().getDatabase(mongoDBService.getDBName()).getCollection("tbl_bar").createIndex(Indexes.ascending("actionTimestamp"));
        return mongoDBService.getClient().upsert(mongoDBService.getDBName(), "tbl_bar", barDocument, filterDocument);
    }

    @Override
    public boolean upsertBarList(List<BarField> barList) {

        if (barList == null || barList.isEmpty()) {
            logger.error("更新插入Bar集合错误,数据集合为空");
            return false;
        }

        List<WriteModel<Document>> writeModelList = new ArrayList<WriteModel<Document>>();

        long beginTime = System.currentTimeMillis();
        for (BarField bar : barList) {
            Document filterDocument = new Document();
            filterDocument.put("uniformSymbol", bar.getUniformSymbol());
            filterDocument.put("actionTimestamp", bar.getActionTimestamp());

            Document barDocument = barToDocument(bar);
            ReplaceOptions replaceOptions = new ReplaceOptions();
            replaceOptions.upsert(true);

            ReplaceOneModel<Document> replaceOneModel = new ReplaceOneModel<Document>(filterDocument, barDocument, replaceOptions);
            writeModelList.add(replaceOneModel);
        }
        logger.info("更新插入Bar集合,数据转换耗时{}ms,共{}条数据",  (System.currentTimeMillis() - beginTime), barList.size());
        beginTime = System.currentTimeMillis();
        mongoDBService.getClient().getDatabase(mongoDBService.getDBName()).getCollection("tbl_bar").createIndex(Indexes.ascending("actionTimestamp"));
        mongoDBService.getClient().getDatabase(mongoDBService.getDBName()).getCollection("tbl_bar").bulkWrite(writeModelList);
        logger.info("更新插入Bar集合,数据库操作耗时{}ms,共{}条操作", (System.currentTimeMillis() - beginTime), writeModelList.size());
        return true;
    }


    @Override
    public boolean upsertTick(TickField tick) {
        Document tickDocument = tickToDocument(tick);
        Document filterDocument = new Document();
        filterDocument.put("uniformSymbol", tick.getUniformSymbol());
        filterDocument.put("actionTimestamp", tick.getActionTimestamp());

        mongoDBService.getClient().getDatabase(mongoDBService.getDBName()).getCollection("tbl_tick").createIndex(Indexes.ascending("actionTimestamp"));
        return mongoDBService.getClient().upsert(mongoDBService.getDBName(), "tbl_tick", tickDocument, filterDocument);
    }

    @Override
    public boolean upsertTickList(List<TickField> tickList) {

        if (tickList == null || tickList.isEmpty()) {
            logger.error("更新插入Tick集合错误,数据集合为空");
            return false;
        }

        List<WriteModel<Document>> writeModelList = new ArrayList<WriteModel<Document>>();

        long beginTime = System.currentTimeMillis();
        for (TickField tick : tickList) {
            Document filterDocument = new Document();
            filterDocument.put("uniformSymbol", tick.getUniformSymbol());
            filterDocument.put("actionTimestamp", tick.getActionTimestamp());

            Document tickDocument = tickToDocument(tick);
            ReplaceOptions replaceOptions = new ReplaceOptions();
            replaceOptions.upsert(true);

            ReplaceOneModel<Document> replaceOneModel = new ReplaceOneModel<Document>(filterDocument, tickDocument, replaceOptions);
            writeModelList.add(replaceOneModel);
        }
        logger.info("更新插入Tick集合,数据转换耗时{}ms,共{}条数据",  (System.currentTimeMillis() - beginTime), tickList.size());
        beginTime = System.currentTimeMillis();
        mongoDBService.getClient().getDatabase(mongoDBService.getDBName()).getCollection("tbl_tick").createIndex(Indexes.ascending("actionTimestamp"));
        mongoDBService.getClient().getDatabase(mongoDBService.getDBName()).getCollection("tbl_tick").bulkWrite(writeModelList);
        logger.info("更新插入Tick集合,数据库操作耗时{}ms,共{}条操作", (System.currentTimeMillis() - beginTime), writeModelList.size());
        return true;
    }

    private static List<TickField> documentListToTickList(List<Document> documentList, String gatewayId) {
        List<TickField> tickList = new ArrayList<>();
        if (documentList != null && !documentList.isEmpty()) {
            long beginTime = System.currentTimeMillis();
            for (Document document : documentList) {
                try {
                    TickField.Builder tickBuilder = TickField.newBuilder();

                    tickBuilder.setUniformSymbol(document.getString("uniformSymbol"));
                    tickBuilder.setGatewayId(gatewayId);

                    List<Double> askPriceList = new ArrayList<>();
                    List<Integer> askVolumeList = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        askPriceList.add(document.getDouble("askPrice" + (i + 1)));
                        askVolumeList.add(document.getInteger("askVolume" + (i + 1)));
                    }
                    tickBuilder.addAllAskPrice(askPriceList);
                    tickBuilder.addAllAskVolume(askVolumeList);

                    tickBuilder.setActionDay(document.getInteger("actionDay"));
                    tickBuilder.setActionTime(document.getInteger("actionTime"));
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
                    tickBuilder.setLowPrice(document.getDouble("lowPrice"));
                    tickBuilder.setLowerLimit(document.getDouble("lowerLimit"));
                    tickBuilder.setOpenInterest(document.getDouble("openInterest"));
                    tickBuilder.setOpenInterestDelta(document.getDouble("openInterestDelta"));
                    tickBuilder.setOpenPrice(document.getDouble("openPrice"));
                    tickBuilder.setPreClosePrice(document.getDouble("preClosePrice"));
                    tickBuilder.setPreOpenInterest(document.getDouble("preOpenInterest"));
                    tickBuilder.setPreSettlePrice(document.getDouble("preSettlePrice"));
                    tickBuilder.setSettlePrice(document.getDouble("settlePrice"));
                    tickBuilder.setTradingDay(document.getInteger("tradingDay"));
                    tickBuilder.setTurnover(document.getDouble("turnover"));
                    tickBuilder.setTurnoverDelta(document.getDouble("turnoverDelta"));
                    tickBuilder.setUpperLimit(document.getDouble("upperLimit"));
                    tickBuilder.setVolume(document.getLong("volume"));
                    tickBuilder.setVolumeDelta(document.getLong("volumeDelta"));

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


    public List<BarField> documentListToBarList(List<Document> documentList, String gatewayId) {
        List<BarField> barList = new ArrayList<>();
        if (documentList != null && !documentList.isEmpty()) {
            long beginTime = System.currentTimeMillis();
            for (Document document : documentList) {
                try {
                    BarField.Builder barBuilder = BarField.newBuilder();

                    barBuilder.setUniformSymbol(document.getString("uniformSymbol"));
                    barBuilder.setGatewayId(gatewayId);
                    barBuilder.setActionDay(document.getInteger("actionDay"));
                    barBuilder.setActionTime(document.getInteger("actionTime"));
                    barBuilder.setActionTimestamp(document.getLong("actionTimestamp"));
                    barBuilder.setClosePrice(document.getDouble("closePrice"));
                    barBuilder.setHighPrice(document.getDouble("highPrice"));
                    barBuilder.setLowPrice(document.getDouble("lowPrice"));
                    barBuilder.setOpenInterest(document.getDouble("openInterest"));
                    barBuilder.setOpenInterestDelta(document.getDouble("openInterestDelta"));
                    barBuilder.setOpenPrice(document.getDouble("openPrice"));
                    barBuilder.setTradingDay(document.getInteger("tradingDay"));
                    barBuilder.setTurnover(document.getDouble("turnover"));
                    barBuilder.setTurnoverDelta(document.getDouble("turnoverDelta"));
                    barBuilder.setVolume(document.getLong("volume"));
                    barBuilder.setVolumeDelta(document.getLong("volumeDelta"));
                    barBuilder.setPeriod(document.getInteger("period"));

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

    private static Document tickToDocument(TickField tick) {
        Document tickDocument = new Document();
        tickDocument.put("uniformSymbol", tick.getUniformSymbol());

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
        tickDocument.put("lowPrice", tick.getLowPrice());
        tickDocument.put("lowerLimit", tick.getLowerLimit());
        tickDocument.put("openInterest", tick.getOpenInterest());
        tickDocument.put("openInterestDelta", tick.getOpenInterestDelta());
        tickDocument.put("openPrice", tick.getOpenPrice());
        tickDocument.put("preClosePrice", tick.getPreClosePrice());
        tickDocument.put("preOpenInterest", tick.getPreOpenInterest());
        tickDocument.put("preSettlePrice", tick.getPreSettlePrice());
        tickDocument.put("settlePrice", tick.getSettlePrice());
        tickDocument.put("tradingDay", tick.getTradingDay());
        tickDocument.put("turnover", tick.getTurnover());
        tickDocument.put("turnoverDelta", tick.getTurnoverDelta());
        tickDocument.put("upperLimit", tick.getUpperLimit());
        tickDocument.put("volume", tick.getVolume());
        tickDocument.put("volumeDelta", tick.getVolumeDelta());

        return tickDocument;
    }

    private static Document barToDocument(BarField bar) {
        Document barDocument = new Document();
        barDocument.put("uniformSymbol", bar.getUniformSymbol());

        barDocument.put("actionDay", bar.getActionDay());
        barDocument.put("actionTime", bar.getActionTime());
        barDocument.put("actionTimestamp", bar.getActionTimestamp());
        barDocument.put("closePrice", bar.getClosePrice());
        barDocument.put("highPrice", bar.getHighPrice());
        barDocument.put("lowPrice", bar.getLowPrice());
        barDocument.put("openInterest", bar.getOpenInterest());
        barDocument.put("openInterestDelta", bar.getOpenInterestDelta());
        barDocument.put("openPrice", bar.getOpenPrice());
        barDocument.put("tradingDay", bar.getTradingDay());
        barDocument.put("turnover", bar.getTurnover());
        barDocument.put("turnoverDelta", bar.getTurnoverDelta());
        barDocument.put("volume", bar.getVolume());
        barDocument.put("volumeDelta", bar.getVolumeDelta());
        barDocument.put("period", bar.getPeriod());


        return barDocument;
    }
}
