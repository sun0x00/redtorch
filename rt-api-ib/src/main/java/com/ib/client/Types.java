/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.util.Arrays;

import static com.ib.client.Types.AlgoParam.allowPastEndTime;
import static com.ib.client.Types.AlgoParam.catchUp;
import static com.ib.client.Types.AlgoParam.componentSize;
import static com.ib.client.Types.AlgoParam.displaySize;
import static com.ib.client.Types.AlgoParam.endTime;
import static com.ib.client.Types.AlgoParam.forceCompletion;
import static com.ib.client.Types.AlgoParam.getDone;
import static com.ib.client.Types.AlgoParam.giveUp;
import static com.ib.client.Types.AlgoParam.maxPctVol;
import static com.ib.client.Types.AlgoParam.noTakeLiq;
import static com.ib.client.Types.AlgoParam.noTradeAhead;
import static com.ib.client.Types.AlgoParam.pctVol;
import static com.ib.client.Types.AlgoParam.randomizeSize55;
import static com.ib.client.Types.AlgoParam.randomizeTime20;
import static com.ib.client.Types.AlgoParam.riskAversion;
import static com.ib.client.Types.AlgoParam.startTime;
import static com.ib.client.Types.AlgoParam.strategyType;
import static com.ib.client.Types.AlgoParam.timeBetweenOrders;
import static com.ib.client.Types.AlgoParam.useOddLots;
import static com.ib.client.Types.AlgoParam.waitForFill;

public class Types {
	public enum TickByTickType {
		None, Last, AllLast, BidAsk, MidPoint,
	}
	
	public enum ComboParam {
		NonGuaranteed, PriceCondConid, CondPriceMax, CondPriceMin, ChangeToMktTime1, ChangeToMktTime2, DiscretionaryPct, DontLeginNext, LeginPrio, MaxSegSize,
	}

	public enum AlgoParam {
		startTime, endTime, allowPastEndTime, maxPctVol, pctVol, strategyType, noTakeLiq, riskAversion, forceCompletion, displaySize, getDone, noTradeAhead, useOddLots,
		componentSize, timeBetweenOrders, randomizeTime20, randomizeSize55, giveUp, catchUp, waitForFill	
	}

	public enum AlgoStrategy implements IApiEnum {
		None(),
		Vwap( startTime, endTime, maxPctVol, noTakeLiq, getDone, noTradeAhead, useOddLots),
		Twap( startTime, endTime, allowPastEndTime, strategyType),
		ArrivalPx( startTime, endTime, allowPastEndTime, maxPctVol, riskAversion, forceCompletion),
		DarkIce( startTime, endTime, allowPastEndTime, displaySize),
		PctVol( startTime, endTime, pctVol, noTakeLiq),
		AD( startTime, endTime, componentSize, timeBetweenOrders, randomizeTime20, randomizeSize55, giveUp, catchUp, waitForFill);

		private AlgoParam[] m_params;

		public AlgoParam[] params() {
			return Arrays.copyOf(m_params, m_params.length);
		}

		AlgoStrategy( AlgoParam... params) {
			m_params = Arrays.copyOf(params, params.length);
		}

		public static AlgoStrategy get(String apiString) {
			return getValueOf(apiString, values(), None);
		}

		@Override public String getApiString() {
			return this == None ? "" : super.toString();
		}
	}

	public enum HedgeType implements IApiEnum {
		None, Delta, Beta, Fx, Pair;

		public static HedgeType get(String apiString) {
            return getValueOf(apiString, values(), None);
		}

		@Override public String getApiString() {
			return this == None ? "" : String.valueOf( super.toString().charAt( 0) );
		}
	}

	public enum Right implements IApiEnum {
		None, Put, Call;

		public static Right get( String apiString) {
			if (apiString != null && apiString.length() > 0) {
				switch( apiString.charAt( 0) ) {
					case 'P' : return Put;
					case 'C' : return Call;
				}
			}
			return None;
		}

		@Override public String getApiString() {
			return this == None ? "" : String.valueOf( toString().charAt( 0) );
		}
	}

	public enum VolatilityType implements IApiEnum {
		None, Daily, Annual;

		public static VolatilityType get( int ordinal) {
			return ordinal == Integer.MAX_VALUE ? None : getEnum( ordinal, values() );
		}

		@Override public String getApiString() {
			return String.valueOf(ordinal());
		}
	}

	public enum ReferencePriceType implements IApiEnum {
		None, Midpoint, BidOrAsk;

		public static ReferencePriceType get( int ordinal) {
			return getEnum( ordinal, values() );
		}

		@Override public String getApiString() {
			return String.valueOf(ordinal());
		}
	}

	public enum TriggerMethod implements IApiEnum {
		Default( 0), DoubleBidAsk( 1), Last( 2), DoubleLast( 3), BidAsk( 4), LastOrBidAsk( 7), Midpoint( 8);

		int m_val;

		public int val() { return m_val; }

		TriggerMethod( int val) {
			m_val = val;
		}

		public static TriggerMethod get( int val) {
			for (TriggerMethod m : values() ) {
				if (m.m_val == val) {
					return m;
				}
			}
			return null;
		}

		@Override public String getApiString() {
			return String.valueOf(m_val);
		}
	}

	public enum Action implements IApiEnum {
		BUY, SELL, SSHORT;

        public static Action get(String apiString) {
            return getValueOf(apiString, values(), null);
        }

        @Override public String getApiString() {
			return toString();
		}
	}

	public enum Rule80A implements IApiEnum {
		None(""), Individual("I"), Agency("A"), AgentOtherMember("W"), IndividualPTIA("J"), AgencyPTIA("U"), AgentOtherMemberPTIA("M"), IndividualPT("K"), AgencyPT("Y"), AgentOtherMemberPT("N");

