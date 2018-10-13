/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;


public class EJavaSignal implements EReaderSignal {
    private final Object monitor = new Object();
    private boolean open = false;

    @Override
    public void issueSignal() {
        synchronized (monitor) {
            open = true;
            monitor.notifyAll();
        }
    }

    @Override
    public void waitForSignal() {
        synchronized (monitor) {
            while (!open) {
                try {
                    monitor.wait();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            open = false;
        }
    }
}
