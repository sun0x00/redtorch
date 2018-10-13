/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.contracts;

import com.ib.client.Contract;
import com.ib.client.Types.SecType;

public class OptContract extends Contract {
    public OptContract(String symbol, String lastTradeDateOrContractMonth, double strike, String right) {
        this(symbol, "SMART", lastTradeDateOrContractMonth, strike, right);
    }

    public OptContract(String symbol, String exchange, String lastTradeDateOrContractMonth, double strike, String right) {
        symbol(symbol);
        secType(SecType.OPT.name());
        exchange(exchange);
        currency("USD");
        lastTradeDateOrContractMonth(lastTradeDateOrContractMonth);
        strike(strike);
        right(right);
    }
}
