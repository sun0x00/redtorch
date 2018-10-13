package xyz.redtorch.utils;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

/**
 * 单例形式的MongoDB客户端实现（暂未使用）
 * 
 * @author sun0x00@gmail.com
 *
 */
public class MongoDBClientSingleton extends MongoDBClient {

	private static Logger log = LoggerFactory.getLogger(MongoDBClientSingleton.class);

	private static MongoClient mongoClient = null;

	private MongoDBClientSingleton() {
		super(mongoClient);
	}

	private static MongoDBClientSingleton mongoDBClientSingleton = null;

	public synchronized static MongoDBClientSingleton getInstance(String instenceName) {
		if (mongoDBClientSingleton == null) {
			if (mongoClient == null) {
				CompositeConfiguration config = new CompositeConfiguration();
				try {
					FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
							PropertiesConfiguration.class)
									.configure(new Parameters().properties().setFileName("mongodb.properties")
											.setThrowExceptionOnMissing(true)
											.setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
											.setIncludesAllowed(false));
					PropertiesConfiguration propertiesConfiguration = builder.getConfiguration();
					config.addConfiguration(propertiesConfiguration);
				} catch (ConfigurationException e) {
					log.error("MongoDBClientSingleton配置读取失败", e);
				}

				String ip = config.getString("host");
				int port = config.getInt("port");
				String username = config.getString("username");
				String password = config.getString("password");
				String authdb = config.getString("authdb");
				log.info("MongoDB IP:{} Port:{}", ip, port);
				MongoClientOptions.Builder build = new MongoClientOptions.Builder();
				/*
				 * 一个线程访问数据库的时候,在成功获取到一个可用数据库连接之前的最长等待时间为2分钟
				 * 这里比较危险,如果超过maxWaitTime都没有获取到这个连接的话,该线程就会抛出Exception
				 * 故这里设置的maxWaitTime应该足够大,以免由于排队线程过多造成的数据库访问失败
				 */
				build.maxWaitTime(1000 * 60 * 2);
				build.connectTimeout(1000 * 60 * 1); // 与数据库建立连接的timeout设置为1分钟
				build.socketTimeout(0);// 套接字超时时间,0无限制
				build.connectionsPerHost(300); // 连接池设置为300个连接,默认为100
				build.threadsAllowedToBlockForConnectionMultiplier(5000);// 线程队列数,如果连接线程排满了队列就会抛出“Out of semaphores to
																			// get db”错误
				build.writeConcern(WriteConcern.ACKNOWLEDGED);

				MongoClientOptions myOptions = build.build();
				try {
					MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, authdb,
							password.toCharArray());
					// 数据库连接实例
					mongoClient = new MongoClient(new ServerAddress(ip, port), mongoCredential, myOptions);
				} catch (MongoException e) {
					log.error("MongoDB连接失败", e);
				}

			}
			mongoDBClientSingleton = new MongoDBClientSingleton();
		}
		return mongoDBClientSingleton;
	}
}
