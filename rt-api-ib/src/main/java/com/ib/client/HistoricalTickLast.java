/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class HistoricalTickLast {
    private long m_time;
    private int m_mask;
    private double m_price;
    private long m_size;
    private String m_exchange;
    private String m_specialConditions;

    public HistoricalTickLast(long time, int mask, double price, long size, String exchange, String specialConditions) {
        m_time = time;
        m_mask = mask;
        m_price = price;
        m_size = size;
        m_exchange = exchange;
        m_specialConditions = specialConditions;
    }

    public long time() {
        return m_time;
    }

    public int mask() {
        return m_mask;
    }

    public double price() {
        return m_price;
    }

    public long size() {
        return m_size;
    }

    public String exchange() {
        return m_exchange;
    }

    public String specialConditions() {
        return m_specialConditions;
    }
}