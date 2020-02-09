package xyz.redtorch.common.mongo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoDBClient {

	private static final Logger logger = LoggerFactory.getLogger(MongoDBClient.class);
	/*
	 * MongoClient的实例代表数据库连接池,是线程安全的,可以被多线程共享,客户端在多线程条件下仅维持一个实例即可
	 * Mongo是非线程安全的,目前mongodb API中已经建议用MongoClient替代Mongo
	 */
	private MongoClient mongoClient = null;

	/**
	 * 构造方法,用于实现单例等特殊需求
	 * 
	 * @param mongoClient
	 */
	public MongoDBClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	/**
	 * 构造方法,创建MongoClient实例
	 * 
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param authdb
	 * @throws Exception
	 */
	public MongoDBClient(String host, int port, String username, String password, String authdb) throws Exception {
		logger.info("连接MongoDB Host:{} Port:{}", host, port);
		MongoClientOptions.Builder build = new MongoClientOptions.Builder();
		/*
		 * 一个线程访问数据库的时候,在成功获取到一个可用数据库连接之前的最长等待时间为2分钟
		 * 这里比较危险,如果超过maxWaitTime都没有获取到这个连接的话,该线程就会抛出Exception
		 * 故这里设置的maxWaitTime应该足够大,以免由于排队线程过多造成的数据库访问失败
		 */
		build.maxWaitTime(1000 * 60 * 2);
		build.connectTimeout(1000 * 60 * 1); // 与数据库建立连接的timeout设置为1分钟
		build.socketTimeout(0);// 套接字超时时间,0无限制
		build.maxConnectionIdleTime(60000);
		build.maxConnectionIdleTime(0);
		build.retryWrites(true);
		build.connectionsPerHost(300); // 连接池设置为300个连接,默认为100
		build.threadsAllowedToBlockForConnectionMultiplier(1000);// 线程队列数,如果连接线程排满了队列就会抛出“Out of semaphores to get db" 错误
		build.writeConcern(WriteConcern.ACKNOWLEDGED); // 写操作需要得到确认

		MongoClientOptions myOptions = build.build();
		try {
			if (StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(authdb)) {
				logger.info("使用无认证方式连接MongoDB");
				mongoClient = new MongoClient(new ServerAddress(host, port), myOptions);
			} else {
				logger.info("使用认证方式连接MongoDB");
				MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, authdb,
						password.toCharArray());
				// 数据库连接实例
				mongoClient = new MongoClient(new ServerAddress(host, port), mongoCredential, myOptions);
			}
		} catch (MongoException e) {
			throw new Exception("MongoDB连接失败", e);
		}
	}

	/**
	 * 判断数据是否存在
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param filter
	 * @return
	 */
	public boolean isExits(String dbName, String collectionName, Document filter) {
		if (filter != null) {
			FindIterable<Document> docs = mongoClient.getDatabase(dbName).getCollection(collectionName)
					.find(new Document(filter));

			Document doc = docs.first();
			if (doc != null) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * 插入数据
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param document
	 * @return
	 */
	public boolean insert(String dbName, String collectionName, Document document) {
		if (document != null) {
			mongoClient.getDatabase(dbName).getCollection(collectionName).insertOne(document);
			return true;
		}
		return false;
	}

	/**
	 * 更新或插入数据
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param document
	 * @param filter
	 * @return
	 */
	public boolean upsert(String dbName, String collectionName, Document document, Document filter) {
		if (document != null) {
			ReplaceOptions replaceOptions = new ReplaceOptions();
			replaceOptions.upsert(true);
			mongoClient.getDatabase(dbName).getCollection(collectionName).replaceOne(filter, document, replaceOptions);
			return true;
		}
		return false;
	}

	/**
	 * 更新或插入数据集合
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param document
	 * @param filter
	 * @return
	 */
	public boolean upsertMany(String dbName, String collectionName, Document document, Document filter) {
		if (document != null) {
			UpdateOptions updateOptions = new UpdateOptions();
			updateOptions.upsert(true);
			mongoClient.getDatabase(dbName).getCollection(collectionName).updateMany(filter, document, updateOptions);
			return true;
		}
		return false;
	}

	/**
	 * 插入数据集合
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param documentList
	 * @return
	 */
	public boolean insertMany(String dbName, String collectionName, List<Document> documentList) {
		if (documentList != null) {
			Long startTimestamp = System.currentTimeMillis();
			mongoClient.getDatabase(dbName).getCollection(collectionName).insertMany(documentList);
			logger.info("MongoDB插入" + collectionName + "共" + documentList.size() + "条,耗时"
					+ (System.currentTimeMillis() - startTimestamp) + "ms");
			return true;
		}
		return false;
	}

	/**
	 * 通过_id删除数据
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param _id
	 * @return
	 */
	public boolean deleteById(String dbName, String collectionName, String _id) {
		ObjectId objectId = new ObjectId(_id);
		Bson filter = Filters.eq("_id", objectId);

		DeleteResult deleteResult = getDatabase(dbName).getCollection(collectionName).deleteOne(filter);
		long deletedCount = deleteResult.getDeletedCount();

		return deletedCount > 0 ? true : false;
	}

	/**
	 * 通过过滤条件删除数据
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param filter
	 * @return
	 */
	public boolean delete(String dbName, String collectionName, Document filter) {
		if (filter != null) {
			DeleteResult result = mongoClient.getDatabase(dbName).getCollection(collectionName)
					.deleteMany(new Document(filter));
			long deletedCount = result.getDeletedCount();
			return deletedCount > 0 ? true : false;
		}
		return false;
	}

	/**
	 * 更新
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param filter
	 * @param document
	 * @return
	 */
	public boolean updateOne(String dbName, String collectionName, Document filter, Document document) {
		if (filter != null && filter.size() > 0 && document != null) {
			UpdateResult result = mongoClient.getDatabase(dbName).getCollection(collectionName)
					.updateOne(new Document(filter), new Document("$set", new Document(document)));
			long modifiedCount = result.getModifiedCount();
			return modifiedCount > 0 ? true : false;
		}

		return false;
	}

	/**
	 * 通过_id更新
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param _id
	 * @param document
	 * @return
	 */
	public boolean updateById(String dbName, String collectionName, String _id, Document document) {
		ObjectId objectId = new ObjectId(_id);
		Bson filter = Filters.eq("_id", objectId);

		UpdateResult result = getDatabase(dbName).getCollection(collectionName).updateOne(filter,
				new Document("$set", document));
		long modifiedCount = result.getModifiedCount();

		return modifiedCount > 0 ? true : false;
	}

	/**
	 * 查询全部数据（可能造成溢出）
	 * 
	 * @param dbName
	 * @param collectionName
	 * @return
	 */
	public List<Document> find(String dbName, String collectionName) {
		List<Document> resultList = new ArrayList<Document>();
		FindIterable<Document> docs = mongoClient.getDatabase(dbName).getCollection(collectionName).find();

		MongoCursor<Document> mongoCursor = docs.iterator();
		while (mongoCursor.hasNext()) {
			resultList.add(mongoCursor.next());
		}
		return resultList;
	}

	/**
	 * 通过过滤条件查询数据
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param filter
	 * @return
	 */
	public List<Document> find(String dbName, String collectionName, Bson filter) {
		List<Document> resultList = new ArrayList<Document>();
		if (filter != null) {
			FindIterable<Document> docs = mongoClient.getDatabase(dbName).getCollection(collectionName).find(filter);
			MongoCursor<Document> mongoCursor = docs.iterator();
			while (mongoCursor.hasNext()) {
				resultList.add(mongoCursor.next());
			}
		}

		return resultList;
	}

	/**
	 * 通过过滤条件查询数据并排序
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param filter
	 * @param sort
	 * @return
	 */
	public List<Document> find(String dbName, String collectionName, Bson filter, BasicDBObject sort) {
		List<Document> resultList = new ArrayList<Document>();
		if (filter != null) {
			FindIterable<Document> docs = mongoClient.getDatabase(dbName).getCollection(collectionName).find(filter)
					.sort(sort);
			MongoCursor<Document> mongoCursor = docs.iterator();
			while (mongoCursor.hasNext()) {
				resultList.add(mongoCursor.next());
			}
		}

		return resultList;
	}

	/**
	 * 通过_id查找
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param _id
	 * @return
	 */
	public Document findById(String dbName, String collectionName, String _id) {
		ObjectId objectId = new ObjectId(_id);

		Document doc = getDatabase(dbName).getCollection(collectionName).find(Filters.eq("_id", objectId)).first();
		return doc;
	}

	/**
	 * 分页查询
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param filter
	 * @param pageIndex      从1开始
	 * @param pageSize
	 * @return
	 */
	public List<Document> findByPage(String dbName, String collectionName, Bson filter, int pageIndex, int pageSize) {
		Bson orderBy = new BasicDBObject("_id", 1);

		List<Document> resultList = new ArrayList<Document>();
		FindIterable<Document> docs = getDatabase(dbName).getCollection(collectionName).find(filter).sort(orderBy)
				.skip((pageIndex - 1) * pageSize).limit(pageSize);

		MongoCursor<Document> mongoCursor = docs.iterator();
		while (mongoCursor.hasNext()) {
			resultList.add(mongoCursor.next());
		}

		return resultList;
	}

	/**
	 * 查询,可指定排序
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param filter
	 * @param sort
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<Document> findByPage(String dbName, String collectionName, Bson filter, BasicDBObject sort,
			int pageIndex, int pageSize) {
		List<Document> resultList = new ArrayList<Document>();
		FindIterable<Document> docs = getDatabase(dbName).getCollection(collectionName).find(filter).sort(sort)
				.skip((pageIndex - 1) * pageSize).limit(pageSize);

		MongoCursor<Document> mongoCursor = docs.iterator();
		while (mongoCursor.hasNext()) {
			resultList.add(mongoCursor.next());
		}

		return resultList;
	}

	/**
	 * 获取Collection实例
	 * 
	 * @param dbName
	 * @param collectionName
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public MongoCollection getCollection(String dbName, String collectionName) {
		return mongoClient.getDatabase(dbName).getCollection(collectionName);
	}

	/**
	 * 获取数据库实例
	 * 
	 * @param dbName
	 * @return
	 */
	public MongoDatabase getDatabase(String dbName) {
		return mongoClient.getDatabase(dbName);
	}

	/**
	 * 获取Collection统计
	 * 
	 * @param dbName
	 * @param collectionName
	 * @return
	 */
	public long getCount(String dbName, String collectionName) {
		return getDatabase(dbName).getCollection(collectionName).countDocuments();
	}

	/**
	 * 查询dbName下的所有表名
	 * 
	 * @param dbName
	 * @return
	 */
	public List<String> getAllCollections(String dbName) {
		MongoIterable<String> cols = getDatabase(dbName).listCollectionNames();
		List<String> _list = new ArrayList<String>();
		for (String s : cols) {
			_list.add(s);
		}
		return _list;
	}

	/**
	 * 获取所有数据库名称列表
	 * 
	 * @return
	 */
	public MongoIterable<String> getAllDatabaseName() {
		MongoIterable<String> s = mongoClient.listDatabaseNames();
		return s;
	}

	/**
	 * 删除一个数据库
	 * 
	 * @param dbName
	 */
	public void dropDatabase(String dbName) {
		getDatabase(dbName).drop();
	}

	/**
	 * 删除collection
	 * 
	 * @param dbName
	 * @param collectionName
	 */
	public void dropCollection(String dbName, String collectionName) {
		getDatabase(dbName).getCollection(collectionName).drop();
	}

	/**
	 * 关闭客户端
	 */
	public void close() {
		if (mongoClient != null) {
			mongoClient.close();
			mongoClient = null;
		}
	}
}
