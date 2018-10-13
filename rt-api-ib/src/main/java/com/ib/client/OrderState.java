/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;


public class OrderState {
    private String m_status;
    private String m_initMarginBefore;
    private String m_maintMarginBefore;
    private String m_equityWithLoanBefore;
    private String m_initMarginChange;
    private String m_maintMarginChange;
    private String m_equityWithLoanChange;
    private String m_initMarginAfter;
    private String m_maintMarginAfter;
    private String m_equityWithLoanAfter;
    private double m_commission;
    private double m_minCommission;
    private double m_maxCommission;
    private String m_commissionCurrency;
    private String m_warningText;

    // Get
    public double commission()           { return m_commission; }
    public double maxCommission()        { return m_maxCommission; }
    public double minCommission()        { return m_minCommission; }
    public OrderStatus status()          { return OrderStatus.get(m_status); }
    public String getStatus()            { return m_status; }
    public String commissionCurrency()   { return m_commissionCurrency; }
    public String initMarginBefore()     { return m_initMarginBefore; }
    public String maintMarginBefore()    { return m_maintMarginBefore; }
    public String equityWithLoanBefore() { return m_equityWithLoanBefore; }
    public String initMarginChange()     { return m_initMarginChange; }
    public String maintMarginChange()    { return m_maintMarginChange; }
    public String equityWithLoanChange() { return m_equityWithLoanChange; }
    public String initMarginAfter()      { return m_initMarginAfter; }
    public String maintMarginAfter()     { return m_maintMarginAfter; }
    public String equityWithLoanAfter()  { return m_equityWithLoanAfter; }
    public String warningText()          { return m_warningText; }

    // Set
    public void commission(double v)           { m_commission = v; }
    public void commissionCurrency(String v)   { m_commissionCurrency = v; }
    public void initMarginBefore(String v)     { m_initMarginBefore = v; }
    public void maintMarginBefore(String v)    { m_maintMarginBefore = v; }
    public void equityWithLoanBefore(String v) { m_equityWithLoanBefore = v; }
    public void initMarginChange(String v)     { m_initMarginChange = v; }
    public void maintMarginChange(String v)    { m_maintMarginChange = v; }
    public void equityWithLoanChange(String v) { m_equityWithLoanChange = v; }
    public void initMarginAfter(String v)      { m_initMarginAfter = v; }
    public void maintMarginAfter(String v)     { m_maintMarginAfter = v; }
    public void equityWithLoanAfter(String v)  { m_equityWithLoanAfter = v; }
    public void maxCommission(double v)        { m_maxCommission = v; }
    public void minCommission(double v)        { m_minCommission = v; }
    public void status(OrderStatus v)          { m_status = ( v == null ) ? null : v.name(); }
    public void status(String v)               { m_status = v; }
    public void warningText(String v)          { m_warningText = v; }

	OrderState() {
		this (null, null, null, null, null, null, null, null, null, null, 0.0, 0.0, 0.0, null, null);
	}

	OrderState(String status, 
			String initMarginBefore, String maintMarginBefore, String equityWithLoanBefore, 
			String initMarginChange, String maintMarginChange, String equityWithLoanChange, 
			String initMarginAfter, String maintMarginAfter, String equityWithLoanAfter, 
			double commission, double minCommission,
			double maxCommission, String commissionCurrency, String warningText) {
		m_status = status;
		m_initMarginBefore = initMarginBefore;
		m_maintMarginBefore = maintMarginBefore;
		m_equityWithLoanBefore = equityWithLoanBefore;
		m_initMarginChange = initMarginChange;
		m_maintMarginChange = maintMarginChange;
		m_equityWithLoanChange = equityWithLoanChange;
		m_initMarginAfter = initMarginAfter;
		m_maintMarginAfter = maintMarginAfter;
		m_equityWithLoanAfter = equityWithLoanAfter;
		m_commission = commission;
		m_minCommission = minCommission;
		m_maxCommission = maxCommission;
		m_commissionCurrency = commissionCurrency;
		m_warningText = warningText;
	}

	@Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OrderState)) {
            return false;
        }
        OrderState state = (OrderState)other;

        if (m_commission != state.m_commission ||
        	m_minCommission != state.m_minCommission ||
        	m_maxCommission != state.m_maxCommission) {
        	return false;
        }

        if (Util.StringCompare(m_status, state.m_status) != 0 ||
            Util.StringCompare(m_initMarginBefore, state.m_initMarginBefore) != 0 ||
            Util.StringCompare(m_maintMarginBefore, state.m_maintMarginBefore) != 0 ||
            Util.StringCompare(m_equityWithLoanBefore, state.m_equityWithLoanBefore) != 0 ||
            Util.StringCompare(m_initMarginChange, state.m_initMarginChange) != 0 ||
            Util.StringCompare(m_maintMarginChange, state.m_maintMarginChange) != 0 ||
            Util.StringCompare(m_equityWithLoanChange, state.m_equityWithLoanChange) != 0 ||
            Util.StringCompare(m_initMarginAfter, state.m_initMarginAfter) != 0 ||
            Util.StringCompare(m_maintMarginAfter, state.m_maintMarginAfter) != 0 ||
            Util.StringCompare(m_equityWithLoanAfter, state.m_equityWithLoanAfter) != 0 ||
        	Util.StringCompare(m_commissionCurrency, state.m_commissionCurrency) != 0) {
        	return false;
        }
        return true;
	}

    @Override
    public int hashCode() {
        // Use a few fields as a compromise between performance and hashCode quality.
        int result;
        long temp;
        temp = Double.doubleToLongBits(m_commission);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m_minCommission);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m_maxCommission);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
