package xyz.redtorch.common.mongo;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoDBClient {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBClient.class);
    /*
     * MongoClient的实例代表数据库连接池,是线程安全的,可以被多线程共享,客户端在多线程条件下仅维持一个实例即可
     * Mongo是非线程安全的,目前mongodb API中已经建议用MongoClient替代Mongo
     */
    private MongoClient mongoClient = null;


    public MongoDBClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }


    public MongoDBClient(String host, int port, String username, String password, String authDB) throws Exception {
        logger.info("尝试建立MongoDB连接 Host:{} Port:{}", host, port);
        MongoClientOptions.Builder build = new MongoClientOptions.Builder();
        /*
         * 一个线程访问数据库的时候,在成功获取到一个可用数据库连接之前的最长等待时间为2分钟
         * 这里比较危险,如果超过maxWaitTime都没有获取到这个连接的话,该线程就会抛出Exception
         * 故这里设置的maxWaitTime应该足够大,以免由于排队线程过多造成的数据库访问失败
         */
        build.maxWaitTime(1000 * 60 * 2);
        build.connectTimeout(1000 * 60); // 与数据库建立连接的timeout设置为1分钟
        build.socketTimeout(0);// 套接字超时时间,0无限制
        build.maxConnectionIdleTime(60000);
        build.maxConnectionIdleTime(0);
        build.retryWrites(true);
        build.connectionsPerHost(300); // 连接池设置为300个连接,默认为100
        build.writeConcern(WriteConcern.ACKNOWLEDGED); // 写操作需要得到确认

        MongoClientOptions myOptions = build.build();
        try {
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(authDB)) {
                logger.info("使用无认证方式建立MongoDB连接");
                mongoClient = new MongoClient(new ServerAddress(host, port), myOptions);
            } else {
                logger.info("使用认证方式建立MongoDB连接");
                MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, authDB,
                        password.toCharArray());
                // 数据库连接实例
                mongoClient = new MongoClient(new ServerAddress(host, port), mongoCredential, myOptions);
            }
        } catch (MongoException e) {
            throw new Exception("MongoDB连接失败", e);
        }
    }


    public boolean isExits(String dbName, String collectionName, Document filter) {
        if (filter != null) {
            FindIterable<Document> docs = mongoClient.getDatabase(dbName).getCollection(collectionName)
                    .find(new Document(filter));

            Document doc = docs.first();
            return doc != null;
        }
        return false;
    }

    public boolean insert(String dbName, String collectionName, Document document) {
        if (document != null) {
            mongoClient.getDatabase(dbName).getCollection(collectionName).insertOne(document);
            return true;
        }
        return false;
    }


    public boolean upsert(String dbName, String collectionName, Document document, Document filter) {
        if (document != null) {
            ReplaceOptions replaceOptions = new ReplaceOptions();
            replaceOptions.upsert(true);
            mongoClient.getDatabase(dbName).getCollection(collectionName).replaceOne(filter, document, replaceOptions);
            return true;
        }
        return false;
    }


    public boolean upsertMany(String dbName, String collectionName, Document document, Document filter) {
        if (document != null) {
            UpdateOptions updateOptions = new UpdateOptions();
            updateOptions.upsert(true);
            mongoClient.getDatabase(dbName).getCollection(collectionName).updateMany(filter, document, updateOptions);
            return true;
        }
        return false;
    }


    public boolean insertMany(String dbName, String collectionName, List<Document> documentList) {
        if (documentList != null) {
            long startTimestamp = System.currentTimeMillis();
            mongoClient.getDatabase(dbName).getCollection(collectionName).insertMany(documentList);
            logger.info("MongoDB插入" + collectionName + "共" + documentList.size() + "条,耗时"
                    + (System.currentTimeMillis() - startTimestamp) + "ms");
            return true;
        }
        return false;
    }


    public boolean deleteById(String dbName, String collectionName, String _id) {
        ObjectId objectId = new ObjectId(_id);
        Bson filter = Filters.eq("_id", objectId);

        DeleteResult deleteResult = getDatabase(dbName).getCollection(collectionName).deleteOne(filter);
        long deletedCount = deleteResult.getDeletedCount();

        return deletedCount > 0;
    }

    public boolean delete(String dbName, String collectionName, Document filter) {
        if (filter != null) {
            DeleteResult result = mongoClient.getDatabase(dbName).getCollection(collectionName)
                    .deleteMany(new Document(filter));
            long deletedCount = result.getDeletedCount();
            return deletedCount > 0;
        }
        return false;
    }

    public boolean updateOne(String dbName, String collectionName, Document filter, Document document) {
        if (filter != null && filter.size() > 0 && document != null) {
            UpdateResult result = mongoClient.getDatabase(dbName).getCollection(collectionName)
                    .updateOne(new Document(filter), new Document("$set", new Document(document)));
            long modifiedCount = result.getModifiedCount();
            return modifiedCount > 0;
        }

        return false;
    }

    public boolean updateById(String dbName, String collectionName, String _id, Document document) {
        ObjectId objectId = new ObjectId(_id);
        Bson filter = Filters.eq("_id", objectId);

        UpdateResult result = getDatabase(dbName).getCollection(collectionName).updateOne(filter,
                new Document("$set", document));
        long modifiedCount = result.getModifiedCount();

        return modifiedCount > 0;
    }

    public List<Document> find(String dbName, String collectionName) {
        List<Document> resultList = new ArrayList<>();
        FindIterable<Document> docs = mongoClient.getDatabase(dbName).getCollection(collectionName).find();

        for (Document doc : docs) {
            resultList.add(doc);
        }
        return resultList;
    }


    public List<Document> find(String dbName, String collectionName, Bson filter) {
        List<Document> resultList = new ArrayList<>();
        if (filter != null) {
            FindIterable<Document> docs = mongoClient.getDatabase(dbName).getCollection(collectionName).find(filter);
            for (Document doc : docs) {
                resultList.add(doc);
            }
        }

        return resultList;
    }

    public List<Document> find(String dbName, String collectionName, Bson filter, BasicDBObject sort) {
        List<Document> resultList = new ArrayList<>();
        if (filter != null) {
            FindIterable<Document> docs = mongoClient.getDatabase(dbName).getCollection(collectionName).find(filter)
                    .sort(sort);
            for (Document doc : docs) {
                resultList.add(doc);
            }
        }

        return resultList;
    }

    public Document findById(String dbName, String collectionName, String _id) {
        ObjectId objectId = new ObjectId(_id);

        return getDatabase(dbName).getCollection(collectionName).find(Filters.eq("_id", objectId)).first();
    }


    public List<Document> findByPage(String dbName, String collectionName, Bson filter, int pageIndex, int pageSize) {
        return findByPage(dbName, collectionName, filter, new BasicDBObject("_id", 1), pageIndex, pageSize);
    }

    public List<Document> findByPage(String dbName, String collectionName, Bson filter, BasicDBObject sort,
                                     int pageIndex, int pageSize) {
        List<Document> resultList = new ArrayList<>();
        FindIterable<Document> docs = getDatabase(dbName).getCollection(collectionName).find(filter).sort(sort)
                .skip((pageIndex - 1) * pageSize).limit(pageSize);

        for (Document doc : docs) {
            resultList.add(doc);
        }

        return resultList;
    }


    @SuppressWarnings("rawtypes")
    public MongoCollection getCollection(String dbName, String collectionName) {
        return mongoClient.getDatabase(dbName).getCollection(collectionName);
    }


    public MongoDatabase getDatabase(String dbName) {
        return mongoClient.getDatabase(dbName);
    }


    public long getCount(String dbName, String collectionName) {
        return getDatabase(dbName).getCollection(collectionName).countDocuments();
    }


    public List<String> getAllCollections(String dbName) {
        MongoIterable<String> cols = getDatabase(dbName).listCollectionNames();
        List<String> _list = new ArrayList<>();
        for (String s : cols) {
            _list.add(s);
        }
        return _list;
    }

    public MongoIterable<String> getAllDatabaseName() {
        return mongoClient.listDatabaseNames();
    }

    public void dropDatabase(String dbName) {
        getDatabase(dbName).drop();
    }

    public void dropCollection(String dbName, String collectionName) {
        getDatabase(dbName).getCollection(collectionName).drop();
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }
}
