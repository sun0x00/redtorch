/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class TickByTick {
    private int m_tickType; // 0 - None, 1 - Last, 2 - AllLast, 3 -BidAsk, 4 - MidPoint
    private long m_time;  // in seconds
    private double m_price;
    private long m_size;
    private TickAttr m_attribs;
    private String m_exchange;
    private String m_specialConditions;
    private double m_bidPrice;
    private long m_bidSize;
    private double m_askPrice;
    private long m_askSize;
    private double m_midPoint;

    public TickByTick(int tickType, long time, double price, long size, TickAttr attribs, String exchange, String specialConditions) {
    	m_tickType = tickType;
        m_time = time;
        m_price = price;
        m_size = size;
        m_attribs = attribs;
        m_exchange = exchange;
        m_specialConditions = specialConditions;
    }

    public TickByTick(long time, double bidPrice, long bidSize, double askPrice, long askSize, TickAttr attribs) {
    	m_tickType = 3;
        m_time = time;
        m_bidPrice = bidPrice;
        m_bidSize = bidSize;
        m_askPrice = askPrice;
        m_askSize = askSize;
        m_attribs = attribs;
    }

    public TickByTick(long time, double midPoint) {
    	m_tickType = 4;
        m_time = time;
        m_midPoint = midPoint;
    }
    
    public int tickType() {
        return m_tickType;
    }
    
    public long time() {
        return m_time;
    }

    public double price() {
        return m_price;
    }

    public long size() {
        return m_size;
    }

    public TickAttr attribs() {
    	return m_attribs;
    }

    public String attribsStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(m_attribs.pastLimit() ? "PastLimit " : "");
        sb.append(m_attribs.unreported() ? "Unreported " : "");
        sb.append(m_attribs.bidPastLow() ? "BidPastLow " : "");
        sb.append(m_attribs.askPastHigh() ? "AskPastHigh " : "");
        return sb.toString();
    }

    public String exchange() {
        return m_exchange;
    }

    public String specialConditions() {
        return m_specialConditions;
    }

    public double bidPrice() {
        return m_bidPrice;
    }

    public long bidSize() {
        return m_bidSize;
    }
    
    public double askPrice() {
        return m_askPrice;
    }

    public long askSize() {
        return m_askSize;
    }

    public double midPoint() {
        return m_midPoint;
    }
}