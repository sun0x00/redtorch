/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class EDecoder implements ObjectInput {
    // incoming msg id's
    private static final int END_CONN           = -1;
    private static final int TICK_PRICE		= 1;
    private static final int TICK_SIZE		= 2;
    private static final int ORDER_STATUS	= 3;
    private static final int ERR_MSG		= 4;
    private static final int OPEN_ORDER         = 5;
    private static final int ACCT_VALUE         = 6;
    private static final int PORTFOLIO_VALUE    = 7;
    private static final int ACCT_UPDATE_TIME   = 8;
    private static final int NEXT_VALID_ID      = 9;
    private static final int CONTRACT_DATA      = 10;
    private static final int EXECUTION_DATA     = 11;
    private static final int MARKET_DEPTH     	= 12;
    private static final int MARKET_DEPTH_L2    = 13;
    private static final int NEWS_BULLETINS    	= 14;
    private static final int MANAGED_ACCTS    	= 15;
    private static final int RECEIVE_FA    	    = 16;
    private static final int HISTORICAL_DATA    = 17;
    private static final int BOND_CONTRACT_DATA = 18;
    private static final int SCANNER_PARAMETERS = 19;
    private static final int SCANNER_DATA       = 20;
    private static final int TICK_OPTION_COMPUTATION = 21;
    private static final int TICK_GENERIC = 45;
    private static final int TICK_STRING = 46;
    private static final int TICK_EFP = 47;
    private static final int CURRENT_TIME = 49;
    private static final int REAL_TIME_BARS = 50;
    private static final int FUNDAMENTAL_DATA = 51;
    private static final int CONTRACT_DATA_END = 52;
    private static final int OPEN_ORDER_END = 53;
    private static final int ACCT_DOWNLOAD_END = 54;
    private static final int EXECUTION_DATA_END = 55;
    private static final int DELTA_NEUTRAL_VALIDATION = 56;
    private static final int TICK_SNAPSHOT_END = 57;
    private static final int MARKET_DATA_TYPE = 58;
    private static final int COMMISSION_REPORT = 59;
    private static final int POSITION = 61;
    private static final int POSITION_END = 62;
    private static final int ACCOUNT_SUMMARY = 63;
    private static final int ACCOUNT_SUMMARY_END = 64;
    private static final int VERIFY_MESSAGE_API = 65;
    private static final int VERIFY_COMPLETED = 66;
    private static final int DISPLAY_GROUP_LIST = 67;
    private static final int DISPLAY_GROUP_UPDATED = 68;
    private static final int VERIFY_AND_AUTH_MESSAGE_API = 69;
    private static final int VERIFY_AND_AUTH_COMPLETED = 70;
    private static final int POSITION_MULTI = 71;
    private static final int POSITION_MULTI_END = 72;
    private static final int ACCOUNT_UPDATE_MULTI = 73;
    private static final int ACCOUNT_UPDATE_MULTI_END = 74;
    private static final int SECURITY_DEFINITION_OPTION_PARAMETER = 75;
    private static final int SECURITY_DEFINITION_OPTION_PARAMETER_END = 76;
    private static final int SOFT_DOLLAR_TIERS = 77;
    private static final int FAMILY_CODES = 78;
    private static final int SYMBOL_SAMPLES = 79;
    private static final int MKT_DEPTH_EXCHANGES = 80;
    private static final int TICK_REQ_PARAMS = 81;
    private static final int SMART_COMPONENTS = 82;
    private static final int NEWS_ARTICLE = 83;
    private static final int TICK_NEWS = 84;
    private static final int NEWS_PROVIDERS = 85;
    private static final int HISTORICAL_NEWS = 86;
    private static final int HISTORICAL_NEWS_END = 87;
    private static final int HEAD_TIMESTAMP = 88;
    private static final int HISTOGRAM_DATA = 89;
    private static final int HISTORICAL_DATA_UPDATE = 90;
    private static final int REROUTE_MKT_DATA_REQ = 91;
    private static final int REROUTE_MKT_DEPTH_REQ = 92;
    private static final int MARKET_RULE = 93;
    private static final int PNL = 94;
    private static final int PNL_SINGLE = 95;
    private static final int HISTORICAL_TICKS = 96;
    private static final int HISTORICAL_TICKS_BID_ASK = 97;
    private static final int HISTORICAL_TICKS_LAST = 98;
    private static final int TICK_BY_TICK = 99;
    

    static final int MAX_MSG_LENGTH = 0xffffff;
    private static final int REDIRECT_MSG_ID = -1;

    private EClientMsgSink m_clientMsgSink;
    private EWrapper m_EWrapper;
    private int m_serverVersion;
	private IMessageReader m_messageReader;

	public EDecoder(int serverVersion, EWrapper callback) {
		this(serverVersion, callback, null);
	}
	
	public EDecoder(int serverVersion, EWrapper callback, EClientMsgSink sink) {
		m_clientMsgSink = sink;
		m_serverVersion = serverVersion;
		m_EWrapper = callback;
	}
	
    private void processFirstMsg() throws IOException {
        m_serverVersion = readInt();
        
        // Handle redirect
        if( m_serverVersion == REDIRECT_MSG_ID ) {
            String newAddress = readStr();

            m_serverVersion = 0;
            
            if (m_clientMsgSink != null)
            	m_clientMsgSink.redirect(newAddress);
        	
        	return;
        }
       
        
		if (m_serverVersion >= 20) {
			// currently with Unified both server version and time sent in one
			// message
			String twsTime = readStr();
			
			if (m_clientMsgSink != null)
				m_clientMsgSink.serverVersion(m_serverVersion, twsTime);
		} else {
			if (m_clientMsgSink != null)
				m_clientMsgSink.serverVersion(m_serverVersion, null);
		}
		
		m_EWrapper.connectAck();
    } 
    
    private boolean readMessageToInternalBuf(InputStream dis) {
  		m_messageReader = new PreV100MessageReader(dis);
    	return true;
    }
    
    public int processMsg(EMessage msg) throws IOException {
    	if (!readMessageToInternalBuf(msg.getStream())) {
    		return 0;
    	}
    	
    	if (m_serverVersion == 0) {
    		processFirstMsg();
    		
    		return m_messageReader.msgLength();
    	}
    	
    	int msgId = readInt();

        switch( msgId) {
            case END_CONN:
                return 0;
                
            case TICK_PRICE:
                processTickPriceMsg();
                break;

            case TICK_SIZE:
                processTickSizeMsg();
                break;

            case POSITION:
                processPositionMsg();
                break;

            case POSITION_END:
                processPositionEndMsg();
                break;

            case ACCOUNT_SUMMARY:
                processAccountSummaryMsg();
                break;

            case ACCOUNT_SUMMARY_END:
                processAccountSummaryEndMsg();
                break;

            case TICK_OPTION_COMPUTATION:
                processTickOptionComputationMsg();
            	break;

            case TICK_GENERIC:
                processTickGenericMsg();
                break;

            case TICK_STRING:
                processTickStringMsg();
                break;

            case TICK_EFP:
                processTickEFPMsg();
                break;

            case ORDER_STATUS:
                processOrderStatusMsg();
                break;

            case ACCT_VALUE:
                processAcctValueMsg();
                break;

            case PORTFOLIO_VALUE:
                processPortfolioValueMsg();
                break;

            case ACCT_UPDATE_TIME:
                processAcctUpdateTimeMsg();
                break;

            case ERR_MSG:
                processErrMsgMsg();
                break;

            case OPEN_ORDER:
                processOpenOrderMsg();
                break;

            case NEXT_VALID_ID:
                processNextValidIdMsg();
                break;

            case SCANNER_DATA:
                processScannerDataMsg();
                break;

            case CONTRACT_DATA:
                processContractDataMsg();
                break;
           
            case BOND_CONTRACT_DATA:
                processBondContractDataMsg();
                break;
            
            case EXECUTION_DATA:
                processExecutionDataMsg();
                break;
            
            case MARKET_DEPTH:
                processMarketDepthMsg();
                break;
            
            case MARKET_DEPTH_L2:
                processMarketDepthL2Msg();
                break;
            
            case NEWS_BULLETINS:
                processNewsBulletinsMsg();
                break;
            
            case MANAGED_ACCTS:
                processManagedAcctsMsg();
                break;

            case RECEIVE_FA:
                processReceiveFaMsg();
                break;
            
            case HISTORICAL_DATA:
                processHistoricalDataMsg();
                break;
            
            case SCANNER_PARAMETERS:
                processScannerParametersMsg();
                break;

            case CURRENT_TIME:
                processCurrentTimeMsg();
                break;
            
            case REAL_TIME_BARS:
                processRealTimeBarsMsg();
                break;
            
            case FUNDAMENTAL_DATA:
                processFundamentalDataMsg();
                break;
            
            case CONTRACT_DATA_END:
                processContractDataEndMsg();
                break;
            
            case OPEN_ORDER_END:
                processOpenOrderEndMsg();
                break;
            
            case ACCT_DOWNLOAD_END:
                processAcctDownloadEndMsg();
                break;
            
            case EXECUTION_DATA_END:
                processExecutionDataEndMsg();
                break;
            
            case DELTA_NEUTRAL_VALIDATION:
                processDeltaNeutralValidationMsg();
                break;
            
            case TICK_SNAPSHOT_END:
                processTickSnapshotEndMsg();
                break;
            
            case MARKET_DATA_TYPE:
                processMarketDataTypeMsg();
                break;
            
            case COMMISSION_REPORT:
                processCommissionReportMsg();
                break;
            
            case VERIFY_MESSAGE_API:
                processVerifyMessageApiMsg();
                break;
            
            case VERIFY_COMPLETED:
                processVerifyCompletedMsg();
                break;
            
            case DISPLAY_GROUP_LIST:
                processDisplayGroupListMsg();
                break;
            
            case DISPLAY_GROUP_UPDATED:
                processDisplayGroupUpdatedMsg();
                break;
            
            case VERIFY_AND_AUTH_MESSAGE_API:
                processVerifyAndAuthMessageMsg();
                break;
            
            case VERIFY_AND_AUTH_COMPLETED:
                processVerifyAndAuthCompletedMsg();
                break;
            
            case POSITION_MULTI:
                processPositionMultiMsg();
                break;
            
            case POSITION_MULTI_END:
                processPositionMultiEndMsg();
                break;
            
            case ACCOUNT_UPDATE_MULTI:
                processAccountUpdateMultiMsg();
                break;
            
            case ACCOUNT_UPDATE_MULTI_END:
                processAccountUpdateMultiEndMsg();
                break;            
            
            case SECURITY_DEFINITION_OPTION_PARAMETER:
            	processSecurityDefinitionOptionalParameterMsg();
            	break;
            	
            case SECURITY_DEFINITION_OPTION_PARAMETER_END:
            	processSecurityDefinitionOptionalParameterEndMsg();
            	break;
            	
            case SOFT_DOLLAR_TIERS:
            	processSoftDollarTiersMsg();
            	break;

            case FAMILY_CODES:
                processFamilyCodesMsg();
                break;           	
            	
            case SMART_COMPONENTS:
            	processSmartComponentsMsg();
            	break;
            	
            case TICK_REQ_PARAMS:
            	processTickReqParamsMsg();
            	break;

            case SYMBOL_SAMPLES:
                processSymbolSamplesMsg();
                break;

            case MKT_DEPTH_EXCHANGES:
                processMktDepthExchangesMsg();
                break;
                
            case HEAD_TIMESTAMP:
            	processHeadTimestampMsg();
            	break;

            case TICK_NEWS:
                processTickNewsMsg();
                break;

            case NEWS_PROVIDERS:
                processNewsProvidersMsg();
                break;

            case NEWS_ARTICLE:
                processNewsArticleMsg();
                break;

            case HISTORICAL_NEWS:
                processHistoricalNewsMsg();
                break;

            case HISTORICAL_NEWS_END:
                processHistoricalNewsEndMsg();
                break;
                
            case HISTOGRAM_DATA:
            	processHistogramDataMsg();
            	break;
            	
            case HISTORICAL_DATA_UPDATE:
                processHistoricalDataUpdateMsg();
                break;

            case REROUTE_MKT_DATA_REQ:
                processRerouteMktDataReq();
                break;

            case REROUTE_MKT_DEPTH_REQ:
                processRerouteMktDepthReq();
                break;

            case MARKET_RULE:
                processMarketRuleMsg();
                break;
                
            case PNL:
            	processPnLMsg();
            	break;
            	
            case PNL_SINGLE:
            	processPnLSingleMsg();
            	break;
            	
            case HISTORICAL_TICKS:
                processHistoricalTicks();
                break;
                
            case HISTORICAL_TICKS_BID_ASK:
                processHistoricalTicksBidAsk();
                break;
                
            case HISTORICAL_TICKS_LAST:
                processHistoricalTicksLast();
                break;

            case TICK_BY_TICK:
                processTickByTickMsg();
                break;
                
            default: {
                m_EWrapper.error( EClientErrors.NO_VALID_ID, EClientErrors.UNKNOWN_ID.code(), EClientErrors.UNKNOWN_ID.msg());
                return 0;
            }
        }
        
        m_messageReader.close();
        return m_messageReader.msgLength();
    }

    private void processHistoricalTicksLast() throws IOException {
        int reqId = readInt(),
            tickCount = readInt();
                
        List<HistoricalTickLast> ticks = new ArrayList<>();
        
        for (int i = 0; i < tickCount; i++) {
            long time = readLong();
            int mask = readInt();
            double price = readDouble();
            long size = readLong();
            String exchange = readStr(),
                   specialConditions = readStr();

            ticks.add(new HistoricalTickLast(time, mask, price, size, exchange, specialConditions));
        }

        boolean done = readBoolean();

        m_EWrapper.historicalTicksLast(reqId, ticks, done);
    }

    private void processHistoricalTicksBidAsk() throws IOException {
        int reqId = readInt(),
            tickCount = readInt();
            
        List<HistoricalTickBidAsk> ticks = new ArrayList<>();
        
        for (int i = 0; i < tickCount; i++) {
            long time = readLong();
            int mask = readInt();
            double priceBid = readDouble(),
                   priceAsk = readDouble();
            long sizeBid = readLong(),
                 sizeAsk = readLong();

            ticks.add(new HistoricalTickBidAsk(time, mask, priceBid, priceAsk, sizeBid, sizeAsk));
        }

        boolean done = readBoolean();

        m_EWrapper.historicalTicksBidAsk(reqId, ticks, done);       
    }

    private void processHistoricalTicks() throws IOException {
        int reqId = readInt(),
            tickCount = readInt();
        
        List<HistoricalTick> ticks = new ArrayList<>();
        
        for (int i = 0; i < tickCount; i++) {
            long time = readLong();
            readInt();//for consistency
            double price = readDouble();
            long size = readLong();
            
            ticks.add(new HistoricalTick(time, price, size));
        }
        
        boolean done = readBoolean();

        m_EWrapper.historicalTicks(reqId, ticks, done);       
    }

    private void processMarketRuleMsg() throws IOException {
        int marketRuleId = readInt();

        PriceIncrement[] priceIncrements;
        int nPriceIncrements = readInt();
        if (nPriceIncrements > 0) {
            priceIncrements = new PriceIncrement[nPriceIncrements];
            for (int i = 0; i < nPriceIncrements; i++){
                priceIncrements[i] = new PriceIncrement(readDouble(), readDouble());
            }
        } else {
            priceIncrements = new PriceIncrement[0];
        }

        m_EWrapper.marketRule(marketRuleId, priceIncrements);
    }

    private void processRerouteMktDepthReq() throws IOException {
        int reqId = readInt();
        int conId = readInt();
        String exchange = readStr();

        m_EWrapper.rerouteMktDepthReq(reqId, conId, exchange);
    }
    
    private void processRerouteMktDataReq() throws IOException {
        int reqId = readInt();
        int conId = readInt();
        String exchange = readStr();

        m_EWrapper.rerouteMktDataReq(reqId, conId, exchange);
    }
    
    private void processHistoricalDataUpdateMsg() throws IOException {
        int reqId = readInt();
        int barCount = readInt();
        String date = readStr();
        double open = readDouble();
        double close = readDouble();
        double high = readDouble();
        double low = readDouble();
        double WAP = readDouble();
        long volume = readLong();

        m_EWrapper.historicalDataUpdate(reqId, new Bar(date, open, high, low, close, volume, barCount, WAP));
    }

    private void processPnLSingleMsg() throws IOException {
    	int reqId = readInt();
    	int pos = readInt();
    	double dailyPnL = readDouble();
    	double unrealizedPnL = Double.MAX_VALUE;
        double realizedPnL = Double.MAX_VALUE;
    	
        if (m_serverVersion >= EClient.MIN_SERVER_VER_UNREALIZED_PNL) {
            unrealizedPnL = readDouble();
        }

        if (m_serverVersion >= EClient.MIN_SERVER_VER_REALIZED_PNL) {
            realizedPnL = readDouble();
        }

    	double value = readDouble();
    	
        m_EWrapper.pnlSingle(reqId, pos, dailyPnL, unrealizedPnL, realizedPnL, value);
	}

	private void processPnLMsg() throws IOException {
		int reqId = readInt();
		double dailyPnL = readDouble();
		double unrealizedPnL = Double.MAX_VALUE;
		double realizedPnL = Double.MAX_VALUE;
		
		if (m_serverVersion >= EClient.MIN_SERVER_VER_UNREALIZED_PNL) {
		    unrealizedPnL = readDouble();
		}
		
		if (m_serverVersion >= EClient.MIN_SERVER_VER_REALIZED_PNL) {
		    realizedPnL = readDouble();
		}
		
		m_EWrapper.pnl(reqId, dailyPnL, unrealizedPnL, realizedPnL);
	}

    private void processHistogramDataMsg() throws IOException {
    	int reqId = readInt();
    	int n = readInt();
    	List<HistogramEntry> items = new ArrayList<>(n);
    	
    	for (int i = 0; i < n; i++) {
    		items.add(new HistogramEntry(readDouble(), readLong()));
    	}
    	
    	m_EWrapper.histogramData(reqId, items);
	}

	private void processHistoricalNewsEndMsg() throws IOException {
        int requestId = readInt();
        boolean hasMore = readBoolFromInt();

        m_EWrapper.historicalNewsEnd(requestId, hasMore);
    }

    private void processHistoricalNewsMsg() throws IOException {
        int requestId = readInt();
        String time = readStr();
        String providerCode = readStr();
        String articleId = readStr();
        String headline = readStr();

        m_EWrapper.historicalNews(requestId, time, providerCode, articleId, headline);
    }

    private void processNewsArticleMsg() throws IOException {
        int requestId = readInt();
        int articleType = readInt();
        String articleText = readStr();

        m_EWrapper.newsArticle(requestId, articleType, articleText);
    }

    private void processNewsProvidersMsg() throws IOException {
        NewsProvider[] newsProviders = new NewsProvider[0];
        int nNewsProviders = readInt();

        if (nNewsProviders > 0) {
            newsProviders = new NewsProvider[nNewsProviders];

            for (int i = 0; i < nNewsProviders; i++)
            {
                newsProviders[i] = new NewsProvider(readStr(), readStr());
            }
        }

        m_EWrapper.newsProviders(newsProviders);
    }

    private void processTickNewsMsg() throws IOException {
        int tickerId = readInt();
        long timeStamp = readLong();
        String providerCode = readStr();
        String articleId = readStr();
        String headline = readStr();
        String extraData = readStr();

        m_EWrapper.tickNews(tickerId, timeStamp, providerCode, articleId, headline, extraData);
    }

    private void processHeadTimestampMsg() throws IOException {
		int reqId = readInt();
		String headTimestamp = readStr();
		
		m_EWrapper.headTimestamp(reqId, headTimestamp);
	}

    private void processMktDepthExchangesMsg() throws IOException {
        DepthMktDataDescription[] depthMktDataDescriptions = new DepthMktDataDescription[0];
        int nDepthMktDataDescriptions = readInt();

        if (nDepthMktDataDescriptions > 0) {
            depthMktDataDescriptions = new DepthMktDataDescription[nDepthMktDataDescriptions];

            for (int i = 0; i < nDepthMktDataDescriptions; i++)
            {
                if (m_serverVersion >= EClient.MIN_SERVER_VER_SERVICE_DATA_TYPE) {
                    depthMktDataDescriptions[i] = new DepthMktDataDescription(readStr(), readStr(), readStr(), readStr(), readIntMax());
                } else {
                    depthMktDataDescriptions[i] = new DepthMktDataDescription(readStr(), readStr(), "", readBoolFromInt() ? "Deep2" : "Deep", Integer.MAX_VALUE);
                }
            }
        }

        m_EWrapper.mktDepthExchanges(depthMktDataDescriptions);
    }

    private void processSymbolSamplesMsg() throws IOException {
        int reqId = readInt();
        ContractDescription[] contractDescriptions = new ContractDescription[0];
        int nContractDescriptions = readInt();

        if (nContractDescriptions > 0){
            contractDescriptions = new ContractDescription[nContractDescriptions];

            for (int i = 0; i < nContractDescriptions; i++)
            {
                // read contract fields
                Contract contract = new Contract();
                contract.conid(readInt());
                contract.symbol(readStr());
                contract.secType(readStr());
                contract.primaryExch(readStr());
                contract.currency(readStr());

                // read derivative sec types list
                String[] derivativeSecTypes = new String[0];
                int nDerivativeSecTypes = readInt();

                if (nDerivativeSecTypes > 0){
                    derivativeSecTypes = new String[nDerivativeSecTypes];
                    for (int j = 0; j < nDerivativeSecTypes; j++)
                    {
                        derivativeSecTypes[j] = readStr();
                    }
                }

                ContractDescription contractDescription = new ContractDescription(contract, derivativeSecTypes);
                contractDescriptions[i] = contractDescription;
            }
        }

        m_EWrapper.symbolSamples(reqId, contractDescriptions);
    }

    private void processFamilyCodesMsg() throws IOException {
        FamilyCode[] familyCodes = new FamilyCode[0];
        int nFamilyCodes = readInt();

        if (nFamilyCodes > 0) {
            familyCodes = new FamilyCode[nFamilyCodes];

            for (int i = 0; i < nFamilyCodes; i++)
            {
                familyCodes[i] = new FamilyCode(readStr(), readStr());
            }
        }

        m_EWrapper.familyCodes(familyCodes);
    }

	private void processSoftDollarTiersMsg() throws IOException {
		int reqId = readInt();
		int nTiers = readInt();
		SoftDollarTier[] tiers = new SoftDollarTier[nTiers]; 
		
		for (int i = 0; i < nTiers; i++) {
			tiers[i] = new SoftDollarTier(readStr(), readStr(), readStr()); 
		}
		
		m_EWrapper.softDollarTiers(reqId, tiers);
	}

	private void processSecurityDefinitionOptionalParameterEndMsg() throws IOException {
		int reqId = readInt();
		
		m_EWrapper.securityDefinitionOptionalParameterEnd(reqId);
	}

	private void processSecurityDefinitionOptionalParameterMsg() throws IOException {
		int reqId = readInt();	
		String exchange = readStr();
		int underlyingConId = readInt();
		String tradingClass = readStr();
		String multiplier = readStr();
		int expirationsSize = readInt();
		Set<String> expirations = new HashSet<>();
		Set<Double> strikes = new HashSet<>();
		
		for (int i = 0; i < expirationsSize; i++) {
			expirations.add(readStr());
		}
		
		int strikesSize = readInt();
		
		for (int i = 0; i < strikesSize; i++) {
			strikes.add(readDouble());
		}
		
		m_EWrapper.securityDefinitionOptionalParameter(reqId, exchange, underlyingConId, tradingClass, multiplier, expirations, strikes);
	}

	private void processVerifyAndAuthCompletedMsg() throws IOException {
		/*int version =*/ readInt();
		String isSuccessfulStr = readStr();
		boolean isSuccessful = "true".equals(isSuccessfulStr);
		String errorText = readStr();

		m_EWrapper.verifyAndAuthCompleted(isSuccessful, errorText);
	}

	private void processVerifyAndAuthMessageMsg() throws IOException {
		/*int version =*/ readInt();
		String apiData = readStr();
		String xyzChallenge = readStr();

		m_EWrapper.verifyAndAuthMessageAPI(apiData, xyzChallenge);
	}

	private void processDisplayGroupUpdatedMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();
		String contractInfo = readStr();

		m_EWrapper.displayGroupUpdated(reqId, contractInfo);
	}

	private void processDisplayGroupListMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();
		String groups = readStr();

		m_EWrapper.displayGroupList(reqId, groups);
	}

	private void processVerifyCompletedMsg() throws IOException {
		/*int version =*/ readInt();
		String isSuccessfulStr = readStr();
		boolean isSuccessful = "true".equals(isSuccessfulStr);
		String errorText = readStr();

		m_EWrapper.verifyCompleted(isSuccessful, errorText);
	}

	private void processVerifyMessageApiMsg() throws IOException {
		/*int version =*/ readInt();
		String apiData = readStr();

		m_EWrapper.verifyMessageAPI(apiData);
	}

	private void processCommissionReportMsg() throws IOException {
		/*int version =*/ readInt();

		CommissionReport commissionReport = new CommissionReport();
		commissionReport.m_execId = readStr();
		commissionReport.m_commission = readDouble();
		commissionReport.m_currency = readStr();
		commissionReport.m_realizedPNL = readDouble();
		commissionReport.m_yield = readDouble();
		commissionReport.m_yieldRedemptionDate = readInt();

		m_EWrapper.commissionReport( commissionReport);
	}

	private void processMarketDataTypeMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();
		int marketDataType = readInt();

		m_EWrapper.marketDataType( reqId, marketDataType);
	}

	private void processTickSnapshotEndMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();

		m_EWrapper.tickSnapshotEnd( reqId);
	}

	private void processDeltaNeutralValidationMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();

		DeltaNeutralContract deltaNeutralContract = new DeltaNeutralContract(readInt(), readDouble(), readDouble());
		m_EWrapper.deltaNeutralValidation( reqId, deltaNeutralContract);
	}

	private void processExecutionDataEndMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();
		m_EWrapper.execDetailsEnd( reqId);
	}

	private void processAcctDownloadEndMsg() throws IOException {
		/*int version =*/ readInt();
		String accountName = readStr();
		m_EWrapper.accountDownloadEnd( accountName);
	}

	private void processOpenOrderEndMsg() throws IOException {
		/*int version =*/ readInt();
		m_EWrapper.openOrderEnd();
	}

	private void processContractDataEndMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();
		m_EWrapper.contractDetailsEnd(reqId);
	}

	private void processFundamentalDataMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();
		String data = readStr();
		m_EWrapper.fundamentalData(reqId, data);
	}

	private void processRealTimeBarsMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();
		long time = readLong();
		double open = readDouble();
		double high = readDouble();
		double low = readDouble();
		double close = readDouble();
		long volume = readLong();
		double wap = readDouble();
		int count = readInt();
		m_EWrapper.realtimeBar(reqId, time, open, high, low, close, volume, wap, count);
	}

	private void processCurrentTimeMsg() throws IOException {
		/*int version =*/ readInt();
		long time = readLong();
		m_EWrapper.currentTime(time);
	}

	private void processScannerParametersMsg() throws IOException {
		/*int version =*/ readInt();
		String xml = readStr();
		m_EWrapper.scannerParameters(xml);
	}

	private void processHistoricalDataMsg() throws IOException {
	    int version = Integer.MAX_VALUE;
	    
	    if (m_serverVersion < EClient.MIN_SERVER_VER_SYNT_REALTIME_BARS) {
	        version = readInt();
	    }
	    
	    int reqId = readInt();
	    String startDateStr = "";
	    String endDateStr = "";

	    if (version >= 2) {
	        startDateStr = readStr();
	        endDateStr = readStr();
	    }
	    int itemCount = readInt();
	    for (int ctr = 0; ctr < itemCount; ctr++) {
	        String date = readStr();
	        double open = readDouble();
	        double high = readDouble();
	        double low = readDouble();
	        double close = readDouble();
            long volume = m_serverVersion < EClient.MIN_SERVER_VER_SYNT_REALTIME_BARS ? readInt() : readLong();
	        double WAP = readDouble();
	        
	        if (m_serverVersion < EClient.MIN_SERVER_VER_SYNT_REALTIME_BARS) {	        
	            /*String hasGaps = */readStr();
	        }
	        
	        int barCount = -1;
	        
	        if (version >= 3) {
	            barCount = readInt();
	        }
	        
	        m_EWrapper.historicalData(reqId, new Bar(date, open, high, low, close, volume, barCount, WAP));
	    }
	    // send end of dataset marker
	    m_EWrapper.historicalDataEnd(reqId, startDateStr, endDateStr);
	}

	private void processReceiveFaMsg() throws IOException {
	    /*int version =*/ readInt();
	    int faDataType = readInt();
	    String xml = readStr();

	    m_EWrapper.receiveFA(faDataType, xml);
	}

	private void processManagedAcctsMsg() throws IOException {
		/*int version =*/ readInt();
		String accountsList = readStr();

		m_EWrapper.managedAccounts( accountsList);
	}

	private void processNewsBulletinsMsg() throws IOException {
		/*int version =*/ readInt();
		int newsMsgId = readInt();
		int newsMsgType = readInt();
		String newsMessage = readStr();
		String originatingExch = readStr();

		m_EWrapper.updateNewsBulletin( newsMsgId, newsMsgType, newsMessage, originatingExch);
	}

	private void processMarketDepthL2Msg() throws IOException {
		/*int version =*/ readInt();
		int id = readInt();

		int position = readInt();
		String marketMaker = readStr();
		int operation = readInt();
		int side = readInt();
		double price = readDouble();
		int size = readInt();

		m_EWrapper.updateMktDepthL2(id, position, marketMaker,
		                operation, side, price, size);
	}

	private void processMarketDepthMsg() throws IOException {
		/*int version =*/ readInt();
		int id = readInt();

		int position = readInt();
		int operation = readInt();
		int side = readInt();
		double price = readDouble();
		int size = readInt();

		m_EWrapper.updateMktDepth(id, position, operation,
		                side, price, size);
	}

	private void processExecutionDataMsg() throws IOException {
		int version = m_serverVersion;
		
		if (m_serverVersion < EClient.MIN_SERVER_VER_LAST_LIQUIDITY) {
		    version = readInt();
		}

		int reqId = -1;
		if (version >= 7) {
			reqId = readInt();
		}

		int orderId = readInt();

		// read contract fields
		Contract contract = new Contract();
		if (version >= 5) {
			contract.conid(readInt());
		}
		contract.symbol(readStr());
		contract.secType(readStr());
		contract.lastTradeDateOrContractMonth(readStr());
		contract.strike(readDouble());
		contract.right(readStr());
		if (version >= 9) {
		    contract.multiplier(readStr());
		}
		contract.exchange(readStr());
		contract.currency(readStr());
		contract.localSymbol(readStr());
		if (version >= 10) {
		    contract.tradingClass(readStr());
		}

		Execution exec = new Execution();
		exec.orderId(orderId);
		exec.execId(readStr());
		exec.time(readStr());
		exec.acctNumber(readStr());
		exec.exchange(readStr());
		exec.side(readStr());
		
		if (m_serverVersion >= EClient.MIN_SERVER_VER_FRACTIONAL_POSITIONS)			
			exec.shares(readDouble());
		else
			exec.shares(readInt());
				
		exec.price(readDouble());
		if ( version >= 2 ) {
		    exec.permId(readInt());
		}
		if ( version >= 3) {
		    exec.clientId(readInt());
		}
		if ( version >= 4) {
		    exec.liquidation(readInt());
		}
		if (version >= 6) {
			exec.cumQty(readDouble());
			exec.avgPrice(readDouble());
		}
		if (version >= 8) {
		    exec.orderRef(readStr());
		}
		if (version >= 9) {
		    exec.evRule(readStr());
		    exec.evMultiplier(readDouble());
		}
		if (m_serverVersion >= EClient.MIN_SERVER_VER_MODELS_SUPPORT) {
			exec.modelCode(readStr());
		}
		
        if (m_serverVersion >= EClient.MIN_SERVER_VER_LAST_LIQUIDITY) {
            exec.lastLiquidity(readInt());
        }


		m_EWrapper.execDetails( reqId, contract, exec);
	}

	private void processBondContractDataMsg() throws IOException {
		int version = readInt();

		int reqId = -1;
		if (version >= 3) {
			reqId = readInt();
		}

		ContractDetails contract = new ContractDetails();

		contract.contract().symbol(readStr());
		contract.contract().secType(readStr());
		contract.cusip(readStr());
		contract.coupon(readDouble());
		readLastTradeDate(contract, true);
		contract.issueDate(readStr());
		contract.ratings(readStr());
		contract.bondType(readStr());
		contract.couponType(readStr());
		contract.convertible(readBoolFromInt());
		contract.callable(readBoolFromInt());
		contract.putable(readBoolFromInt());
		contract.descAppend(readStr());
		contract.contract().exchange(readStr());
		contract.contract().currency(readStr());
		contract.marketName(readStr());
		contract.contract().tradingClass(readStr());
		contract.contract().conid(readInt());
		contract.minTick(readDouble());
		if (m_serverVersion >= EClient.MIN_SERVER_VER_MD_SIZE_MULTIPLIER) {
			contract.mdSizeMultiplier(readInt());
		}
		contract.orderTypes(readStr());
		contract.validExchanges(readStr());
		if (version >= 2) {
			contract.nextOptionDate(readStr());
			contract.nextOptionType(readStr());
			contract.nextOptionPartial(readBoolFromInt());
			contract.notes(readStr());
		}
		if( version >= 4) {
		   contract.longName(readStr());
		}
		if ( version >= 6) {
		    contract.evRule(readStr());
		    contract.evMultiplier(readDouble());
		}
		if (version >= 5) {
		    int secIdListCount = readInt();
		    if (secIdListCount  > 0) {
		        contract.secIdList(new ArrayList<>(secIdListCount));
		        for (int i = 0; i < secIdListCount; ++i) {
		            TagValue tagValue = new TagValue();
		            tagValue.m_tag = readStr();
		            tagValue.m_value = readStr();
		            contract.secIdList().add(tagValue);
		        }
		    }
		}
		if (m_serverVersion >= EClient.MIN_SERVER_VER_AGG_GROUP) {
			contract.aggGroup(readInt());
		}
		if (m_serverVersion >= EClient.MIN_SERVER_VER_MARKET_RULES) {
			contract.marketRuleIds(readStr());
		}
		
		m_EWrapper.bondContractDetails( reqId, contract);
	}

	private void processContractDataMsg() throws IOException {
		int version = readInt();

		int reqId = -1;
		if (version >= 3) {
			reqId = readInt();
		}

		ContractDetails contract = new ContractDetails();
		contract.contract().symbol(readStr());
		contract.contract().secType(readStr());
		readLastTradeDate(contract, false);
		contract.contract().strike(readDouble());
		contract.contract().right(readStr());
		contract.contract().exchange(readStr());
		contract.contract().currency(readStr());
		contract.contract().localSymbol(readStr());
		contract.marketName(readStr());
		contract.contract().tradingClass(readStr());
		contract.contract().conid(readInt());
		contract.minTick(readDouble());
		if (m_serverVersion >= EClient.MIN_SERVER_VER_MD_SIZE_MULTIPLIER) {
			contract.mdSizeMultiplier(readInt());
		}
		contract.contract().multiplier(readStr());
		contract.orderTypes(readStr());
		contract.validExchanges(readStr());
		if (version >= 2) {
		    contract.priceMagnifier(readInt());
		}
		if (version >= 4) {
			contract.underConid(readInt());
		}
		if( version >= 5) {
		   contract.longName(readStr());
		   contract.contract().primaryExch(readStr());
		}
		if( version >= 6) {
		    contract.contractMonth(readStr());
		    contract.industry(readStr());
		    contract.category(readStr());
		    contract.subcategory(readStr());
		    contract.timeZoneId(readStr());
		    contract.tradingHours(readStr());
		    contract.liquidHours(readStr());
		 }
		if (version >= 8) {
		    contract.evRule(readStr());
		    contract.evMultiplier(readDouble());
		}
		if (version >= 7) {
		    int secIdListCount = readInt();
		        if (secIdListCount  > 0) {
		            contract.secIdList(new ArrayList<>(secIdListCount));
		            for (int i = 0; i < secIdListCount; ++i) {
		                TagValue tagValue = new TagValue();
		                tagValue.m_tag = readStr();
		                tagValue.m_value = readStr();
		                contract.secIdList().add(tagValue);
		            }
		        }
		}
		if (m_serverVersion >= EClient.MIN_SERVER_VER_AGG_GROUP) {
			contract.aggGroup(readInt());
		}
		if (m_serverVersion >= EClient.MIN_SERVER_VER_UNDERLYING_INFO) {
			contract.underSymbol(readStr());
			contract.underSecType(readStr());
		}
		if (m_serverVersion >= EClient.MIN_SERVER_VER_MARKET_RULES) {
			contract.marketRuleIds(readStr());
		}
		if (m_serverVersion >= EClient.MIN_SERVER_VER_REAL_EXPIRATION_DATE) {
			contract.realExpirationDate(readStr());
		}

		m_EWrapper.contractDetails( reqId, contract);
	}

	private void processScannerDataMsg() throws IOException {
		int version = readInt();
		int tickerId = readInt();
		int numberOfElements = readInt();
		for (int ctr=0; ctr < numberOfElements; ctr++) {
		    int rank = readInt();
		    ContractDetails contract = new ContractDetails();
		    if (version >= 3) {
		    	contract.contract().conid(readInt());
		    }
		    contract.contract().symbol(readStr());
		    contract.contract().secType(readStr());
		    contract.contract().lastTradeDateOrContractMonth(readStr());
		    contract.contract().strike(readDouble());
		    contract.contract().right(readStr());
		    contract.contract().exchange(readStr());
		    contract.contract().currency(readStr());
		    contract.contract().localSymbol(readStr());
		    contract.marketName(readStr());
		    contract.contract().tradingClass(readStr());
		    String distance = readStr();
		    String benchmark = readStr();
		    String projection = readStr();
		    String legsStr = null;
		    if (version >= 2) {
		    	legsStr = readStr();
		    }
		    m_EWrapper.scannerData(tickerId, rank, contract, distance,
		        benchmark, projection, legsStr);
		}
		m_EWrapper.scannerDataEnd(tickerId);
	}

	private void processNextValidIdMsg() throws IOException {
		/*int version =*/ readInt();
		int orderId = readInt();
		m_EWrapper.nextValidId( orderId);
	}

	private void processOpenOrderMsg() throws IOException {
		// read version
		int version = readInt();

		// read order id
		Order order = new Order();
		order.orderId(readInt());

		// read contract fields
		Contract contract = new Contract();
		if (version >= 17) {
			contract.conid(readInt());
		}
		contract.symbol(readStr());
		contract.secType(readStr());
		contract.lastTradeDateOrContractMonth(readStr());
		contract.strike(readDouble());
		contract.right(readStr());
		if ( version >= 32) {
		   contract.multiplier(readStr());
		}
		contract.exchange(readStr());
		contract.currency(readStr());
		if ( version >= 2 ) {
		    contract.localSymbol(readStr());
		}
		if (version >= 32) {
		    contract.tradingClass(readStr());
		}

		// read order fields
		order.action(readStr());
		
		if (m_serverVersion >= EClient.MIN_SERVER_VER_FRACTIONAL_POSITIONS)		
			order.totalQuantity(readDouble());
		else
			order.totalQuantity(readInt());
		
		order.orderType(readStr());
		if (version < 29) {
		    order.lmtPrice(readDouble());
		}
		else {
		    order.lmtPrice(readDoubleMax());
		}
		if (version < 30) {
		    order.auxPrice(readDouble());
		}
		else {
		    order.auxPrice(readDoubleMax());
		}
		order.tif(readStr());
		order.ocaGroup(readStr());
		order.account(readStr());
		order.openClose(readStr());
		order.origin(readInt());
		order.orderRef(readStr());

		if(version >= 3) {
		    order.clientId(readInt());
		}

		if( version >= 4 ) {
		    order.permId(readInt());
		    if ( version < 18) {
		        // will never happen
		    	/* order.m_ignoreRth = */ readBoolFromInt();
		    }
		    else {
		    	order.outsideRth(readBoolFromInt());
		    }
		    order.hidden(readInt() == 1);
		    order.discretionaryAmt(readDouble());
		}

		if ( version >= 5 ) {
		    order.goodAfterTime(readStr());
		}

		if ( version >= 6 ) {
			// skip deprecated sharesAllocation field
		    readStr();
		}

		if ( version >= 7 ) {
		    order.faGroup(readStr());
		    order.faMethod(readStr());
		    order.faPercentage(readStr());
		    order.faProfile(readStr());
		}

		if ( m_serverVersion >= EClient.MIN_SERVER_VER_MODELS_SUPPORT) {
			order.modelCode(readStr());
		}

		if ( version >= 8 ) {
		    order.goodTillDate(readStr());
		}

		if ( version >= 9) {
		    order.rule80A(readStr());
		    order.percentOffset(readDoubleMax());
		    order.settlingFirm(readStr());
		    order.shortSaleSlot(readInt());
		    order.designatedLocation(readStr());
		    if ( m_serverVersion == 51){
		        readInt(); // exemptCode
		    }
		    else if ( version >= 23){
		    	order.exemptCode(readInt());
		    }
		    order.auctionStrategy(readInt());
		    order.startingPrice(readDoubleMax());
		    order.stockRefPrice(readDoubleMax());
		    order.delta(readDoubleMax());
		    order.stockRangeLower(readDoubleMax());
		    order.stockRangeUpper(readDoubleMax());
		    order.displaySize(readInt());
		    if ( version < 18) {
		        // will never happen
		    	/* order.m_rthOnly = */ readBoolFromInt();
		    }
		    order.blockOrder(readBoolFromInt());
		    order.sweepToFill(readBoolFromInt());
		    order.allOrNone(readBoolFromInt());
		    order.minQty(readIntMax());
		    order.ocaType(readInt());
		    order.eTradeOnly(readBoolFromInt());
		    order.firmQuoteOnly(readBoolFromInt());
		    order.nbboPriceCap(readDoubleMax());
		}

		if ( version >= 10) {
		    order.parentId(readInt());
		    order.triggerMethod(readInt());
		}

		if (version >= 11) {
		    order.volatility(readDoubleMax());
		    order.volatilityType(readInt());
		    if (version == 11) {
		    	int receivedInt = readInt();
		    	order.deltaNeutralOrderType( (receivedInt == 0) ? "NONE" : "MKT" );
		    } else { // version 12 and up
		    	order.deltaNeutralOrderType(readStr());
		    	order.deltaNeutralAuxPrice(readDoubleMax());

		        if (version >= 27 && !Util.StringIsEmpty(order.getDeltaNeutralOrderType())) {
		            order.deltaNeutralConId(readInt());
		            order.deltaNeutralSettlingFirm(readStr());
		            order.deltaNeutralClearingAccount(readStr());
		            order.deltaNeutralClearingIntent(readStr());
		        }

		        if (version >= 31 && !Util.StringIsEmpty(order.getDeltaNeutralOrderType())) {
		            order.deltaNeutralOpenClose(readStr());
		            order.deltaNeutralShortSale(readBoolFromInt());
		            order.deltaNeutralShortSaleSlot(readInt());
		            order.deltaNeutralDesignatedLocation(readStr());
		        }
		    }
		    order.continuousUpdate(readInt());
		    if (m_serverVersion == 26) {
		    	order.stockRangeLower(readDouble());
		    	order.stockRangeUpper(readDouble());
		    }
		    order.referencePriceType(readInt());
		}

		if (version >= 13) {
			order.trailStopPrice(readDoubleMax());
		}

		if (version >= 30) {
			order.trailingPercent(readDoubleMax());
		}

		if (version >= 14) {
			order.basisPoints(readDoubleMax());
			order.basisPointsType(readIntMax());
			contract.comboLegsDescrip(readStr());
		}

		if (version >= 29) {
			int comboLegsCount = readInt();
			if (comboLegsCount > 0) {
				contract.comboLegs(new ArrayList<>(comboLegsCount));
				for (int i = 0; i < comboLegsCount; ++i) {
					int conId = readInt();
					int ratio = readInt();
					String action = readStr();
					String exchange = readStr();
					int openClose = readInt();
					int shortSaleSlot = readInt();
					String designatedLocation = readStr();
					int exemptCode = readInt();

					ComboLeg comboLeg = new ComboLeg(conId, ratio, action, exchange, openClose,
							shortSaleSlot, designatedLocation, exemptCode);
					contract.comboLegs().add(comboLeg);
				}
			}

			int orderComboLegsCount = readInt();
			if (orderComboLegsCount > 0) {
				order.orderComboLegs(new ArrayList<>(orderComboLegsCount));
				for (int i = 0; i < orderComboLegsCount; ++i) {
					double price = readDoubleMax();

					OrderComboLeg orderComboLeg = new OrderComboLeg(price);
					order.orderComboLegs().add(orderComboLeg);
				}
			}
		}

		if (version >= 26) {
			int smartComboRoutingParamsCount = readInt();
			if (smartComboRoutingParamsCount > 0) {
				order.smartComboRoutingParams(new ArrayList<>(smartComboRoutingParamsCount));
				for (int i = 0; i < smartComboRoutingParamsCount; ++i) {
					TagValue tagValue = new TagValue();
					tagValue.m_tag = readStr();
					tagValue.m_value = readStr();
					order.smartComboRoutingParams().add(tagValue);
				}
			}
		}

		if (version >= 15) {
			if (version >= 20) {
				order.scaleInitLevelSize(readIntMax());
				order.scaleSubsLevelSize(readIntMax());
			}
			else {
				/* int notSuppScaleNumComponents = */ readIntMax();
				order.scaleInitLevelSize(readIntMax());
			}
			order.scalePriceIncrement(readDoubleMax());
		}

		if (version >= 28 && order.scalePriceIncrement() > 0.0 && order.scalePriceIncrement() != Double.MAX_VALUE) {
		    order.scalePriceAdjustValue(readDoubleMax());
		    order.scalePriceAdjustInterval(readIntMax());
		    order.scaleProfitOffset(readDoubleMax());
		    order.scaleAutoReset(readBoolFromInt());
		    order.scaleInitPosition(readIntMax());
		    order.scaleInitFillQty(readIntMax());
		    order.scaleRandomPercent(readBoolFromInt());
		}

		if (version >= 24) {
			order.hedgeType(readStr());
		    if (!Util.StringIsEmpty(order.getHedgeType())) {
				order.hedgeParam(readStr());
			}
		}

		if (version >= 25) {
			order.optOutSmartRouting(readBoolFromInt());
		}

		if (version >= 19) {
			order.clearingAccount(readStr());
			order.clearingIntent(readStr());
		}

		if (version >= 22) {
			order.notHeld(readBoolFromInt());
		}

		if (version >= 20) {
		    if (readBoolFromInt()) {
		        DeltaNeutralContract deltaNeutralContract = new DeltaNeutralContract();
		        deltaNeutralContract.conid(readInt());
		        deltaNeutralContract.delta(readDouble());
		        deltaNeutralContract.price(readDouble());
		        contract.deltaNeutralContract(deltaNeutralContract);
		    }
		}

		if (version >= 21) {
			order.algoStrategy(readStr());
		    if (!Util.StringIsEmpty(order.getAlgoStrategy())) {
				int algoParamsCount = readInt();
				if (algoParamsCount > 0) {
					for (int i = 0; i < algoParamsCount; ++i) {
						TagValue tagValue = new TagValue();
						tagValue.m_tag = readStr();
						tagValue.m_value = readStr();
						order.algoParams().add(tagValue);
					}
				}
			}
		}
		
		if (version >= 33) {
			order.solicited(readBoolFromInt());
		}

		OrderState orderState = new OrderState();

		if (version >= 16) {
			order.whatIf(readBoolFromInt());

			orderState.status(readStr());

			if (m_serverVersion >= EClient.MIN_SERVER_VER_WHAT_IF_EXT_FIELDS) {
				orderState.initMarginBefore(readStr());
				orderState.maintMarginBefore(readStr());
				orderState.equityWithLoanBefore(readStr());
				orderState.initMarginChange(readStr());
				orderState.maintMarginChange(readStr());
				orderState.equityWithLoanChange(readStr());
			}

			orderState.initMarginAfter(readStr());
			orderState.maintMarginAfter(readStr());
			orderState.equityWithLoanAfter(readStr());
			orderState.commission(readDoubleMax());
			orderState.minCommission(readDoubleMax());
			orderState.maxCommission(readDoubleMax());
			orderState.commissionCurrency(readStr());
			orderState.warningText(readStr());
		}
		
		if (version >= 34) {
			order.randomizeSize(readBoolFromInt());
			order.randomizePrice(readBoolFromInt());
		}
		
		if (m_serverVersion >= EClient.MIN_SERVER_VER_PEGGED_TO_BENCHMARK) {
			if (order.orderType() == OrderType.PEG_BENCH) {
				order.referenceContractId(readInt());
				order.isPeggedChangeAmountDecrease(readBoolFromInt());
				order.peggedChangeAmount(readDouble());
				order.referenceChangeAmount(readDouble());
				order.referenceExchangeId(readStr());
			}
			
			int nConditions = readInt();

			if (nConditions > 0) {			
				for (int i = 0; i < nConditions; i++) {
					OrderConditionType orderConditionType = OrderConditionType.fromInt(readInt());				
					OrderCondition condition = OrderCondition.create(orderConditionType);

					condition.readFrom(this);					
					order.conditions().add(condition);
				}
				
				order.conditionsIgnoreRth(readBoolFromInt());
				order.conditionsCancelOrder(readBoolFromInt());
			}
						
			order.adjustedOrderType(OrderType.get(readStr()));
			order.triggerPrice(readDoubleMax());
			order.trailStopPrice(readDoubleMax());
			order.lmtPriceOffset(readDoubleMax());
			order.adjustedStopPrice(readDoubleMax());
			order.adjustedStopLimitPrice(readDoubleMax());
			order.adjustedTrailingAmount(readDoubleMax());
			order.adjustableTrailingUnit(readInt());
		}
		
		if (m_serverVersion >= EClient.MIN_SERVER_VER_SOFT_DOLLAR_TIER) {
			order.softDollarTier(new SoftDollarTier(readStr(), readStr(), readStr()));
		}

		if (m_serverVersion >= EClient.MIN_SERVER_VER_CASH_QTY) {
			order.cashQty(readDoubleMax());
		}

		if (m_serverVersion >= EClient.MIN_SERVER_VER_AUTO_PRICE_FOR_HEDGE) {
			order.dontUseAutoPriceForHedge(readBoolFromInt());
		}
		
		m_EWrapper.openOrder( order.orderId(), contract, order, orderState);
	}

	private void processErrMsgMsg() throws IOException {
		int version = readInt();
		if(version < 2) {
		    String msg = readStr();
		    m_EWrapper.error( msg);
		} else {
		    int id = readInt();
		    int errorCode    = readInt();
		    String errorMsg = readStr();
		    m_EWrapper.error(id, errorCode, errorMsg);
		}
	}

	private void processAcctUpdateTimeMsg() throws IOException {
		/*int version =*/ readInt();
		String timeStamp = readStr();
		m_EWrapper.updateAccountTime(timeStamp);
	}

	private void processPortfolioValueMsg() throws IOException {
		int version = readInt();
		Contract contract = new Contract();
		if (version >= 6) {
			contract.conid(readInt());
		}
		contract.symbol(readStr());
		contract.secType(readStr());
		contract.lastTradeDateOrContractMonth(readStr());
		contract.strike(readDouble());
		contract.right(readStr());
		if (version >= 7) {
			contract.multiplier(readStr());
			contract.primaryExch(readStr());
		}
		contract.currency(readStr());
		if ( version >= 2 ) {
		    contract.localSymbol(readStr());
		}
		if (version >= 8) {
		    contract.tradingClass(readStr());
		}

		double position = m_serverVersion >= EClient.MIN_SERVER_VER_FRACTIONAL_POSITIONS ? readDouble() : readInt();
		double marketPrice = readDouble();
		double marketValue = readDouble();
		double  averageCost = 0.0;
		double  unrealizedPNL = 0.0;
		double  realizedPNL = 0.0;
		if (version >=3 ) {
		    averageCost = readDouble();
		    unrealizedPNL = readDouble();
		    realizedPNL = readDouble();
		}

		String accountName = null ;
		if( version >= 4) {
		    accountName = readStr();
		}

		if(version == 6 && m_serverVersion == 39) {
			contract.primaryExch(readStr());
		}

		m_EWrapper.updatePortfolio(contract, position, marketPrice, marketValue,
		                averageCost, unrealizedPNL, realizedPNL, accountName);
	}

	private void processAcctValueMsg() throws IOException {
		int version = readInt();
		String key = readStr();
		String val  = readStr();
		String cur = readStr();
		String accountName = null ;
		if( version >= 2) {
		    accountName = readStr();
		}
		m_EWrapper.updateAccountValue(key, val, cur, accountName);
	}

	private void processOrderStatusMsg() throws IOException {
		int version = m_serverVersion >= EClient.MIN_SERVER_VER_MARKET_CAP_PRICE ? Integer.MAX_VALUE : readInt();
		int id = readInt();
		String status = readStr();
		double filled = m_serverVersion >= EClient.MIN_SERVER_VER_FRACTIONAL_POSITIONS ? readDouble() : readInt();
		double remaining = m_serverVersion >= EClient.MIN_SERVER_VER_FRACTIONAL_POSITIONS ? readDouble() : readInt();
		double avgFillPrice = readDouble();

		int permId = 0;
		if( version >= 2) {
		    permId = readInt();
		}

		int parentId = 0;
		if( version >= 3) {
		    parentId = readInt();
		}

		double lastFillPrice = 0;
		if( version >= 4) {
		    lastFillPrice = readDouble();
		}

		int clientId = 0;
		if( version >= 5) {
		    clientId = readInt();
		}

		String whyHeld = null;
		if( version >= 6) {
			whyHeld = readStr();
		}
		
		double mktCapPrice = Double.MAX_VALUE;
		
		if (m_serverVersion >= EClient.MIN_SERVER_VER_MARKET_CAP_PRICE) {
		    mktCapPrice = readDouble();
		}

		m_EWrapper.orderStatus( id, status, filled, remaining, avgFillPrice,
		                permId, parentId, lastFillPrice, clientId, whyHeld, mktCapPrice);
	}

	private void processTickEFPMsg() throws IOException {
		/*int version =*/ readInt();
		int tickerId = readInt();
		int tickType = readInt();
		double basisPoints = readDouble();
		String formattedBasisPoints = readStr();
		double impliedFuturesPrice = readDouble();
		int holdDays = readInt();
		String futureLastTradeDate = readStr();
		double dividendImpact = readDouble();
		double dividendsToLastTradeDate = readDouble();
		m_EWrapper.tickEFP( tickerId, tickType, basisPoints, formattedBasisPoints,
							impliedFuturesPrice, holdDays, futureLastTradeDate, dividendImpact, dividendsToLastTradeDate);
	}

	private void processTickStringMsg() throws IOException {
		/*int version =*/ readInt();
		int tickerId = readInt();
		int tickType = readInt();
		String value = readStr();

		m_EWrapper.tickString( tickerId, tickType, value);
	}

	private void processTickGenericMsg() throws IOException {
		/*int version =*/ readInt();
		int tickerId = readInt();
		int tickType = readInt();
		double value = readDouble();

		m_EWrapper.tickGeneric( tickerId, tickType, value);
	}

	private void processTickOptionComputationMsg() throws IOException {
		int version = readInt();
		int tickerId = readInt();
		int tickType = readInt();
		double impliedVol = readDouble();
		if (Double.compare(impliedVol, -1) == 0) { // -1 is the "not yet computed" indicator
			impliedVol = Double.MAX_VALUE;
		}
		
		double delta = readDouble();
		if (Double.compare(delta, -2) == 0) { // -2 is the "not yet computed" indicator
			delta = Double.MAX_VALUE;
		}
		double optPrice = Double.MAX_VALUE;
		double pvDividend = Double.MAX_VALUE;
		double gamma = Double.MAX_VALUE;
		double vega = Double.MAX_VALUE;
		double theta = Double.MAX_VALUE;
		double undPrice = Double.MAX_VALUE;
		if (version >= 6 || tickType == TickType.MODEL_OPTION.index()
				|| tickType == TickType.DELAYED_MODEL_OPTION.index()) { // introduced in version == 5
			optPrice = readDouble();
			if (Double.compare(optPrice, -1) == 0) { // -1 is the "not yet computed" indicator
				optPrice = Double.MAX_VALUE;
			}
			pvDividend = readDouble();
			if (Double.compare(pvDividend, -1) == 0) { // -1 is the "not yet computed" indicator
				pvDividend = Double.MAX_VALUE;
			}
		}
		if (version >= 6) {
			gamma = readDouble();
			if (Double.compare(gamma, -2) == 0) { // -2 is the "not yet computed" indicator
				gamma = Double.MAX_VALUE;
			}
			vega = readDouble();
			if (Double.compare(vega,  -2) == 0) { // -2 is the "not yet computed" indicator
				vega = Double.MAX_VALUE;
			}
			theta = readDouble();
			if (Double.compare(theta, -2) == 0) { // -2 is the "not yet computed" indicator
				theta = Double.MAX_VALUE;
			}
			undPrice = readDouble();
			if (Double.compare(undPrice, -1) == 0) { // -1 is the "not yet computed" indicator
				undPrice = Double.MAX_VALUE;
			}
		}

		m_EWrapper.tickOptionComputation( tickerId, tickType, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice);
	}

	private void processAccountSummaryEndMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();
		m_EWrapper.accountSummaryEnd(reqId);
	}

	private void processAccountSummaryMsg() throws IOException {
		/*int version =*/ readInt();
		int reqId = readInt();
		String account = readStr();
		String tag = readStr();
		String value = readStr();
		String currency = readStr();
		m_EWrapper.accountSummary(reqId, account, tag, value, currency);
	}

	private void processPositionEndMsg() throws IOException {
		/*int version =*/ readInt();
		m_EWrapper.positionEnd();
	}

	private void processPositionMsg() throws IOException {
		int version = readInt();
		String account = readStr();

		Contract contract = new Contract();
		contract.conid(readInt());
		contract.symbol(readStr());
		contract.secType(readStr());
		contract.lastTradeDateOrContractMonth(readStr());
		contract.strike(readDouble());
		contract.right(readStr());
		contract.multiplier(readStr());
		contract.exchange(readStr());
		contract.currency(readStr());
		contract.localSymbol(readStr());
		if (version >= 2) {
			contract.tradingClass(readStr());
		}

		double pos = m_serverVersion >= EClient.MIN_SERVER_VER_FRACTIONAL_POSITIONS ? readDouble() : readInt();
		double avgCost = 0;
		if (version >= 3) {
			avgCost = readDouble();
		}

		m_EWrapper.position( account, contract, pos, avgCost);
	}

	private void processTickSizeMsg() throws IOException {
		/*int version =*/ readInt();
		int tickerId = readInt();
		int tickType = readInt();
		int size = readInt();

		m_EWrapper.tickSize( tickerId, tickType, size);
	}

	private void processTickPriceMsg() throws IOException {
		int version = readInt();
		int tickerId = readInt();
		int tickType = readInt();
		double price = readDouble();
		int size = 0;
		TickAttr attribs = new TickAttr();
		
		if( version >= 2) {
		    size = readInt();
		}
		
		if (version >= 3) {		
			int attrMask = readInt();			

			attribs.canAutoExecute(attrMask == 1);
			
			if (m_serverVersion >= EClient.MIN_SERVER_VER_PAST_LIMIT) {
				BitMask mask = new BitMask(attrMask);
				
				attribs.canAutoExecute(mask.get(0));
				attribs.pastLimit(mask.get(1));
				if (m_serverVersion >= EClient.MIN_SERVER_VER_PRE_OPEN_BID_ASK) {
					attribs.preOpen(mask.get(2));
				}
			}
		}

		
		m_EWrapper.tickPrice( tickerId, tickType, price, attribs);

		if( version >= 2) {
		    final int sizeTickType;
		    switch (tickType) {
		        case 1: // BID
		            sizeTickType = 0 ; // BID_SIZE
		            break ;
		        case 2: // ASK
		            sizeTickType = 3 ; // ASK_SIZE
		            break ;
		        case 4: // LAST
		            sizeTickType = 5 ; // LAST_SIZE
		            break ;
		        case 66: // DELAYED_BID
		            sizeTickType = 69 ; // DELAYED_BID_SIZE
		            break ;
		        case 67: // DELAYED_ASK
		            sizeTickType = 70 ; // DELAYED_ASK_SIZE
		            break ;
		        case 68: // DELAYED_LAST
		            sizeTickType = 71 ; // DELAYED_LAST_SIZE
		            break ;
                default:
                    sizeTickType = -1; // not a tick
		    }
		    if (sizeTickType != -1) {
		        m_EWrapper.tickSize( tickerId, sizeTickType, size);
		    }
		}
	}
    
    private void processPositionMultiMsg() throws IOException {
        /*int version =*/ readInt();
        int reqId = readInt();
        String account = readStr();

        Contract contract = new Contract();
        contract.conid(readInt());
        contract.symbol(readStr());
        contract.secType(readStr());
        contract.lastTradeDateOrContractMonth(readStr());
        contract.strike(readDouble());
        contract.right(readStr());
        contract.multiplier(readStr());
        contract.exchange(readStr());
        contract.currency(readStr());
        contract.localSymbol(readStr());
        contract.tradingClass(readStr());
        double pos = readDouble();
        double avgCost = readDouble();
        String modelCode = readStr();

        m_EWrapper.positionMulti( reqId, account, modelCode, contract, pos, avgCost);
    }

    private void processPositionMultiEndMsg() throws IOException {
        /*int version =*/ readInt();
        int reqId = readInt();

        m_EWrapper.positionMultiEnd( reqId);
    }

    private void processAccountUpdateMultiMsg() throws IOException {
        /*int version =*/ readInt();
        int reqId = readInt();
        String account = readStr();
        String modelCode = readStr();
        String key = readStr();
        String value = readStr();
        String currency = readStr();

        m_EWrapper.accountUpdateMulti( reqId, account, modelCode, key, value, currency);
    }

    private void processAccountUpdateMultiEndMsg() throws IOException {
        /*int version =*/ readInt();
        int reqId = readInt();

        m_EWrapper.accountUpdateMultiEnd( reqId);
    }  
    
    private void processSmartComponentsMsg() throws IOException {
    	int reqId = readInt();
    	int n = readInt();    	
    	Map<Integer, Entry<String, Character>> theMap = new HashMap<>();
    	
    	for (int i = 0; i < n; i++) {
    		int bitNumber = readInt();
    		String exchange = readStr();
    		char exchangeLetter = readChar();
    		
    		theMap.put(bitNumber, new SimpleEntry<>(exchange, exchangeLetter));
    }
    
    	m_EWrapper.smartComponents(reqId, theMap);
    }
    
    private void processTickReqParamsMsg() throws IOException {
    	int tickerId = readInt();
    	double minTick = readDouble();
    	String bboExchange = readStr();
    	int snapshotPermissions = readInt();
    	
    	m_EWrapper.tickReqParams(tickerId, minTick, bboExchange, snapshotPermissions);
    }
    
    private void processTickByTickMsg() throws IOException {
        int reqId = readInt();
        int tickType = readInt();
        long time = readLong();

        BitMask mask;
        TickAttr attribs;
        switch(tickType){
            case 0: // None
                break;
            case 1: // Last
            case 2: // AllLast
                double price = readDouble();
                int size = readInt();
                mask = new BitMask(readInt());
                attribs = new TickAttr();
                attribs.pastLimit(mask.get(0));
                attribs.unreported(mask.get(1));
                String exchange = readStr();
                String specialConditions = readStr();
                m_EWrapper.tickByTickAllLast(reqId, tickType, time, price, size, attribs, exchange, specialConditions);
                break;
            case 3: // BidAsk
                double bidPrice = readDouble();
                double askPrice = readDouble();
                int bidSize = readInt();
                int askSize = readInt();
                mask = new BitMask(readInt());
                attribs = new TickAttr();
                attribs.bidPastLow(mask.get(0));
                attribs.askPastHigh(mask.get(1));
                m_EWrapper.tickByTickBidAsk(reqId, time, bidPrice, askPrice, bidSize, askSize, attribs);
                break;
            case 4: // MidPoint
                double midPoint = readDouble();
                m_EWrapper.tickByTickMidPoint(reqId, time, midPoint);
                break;
        }
    }

    private void readLastTradeDate(ContractDetails contract, boolean isBond) throws IOException {
        String lastTradeDateOrContractMonth = readStr();
        if (lastTradeDateOrContractMonth != null) {
            String[] splitted = lastTradeDateOrContractMonth.split("\\s+");
            if (splitted.length > 0) {
                if (isBond) {
                    contract.maturity(splitted[0]);
                } else {
                    contract.contract().lastTradeDateOrContractMonth(splitted[0]);
                }
            }
            if (splitted.length > 1) {
                contract.lastTradeTime(splitted[1]);
            }
            if (isBond && splitted.length > 2) {
                contract.timeZoneId(splitted[2]);
            }
        }
    }

    private String readStr() throws IOException {
    	return m_messageReader.readStr();
    }

    private boolean readBoolFromInt() throws IOException {
        String str = readStr();
        return str != null && (Integer.parseInt(str) != 0);
    }

    public int readInt() throws IOException {
        String str = readStr();
        return str == null ? 0 : Integer.parseInt( str);
    }

    private int readIntMax() throws IOException {
        String str = readStr();
        return (str == null || str.length() == 0) ? Integer.MAX_VALUE
        	                                      : Integer.parseInt( str);
    }

    public long readLong() throws IOException {
        String str = readStr();
        return str == null ? 0L : Long.parseLong(str);
    }

    public double readDouble() throws IOException {
        String str = readStr();
        return str == null ? 0 : Double.parseDouble( str);
    }

    private double readDoubleMax() throws IOException {
        String str = readStr();
        return (str == null || str.length() == 0) ? Double.MAX_VALUE
        	                                      : Double.parseDouble( str);
    }

    /** Message reader interface */
    private interface IMessageReader extends Closeable {
    	String readStr() throws IOException;
    	int msgLength();
    }

    private static class PreV100MessageReader implements IMessageReader {
    	private final InputStream m_din;
    	private int m_msgLength = 0;
    	
    	PreV100MessageReader( InputStream din ) {
    		m_din = din;
    	}
    	
    	@Override
        public int msgLength() {
    		return m_msgLength;
    	}
    	
    	@Override public String readStr() throws IOException {
    		 StringBuilder sb = new StringBuilder();
    		    		 
 	         for(; true; m_msgLength++) {
 	            int c = m_din.read();
 	            if( c <= 0) {
 	            	if ( c < 0 ) {
 	            		throw new EOFException();
 	            	}
 	            	
 	            	m_msgLength++;
 	                break;
 	            }
 	            sb.append( (char)c);
 	        }
 	
 	        String str = sb.toString();
 	        return str.length() == 0 ? null : str;    
 	    }
    	
    	@Override public void close() {
    	    /* noop in pre-v100 */
    	}
    }

	@Override
	public int skipBytes(int arg0) throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public int readUnsignedShort() throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public int readUnsignedByte() throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public String readUTF() throws IOException { return readStr(); }
	
	@Override
	public short readShort() throws IOException { throw new UnsupportedOperationException(); }
		
	@Override
	public String readLine() throws IOException { return readStr(); }
		
	@Override
	public void readFully(byte[] arg0, int arg1, int arg2) throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public void readFully(byte[] arg0) throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public float readFloat() throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public char readChar() throws IOException { return readStr().charAt(0); }
	
	@Override
	public byte readByte() throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public boolean readBoolean() throws IOException { return readBoolFromInt(); }
	
	@Override
	public long skip(long arg0) throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public Object readObject() throws ClassNotFoundException, IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public int read(byte[] arg0, int arg1, int arg2) throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public int read(byte[] arg0) throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public int read() throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public void close() throws IOException { m_messageReader.close(); }
	
	@Override
	public int available() throws IOException { throw new UnsupportedOperationException(); }

}
