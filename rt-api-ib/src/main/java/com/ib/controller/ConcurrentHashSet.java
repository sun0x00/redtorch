/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.controller;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<Key> extends AbstractSet<Key> {
    private static final Object OBJECT = new Object();

    private Map<Key, Object> m_map = new ConcurrentHashMap<>(16,0.75f,1); // use write concurrency level 1 (last param) to decrease memory consumption by ConcurrentHashMap

    /** return true if object was added as "first value" for this key */
    @Override
    public boolean add( Key key) {
        return m_map.put( key, OBJECT) == null; // null means there was no value for given key previously
    }

    @Override
    public boolean contains( Object key) {
        return m_map.containsKey( key);
    }

    @Override
    public Iterator<Key> iterator() {
        return m_map.keySet().iterator();
    }

    /** return true if key was indeed removed */
    @Override
    public boolean remove( Object key) {
        return m_map.remove( key) == OBJECT; // if value not null it was existing in the map
    }

    @Override
    public boolean isEmpty() {
        return m_map.isEmpty();
    }

    @Override
    public int size() {
        return m_map.size();
    }

    @Override
    public void clear() {
        m_map.clear();
    }
}
