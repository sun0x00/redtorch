/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.controller;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Formats {
	private static final Format FMT2 = new DecimalFormat( "#,##0.00");
	private static final Format FMT0 = new DecimalFormat( "#,##0");
	private static final Format PCT = new DecimalFormat( "0.0%");
	private static final ThreadLocal<DateFormat> GMT_DATE_TIME_FORMAT_CACHE = ThreadLocal.withInitial(() -> {
		final DateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format;
	});
    private static final ThreadLocal<DateFormat> DATE_TIME_FORMAT_CACHE = ThreadLocal.withInitial(() -> new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss"));
	private static final ThreadLocal<DateFormat> TIME_FORMAT_CACHE = ThreadLocal.withInitial(() -> new SimpleDateFormat( "HH:mm:ss"));

	/** Format with two decimals. */
	public static String fmt( double v) {
		return v == Double.MAX_VALUE ? null : FMT2.format( v);
	}

	/** Format with two decimals; return null for zero. */
	public static String fmtNz( double v) {
		return v == Double.MAX_VALUE || v == 0 ? null : FMT2.format( v);
	}

	/** Format with no decimals. */
	public static String fmt0( double v) {
		return v == Double.MAX_VALUE ? null : FMT0.format( v);
	}

	/** Format as percent with one decimal. */
	public static String fmtPct( double v) {
		return v == Double.MAX_VALUE ? null : PCT.format( v);
	}

	/** Format date/time for display. */
	public static String fmtDate( long ms) {
		return DATE_TIME_FORMAT_CACHE.get().format( new Date( ms) );
	}

	public static String fmtDateGmt(long ms) {
		return GMT_DATE_TIME_FORMAT_CACHE.get().format( new Date( ms) );
	}

	/** Format time for display. */
	public static String fmtTime( long ms) {
		return TIME_FORMAT_CACHE.get().format( new Date( ms) );
	}
}
