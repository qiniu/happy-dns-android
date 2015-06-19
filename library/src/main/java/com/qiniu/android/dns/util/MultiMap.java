package com.qiniu.android.dns.util;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by bailong on 15/6/18.
 */
public class MultiMap<K, V> {
    private final Hashtable<K, ArrayList<V>> hosts = new Hashtable<>();
}
