/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;


public enum TickType {
    BID_SIZE( 0, "bidSize" ),
    BID( 1, "bidPrice" ),
    ASK( 2, "askPrice" ),
    ASK_SIZE( 3, "askSize" ),
    LAST( 4, "lastPrice" ),
    LAST_SIZE( 5, "lastSize" ),
    HIGH( 6, "high" ),
    LOW( 7, "low" ),
    VOLUME( 8, "volume" ),
    CLOSE( 9, "close" ),
    BID_OPTION( 10, "bidOptComp" ),
    ASK_OPTION( 11, "askOptComp" ),
    LAST_OPTION( 12, "lastOptComp" ),
    MODEL_OPTION( 13, "modelOptComp" ),
    OPEN( 14, "open" ),
    LOW_13_WEEK( 15, "13WeekLow" ),
    HIGH_13_WEEK( 16, "13WeekHigh" ),
    LOW_26_WEEK( 17, "26WeekLow" ),
    HIGH_26_WEEK( 18, "26WeekHigh" ),
    LOW_52_WEEK( 19, "52WeekLow" ),
    HIGH_52_WEEK( 20, "52WeekHigh" ),
    AVG_VOLUME( 21, "AvgVolume" ),
    OPEN_INTEREST( 22, "OpenInterest" ),
    OPTION_HISTORICAL_VOL( 23, "OptionHistoricalVolatility" ),
    OPTION_IMPLIED_VOL( 24, "OptionImpliedVolatility" ),
    OPTION_BID_EXCH( 25, "OptionBidExchStr" ),
    OPTION_ASK_EXCH( 26, "OptionAskExchStr" ),
    OPTION_CALL_OPEN_INTEREST( 27, "OptionCallOpenInterest" ),
    OPTION_PUT_OPEN_INTEREST( 28, "OptionPutOpenInterest" ),
    OPTION_CALL_VOLUME( 29, "OptionCallVolume" ),
    OPTION_PUT_VOLUME( 30, "OptionPutVolume" ),
    INDEX_FUTURE_PREMIUM( 31, "IndexFuturePremium" ),
    BID_EXCH( 32, "bidExch" ), // string
    ASK_EXCH( 33, "askExch" ), // string   
    AUCTION_VOLUME( 34, "auctionVolume" ),
    AUCTION_PRICE( 35, "auctionPrice" ),
    AUCTION_IMBALANCE( 36, "auctionImbalance" ),
    MARK_PRICE( 37, "markPrice" ),
    BID_EFP_COMPUTATION( 38, "bidEFP" ),
    ASK_EFP_COMPUTATION( 39, "askEFP" ),
    LAST_EFP_COMPUTATION( 40, "lastEFP" ),
    OPEN_EFP_COMPUTATION( 41, "openEFP" ),
    HIGH_EFP_COMPUTATION( 42, "highEFP" ),
    LOW_EFP_COMPUTATION( 43, "lowEFP" ),
    CLOSE_EFP_COMPUTATION( 44, "closeEFP" ),
    LAST_TIMESTAMP( 45, "lastTimestamp" ), // string
    SHORTABLE( 46, "shortable" ),
    FUNDAMENTAL_RATIOS( 47, "fundamentals" ), // string
    RT_VOLUME( 48, "RTVolume" ), // string
    HALTED( 49, "halted" ),
    BID_YIELD( 50, "bidYield" ),
    ASK_YIELD( 51, "askYield" ),
    LAST_YIELD( 52, "lastYield" ),
    CUST_OPTION_COMPUTATION( 53, "custOptComp" ),
    TRADE_COUNT( 54, "trades" ),
    TRADE_RATE( 55, "trades/min" ),
    VOLUME_RATE( 56, "volume/min" ),
    LAST_RTH_TRADE( 57, "lastRTHTrade" ),
    RT_HISTORICAL_VOL( 58, "RTHistoricalVol" ),
    IB_DIVIDENDS( 59, "IBDividends" ),
    BOND_FACTOR_MULTIPLIER( 60, "bondFactorMultiplier" ),
    REGULATORY_IMBALANCE( 61, "regulatoryImbalance" ),
    NEWS_TICK( 62, "newsTick" ),
    SHORT_TERM_VOLUME_3_MIN( 63, "shortTermVolume3Min"),
    SHORT_TERM_VOLUME_5_MIN( 64, "shortTermVolume5Min"),
    SHORT_TERM_VOLUME_10_MIN( 65, "shortTermVolume10Min"),
    DELAYED_BID( 66, "delayedBid"),
    DELAYED_ASK( 67, "delayedAsk"),
    DELAYED_LAST( 68, "delayedLast"),
    DELAYED_BID_SIZE( 69, "delayedBidSize"),
    DELAYED_ASK_SIZE( 70, "delayedAskSize"),
    DELAYED_LAST_SIZE( 71, "delayedLastSize"),
    DELAYED_HIGH( 72, "delayedHigh"),
    DELAYED_LOW( 73, "delayedLow"),
    DELAYED_VOLUME( 74, "delayedVolume"),
    DELAYED_CLOSE( 75, "delayedClose"),
    DELAYED_OPEN( 76, "delayedOpen"),
    RT_TRD_VOLUME(77, "rtTrdVolume"),
    CREDITMAN_MARK_PRICE(78, "creditmanMarkPrice"),
    CREDITMAN_SLOW_MARK_PRICE(79, "creditmanSlowMarkPrice"),
    DELAYED_BID_OPTION( 80, "delayedBidOptComp" ),
    DELAYED_ASK_OPTION( 81, "delayedAskOptComp" ),
    DELAYED_LAST_OPTION( 82, "delayedLastOptComp" ),
    DELAYED_MODEL_OPTION( 83, "delayedModelOptComp" ),
    LAST_EXCH(84, "lastExchange"),
    LAST_REG_TIME(85, "lastRegTime"),
    FUTURES_OPEN_INTEREST(86, "futuresOpenInterest"),
    AVG_OPT_VOLUME(87, "avgOptVolume"),
    DELAYED_LAST_TIMESTAMP(88, "delayedLastTimestamp"),

    UNKNOWN( Integer.MAX_VALUE , "unknown" );

    private int m_ndx;
    private String m_field;
    
    // Get
    public int index()    { return m_ndx; }
    public String field() { return m_field; }

    TickType(int ndx, String field) {
        m_ndx = ndx;
        m_field = field;
    }
    
    public static TickType get(int ndx) {
        for( TickType tt : values() ) {
            if( tt.m_ndx == ndx) {
                return tt;
            }
        }
        return UNKNOWN;
    }

    public static String getField(int tickType) {
        return get(tickType).field();
    }
}
