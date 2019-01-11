package xyz.redtorch.core.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.service.MongoDBService;
import xyz.redtorch.utils.MongoDBUtil;
import xyz.redtorch.utils.MongoDBClient;

import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;

/**
 * @author sun0x00@gmail.com
 */
@Service
@PropertySource(value = { "classpath:rt-core.properties" })
public class MongoDBServiceImpl implements MongoDBService, InitializingBean {

	private static Logger log = LoggerFactory.getLogger(MongoDBServiceImpl.class);

	@Value("${mongodb.instance.md.dbname.minute}")
	private String minuteDbName;
	@Value("${mongodb.instance.md.dbname.tick}")
	private String tickDbName;
	@Value("${mongodb.instance.md.dbname.tick.daily}")
	private String tickDailyDbName;

	private MongoDBClient mdDBClient;// 行情数据库客户端

	@Value("${mongodb.instance.md.host}")
	private String mdMongoHost;
	@Value("${mongodb.instance.md.port}")
	private int mdMongoPort;
	@Value("${mongodb.instance.md.username}")
	private String mdMongoUsername;
	@Value("${mongodb.instance.md.password}")
	private String mdMongoPassword;
	@Value("${mongodb.instance.md.authdb}")
	private String mdMongoAuthDB;

	private MongoDBClient defaultDBClient;// 默认数据库客户端

	@Value("${mongodb.instance.default.host}")
	String defaultMongoHost;
	@Value("${mongodb.instance.default.port}")
	int defaultMongoPort;
	@Value("${mongodb.instance.default.username}")
	String defaultMongoUsername;
	@Value("${mongodb.instance.default.password}")
	String defaultMongoPassword;
	@Value("${mongodb.instance.default.authdb}")
	String defaultMongoAuthDB;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			mdDBClient = new MongoDBClient(mdMongoHost, mdMongoPort, mdMongoUsername, mdMongoPassword, mdMongoAuthDB);
		} catch (Exception e) {
			mdDBClient = null;
			throw new Exception("行情MongoDB数据库连接失败", e);
		}

		try {
			defaultDBClient = new MongoDBClient(defaultMongoHost, defaultMongoPort, defaultMongoUsername,
					defaultMongoPassword, defaultMongoAuthDB);
		} catch (Exception e) {
			defaultDBClient = null;
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
		List<Document> documentList = mdDBClient.find(minuteDbName, rtSymbol, filter, sort);

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

		log.info("加载Bar数据完成,合约{},共{}条,耗时{}ms", rtSymbol, barList.size(), System.currentTimeMillis() - startTime);
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
		List<Document> documentList = mdDBClient.find(tickDbName, rtSymbol, filter, sort);

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
		log.info("加载Tick数据完成,合约{},共{}条,耗时{}ms", rtSymbol, tickList.size(), System.currentTimeMillis() - startTime);
		return tickList;
	}
	
	@Override
	public boolean saveTickToDailyDB(Tick tick) {
		Document filter = new Document();
		filter.append("dateTime", tick.getDateTime().toDate()).append("rtSymbol", tick.getRtSymbol());
		
		try {
			Document document = MongoDBUtil.beanToDocument(tick);
			return mdDBClient.upsert(tickDailyDbName,  MongoDBService.dailyMarketDataTickCollection, document, filter);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error("行情记录存储Tick行情发生异常", e);
			return false;
		}
	}
	
	@Override
	public List<Tick> loadTickDataListFromDailyDB(String rtSymbol) {
		log.info("加载Tick数据,合约{}", rtSymbol);
		
		long startTime = System.currentTimeMillis();
		
		Document filter = new Document();
		filter.append("rtSymbol", rtSymbol);
		
		BasicDBObject sort = new BasicDBObject();
		sort.append("dateTime", 1);
		List<Document> documentList = mdDBClient.find(tickDailyDbName, dailyMarketDataTickCollection, filter, sort);

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
		log.info("加载Tick数据完成,合约{},共{}条,耗时{}ms", rtSymbol, tickList.size(), System.currentTimeMillis() - startTime);
		return tickList;
	}
}
