/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;


public class EClientErrors {
    public static final int NO_VALID_ID = -1;
    static final CodeMsgPair ALREADY_CONNECTED = new CodeMsgPair(501, "Already connected.");
    static final CodeMsgPair CONNECT_FAIL = new CodeMsgPair(502, "Couldn't connect to TWS. Confirm that \"Enable ActiveX and Socket Clients\" "
    		+ "is enabled and connection port is the same as \"Socket Port\" on the TWS \"Edit->Global Configuration...->API->Settings\" menu. "
    		+ "Live Trading ports: TWS: 7496; IB Gateway: 4001. Simulated Trading ports for new installations of version 954.1 or newer: "
    		+ "TWS: 7497; IB Gateway: 4002");
    public static final CodeMsgPair UPDATE_TWS = new CodeMsgPair(503, "The TWS is out of date and must be upgraded.");
    public static final CodeMsgPair NOT_CONNECTED = new CodeMsgPair(504, "Not connected");
    static final CodeMsgPair UNKNOWN_ID = new CodeMsgPair(505, "Fatal Error: Unknown message id.");
    static final CodeMsgPair UNSUPPORTED_VERSION = new CodeMsgPair(506, "Unsupported Version");
    static final CodeMsgPair BAD_LENGTH = new CodeMsgPair(507, "Bad Message Length");
    static final CodeMsgPair BAD_MESSAGE = new CodeMsgPair(508, "Bad Message");
    static final CodeMsgPair FAIL_SEND = new CodeMsgPair(509, "Failed to send message - "); // generic message; all future messages should use this
    static final CodeMsgPair FAIL_SEND_REQMKT = new CodeMsgPair(510, "Request Market Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANMKT = new CodeMsgPair(511, "Cancel Market Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_ORDER = new CodeMsgPair(512, "Order Sending Error - ");
    static final CodeMsgPair FAIL_SEND_ACCT = new CodeMsgPair(513, "Account Update Request Sending Error -");
    static final CodeMsgPair FAIL_SEND_EXEC = new CodeMsgPair(514, "Request For Executions Sending Error -");
    static final CodeMsgPair FAIL_SEND_CORDER = new CodeMsgPair(515, "Cancel Order Sending Error -");
    static final CodeMsgPair FAIL_SEND_OORDER = new CodeMsgPair(516, "Request Open Order Sending Error -");
    static final CodeMsgPair UNKNOWN_CONTRACT = new CodeMsgPair(517, "Unknown contract. Verify the contract details supplied.");
    static final CodeMsgPair FAIL_SEND_REQCONTRACT = new CodeMsgPair(518, "Request Contract Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQMKTDEPTH = new CodeMsgPair(519, "Request Market Depth Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANMKTDEPTH = new CodeMsgPair(520, "Cancel Market Depth Sending Error - ");
    static final CodeMsgPair FAIL_SEND_SERVER_LOG_LEVEL = new CodeMsgPair(521, "Set Server Log Level Sending Error - ");
    static final CodeMsgPair FAIL_SEND_FA_REQUEST = new CodeMsgPair(522, "FA Information Request Sending Error - ");
    static final CodeMsgPair FAIL_SEND_FA_REPLACE = new CodeMsgPair(523, "FA Information Replace Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQSCANNER = new CodeMsgPair(524, "Request Scanner Subscription Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANSCANNER = new CodeMsgPair(525, "Cancel Scanner Subscription Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQSCANNERPARAMETERS = new CodeMsgPair(526, "Request Scanner Parameter Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQHISTDATA = new CodeMsgPair(527, "Request Historical Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANHISTDATA = new CodeMsgPair(528, "Request Historical Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQRTBARS = new CodeMsgPair(529, "Request Real-time Bar Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANRTBARS = new CodeMsgPair(530, "Cancel Real-time Bar Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQCURRTIME = new CodeMsgPair(531, "Request Current Time Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQFUNDDATA = new CodeMsgPair(532, "Request Fundamental Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANFUNDDATA = new CodeMsgPair(533, "Cancel Fundamental Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQCALCIMPLIEDVOLAT = new CodeMsgPair(534, "Request Calculate Implied Volatility Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQCALCOPTIONPRICE = new CodeMsgPair(535, "Request Calculate Option Price Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANCALCIMPLIEDVOLAT = new CodeMsgPair(536, "Cancel Calculate Implied Volatility Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANCALCOPTIONPRICE = new CodeMsgPair(537, "Cancel Calculate Option Price Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQGLOBALCANCEL = new CodeMsgPair(538, "Request Global Cancel Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQMARKETDATATYPE = new CodeMsgPair(539, "Request Market Data Type Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQPOSITIONS = new CodeMsgPair(540, "Request Positions Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANPOSITIONS = new CodeMsgPair(541, "Cancel Positions Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQACCOUNTDATA = new CodeMsgPair(542, "Request Account Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANACCOUNTDATA = new CodeMsgPair(543, "Cancel Account Data Sending Error - ");
    static final CodeMsgPair FAIL_SEND_VERIFYREQUEST = new CodeMsgPair(544, "Verify Request Sending Error - ");
    static final CodeMsgPair FAIL_SEND_VERIFYMESSAGE = new CodeMsgPair(545, "Verify Message Sending Error - ");
    static final CodeMsgPair FAIL_SEND_QUERYDISPLAYGROUPS = new CodeMsgPair(546, "Query Display Groups Sending Error - ");
    static final CodeMsgPair FAIL_SEND_SUBSCRIBETOGROUPEVENTS = new CodeMsgPair(547, "Subscribe To Group Events Sending Error - ");
    static final CodeMsgPair FAIL_SEND_UPDATEDISPLAYGROUP = new CodeMsgPair(548, "Update Display Group Sending Error - ");
    static final CodeMsgPair FAIL_SEND_UNSUBSCRIBEFROMGROUPEVENTS = new CodeMsgPair(549, "Unsubscribe From Group Events Sending Error - ");
    static final CodeMsgPair FAIL_SEND_STARTAPI = new CodeMsgPair(550, "Start API Sending Error - ");
    static final CodeMsgPair FAIL_SEND_VERIFYANDAUTHREQUEST = new CodeMsgPair(551, "Verify And Auth Request Sending Error - ");
    static final CodeMsgPair FAIL_SEND_VERIFYANDAUTHMESSAGE = new CodeMsgPair(552, "Verify And Auth Message Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQPOSITIONSMULTI = new CodeMsgPair(553, "Request Positions Multi Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANPOSITIONSMULTI = new CodeMsgPair(554, "Cancel Positions Multi Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQACCOUNTUPDATESMULTI = new CodeMsgPair(555, "Request Account Updates Multi Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANACCOUNTUPDATESMULTI = new CodeMsgPair(556, "Cancel Account Updates Multi Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQSECDEFOPTPARAMS = new CodeMsgPair(557, "Request Security Definition Option Params Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQSOFTDOLLARTIERS = new CodeMsgPair(558, "Request Soft Dollar Tiers Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQFAMILYCODES = new CodeMsgPair(559, "Request Family Codes Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQMATCHINGSYMBOLS = new CodeMsgPair(560, "Request Matching Symbols Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQMKTDEPTHEXCHANGES = new CodeMsgPair(561, "Request Market Depth Exchanges Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQSMARTCOMPONENTS = new CodeMsgPair(562, "Request Smart Components Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQNEWSPROVIDERS = new CodeMsgPair(563, "Request News Providers Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQNEWSARTICLE = new CodeMsgPair(564, "Request News Article Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQHISTORICALNEWS = new CodeMsgPair(565, "Request Historical News Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQHEADTIMESTAMP = new CodeMsgPair(566, "Request Head Time Stamp Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANHEADTIMESTAMP = new CodeMsgPair(567, "Cancel Head Time Stamp Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQMARKETRULE = new CodeMsgPair(568, "Request Market Rule Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQPNL = new CodeMsgPair(566, "Request PnL Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANPNL = new CodeMsgPair(567, "Cancel PnL Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQPNL_SINGLE = new CodeMsgPair(568, "Request PnL Single Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANPNL_SINGLE = new CodeMsgPair(569, "Cancel PnL Single Sending Error - ");
    static final CodeMsgPair FAIL_SEND_HISTORICAL_TICK = new CodeMsgPair(569, "Request Historical Ticks Sending Error - ");
    static final CodeMsgPair FAIL_SEND_REQTICKBYTICK = new CodeMsgPair(570, "Request Tick-By-Tick Sending Error - ");
    static final CodeMsgPair FAIL_SEND_CANTICKBYTICK = new CodeMsgPair(571, "Cancel Tick-By-Tick Sending Error - ");

    public EClientErrors() {
    }

    static public class CodeMsgPair {

        // members vars
        int m_errorCode;
        String m_errorMsg;

        // Get/Set methods
        public int code()    { return m_errorCode; }
        public String msg()  { return m_errorMsg; }

        /** Constructor */
        public CodeMsgPair(int i, String errString) {
            m_errorCode = i;
            m_errorMsg = errString;
        }
    }
}
