/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class MarketDataType {
    // constants - market data types
    public static final int REALTIME   = 1;
    public static final int FROZEN     = 2;
    public static final int DELAYED    = 3;
    public static final int DELAYED_FROZEN = 4;

    private static final String REALTIME_STR = "Real-Time";
    private static final String FROZEN_STR = "Frozen";
    private static final String DELAYED_STR = "Delayed";
    private static final String DELAYED_FROZEN_STR = "Delayed-Frozen";
    private static final String UNKNOWN_STR = "Unknown";

    public static String getField( int marketDataType) {
        switch( marketDataType) {
            case REALTIME:                    return REALTIME_STR;
            case FROZEN:                      return FROZEN_STR;
            case DELAYED:                     return DELAYED_STR;
            case DELAYED_FROZEN:              return DELAYED_FROZEN_STR;

            default:                          return UNKNOWN_STR;
        }
    }

    public static int getField( String marketDataTypeStr) {
        switch( marketDataTypeStr) {
            case REALTIME_STR:                return REALTIME;
            case FROZEN_STR:                  return FROZEN;
            case DELAYED_STR:                 return DELAYED;
            case DELAYED_FROZEN_STR:          return DELAYED_FROZEN;

            default:                          return Integer.MAX_VALUE;
        }
    }

    public static String[] getFields(){
    	int totalFields = MarketDataType.class.getFields().length;
    	String [] fields = new String[totalFields];
    	for (int i = 0; i < totalFields; i++){
    		fields[i] = MarketDataType.getField(i + 1);
    	}
    	return fields;
    }
}
