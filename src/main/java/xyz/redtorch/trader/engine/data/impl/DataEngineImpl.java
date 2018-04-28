package xyz.redtorch.trader.engine.data.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import xyz.redtorch.trader.base.BaseConfig;
import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.utils.MongoDBUtil;
import xyz.redtorch.utils.MongoDBClient;

import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
/**
 * @author sun0x00@gmail.com
 */
public class DataEngineImpl implements DataEngine {

	private static Logger log = LoggerFactory.getLogger(DataEngineImpl.class);

	private MongoDBClient mdDBClient;//行情数据库客户端
	private MongoDBClient defaultDBClient;//默认数据库客户端

	public DataEngineImpl() throws Exception {

		String mdMongoHost = BaseConfig.rtConfig.getString("mongodb.instance.md.host");
		int mdMongoPort = BaseConfig.rtConfig.getInt("mongodb.instance.md.port");
		String mdMongoUsername = BaseConfig.rtConfig.getString("mongodb.instance.md.username");
		String mdMongoPassword = BaseConfig.rtConfig.getString("mongodb.instance.md.password");
		String mdMongoAuthDB = BaseConfig.rtConfig.getString("mongodb.instance.md.authdb");
		try {
			mdDBClient = new MongoDBClient(mdMongoHost, mdMongoPort, mdMongoUsername, mdMongoPassword, mdMongoAuthDB);
		} catch (Exception e) {
			throw new Exception("行情MongoDB数据库连接失败", e);
		}

		String defaultMongoHost = BaseConfig.rtConfig.getString("mongodb.instance.default.host");
		int defaultMongoPort = BaseConfig.rtConfig.getInt("mongodb.instance.default.port");
		String defaultMongoUsername = BaseConfig.rtConfig.getString("mongodb.instance.default.username");
		String defaultMongoPassword = BaseConfig.rtConfig.getString("mongodb.instance.default.password");
		String defaultMongoAuthDB = BaseConfig.rtConfig.getString("mongodb.instance.default.authdb");
		try {
			defaultDBClient = new MongoDBClient(defaultMongoHost, defaultMongoPort, defaultMongoUsername,
					defaultMongoPassword, defaultMongoAuthDB);
		} catch (Exception e) {
			throw new Exception("默认MongoDB数据库连接失败", e);
		}

	}

	@Override
	public MongoDBClient getMdDBClient() {
		return mdDBClient;
	}

	@Override
	public MongoDBClient getDefaultDBClient() {
		return defaultDBClient;
	}

	@Override
	public List<Bar> loadBarDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol) {
		log.info("加载Bar数据,合约{},开始日期时间{}结束日期时间{}", rtSymbol,
				startDateTime.toString(RtConstant.DT_FORMAT_WITH_MS_FORMATTER),
				endDateTime.toString(RtConstant.DT_FORMAT_WITH_MS_FORMATTER));
		long startTime = System.currentTimeMillis();
		Document filter = new Document();
		filter.append("dateTime", new Document("$gte", startDateTime.toDate()).append("$lte", endDateTime.toDate()));

		BasicDBObject sort = new BasicDBObject();
		sort.append("dateTime", 1);
		List<Document> documentList = mdDBClient.find(MINUTE_DB_NAME, rtSymbol, filter, sort);

		List<Bar> barList = new ArrayList<>();
		for (Document document : documentList) {
			Bar bar = new Bar();
			try {
				bar = MongoDBUtil.documentToBean(document, bar);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				log.error("查询Bar数据转换发生错误,Document-", document.toJson(), e);
			}
			barList.add(bar);
		}
		
		log.info("加载Bar数据完成,合约{},共{}条,耗时{}ms", rtSymbol, barList.size(),System.currentTimeMillis() - startTime);
		return barList;
	}

	@Override
	public List<Tick> loadTickDataList(DateTime startDateTime, DateTime endDateTime, String rtSymbol) {
		log.info("加载Tick数据,合约{},开始日期时间{}结束日期时间{}", rtSymbol,
				startDateTime.toString(RtConstant.DT_FORMAT_WITH_MS_FORMATTER),
				endDateTime.toString(RtConstant.DT_FORMAT_WITH_MS_FORMATTER));
		long startTime = System.currentTimeMillis();
		Document filter = new Document();
		filter.append("dateTime", new Document("$gte", startDateTime.toDate()).append("$lte", endDateTime.toDate()));

		BasicDBObject sort = new BasicDBObject();
		sort.append("dateTime", 1);
		List<Document> documentList = mdDBClient.find(TICK_DB_NAME, rtSymbol, filter, sort);

		List<Tick> tickList = new ArrayList<>();
		for (Document document : documentList) {
			Tick tick = new Tick();
			try {
				tick = MongoDBUtil.documentToBean(document, tick);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				log.error("查询Tick数据转换发生错误,Document-", document.toJson(), e);
			}
			tickList.add(tick);
		}
		log.info("加载Tick数据完成,合约{},共{}条,耗时{}ms", rtSymbol, tickList.size(),System.currentTimeMillis() - startTime);
		return tickList;
	}

}
