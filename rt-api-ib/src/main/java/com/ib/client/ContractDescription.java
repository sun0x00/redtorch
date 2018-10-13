/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.util.Arrays;

public class ContractDescription {
    private Contract m_contract;
    private String[] m_derivativeSecTypes;

    // Get
    public Contract contract() {
        return m_contract;
    }

    public String[] derivativeSecTypes() {
        return (m_derivativeSecTypes == null) ? null : Arrays.copyOf(m_derivativeSecTypes, m_derivativeSecTypes.length);
    }

    // Set
    public void contract(Contract contract) {
        m_contract = contract;
    }

    public void derivativeSecTypes(String[] derivativeSecTypes) {
        if (derivativeSecTypes == null) {
            m_derivativeSecTypes = null;
        } else {
            m_derivativeSecTypes = Arrays.copyOf(derivativeSecTypes, derivativeSecTypes.length);
        }
    }

    public ContractDescription() {
        m_contract = new Contract();
    }

    public ContractDescription(Contract p_contract, String[] p_derivativeSecTypes) {
        m_contract = p_contract;
        if (p_derivativeSecTypes == null) {
            m_derivativeSecTypes = null;
        } else {
            m_derivativeSecTypes = Arrays.copyOf(p_derivativeSecTypes, p_derivativeSecTypes.length);
        }
    }

    @Override
    public String toString() {
        return "conid: " + m_contract.conid() + "\n"
                + "symbol: " + m_contract.symbol() + "\n"
                + "secType: " + m_contract.secType().toString() + "\n"
                + "primaryExch: " + m_contract.primaryExch() + "\n"
                + "currency: " + m_contract.currency() + "\n"
                + "derivativeSecTypes: " + Arrays.toString(m_derivativeSecTypes) + "\n";
    }
}
