/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

public abstract class ContractCondition extends OperatorCondition {

	@Override
	public String toString() {
		return toString(null);
	}
	
	public String toString(ContractLookuper lookuper) {
		Contract c = new Contract();
		
		c.conid(conId());
		c.exchange(exchange());
		
		List<ContractDetails> list = lookuper == null ? null : lookuper.lookupContract(c);		
		String strContract = list != null && !list.isEmpty() ? 
				list.get(0).contract().symbol() + " " + list.get(0).contract().secType() + " on " + list.get(0).contract().exchange() :
				conId() + "";
		
		return type() + " of " + strContract + super.toString();
	}

	private int m_conId;
	private String m_exchange;

	@Override
	public void readFrom(ObjectInput in) throws IOException {
		super.readFrom(in);
		
		m_conId = in.readInt();
		m_exchange = in.readUTF();
	}

	@Override
	public void writeTo(ObjectOutput out) throws IOException {
		super.writeTo(out);
		out.writeInt(m_conId);
		out.writeUTF(m_exchange);
	}

	public int conId() {
		return m_conId;
	}

	public void conId(int m_conId) {
		this.m_conId = m_conId;
	}

	public String exchange() {
		return m_exchange;
	}

	public void exchange(String exchange) {
		this.m_exchange = exchange;
	}
}