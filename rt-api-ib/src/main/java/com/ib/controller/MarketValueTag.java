/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.controller;

import com.ib.client.Types;

public enum MarketValueTag {
    AccountOrGroup("AccountOrGroup"),
    RealCurrency("RealCurrency"),
    IssuerOptionValue("IssuerOption"),
    NetLiquidationByCurrency("Net Liq"),
    CashBalance("CashBalance"),
    TotalCashBalance("TotalCashBalance"),
    AccruedCash("AccruedCash"),
    StockMarketValue("Stocks"),
    OptionMarketValue("Options"),
    FutureOptionValue("Futures"),
    FuturesPNL("FuturesPNL"),
    UnrealizedPnL("UnrealizedPnL"),
    RealizedPnL("RealizedPnL"),
    ExchangeRate("ExchangeRate"),
    FundValue("Fund"),
    NetDividend("NetDividend"),
    MutualFundValue("MutualFund"),
    MoneyMarketFundValue("MoneyMarketFund"),
    CorporateBondValue("CorporateBond"),
    TBondValue("TBond"),
    TBillValue("TBill"),
    WarrantValue("Warrant"),
    FxCashBalance("FxCashBalance");

    private final String description;

    MarketValueTag(final String description) {
        this.description = description;
    }

    public static MarketValueTag get(int i) {
        return Types.getEnum(i, values());
    }

    @Override
    public String toString() {
        return description;
    }
}
