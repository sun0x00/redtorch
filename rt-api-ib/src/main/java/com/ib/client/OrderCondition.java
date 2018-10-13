/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public abstract class OrderCondition {

    private OrderConditionType m_type;
    private boolean m_isConjunctionConnection;

    public void readFrom(ObjectInput in) throws IOException {
        conjunctionConnection(in.readUTF().compareToIgnoreCase("a") == 0);
    }

    public void writeTo(ObjectOutput out) throws IOException {
        out.writeUTF(conjunctionConnection() ? "a" : "o");
    }


    @Override
    public String toString() {
        return conjunctionConnection() ? "<AND>" : "<OR>";
    }

    public boolean conjunctionConnection() {
        return m_isConjunctionConnection;
    }

    public void conjunctionConnection(boolean isConjunctionConnection) {
        this.m_isConjunctionConnection = isConjunctionConnection;
    }

    public OrderConditionType type() {
        return m_type;
    }

    public static OrderCondition create(OrderConditionType type) {
        OrderCondition orderCondition;
        switch (type) {
            case Execution:
                orderCondition = new ExecutionCondition();
                break;

            case Margin:
                orderCondition = new MarginCondition();
                break;

            case PercentChange:
                orderCondition = new PercentChangeCondition();
                break;

            case Price:
                orderCondition = new PriceCondition();
                break;

            case Time:
                orderCondition = new TimeCondition();
                break;

            case Volume:
                orderCondition = new VolumeCondition();
                break;

            default:
                return null;
        }
        orderCondition.m_type = type;
        return orderCondition;
    }
}