/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.Socket;

public class EClientSocket extends EClient implements EClientMsgSink  {

	protected int m_redirectCount = 0;
	protected int m_defaultPort;
    private boolean m_allowRedirect;
    protected DataInputStream m_dis;
	private boolean m_asyncEConnect = false;
	private boolean m_connected = false;
	private Socket m_socket;
		
	public void setAsyncEConnect(boolean asyncEConnect) {
		this.m_asyncEConnect = asyncEConnect;
	}

	public boolean isAsyncEConnect() {
		return m_asyncEConnect;
	}

	public EClientSocket(EWrapper eWrapper, EReaderSignal signal) {
		super(eWrapper, signal);
	}

	@Override
	protected Builder prepareBuffer() {
        Builder buf = new Builder( 1024 );
        if( m_useV100Plus ) {
            buf.allocateLengthHeader();
        }
        return buf;
    }

	@Override
	protected void closeAndSend(Builder buf) throws IOException {
    	if( m_useV100Plus ) {
    		buf.updateLength( 0 ); // New buffer means length header position is always zero
    	}
    	
    	EMessage msg = new EMessage(buf);
    	
    	sendMsg(msg);
    }

	protected synchronized void eConnect(Socket socket) throws IOException {
	    // create io streams
	    m_socketTransport = new ESocket(socket);
	    m_dis = new DataInputStream(socket.getInputStream());
	    m_defaultPort = socket.getPort();
	    m_socket = socket;
	
	    sendConnectRequest();
	
	    // start reader thread
	    EReader reader = new EReader(this, m_signal);
	
	    if (!m_asyncEConnect) {
	    	reader.putMessageToQueue();

	    	while (m_serverVersion == 0) {
	    		m_signal.waitForSignal();
	    		reader.processMsgs();
	    	}       
	    }
	}

	public synchronized void eConnect(Socket socket, int clientId) throws IOException {
	    m_clientId = clientId;
	    m_redirectCount = 0;
	    eConnect(socket);
	}

	public synchronized void eConnect( String host, int port, int clientId) {
	    eConnect(host, port, clientId, false);
	}

	public synchronized void eConnect( String host, int port, int clientId, boolean extraAuth) {
	    // already connected?
	    m_host = checkConnected(host);
	
	    m_clientId = clientId;
	    m_extraAuth = extraAuth;
	    m_redirectCount = 0;
	
	    if(m_host == null){
	        return;
	    }
	    try{
	        Socket socket = new Socket( m_host, port);
	        eConnect(socket);
	    }
	    catch( Exception e) {
	    	eDisconnect();
	        connectionError();
	    }
	}

	public boolean allowRedirect() {
		return m_allowRedirect;
	}

	public void allowRedirect(boolean val) {
		m_allowRedirect = val;
	}

	@Override
	public synchronized void redirect(String newAddress) {
	    if( m_useV100Plus ) {
	    	if (!m_allowRedirect) {
	    		m_eWrapper.error(EClientErrors.NO_VALID_ID, EClientErrors.CONNECT_FAIL.code(), EClientErrors.CONNECT_FAIL.msg());
	    		return;
	    	}
	    	
	        ++m_redirectCount;
	        
	        if ( m_redirectCount > REDIRECT_COUNT_MAX ) {
	            eDisconnect();
	            m_eWrapper.error( "Redirect count exceeded" );
	            return;
	        }
	                    
	        eDisconnect( false );
	        
	      	try {
				performRedirect( newAddress, m_defaultPort );
			} catch (IOException e) {
				m_eWrapper.error(e);
			}
	    }
	}

	@Override
	public synchronized void serverVersion(int version, String time) {
		m_serverVersion = version;
		m_TwsTime = time;	
		
		if( m_useV100Plus && (m_serverVersion < MIN_VERSION || m_serverVersion > MAX_VERSION) ) {
			eDisconnect();
			m_eWrapper.error(EClientErrors.NO_VALID_ID, EClientErrors.UNSUPPORTED_VERSION.code(), EClientErrors.UNSUPPORTED_VERSION.msg());
			return;
		}
		
	    if( m_serverVersion < MIN_SERVER_VER_SUPPORTED) {
	    	eDisconnect();
	        m_eWrapper.error( EClientErrors.NO_VALID_ID, EClientErrors.UPDATE_TWS.code(), EClientErrors.UPDATE_TWS.msg());
	        return;
	    }

	    if ( m_serverVersion < MIN_SERVER_VER_LINKING) {
			try {
				send( m_clientId);
			} catch (IOException e) {
				m_eWrapper.error(e);
			}
		}
	    
	    
	    // set connected flag
	    m_connected = true;       

	    if (!m_asyncEConnect)
	    	startAPI();
	}

	protected synchronized void performRedirect( String address, int defaultPort ) throws IOException {
	    System.out.println("Server Redirect: " + address);
	    
	    // Get host:port from address string and reconnect (note: port is optional)
	    String[] array = address.split(":");
	    m_host = array[0]; // reset connected host
	    int newPort;
	    try {
	        newPort = ( array.length > 1 ) ? Integer.parseInt(array[1]) : defaultPort;
	    }
	    catch ( NumberFormatException e ) {
	        System.out.println( "Warning: redirect port is invalid, using default port");
	        newPort = defaultPort;
	    }
	    eConnect( new Socket( m_host, newPort ) );
	}

	@Override
    public synchronized void eDisconnect() {
	    eDisconnect( true );
	}

	private synchronized void eDisconnect( boolean resetState ) {
	    // not connected?
	    if( m_dis == null && m_socketTransport == null) {
	        return;
	    }
	
	    if ( resetState ) {
	    	m_connected = false;
	        m_extraAuth = false;
	        m_clientId = -1;
	        m_serverVersion = 0;
	        m_TwsTime = "";
	        m_redirectCount = 0;
	    }
	
	    FilterInputStream dis = m_dis;
	    m_dis = null;
	    if (m_socketTransport != null) {
			try {
				m_socketTransport.close();
			} catch (IOException ignored) {
			} finally {
				m_socketTransport = null;
			}
		}
	
	    try {
	        if (dis != null)
	        	dis.close();
	    } catch (Exception ignored) {
	    }
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		return m_dis.read(buf, off, len);
	}

	public int readInt() throws IOException {
		return m_dis.readInt();
	}

	@Override
	public synchronized boolean isConnected() {
		return m_socket != null && m_socket.isConnected() && m_connected;
	}
}
