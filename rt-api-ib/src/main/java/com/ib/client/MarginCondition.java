/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class MarginCondition extends OperatorCondition {
	
	public static final OrderConditionType conditionType = OrderConditionType.Margin;
	
	protected MarginCondition() { }
	
	@Override
	public String toString() {		
		return "the margin cushion percent" + super.toString();
	}

	private int m_percent;

	public int percent() {
		return m_percent;
	}

	public void percent(int m_percent) {
		this.m_percent = m_percent;
	}

	@Override
	protected String valueToString() {
		return "" + m_percent;
	}

	@Override
	protected void valueFromString(String v) {
		m_percent = Integer.parseInt(v);
	}
	
}