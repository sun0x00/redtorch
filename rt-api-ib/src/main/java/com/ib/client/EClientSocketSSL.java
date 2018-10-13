/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class EClientSocketSSL extends EClientSocket  {

	public EClientSocketSSL(EWrapper eWrapper, EReaderSignal signal) {
		super(eWrapper, signal);
		
		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
	        SSLContext.setDefault(ctx);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

	@Override
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
	        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(m_host, port);
	        eConnect(socket);
	    }
	    catch( Exception e) {
	    	eDisconnect();
	        wrapper().error(e);
	    }
	}

	@Override
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
	    
	    eConnect( SSLSocketFactory.getDefault().createSocket( m_host, newPort ) );
	}
}
