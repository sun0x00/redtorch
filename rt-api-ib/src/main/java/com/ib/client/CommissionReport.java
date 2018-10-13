/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class CommissionReport {

    public String m_execId;
    public double m_commission;
    public String m_currency;
    public double m_realizedPNL;
    public double m_yield;
    public int    m_yieldRedemptionDate; // YYYYMMDD format

    public CommissionReport() {
        m_commission = 0;
        m_realizedPNL = 0;
        m_yield = 0;
        m_yieldRedemptionDate = 0;
    }

    @Override
    public boolean equals(Object p_other) {
        if (this == p_other) {
            return true;
        }
        if (!(p_other instanceof CommissionReport)) {
            return false;
        }
        CommissionReport l_theOther = (CommissionReport)p_other;
        return m_execId.equals(l_theOther.m_execId);
    }

    @Override
    public int hashCode() {
        // Since equals() uses m_execId only, the hashCode should do as well.
        return m_execId == null ? 0 : m_execId.hashCode();
    }
}
