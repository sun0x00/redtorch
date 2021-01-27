package xyz.redtorch.node.db.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import xyz.redtorch.common.mongo.MongoDBClient;
import xyz.redtorch.node.db.MongoDBClientService;

@Component
public class MongoDBClientServiceImpl implements InitializingBean, MongoDBClientService {
	private static final Logger logger = LoggerFactory.getLogger(MongoDBClientServiceImpl.class);

	private MongoDBClient managementDBClient;

	@Value("${rt.master.managementDBHost}")
	private String managementDBHost;
	@Value("${rt.master.managementDBPort}")
	private int managementDBPort;
	@Value("${rt.master.managementDBUsername}")
	private String managementDBUsername;
	@Value("${rt.master.managementDBPassword}")
	private String managementDBPassword;
	@Value("${rt.master.managementDBAuthDB}")
	private String managementDBAuthDB;
	@Value("${rt.master.managementDBName}")
	private String managementDBName;

	private MongoDBClient marketDataTodayDBClient;

	@Value("${rt.master.marketDataTodayDBHost}")
	private String marketDataTodayDBHost;
	@Value("${rt.master.marketDataTodayDBPort}")
	private int marketDataTodayDBPort;
	@Value("${rt.master.marketDataTodayDBUsername}")
	private String marketDataTodayDBUsername;
	@Value("${rt.master.marketDataTodayDBPassword}")
	private String marketDataTodayDBPassword;
	@Value("${rt.master.marketDataTodayDBAuthDB}")
	private String marketDataTodayDBAuthDB;
	@Value("${rt.master.marketDataTodayDBName}")
	private String marketDataTodayDBName;

	private MongoDBClient marketDataHistDBClient;

	@Value("${rt.master.marketDataHistDBHost}")
	private String marketDataHistDBHost;
	@Value("${rt.master.marketDataHistDBPort}")
	private int marketDataHistDBPort;
	@Value("${rt.master.marketDataHistDBUsername}")
	private String marketDataHistDBUsername;
	@Value("${rt.master.marketDataHistDBPassword}")
	private String marketDataHistDBPassword;
	@Value("${rt.master.marketDataHistDBAuthDB}")
	private String marketDataHistDBAuthDB;
	@Value("${rt.master.marketDataHistDBName}")
	private String marketDataHistDBName;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			managementDBClient = new MongoDBClient(managementDBHost, managementDBPort, managementDBUsername, managementDBPassword, managementDBAuthDB);
		} catch (Exception e) {
			logger.error("管理数据库连接失败,程序终止", e);
			System.exit(0);
		}

		try {
			marketDataTodayDBClient = new MongoDBClient(marketDataTodayDBHost, marketDataTodayDBPort, marketDataTodayDBUsername, marketDataTodayDBPassword, marketDataTodayDBAuthDB);
		} catch (Exception e) {
			logger.error("今日行情数据库连接失败,程序终止", e);
			System.exit(0);
		}

		try {
			marketDataHistDBClient = new MongoDBClient(marketDataHistDBHost, marketDataHistDBPort, marketDataHistDBUsername, marketDataHistDBPassword, marketDataHistDBAuthDB);
		} catch (Exception e) {
			logger.error("历史行情数据库连接失败,程序终止", e);
			System.exit(0);
		}
	}

	@Override
	public MongoDBClient getManagementDBClient() {
		return this.managementDBClient;
	}

	@Override
	public MongoDBClient getMarketDataTodayDBClient() {
		return this.marketDataTodayDBClient;
	}

	@Override
	public String getManagementDBName() {
		return this.managementDBName;
	}

	@Override
	public String getMarketDataTodayDBName() {
		return this.marketDataTodayDBName;
	}

	@Override
	public MongoDBClient getMarketDataHistDBClient() {
		return this.marketDataHistDBClient;
	}

	@Override
	public String getMarketDataHistDBName() {
		return this.marketDataHistDBName;
	}

}
