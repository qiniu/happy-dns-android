package com.qiniu.android.dns.local;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by bailong on 15/6/18.
 */
public class Hosts implements IResolver {

    @Override
    public Record[] query(Domain domain) throws IOException {
        ArrayList<Value> vals = hosts.get(domain.domain);
        if(vals == null || vals.isEmpty()){
            return null;
        }
        //todo filter the math network type

        return shuffle(vals);
    }

    public Record[] shuffle(ArrayList<Value> vals){
        int size = vals.size();
        int step = (random.nextInt()&0XFF)%size;

        long timeStamp = System.currentTimeMillis()/1000;
        Record[] r = new Record[size];
        for (int i = 0; i < size; i++) {
            Value v = vals.get((i+step)%size);
            r[i] = new Record(v.ip, v.recordType, v.ttl, timeStamp);
        }
        return r;
    }

    public static class Value {
        public final String ip;
        public final int recordType;
        public final int networkType;
        public final int ttl;

        public Value(String ip, int ttl, int recordType, int networkType) {
            this.ip = ip;
            this.recordType = recordType;
            this.networkType = networkType;
            this.ttl = ttl;
        }

        public Value(String ip) {
            this(ip, 60, Record.TYPE_A, 0);
        }

        public boolean equals(Object o){
            if (this == o){
                return true;
            }
            if (o == null || !(o instanceof Value)){
                return false;
            }
            Value another = (Value)o;
            return this.ip.equals(another.ip)
                    && this.recordType == another.recordType
                    && this.networkType == another.networkType;
        }
    }

    public Hosts put(String domain, Value val){
        ArrayList<Value> vals = hosts.get(domain);
        if(vals == null){
            vals = new ArrayList<>();
        }
        vals.add(val);
        hosts.put(domain, vals);
        return this;
    }

    public Hosts put(String domain, String val){
        put(domain, new Value(val));
        return this;
    }

    private final Hashtable<String, ArrayList<Value>> hosts = new Hashtable<>();
    private final Random random = new Random();
}
