/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface EWrapper {
    ///////////////////////////////////////////////////////////////////////
    // Interface methods
    ///////////////////////////////////////////////////////////////////////
    void tickPrice( int tickerId, int field, double price, TickAttr attrib);
    void tickSize( int tickerId, int field, int size);
    void tickOptionComputation( int tickerId, int field, double impliedVol,
    		double delta, double optPrice, double pvDividend,
    		double gamma, double vega, double theta, double undPrice);
	void tickGeneric(int tickerId, int tickType, double value);
	void tickString(int tickerId, int tickType, String value);
	void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate);
    void orderStatus( int orderId, String status, double filled, double remaining,
            double avgFillPrice, int permId, int parentId, double lastFillPrice,
            int clientId, String whyHeld, double mktCapPrice);
    void openOrder( int orderId, Contract contract, Order order, OrderState orderState);
    void openOrderEnd();
    void updateAccountValue(String key, String value, String currency, String accountName);
    void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue,
            double averageCost, double unrealizedPNL, double realizedPNL, String accountName);
    void updateAccountTime(String timeStamp);
    void accountDownloadEnd(String accountName);
    void nextValidId( int orderId);
    void contractDetails(int reqId, ContractDetails contractDetails);
    void bondContractDetails(int reqId, ContractDetails contractDetails);
    void contractDetailsEnd(int reqId);
    void execDetails( int reqId, Contract contract, Execution execution);
    void execDetailsEnd( int reqId);
    void updateMktDepth( int tickerId, int position, int operation, int side, double price, int size);
    void updateMktDepthL2( int tickerId, int position, String marketMaker, int operation,
    		int side, double price, int size);
    void updateNewsBulletin( int msgId, int msgType, String message, String origExchange);
    void managedAccounts( String accountsList);
    void receiveFA(int faDataType, String xml);
    void historicalData(int reqId, Bar bar);
    void scannerParameters(String xml);
    void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance,
    		String benchmark, String projection, String legsStr);
    void scannerDataEnd(int reqId);
    void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count);
    void currentTime(long time);
    void fundamentalData(int reqId, String data);
    void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract);
    void tickSnapshotEnd(int reqId);
    void marketDataType(int reqId, int marketDataType);
    void commissionReport(CommissionReport commissionReport);
    void position(String account, Contract contract, double pos, double avgCost);
    void positionEnd();
    void accountSummary(int reqId, String account, String tag, String value, String currency);
    void accountSummaryEnd(int reqId);
    void verifyMessageAPI( String apiData);
    void verifyCompleted( boolean isSuccessful, String errorText);
    void verifyAndAuthMessageAPI( String apiData, String xyzChallenge);
    void verifyAndAuthCompleted( boolean isSuccessful, String errorText);
    void displayGroupList( int reqId, String groups);
    void displayGroupUpdated( int reqId, String contractInfo);
    void error( Exception e);
    void error( String str);
    void error(int id, int errorCode, String errorMsg);
    void connectionClosed();
    void connectAck();
    void positionMulti( int reqId, String account, String modelCode, Contract contract, double pos, double avgCost);
    void positionMultiEnd( int reqId);
    void accountUpdateMulti( int reqId, String account, String modelCode, String key, String value, String currency);
    void accountUpdateMultiEnd( int reqId);
    void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes);
    void securityDefinitionOptionalParameterEnd(int reqId);
	void softDollarTiers(int reqId, SoftDollarTier[] tiers);
    void familyCodes(FamilyCode[] familyCodes);
    void symbolSamples(int reqId, ContractDescription[] contractDescriptions);
	void historicalDataEnd(int reqId, String startDateStr, String endDateStr);
    void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions);
    void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData);
	void smartComponents(int reqId, Map<Integer, Entry<String, Character>> theMap);
	void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions);
    void newsProviders(NewsProvider[] newsProviders);
    void newsArticle(int requestId, int articleType, String articleText);
    void historicalNews(int requestId, String time, String providerCode, String articleId, String headline);
    void historicalNewsEnd(int requestId, boolean hasMore);
	void headTimestamp(int reqId, String headTimestamp);
	void histogramData(int reqId, List<HistogramEntry> items);
    void historicalDataUpdate(int reqId, Bar bar);
	void rerouteMktDataReq(int reqId, int conId, String exchange);
	void rerouteMktDepthReq(int reqId, int conId, String exchange);
    void marketRule(int marketRuleId, PriceIncrement[] priceIncrements);
	void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL);
	void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value);
    void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done);
    void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done);
    void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done);
    void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttr attribs, String exchange, String specialConditions);
    void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize, TickAttr attribs);
    void tickByTickMidPoint(int reqId, long time, double midPoint);
}

