package com.qiniu.android.dns;

import com.qiniu.android.dns.util.BitSet;
import com.qiniu.android.dns.util.LruCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;


/**
 * Created by bailong on 15/6/12.
 */
public final class DnsManager {

    private NetworkInfo info = null;
    private final ArrayList<IResolver> resolvers = new ArrayList<>();
    private final LruCache<String, Record[]> cache;
    private final BitSet resolversStatus = new BitSet();

    public DnsManager(NetworkInfo info, IResolver[] resolvers) {
        this.info = info == null ? NetworkInfo.normal() : info;
        Collections.addAll(this.resolvers, resolvers);
        cache = new LruCache<>();
    }

    private static Record[] trimCname(Record[] records){
        ArrayList<Record> a = new ArrayList<>(records.length);
        for (Record r : records) {
            if (r != null && r.type == Record.TYPE_A) {
                a.add(r);
            }
        }
        return a.toArray(new Record[a.size()]);
    }

    private static String[] records2Ip(Record[] records) {
        if (records == null || records.length == 0) {
            return null;
        }
        ArrayList<String> a = new ArrayList<>(records.length);
        for (Record r : records) {
            a.add(r.value);
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
//        有些手机网络状态可能不对
//        if (info.netStatus == NetworkInfo.NO_NETWORK){
//            return null;
//        }
        Record[] records;
        long now = System.currentTimeMillis();
        synchronized (cache) {
            records = cache.get(domain.domain);
            if (records != null && records.length != 0) {
                if (records[0].isExpired(now)) {
                    return records2Ip(records);
                }
            }
        }

        int len = resolvers.size();
        records = null;
        LinkedList<IResolver> l = view();
        for (int i = 0; i < len; i++) {
            IResolver r = l.get(i);
            try {
                records = r.query(domain, info);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (records == null || records.length == 0) {
                synchronized (resolversStatus) {
                    resolversStatus.set(i);
                }
            }
        }
        if (records == null){
            return null;
        }
        records = trimCname(records);
        synchronized (cache) {
            cache.put(domain.domain, records);
        }
        return records2Ip(records);
    }

    public void onNetworkChange(NetworkInfo info) {
        clearCache();
        this.info = info == null ? NetworkInfo.normal() : info;
        synchronized (resolversStatus){
            resolversStatus.clear();
        }
    }

    private void clearCache() {
        synchronized (cache){
            cache.clear();
        }
    }

    private LinkedList<IResolver> view() {
        LinkedList<IResolver> v = new LinkedList<>();
        synchronized (resolversStatus) {
            for (int i = 0; i < resolvers.size(); i++) {
                if (resolversStatus.isSet(i)) {
                    v.addLast(resolvers.get(i));
                } else {
                    v.addFirst(resolvers.get(i));
                }
            }
        }
        return v;
    }
}
