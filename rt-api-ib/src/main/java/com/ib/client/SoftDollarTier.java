/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class SoftDollarTier {

	private String m_name, m_value, m_displayName;
	
	public SoftDollarTier(String name, String val, String displayName) {
		name(name);
		value(val);	
		
		m_displayName = displayName;
	}
	
	public String value() {
		return m_value;
	}
	
	private void value(String value) {
		this.m_value = value;
	}

	public String name() {
		return m_name;
	}

	private void name(String name) {
		this.m_name = name;
	}	

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
		result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
		
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof SoftDollarTier)) {
			return false;
		}
		
		SoftDollarTier other = (SoftDollarTier) obj;
		
		if (m_name == null) {
			if (other.m_name != null) {
				return false;
			}
		} else if (Util.StringCompare(m_name, other.m_name) != 0) {
			return false;
		}
		
		if (m_value == null) {
			if (other.m_value != null) {
				return false;
			}
		} else if (Util.StringCompare(m_value, other.m_value) != 0) {
			return false;
		}
		
		return true;
	}

	@Override public String toString() {
		return m_displayName;
	}

}
