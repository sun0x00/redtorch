/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;


public class OrderComboLeg {
    private double m_price; // price per leg

    public double price()       { return m_price; }
    public void price(double v) { m_price = v; }
    
    public OrderComboLeg() {
        m_price = Double.MAX_VALUE;
    }

    public OrderComboLeg(double p_price) {
        m_price = p_price;
    }

    @Override
    public boolean equals(Object p_other) {
        if (this == p_other) {
            return true;
        }
        if (!(p_other instanceof OrderComboLeg)) {
            return false;
        }

        OrderComboLeg l_theOther = (OrderComboLeg)p_other;

        return m_price == l_theOther.m_price;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(m_price);
        return (int) (temp ^ (temp >>> 32));
    }
}
