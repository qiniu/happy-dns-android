package com.qiniu.android.dns;

import android.net.NetworkInfo;

import com.qiniu.android.dns.util.LruCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;


/**
 * Created by bailong on 15/6/12.
 */
public final class DnsManager {

    private int netType = 0;
    private Deque<IResolver> resolvers;
    private LruCache<String, Record[]> cache;
    private volatile boolean disconnected = false;

    public DnsManager(NetworkInfo info, IResolver[] resolvers) {
        netType = info.getSubtype();
        for (IResolver r : resolvers) {
            this.resolvers.add(r);
        }
    }

    private static String[] records2Ip(Record[] records) {
        if (records == null || records.length == 0) {
            return null;
        }
        ArrayList<String> a = new ArrayList<>(records.length);
        for (Record r : records) {
            if (r != null && r.type == Record.TYPE_A) {
                a.add(r.value);
            }
        }
        if (a.size() == 0) {
            return null;
        }
        return a.toArray(new String[a.size()]);
    }

    //    查询域名，返回IP列表
    public String[] query(String domain) {
        return query(new Domain(domain));
    }

//    todo merge requests
    public String[] query(Domain domain) {
        if (disconnected){
            return null;
        }
        Record[] records;
        long now = System.currentTimeMillis();
        synchronized (cache){
            records = cache.get(domain.domain);
            if (records != null && records.length != 0){
                if (records[0].expired() >= now ){
                    return records2Ip(records);
                }
            }
        }

        int len = resolvers.size();
        records = null;
        for (int i = 0; i < len; i++) {
            IResolver r = resolvers.peek();
            try {
                records = r.query(domain);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (records == null || records.length == 0) {
                resolvers.add(resolvers.remove());
            }
        }
        synchronized (cache){
            cache.put(domain.domain, records);
        }
        return records2Ip(records);
    }

    public void onNetworkChange(NetworkInfo info, String deviceIp) {
        clearCache();
        if (info != null){
            netType = info.getSubtype();
            disconnected = false;
        } else {
            disconnected = true;
        }
    }

    private void clearCache() {
        cache.clear();
    }
}
