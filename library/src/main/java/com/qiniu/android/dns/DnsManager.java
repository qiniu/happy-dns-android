package com.qiniu.android.dns;

import com.qiniu.android.dns.local.Hosts;
import com.qiniu.android.dns.util.BitSet;
import com.qiniu.android.dns.util.LruCache;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by bailong on 15/6/12.
 */
public final class DnsManager {

    private volatile NetworkInfo info = null;
    private final IResolver[] resolvers;
    private final LruCache<String, Record[]> cache;
    private final BitSet resolversStatus = new BitSet();
    private final Hosts hosts = new Hosts();

    public DnsManager(NetworkInfo info, IResolver[] resolvers) {
        this.info = info == null ? NetworkInfo.normal() : info;
        this.resolvers = resolvers.clone();
        cache = new LruCache<>();
    }

    private static Record[] trimCname(Record[] records) {
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
        if (domain.hostsFirst){
            String[] ret = hosts.query(domain, info);
            if (ret != null && ret.length != 0){
                return ret;
            }
        }
        long now = System.currentTimeMillis();
        synchronized (cache) {
            records = cache.get(domain.domain);
            if (records != null && records.length != 0) {
                if (records[0].isExpired(now)) {
                    return records2Ip(records);
                }
            }
        }

        int firstOk;
        synchronized (resolversStatus) {
            firstOk = 32 - resolversStatus.leadingZeros();
        }

        for (int i = 0; i < resolvers.length; i++) {
            int pos = (firstOk + i) % resolvers.length;
            NetworkInfo before = info;
            try {
                records = resolvers[pos].query(domain, info);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (info == before && (records == null || records.length == 0)) {
                synchronized (resolversStatus) {
                    resolversStatus.set(pos);
                }
            } else {
                break;
            }
        }

        if (records == null || records.length == 0) {
            if (!domain.hostsFirst){
                return hosts.query(domain, info);
            }
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
        synchronized (resolversStatus) {
            resolversStatus.clear();
        }
    }

    private void clearCache() {
        synchronized (cache) {
            cache.clear();
        }
    }

    public DnsManager putHosts(String domain, String ip, int provider) {
        hosts.put(domain, new Hosts.Value(ip, provider));
        return this;
    }

    public DnsManager putHosts(String domain, String ip) {
        hosts.put(domain, ip);
        return this;
    }
}
