package com.qiniu.android.dns.local;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.NetworkInfo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created by bailong on 15/6/18.
 */
public final class Hosts {

    private final Hashtable<String, LinkedList<Value>> hosts = new Hashtable<>();

    public synchronized String[] query(Domain domain, NetworkInfo info) {
        LinkedList<Value> values = hosts.get(domain.domain);
        if (values == null || values.isEmpty()) {
            return null;
        }
        if (values.size() > 1) {
            Value first = values.get(0);
            values.remove(0);
            values.add(first);
        }
        values = filter(values, info);
        return toIps(values);
    }

    private LinkedList<Value> filter(LinkedList<Value> origin, NetworkInfo info) {
        LinkedList<Value> normal = new LinkedList<>();
        LinkedList<Value> special = new LinkedList<>();
        for (Value v : origin) {
            if (v.provider == NetworkInfo.ISP_GENERAL) {
                normal.add(v);
            }
            if (info.provider != NetworkInfo.ISP_GENERAL
                    && v.provider == info.provider) {
                special.add(v);
            }
        }
        if (special.size() != 0) {
            return special;
        }
        return normal;
    }

    public synchronized String[] toIps(LinkedList<Value> vals) {
        int size = vals.size();

        String[] r = new String[size];
        for (int i = 0; i < size; i++) {
            Value v = vals.get(i);
            r[i] = v.ip;
        }
        return r;
    }

    @SuppressWarnings("UnusedReturnValue")
    public synchronized Hosts put(String domain, Value val) {
        LinkedList<Value> vals = hosts.get(domain);
        if (vals == null) {
            vals = new LinkedList<>();
        }
        vals.add(val);
        hosts.put(domain, vals);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Hosts put(String domain, String val) {
        put(domain, new Value(val));
        return this;
    }

    public static class Value {
        public final String ip;
        public final int provider;

        public Value(String ip, int provider) {
            this.ip = ip;
            this.provider = provider;
        }

        public Value(String ip) {
            this(ip, NetworkInfo.ISP_GENERAL);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof Value)) {
                return false;
            }
            Value another = (Value) o;
            return this.ip.equals(another.ip)
                    && this.provider == another.provider;
        }
    }
}
