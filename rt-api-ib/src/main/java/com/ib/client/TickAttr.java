/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class TickAttr {
	private boolean m_canAutoExecute = false;
	private boolean m_pastLimit = false;
	private boolean m_preOpen = false;
	private boolean m_unreported = false;
	private boolean m_bidPastLow = false;
	private boolean m_askPastHigh = false;
	
	public boolean canAutoExecute() {
		return m_canAutoExecute;
	}
	public boolean pastLimit() {
		return m_pastLimit;
	}
	public boolean preOpen() {
		return m_preOpen;
	}
	public boolean unreported() {
		return m_unreported;
	}
	public boolean bidPastLow() {
		return m_bidPastLow;
	}
	public boolean askPastHigh() {
		return m_askPastHigh;
	}
	public void canAutoExecute(boolean canAutoExecute) {
		this.m_canAutoExecute = canAutoExecute;
	}
	public void pastLimit(boolean pastLimit) {
		this.m_pastLimit = pastLimit;
	}
	public void preOpen(boolean preOpen) {
		this.m_preOpen = preOpen;
	}
	public void unreported(boolean unreported) {
		this.m_unreported = unreported;
	}
	public void bidPastLow(boolean bidPastLow) {
		this.m_bidPastLow = bidPastLow;
	}
	public void askPastHigh(boolean askPastHigh) {
		this.m_askPastHigh = askPastHigh;
	}
}
