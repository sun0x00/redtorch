/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class PriceCondition extends ContractCondition {
	
	public static final OrderConditionType conditionType = OrderConditionType.Price;
	
	protected PriceCondition() { }
	
	private double m_price;
	private int m_triggerMethod;
	private static String[] mthdNames = new String[] { "default", "double bid/ask", "last", "double last", "bid/ask", "", "", "last of bid/ask", "mid-point" };

	@Override
	public String toString() {
		return toString(null);
	}

	public double price() {
		return m_price;
	}

	public void price(double m_price) {
		this.m_price = m_price;
	}

	@Override
	public String toString(ContractLookuper lookuper) {
		return strTriggerMethod() + " " + super.toString(lookuper);
	}

	public int triggerMethod() {
		return m_triggerMethod;
	}
	
	String strTriggerMethod() {		
		return mthdNames[triggerMethod()];
	}

	public void triggerMethod(int m_triggerMethod) {
		this.m_triggerMethod = m_triggerMethod;
	}

	@Override
	protected String valueToString() {
		return "" + m_price;
	}

	@Override
	protected void valueFromString(String v) {
		m_price = Double.parseDouble(v);
	}

	@Override
	public void readFrom(ObjectInput in) throws IOException {
		super.readFrom(in);
		
		m_triggerMethod = in.readInt();
	}

	@Override
	public void writeTo(ObjectOutput out) throws IOException {
		super.writeTo(out);
		out.writeInt(m_triggerMethod);
	}
	
}