package com.qiniu.android.dns.local;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.NetworkInfo;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by bailong on 15/6/18.
 */
public final class Hosts {

    private final Hashtable<String, ArrayList<Value>> hosts = new Hashtable<>();

    public String[] query(Domain domain, NetworkInfo info) {
        ArrayList<Value> values = hosts.get(domain.domain);
        if (values == null || values.isEmpty()) {
            return null;
        }
        values = filter(values, info);
        return toIps(values);
    }

    private ArrayList<Value> filter(ArrayList<Value> origin, NetworkInfo info) {
        ArrayList<Value> normal = new ArrayList<>();
        ArrayList<Value> special = new ArrayList<>();
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

    public String[] toIps(ArrayList<Value> vals) {
        int size = vals.size();

        String[] r = new String[size];
        for (int i = 0; i < size; i++) {
            Value v = vals.get(i);
            r[i] = v.ip;
        }
        return r;
    }

    public Hosts put(String domain, Value val) {
        ArrayList<Value> vals = hosts.get(domain);
        if (vals == null) {
            vals = new ArrayList<>();
        }
        vals.add(val);
        hosts.put(domain, vals);
        return this;
    }

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
