/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

public class HistogramEntry implements Comparable<HistogramEntry> {

    public double price;
    public long size;

    public HistogramEntry(double price, long size) {
        this.price = price;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof HistogramEntry)) return false;
        HistogramEntry he = (HistogramEntry) o;
        return Double.compare(price, he.price) == 0 && size == he.size;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(price);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (size ^ (size >>> 32));
        return result;
    }

    @Override
    public int compareTo(HistogramEntry he) {
        final int d = Double.compare(price, he.price);
        if (d != 0) {
            return d;
        }
        return Long.compare(size, he.size);
    }

    @Override
    public String toString() {
        return "HistogramEntry{" +
                "price=" + price +
                ", size=" + size +
                '}';
    }
}
