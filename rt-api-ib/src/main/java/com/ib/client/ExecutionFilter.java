/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class ExecutionFilter{
    private int 	m_clientId; // zero means no filtering on this field
    private String 	m_acctCode;
    private String 	m_time;
    private String 	m_symbol;
    private String 	m_secType;
    private String 	m_exchange;
    private String 	m_side;

    // Get
    public int clientId()    { return m_clientId; }
    public String acctCode() { return m_acctCode; }
    public String time()     { return m_time; }
    public String symbol()   { return m_symbol; }
    public String secType()  { return m_secType; }
    public String exchange() { return m_exchange; }
    public String side()     { return m_side; }

    // Set
    public void clientId(int clientId)    { m_clientId = clientId; }
    public void acctCode(String acctCode) { m_acctCode = acctCode; }
    public void time(String time)         { m_time = time; }
    public void symbol(String symbol)     { m_symbol = symbol; }
    public void secType(String secType)   { m_secType = secType; }
    public void exchange(String exchange) { m_exchange = exchange; }
    public void side(String side)         { m_side = side; }

    public ExecutionFilter() {
        clientId(0);
    }

    public ExecutionFilter( int p_clientId, String p_acctCode, String p_time,
    		String p_symbol, String p_secType, String p_exchange, String p_side) {
        m_clientId = p_clientId;
        m_acctCode = p_acctCode;
        m_time = p_time;
        m_symbol = p_symbol;
        m_secType = p_secType;
        m_exchange = p_exchange;
        m_side = p_side;
    }

    @Override
    public boolean equals(Object p_other) {
        if (this == p_other) {
            return true;
        }
        if (!(p_other instanceof ExecutionFilter)) {
            return false;
        }
        ExecutionFilter l_theOther = (ExecutionFilter)p_other;
        return m_clientId == l_theOther.m_clientId &&
               m_acctCode.equalsIgnoreCase( l_theOther.m_acctCode) &&
               m_time.equalsIgnoreCase( l_theOther.m_time) &&
               m_symbol.equalsIgnoreCase( l_theOther.m_symbol) &&
               m_secType.equalsIgnoreCase( l_theOther.m_secType) &&
               m_exchange.equalsIgnoreCase( l_theOther.m_exchange) &&
               m_side.equalsIgnoreCase( l_theOther.m_side);
    }

    @Override
    public int hashCode() {
        // Use m_clientId only since other fields are string compared ignoring case and with null checks.
        return m_clientId;
    }
}
