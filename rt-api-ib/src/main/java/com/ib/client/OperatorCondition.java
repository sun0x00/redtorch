/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public abstract class OperatorCondition extends OrderCondition {
	
	private boolean m_isMore;

	protected abstract String valueToString();
	protected abstract void valueFromString(String v);
	
	
	@Override
	public void readFrom(ObjectInput in) throws IOException {
		super.readFrom(in);
		
		m_isMore = in.readBoolean();
		
		valueFromString(in.readUTF());
	}
	
	@Override
	public String toString() {
		return " is " + (isMore() ? ">= " : "<= ") + valueToString();
	}
	
	@Override
	public void writeTo(ObjectOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(m_isMore);
		out.writeUTF(valueToString());
	}
	
	public boolean isMore() {
		return m_isMore;
	}

	public void isMore(boolean m_isMore) {
		this.m_isMore = m_isMore;
	}
}