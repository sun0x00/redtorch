/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

interface EClientMsgSink {
	void serverVersion(int version, String time);
	void redirect(String host);
}
