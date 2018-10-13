/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class HistoricalTick {
    private long m_time;
    private double m_price;
    private long m_size;

    public HistoricalTick(long time, double price, long size) {
        m_time = time;
        m_price = price;
        m_size = size;
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
}