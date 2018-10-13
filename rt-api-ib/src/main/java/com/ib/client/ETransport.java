/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.Closeable;
import java.io.IOException;

public interface ETransport extends Closeable {
	void send(EMessage msg) throws IOException;
}
