package com.hapi.ut.safe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author czwathou
 * @date 2017/7/11
 */
public class SafeMap<K, V> {

    public Map<K, V> map = Collections.synchronizedMap(new HashMap<K, V>());

    public V put(K key, V value) {
        synchronized (map) {
            return map.put(key, value);
        }
    }

    public V remove(K key) {
        synchronized (map) {
            return map.remove(key);
        }
    }
}