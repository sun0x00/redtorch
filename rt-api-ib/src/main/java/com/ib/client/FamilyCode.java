/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class FamilyCode {
    private String 	m_accountID;
    private String 	m_familyCodeStr;

    // Get
    public String accountID() { return m_accountID; }
    public String familyCodeStr() { return m_familyCodeStr; }

    // Set 
    public void accountID(String accountID) { m_accountID = accountID; }
    public void familyCodeStr(String familyCodeStr) { m_familyCodeStr = familyCodeStr; }
    
    public FamilyCode() {
    }

    public FamilyCode(String p_accountID, String p_familyCodeStr) {
        m_accountID = p_accountID;
        m_familyCodeStr = p_familyCodeStr;
    }
}
