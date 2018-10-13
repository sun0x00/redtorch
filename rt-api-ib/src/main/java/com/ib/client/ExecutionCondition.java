/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ExecutionCondition extends OrderCondition {
	
	public static final OrderConditionType conditionType = OrderConditionType.Execution;
	
	protected ExecutionCondition() { }
	
	@Override
	public void readFrom(ObjectInput in) throws IOException{
		super.readFrom(in);
		
		m_secType = in.readUTF();
		m_exchange = in.readUTF();
		m_symbol = in.readUTF();
	}

	@Override
	public String toString() {
		return "trade occurs for " + m_symbol + " symbol on " + m_exchange + " exchange for " + m_secType + " security type";
	}

	@Override
	public void writeTo(ObjectOutput out) throws IOException {
		super.writeTo(out);
		
		out.writeUTF(m_secType);
		out.writeUTF(m_exchange);
		out.writeUTF(m_symbol);
	}

	private String m_exchange;
	private String m_secType;
	private String m_symbol;

	public String exchange() {
		return m_exchange;
	}

	public void exchange(String m_exchange) {
		this.m_exchange = m_exchange;
	}

	public String secType() {
		return m_secType;
	}

	public void secType(String m_secType) {
		this.m_secType = m_secType;
	}

	public String symbol() {
		return m_symbol;
	}

	public void symbol(String m_symbol) {
		this.m_symbol = m_symbol;
	} 
	
}