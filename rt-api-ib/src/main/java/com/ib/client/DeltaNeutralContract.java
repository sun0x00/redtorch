/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class DeltaNeutralContract {
	private int    m_conid;
	private double m_delta;
	private double m_price;

	// Get
	public int    conid()  { return m_conid; }
	public double delta()  { return m_delta; }
	public double price()  { return m_price; }

    // Set
	public void conid(int conid)     { m_conid = conid; }
    public void delta(double delta)  { m_delta = delta; }
    public void price(double price)  { m_price = price; }

    public DeltaNeutralContract() {
	    m_conid = 0;
	    m_delta = 0;
	    m_price = 0;
	}

	public DeltaNeutralContract(int conid, double delta, double price) {
		m_conid = conid;
		m_delta = delta;
		m_price = price;
	}

    @Override
    public boolean equals(Object p_other) {
        if (this == p_other) {
            return true;
        }

        if (p_other == null || !(p_other instanceof DeltaNeutralContract)) {
            return false;
        }

        DeltaNeutralContract l_theOther = (DeltaNeutralContract)p_other;

        if (m_conid != l_theOther.m_conid) {
            return false;
        }
        if (m_delta != l_theOther.m_delta) {
            return false;
        }
        if (m_price != l_theOther.m_price) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = m_conid;
        temp = Double.doubleToLongBits(m_delta);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m_price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
