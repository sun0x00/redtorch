/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class DepthMktDataDescription {
    private String 	m_exchange;
    private String 	m_secType;
    private String 	m_listingExch;
    private String 	m_serviceDataType;
    private int 	m_aggGroup;

    // Get
    public String exchange() { return m_exchange; }
    public String secType() { return m_secType; }
    public String listingExch() { return m_listingExch; }
    public String serviceDataType() { return m_serviceDataType; }
    public int aggGroup() { return m_aggGroup; }

    // Set 
    public void exchange(String exchange) { m_exchange = exchange; }
    public void secType(String secType) { m_secType = secType; }
    public void listingExch(String listingExch) { m_listingExch = listingExch; }
    public void serviceDataType(String serviceDataType) { m_serviceDataType = serviceDataType; }
    public void aggGroup(int aggGroup) { m_aggGroup = aggGroup; }

    public DepthMktDataDescription() {
    }

    public DepthMktDataDescription(String p_exchange, String p_secType, String listingExch, String serviceDataType, int aggGroup) {
        m_exchange = p_exchange;
        m_secType = p_secType;
        m_listingExch = listingExch;
        m_serviceDataType = serviceDataType;
        m_aggGroup = aggGroup;
    }
}
