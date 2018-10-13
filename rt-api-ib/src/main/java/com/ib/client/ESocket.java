/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ESocket implements ETransport {

    protected DataOutputStream m_dos;   // the socket output stream

    @Override
    public void send(EMessage msg) throws IOException {
        byte[] buf = msg.getRawData();

        m_dos.write(buf, 0, buf.length);
    }

    ESocket(Socket s) throws IOException {
        m_dos = new DataOutputStream(s.getOutputStream());
    }

    // Sends String without length prefix (pre-V100 style)
    protected void send(String str) throws IOException {
        // Write string to data buffer
        try (Builder b = new Builder(1024)) {
            b.send(str);
            b.writeTo(m_dos);
        }
    }

    @Override
    public void close() throws IOException {
        if (m_dos != null) {
            m_dos.close();
        }
    }
}
