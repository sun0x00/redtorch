/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class Bar {
    
    private String m_time;
    private double m_open;
    private double m_high;
    private double m_low;
    private double m_close;
    private long m_volume;
    private int m_count;
    private double m_wap;

    public Bar(String time, double open, double high, double low, double close, long volume, int count, double wap) {
        this.m_time = time;
        this.m_open = open;
        this.m_high = high;
        this.m_low = low;
        this.m_close = close;
        this.m_volume = volume;
        this.m_count = count;
        this.m_wap = wap;
    }

    public String time() {
        return m_time;
    }

    public double open() {
        return m_open;
    }

    public double high() {
        return m_high;
    }

    public double low() {
        return m_low;
    }

    public double close() {
        return m_close;
    }

    public long volume() {
        return m_volume;
    }

    public int count() {
        return m_count;
    }

    public double wap() {
        return m_wap;
    }
    
}