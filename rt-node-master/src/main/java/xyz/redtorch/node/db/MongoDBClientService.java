package xyz.redtorch.node.db;

import xyz.redtorch.common.mongo.MongoDBClient;

public interface MongoDBClientService {
	MongoDBClient getManagementDBClient();

	MongoDBClient getMarketDataTodayDBClient();

	MongoDBClient getMarketDataHistDBClient();

	String getManagementDBName();

	String getMarketDataTodayDBName();

	String getMarketDataHistDBName();
}