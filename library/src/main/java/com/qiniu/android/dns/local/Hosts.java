package com.qiniu.android.dns.local;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bailong on 15/6/18.
 */
public final class Hosts {

    private final Map<String, LinkedList<Value>> hosts = new ConcurrentHashMap<>();

    public synchronized Record[] query(Domain domain, NetworkInfo info) {
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
        return toRecords(values);
    }

    private LinkedList<Value> filter(final LinkedList<Value> origin, NetworkInfo info) {
        if (origin == null) {
            return null;
        }

        LinkedList<Value> normal = new LinkedList<>();
        LinkedList<Value> special = new LinkedList<>();
        for (Value v : origin) {
            if (v.provider == NetworkInfo.ISP_GENERAL) {
                normal.add(v);
            } else if (v.provider == info.provider) {
                special.add(v);
            }
        }

        if (special.size() != 0) {
            return special;
        }
        return normal;
    }

    private Record[] toRecords(LinkedList<Value> vals) {
        if (vals == null) {
            return null;
        }

        int size = vals.size();
        List<Record> records = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Value v = vals.get(i);
            if (v != null && v.record != null && v.record.value != null) {
                records.add(v.record);
            }
        }

        return records.toArray(new Record[records.size()]);
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
    public Hosts put(String domain, Record record) {
        put(domain, new Value(record));
        return this;
    }

    public static class Value {
        public final Record record;
        public final int provider;

        public Value(Record record, int provider) {
            this.record = record;
            this.provider = provider;
        }

        public Value(Record record) {
            this(record, NetworkInfo.ISP_GENERAL);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Value)) {
                return false;
            }

            Value another = (Value) o;
            if (record == another.record) {
                return true;
            }

            if (record == null || another.record == null) {
                return false;
            }

            return this.record.value.equals(another.record.value) && this.provider == another.provider;
        }
    }
}
