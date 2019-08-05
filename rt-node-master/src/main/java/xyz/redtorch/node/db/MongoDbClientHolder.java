package xyz.redtorch.node.db;

import xyz.redtorch.common.mongo.MongoDBClient;

public interface MongoDbClientHolder {

	MongoDBClient getManagementDbClient();

	MongoDBClient getMarketDataDbClient();

	String getManagementDbName();

	String getMarketDataDbName();

}