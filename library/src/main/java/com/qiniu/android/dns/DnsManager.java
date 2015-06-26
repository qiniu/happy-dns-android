package com.qiniu.android.dns;

import com.qiniu.android.dns.util.LruCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;


/**
 * Created by bailong on 15/6/12.
 */
public final class DnsManager {

    private NetworkInfo info = null;
    private Deque<IResolver> resolvers;
    private LruCache<String, Record[]> cache;

    public DnsManager(NetworkInfo info, IResolver[] resolvers) {
        this.info = info == null? NetworkInfo.normal():info;
        this.resolvers = new LinkedList<>();
        for (IResolver r : resolvers) {
            this.resolvers.add(r);
        }
        cache = new LruCache<>();
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
//        if (info.netStatus == NetworkInfo.NO_NETWORK){
//            return null;
//        }
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
                records = r.query(domain, info);
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

    public void onNetworkChange(NetworkInfo info){
        clearCache();
        this.info = info == null? NetworkInfo.normal():info;
    }

    private void clearCache() {
        cache.clear();
    }
}
