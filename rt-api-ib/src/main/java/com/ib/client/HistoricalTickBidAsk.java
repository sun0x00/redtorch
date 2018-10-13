/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class HistoricalTickBidAsk {
    private long m_time;
    private int m_mask;
    private double m_priceBid;
    private double m_priceAsk;
    private long m_sizeBid;
    private long m_sizeAsk;

    public HistoricalTickBidAsk(long time, int mask, double priceBid, double priceAsk, long sizeBid, long sizeAsk) {
        m_time = time;
        m_mask = mask;
        m_priceBid = priceBid;
        m_priceAsk = priceAsk;
        m_sizeBid = sizeBid;
        m_sizeAsk = sizeAsk;
    }

    public long time() {
        return m_time;
    }

    public int mask() {
        return m_mask;
    }

    public double priceBid() {
        return m_priceBid;
    }

    public double priceAsk() {
        return m_priceAsk;
    }

    public long sizeBid() {
        return m_sizeBid;
    }

    public long sizeAsk() {
        return m_sizeAsk;
    }

}