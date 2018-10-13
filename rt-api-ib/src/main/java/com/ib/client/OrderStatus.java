/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;


public enum OrderStatus {
	ApiPending,
	ApiCancelled,
	PreSubmitted,
	PendingCancel,
	Cancelled,
	Submitted,
	Filled,
	Inactive,
	PendingSubmit,
	Unknown;

    public static OrderStatus get(String apiString) {
        for( OrderStatus type : values() ) {
            if( type.name().equalsIgnoreCase(apiString) ) {
                return type;
            }
        }
        return Unknown;
    }
    
	public boolean isActive() {
		return this == PreSubmitted || this == PendingCancel || this == Submitted || this == PendingSubmit;
	}
}
