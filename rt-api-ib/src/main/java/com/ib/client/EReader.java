/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;



/**
 * This class reads commands from TWS and passes them to the user defined
 * EWrapper.
 *
 * This class is initialized with a DataInputStream that is connected to the
 * TWS. Messages begin with an ID and any relevant data are passed afterwards.
 */
public class EReader extends Thread {
    private EClientSocket 	m_clientSocket;
    private EReaderSignal m_signal;
    private EDecoder m_processMsgsDecoder;
    private static final EWrapper defaultWrapper = new DefaultEWrapper();
    private static final int IN_BUF_SIZE_DEFAULT = 8192;
    private byte[] m_iBuf = new byte[IN_BUF_SIZE_DEFAULT];
    private int m_iBufLen = 0;
    private final Deque<EMessage> m_msgQueue = new LinkedList<>();
    
    protected boolean isUseV100Plus() {
		return m_clientSocket.isUseV100Plus();
	}

	protected EClient parent()    { return m_clientSocket; }
    private EWrapper eWrapper()         { return parent().wrapper(); }

    /**
     * Construct the EReader.
     * @param parent An EClientSocket connected to TWS.
     * @param signal A callback that informs that there are messages in msg queue.
     */
    public EReader(EClientSocket parent, EReaderSignal signal) {
    	m_clientSocket = parent;
        m_signal = signal;
        m_processMsgsDecoder = new EDecoder(parent.serverVersion(), parent.wrapper(), parent);
    }
    
    /**
     * Read and put messages to the msg queue until interrupted or TWS closes connection.
     */
    @Override
    public void run() {
        try {
            // loop until thread is terminated
            while (!isInterrupted()) {
            	if (!putMessageToQueue())
            		break;
            }
        }
        catch ( Exception ex ) {
        	//if (parent().isConnected()) {
        		if( ex instanceof EOFException ) {
            		eWrapper().error(EClientErrors.NO_VALID_ID, EClientErrors.BAD_LENGTH.code(),
            				EClientErrors.BAD_LENGTH.msg() + " " + ex.getMessage());
        		}
        		else {
        			eWrapper().error( ex);
        		}
        		
        		parent().eDisconnect();
        	//}
        } 
        
        m_signal.issueSignal();
    }

	public boolean putMessageToQueue() throws IOException {
		EMessage msg = readSingleMessage();
		
		if (msg == null)
			return false;
		
		synchronized(m_msgQueue) {
			m_msgQueue.addFirst(msg);
		}
		
		m_signal.issueSignal();
		
		return true;
	}   

	protected EMessage getMsg() {
    	synchronized (m_msgQueue) {
    		return m_msgQueue.isEmpty() ? null : m_msgQueue.removeLast();
		}
    }
	
    static final int MAX_MSG_LENGTH = 0xffffff;

	private static class InvalidMessageLengthException extends IOException {
		private static final long serialVersionUID = 1L;

		InvalidMessageLengthException(String message) {
			super(message);
		}
    }
    
    public void processMsgs() throws IOException {
    	EMessage msg = getMsg();
    	
    	while (msg != null && m_processMsgsDecoder.processMsg(msg) > 0) {
    		msg = getMsg();
    	}
    }

	private EMessage readSingleMessage() throws IOException {
		if (isUseV100Plus()) {
			int msgSize = m_clientSocket.readInt();

			if (msgSize > MAX_MSG_LENGTH) {
				throw new InvalidMessageLengthException("message is too long: "
						+ msgSize);
			}
			
			byte[] buf = new byte[msgSize];
			
			int offset = 0;
			
			while (offset < msgSize) {
				offset += m_clientSocket.read(buf, offset, msgSize - offset);
			}
						
			return new EMessage(buf, buf.length);
		}
		
		if (m_iBufLen == 0) {
			m_iBufLen = appendIBuf();
		}
				
		int msgSize;
		
		while (true)
			try {
				msgSize = 0;
				if (m_iBufLen > 0) {
				  try (EDecoder decoder = new EDecoder(m_clientSocket.serverVersion(), defaultWrapper)) {
				    msgSize = decoder.processMsg(new EMessage(m_iBuf, m_iBufLen));
				  }
				}
				break;
			} catch (IOException e) {
				if (m_iBufLen >= m_iBuf.length * 3/4) {
					byte[] tmp = new byte[m_iBuf.length * 2];
					
					System.arraycopy(m_iBuf, 0, tmp, 0, m_iBuf.length);
					
					m_iBuf = tmp;
				}
				
				m_iBufLen += appendIBuf();
			}
		
		if (msgSize == 0)
			return null;
		
		
		EMessage msg = new EMessage(m_iBuf, msgSize);
		
		System.arraycopy(Arrays.copyOfRange(m_iBuf, msgSize, m_iBuf.length), 0, m_iBuf, 0, m_iBuf.length - msgSize);
		
		m_iBufLen -= msgSize;
		
		if (m_iBufLen < IN_BUF_SIZE_DEFAULT && m_iBuf.length > IN_BUF_SIZE_DEFAULT) {
			byte[] tmp = new byte[IN_BUF_SIZE_DEFAULT];
			
			System.arraycopy(m_iBuf, 0, tmp, 0, tmp.length);
			
			m_iBuf = tmp;
		}			
		
		return msg;
	}

	protected int appendIBuf() throws IOException {
		return m_clientSocket.read(m_iBuf, m_iBufLen, m_iBuf.length - m_iBufLen);
	}   
}
