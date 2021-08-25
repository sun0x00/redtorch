package xyz.redtorch.node.db.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import xyz.redtorch.common.mongo.MongoDBClient;
import xyz.redtorch.common.service.MongoDBService;

@Component
public class MongoDBServiceImpl implements InitializingBean, MongoDBService {


    private static final Logger logger = LoggerFactory.getLogger(MongoDBServiceImpl.class);

    private MongoDBClient marketDataTodayDBClient;

    private final String marketDataTodayDBHost;
    private final int marketDataTodayDBPort;
    private final String marketDataTodayDBUsername;
    private final String marketDataTodayDBPassword;
    private final String marketDataTodayDBAuthDB;
    private final String marketDataTodayDBName;

    public MongoDBServiceImpl(@Value("${rt.master.marketDataTodayDBHost}") String marketDataTodayDBHost, //
                              @Value("${rt.master.marketDataTodayDBPort}") int marketDataTodayDBPort, //
                              @Value("${rt.master.marketDataTodayDBUsername}") String marketDataTodayDBUsername, //
                              @Value("${rt.master.marketDataTodayDBPassword}") String marketDataTodayDBPassword, //
                              @Value("${rt.master.marketDataTodayDBAuthDB}") String marketDataTodayDBAuthDB, //
                              @Value("${rt.master.marketDataTodayDBName}") String marketDataTodayDBName){
        this.marketDataTodayDBHost = marketDataTodayDBHost;
        this.marketDataTodayDBPort = marketDataTodayDBPort;
        this.marketDataTodayDBUsername = marketDataTodayDBUsername;
        this.marketDataTodayDBPassword = marketDataTodayDBPassword;
        this.marketDataTodayDBAuthDB = marketDataTodayDBAuthDB;
        this.marketDataTodayDBName = marketDataTodayDBName;

    }


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            marketDataTodayDBClient = new MongoDBClient(marketDataTodayDBHost, marketDataTodayDBPort, marketDataTodayDBUsername, marketDataTodayDBPassword, marketDataTodayDBAuthDB);
        } catch (Exception e) {
            logger.error("今日行情数据库连接失败,程序终止", e);
            System.exit(0);
        }
    }

    @Override
    public MongoDBClient getClient() {
        return marketDataTodayDBClient;
    }

    @Override
    public String getDBName() {
        return marketDataTodayDBName;
    }
}
