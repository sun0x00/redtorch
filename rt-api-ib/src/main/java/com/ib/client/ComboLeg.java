/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import com.ib.client.Types.Action;

public class ComboLeg {
    public enum OpenClose implements IApiEnum {
        Same, Open, Close, Unknown;

        static OpenClose get( int i) {
            return Types.getEnum( i, values() );
        }

        @Override
        public String getApiString() {
            return String.valueOf(ordinal());
        }
    }

    private int m_conid;
    private int m_ratio;
    private String m_action = "BUY"; // BUY/SELL/SSHORT/SSHORTX
    private String m_exchange;
    private int m_openClose = 0; // Same
    // for stock legs when doing short sale
    private int m_shortSaleSlot; // 1 = clearing broker, 2 = third party
    private String m_designatedLocation;
    private int m_exemptCode;

    // Get
    public Action action()              { return Action.get(m_action); }
    public String getAction()           { return m_action; }
    public int conid()                  { return m_conid; }
    public int exemptCode()             { return m_exemptCode; }
    public int ratio()                  { return m_ratio; }
    public int shortSaleSlot()          { return m_shortSaleSlot; }
    public OpenClose openClose()        { return OpenClose.get(m_openClose); }
    public int getOpenClose()           { return m_openClose; }
    public String designatedLocation()  { return m_designatedLocation; }
    public String exchange()            { return m_exchange; }

    // Set
    public void action(Action v)             { m_action = ( v == null ) ? null : v.getApiString(); }
    public void action(String v)             { m_action = v; }
    public void conid(int v)                 { m_conid = v; }
    public void designatedLocation(String v) { m_designatedLocation = v; }
    public void exchange(String v)           { m_exchange = v; }
    public void exemptCode(int v)            { m_exemptCode = v; }
    public void openClose(OpenClose v)       { m_openClose = ( v == null ) ? 0 : v.ordinal(); }
    public void openClose(int v)             { m_openClose = v; }
    public void ratio(int v)                 { m_ratio = v; }
    public void shortSaleSlot(int v)         { m_shortSaleSlot = v; }

    public ComboLeg() {
    	this(/* conId */ 0, /* ratio */ 0, /* action */ null,
    		/* exchange */ null, /* openClose */ 0,
    		/* shortSaleSlot */ 0, /* designatedLocation*/ null, /* exemptCode */ -1);
    }

    public ComboLeg(int p_conId, int p_ratio, String p_action, String p_exchange, int p_openClose) {
    	this(p_conId, p_ratio, p_action, p_exchange, p_openClose,
    		/* shortSaleSlot */ 0, /* designatedLocation*/ null, /* exemptCode */ -1);

    }

    public ComboLeg(int p_conId, int p_ratio, String p_action, String p_exchange,
    		int p_openClose, int p_shortSaleSlot, String p_designatedLocation) {
    	this(p_conId, p_ratio, p_action, p_exchange, p_openClose, p_shortSaleSlot, p_designatedLocation,
    		/* exemptCode */ -1);

    }

    public ComboLeg(int p_conId, int p_ratio, String p_action, String p_exchange,
    		int p_openClose, int p_shortSaleSlot, String p_designatedLocation, int p_exemptCode) {
        m_conid = p_conId;
        m_ratio = p_ratio;
        m_action = p_action;
        m_exchange = p_exchange;
        m_openClose = p_openClose;
        m_shortSaleSlot = p_shortSaleSlot;
        m_designatedLocation = p_designatedLocation;
        m_exemptCode = p_exemptCode;
    }

    @Override
    public boolean equals(Object p_other) {
        if (this == p_other) {
            return true;
        }
        if (!(p_other instanceof ComboLeg)) {
            return false;
        }

        ComboLeg l_theOther = (ComboLeg)p_other;

        if (m_conid != l_theOther.m_conid ||
        	m_ratio != l_theOther.m_ratio ||
        	m_openClose != l_theOther.m_openClose ||
        	m_shortSaleSlot != l_theOther.m_shortSaleSlot ||
        	m_exemptCode != l_theOther.m_exemptCode) {
        	return false;
        }

        if (Util.StringCompareIgnCase(m_action, l_theOther.m_action) != 0 ||
        	Util.StringCompareIgnCase(m_exchange, l_theOther.m_exchange) != 0 ||
        	Util.StringCompareIgnCase(m_designatedLocation, l_theOther.m_designatedLocation) != 0) {
        	return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = m_conid;
        result = result * 31 + m_ratio;
        result = result * 31 + m_openClose;
        result = result * 31 + m_shortSaleSlot;
        result = result * 31 + m_exemptCode;
        // Other fields are strings compared ignoring case and with null checks. Do not use them.
        return result;
    }

    @Override public String toString() {
        return String.format( "%s %s %s", m_action, m_ratio, m_conid);
    }
}
