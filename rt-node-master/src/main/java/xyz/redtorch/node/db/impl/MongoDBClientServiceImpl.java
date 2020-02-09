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

	@Value("${rt.node.master.db.management.host}")
	private String managementDBHost;
	@Value("${rt.node.master.db.management.port}")
	private int managementDBPort;
	@Value("${rt.node.master.db.management.username}")
	private String managementDBUsername;
	@Value("${rt.node.master.db.management.password}")
	private String managementDBPassword;
	@Value("${rt.node.master.db.management.authdb}")
	private String managementDBAuthDB;
	@Value("${rt.node.master.db.management.dbname}")
	private String managementDBName;

	private MongoDBClient marketDataTodayDBClient;

	@Value("${rt.node.master.db.market-data-today.host}")
	private String marketDataTodayDBHost;
	@Value("${rt.node.master.db.market-data-today.port}")
	private int marketDataTodayDBPort;
	@Value("${rt.node.master.db.market-data-today.username}")
	private String marketDataTodayDBUsername;
	@Value("${rt.node.master.db.market-data-today.password}")
	private String marketDataTodayDBPassword;
	@Value("${rt.node.master.db.market-data-today.authdb}")
	private String marketDataTodayDBAuthDB;
	@Value("${rt.node.master.db.market-data-today.dbname}")
	private String marketDataTodayDBName;

	private MongoDBClient marketDataHistDBClient;

	@Value("${rt.node.master.db.market-data-hist.host}")
	private String marketDataHistDBHost;
	@Value("${rt.node.master.db.market-data-hist.port}")
	private int marketDataHistDBPort;
	@Value("${rt.node.master.db.market-data-hist.username}")
	private String marketDataHistDBUsername;
	@Value("${rt.node.master.db.market-data-hist.password}")
	private String marketDataHistDBPassword;
	@Value("${rt.node.master.db.market-data-hist.authdb}")
	private String marketDataHistDBAuthDB;
	@Value("${rt.node.master.db.market-data-hist.dbname}")
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
