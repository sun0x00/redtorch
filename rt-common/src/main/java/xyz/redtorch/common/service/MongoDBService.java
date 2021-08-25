package xyz.redtorch.common.service;

import xyz.redtorch.common.mongo.MongoDBClient;

public interface MongoDBService {
    MongoDBClient getClient();

    String getDBName();
}
