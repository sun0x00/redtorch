/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.controller;

import com.ib.client.Contract;


public class Position {
	private Contract m_contract;
	private String m_account;
	private double m_position;
	private double m_marketPrice;
	private double m_marketValue;
	private double m_averageCost;
	private double m_unrealPnl;
	private double m_realPnl;

	public Contract contract()      { return m_contract; }
	public int conid()				{ return m_contract.conid(); }
	public double averageCost() 	{ return m_averageCost;}
	public double marketPrice() 	{ return m_marketPrice;}
	public double marketValue() 	{ return m_marketValue;}
	public double realPnl() 		{ return m_realPnl;}
	public double unrealPnl() 		{ return m_unrealPnl;}
	public double position() 			{ return m_position;}
	public String account() 		{ return m_account;}

	public Position( Contract contract, String account, double position, double marketPrice, double marketValue, double averageCost, double unrealPnl, double realPnl) {
		m_contract = contract;
		m_account = account;
		m_position = position;
		m_marketPrice = marketPrice;
		m_marketValue =marketValue;
		m_averageCost = averageCost;
		m_unrealPnl = unrealPnl;
		m_realPnl = realPnl;
	}
}