		private String m_apiString;

		Rule80A( String apiString) {
			m_apiString = apiString;
		}

		public static Rule80A get( String apiString) {
            return getValueOf(apiString, values(), None);
		}

		@Override
        public String getApiString() {
			return m_apiString;
		}
	}

	public enum OcaType implements IApiEnum {
		None, CancelWithBlocking, ReduceWithBlocking, ReduceWithoutBlocking;

		public static OcaType get( int ordinal) {
			return getEnum( ordinal, values() );
		}

		@Override public String getApiString() {
			return String.valueOf(ordinal());
		}
	}

	public enum TimeInForce implements IApiEnum {
		DAY, GTC, OPG, IOC, GTD, GTT, AUC, FOK, GTX, DTC;

        public static TimeInForce get(String apiString) {
            return getValueOf(apiString, values(), null);
        }

		@Override public String getApiString() {
			return toString();
		}
	}

	public enum ExerciseType {
		None, Exercise, Lapse,
	}

	public enum FundamentalType {
		ReportSnapshot("Company overview"),
		ReportsFinSummary("Financial summary"),
		ReportRatios("Financial ratios"),
		ReportsFinStatements("Financial statements"),
		RESC("Analyst estimates"),
		CalendarReport("Company calendar");

		private final String description;

		FundamentalType(final String description) {
			this.description = description;
		}

		public String getApiString() {
			return super.toString();
		}

		@Override public String toString() {
			return description;
		}
	}

	public enum WhatToShow {
		TRADES, MIDPOINT, BID, ASK, // << only these are valid for real-time bars
        BID_ASK, HISTORICAL_VOLATILITY, OPTION_IMPLIED_VOLATILITY, YIELD_ASK, YIELD_BID, YIELD_BID_ASK, YIELD_LAST, ADJUSTED_LAST
	}

	public enum BarSize {
		_1_secs("1 secs"),
		_5_secs("5 secs"),
		_10_secs("10 secs"),
		_15_secs("15 secs"),
		_30_secs("30 secs"),
		_1_min("1 min"),
		_2_mins("2 mins"),
		_3_mins("3 mins"),
		_5_mins("5 mins"),
		_10_mins("10 mins"),
		_15_mins("15 mins"),
		_20_mins("20 mins"),
		_30_mins("30 mins"),
		_1_hour("1 hour"),
		_4_hours("4 hours"),
		_1_day("1 day"),
		_1_week("1 week"),
		_1_month("1 month");

		private final String description;

		BarSize(final String description) {
			this.description = description;
		}

		@Override
        public String toString() {
			return description;
		}
	}

	public enum DurationUnit {
		SECOND, DAY, WEEK, MONTH, YEAR,
	}

	public enum DeepType {
	    INSERT, UPDATE, DELETE;

	    public static DeepType get( int ordinal) {
	    	return getEnum( ordinal, values() );
	    }
	}

	public enum DeepSide {
	    SELL, BUY;

	    public static DeepSide get( int ordinal) {
	    	return getEnum( ordinal, values() );
	    }
	}

	public enum NewsType {
		UNKNOWN, BBS, LIVE_EXCH, DEAD_EXCH, HTML, POPUP_TEXT, POPUP_HTML;

		public static NewsType get( int ordinal) {
			return getEnum( ordinal, values() );
		}
	}

	public enum FADataType {
		UNUSED, GROUPS, PROFILES, ALIASES;

		public static FADataType get( int ordinal) {
			return getEnum( ordinal, values() );
		}
	}

	public enum SecIdType implements IApiEnum {
	    None, CUSIP, SEDOL, ISIN, RIC;

		public static SecIdType get(String str) {
            return getValueOf(str, values(), None);
		}

		@Override public String getApiString() {
			return this == None ? "" : super.toString();
		}
	}

	public enum SecType implements IApiEnum {
		None, STK, OPT, FUT, CONTFUT, CASH, BOND, CFD, FOP, WAR, IOPT, FWD, BAG, IND, BILL, FUND, FIXED, SLB, NEWS, CMDTY, BSK, ICU, ICS;

        public static SecType get(String str) {
            return getValueOf(str, values(), None);
        }

		@Override public String getApiString() {
			return this == None ? "" : super.toString();
		}
	}

	public enum MktDataType {
		Unknown, Realtime, Frozen, Delayed, DelayedFrozen;

		public static MktDataType get( int ordinal) {
			return getEnum( ordinal, values() );
		}
	}

	public enum Method implements IApiEnum {
		None, EqualQuantity, AvailableEquity, NetLiq, PctChange;

	    public static Method get( String str) {
            return getValueOf(str, values(), None);
	    }

	    @Override public String getApiString() {
			return this == None ? "" : super.toString();
		}
	}

	public static <T extends Enum<?> & IApiEnum> T getValueOf( String v, T[] values, T defaultValue ) {
        for( T currentEnum : values ) {
            if( currentEnum.getApiString().equals(v) ) {
                return currentEnum;
            }
        }
        return defaultValue;
    }

	/** Lookup enum by ordinal. Use Enum.valueOf() to lookup by string. */
	public static <T extends Enum<T>> T getEnum(int ordinal, T[] values) {
		if (ordinal == Integer.MAX_VALUE) {
			return null;
		}

		for (T val : values) {
			if (val.ordinal() == ordinal) {
				return val;
			}
		}
		String str = String.format( "Error: %s is not a valid value for enum %s", ordinal, values[0].getClass().getName() );
		throw new IllegalArgumentException( str);
	}
}
