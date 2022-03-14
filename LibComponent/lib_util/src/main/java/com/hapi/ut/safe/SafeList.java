package com.hapi.ut.safe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author czwathou
 * @date 2017/7/11
 */
public class SafeList<E> {

    public List<E> list = Collections.synchronizedList(new ArrayList<E>());

    public int size() {
        return list.size();
    }

    public void add(E x) {
        synchronized (list) {
            list.add(x);
        }
    }

    public boolean addIfAbsent(E x) {
        synchronized (list) {
            boolean absent = !list.contains(x);
            if (absent)
                list.add(x);
            return absent;
        }
    }

    public boolean remove(E x) {
        synchronized (list) {
            return list.remove(x);
        }
    }
}