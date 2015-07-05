package com.qiniu.android.dns;

import com.qiniu.android.dns.local.Hosts;
import com.qiniu.android.dns.util.BitSet;
import com.qiniu.android.dns.util.LruCache;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;


/**
 * DNS解析管理类，可以重复使用
 */
public final class DnsManager {

    private final IResolver[] resolvers;
    private final LruCache<String, Record[]> cache;
    private final BitSet resolversStatus = new BitSet();
    private final Hosts hosts = new Hosts();
    private volatile NetworkInfo info = null;

    /**
     * @param info      当前的网络信息，从Android context中获取
     * @param resolvers 具体的dns 解析示例，可以是local或者httpdns
     */
    public DnsManager(NetworkInfo info, IResolver[] resolvers) {
        this.info = info == null ? NetworkInfo.normal : info;
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

    /**
     * 查询域名
     *
     * @param domain 域名
     * @return ip 列表
     */
    public String[] query(String domain) {
        return query(new Domain(domain));
    }

    /**
     * 查询域名
     *
     * @param domain 域名参数
     * @return ip 列表
     */
    public String[] query(Domain domain) {
//        有些手机网络状态可能不对
//        if (info.netStatus == NetworkInfo.NO_NETWORK){
//            return null;
//        }
        Record[] records = null;
        if (domain.hostsFirst) {
            String[] ret = hosts.query(domain, info);
            if (ret != null && ret.length != 0) {
                return ret;
            }
        }
        long now = System.currentTimeMillis();
        synchronized (cache) {
            if (info.equals(NetworkInfo.normal) && Network.isNetworkChanged()) {
                cache.clear();
                synchronized (resolversStatus) {
                    resolversStatus.clear();
                }
            } else {
                records = cache.get(domain.domain);
                if (records != null && records.length != 0) {
                    if (records[0].isExpired(now)) {
                        return records2Ip(records);
                    }
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
            String ip = Network.getIp();
            try {
                records = resolvers[pos].query(domain, info);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String ip2 = Network.getIp();
            if (info == before && (records == null || records.length == 0) && ip.equals(ip2)) {
                synchronized (resolversStatus) {
                    resolversStatus.set(pos);
                }
            } else {
                break;
            }
        }

        if (records == null || records.length == 0) {
            if (!domain.hostsFirst) {
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

    /**
     * 当网络发生变化时，通知当前的网络信息
     *
     * @param info 网络信息
     */
    public void onNetworkChange(NetworkInfo info) {
        clearCache();
        this.info = info == null ? NetworkInfo.normal : info;
        synchronized (resolversStatus) {
            resolversStatus.clear();
        }
    }

    private void clearCache() {
        synchronized (cache) {
            cache.clear();
        }
    }

    /**
     * 插入指定运营商的hosts配置
     *
     * @param domain   域名
     * @param ip       ip
     * @param provider 运营商，见 NetworkInfo
     * @return 当前的Dnsmanager，便于链式调用
     */
    public DnsManager putHosts(String domain, String ip, int provider) {
        hosts.put(domain, new Hosts.Value(ip, provider));
        return this;
    }

    /**
     * 插入指定运营商的hosts配置
     *
     * @param domain 域名
     * @param ip     ip
     * @return 当前的Dnsmanager，便于链式调用
     */
    public DnsManager putHosts(String domain, String ip) {
        hosts.put(domain, ip);
        return this;
    }

    private String[] systemResolv(String domain){
        try {
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            String[] x = new String[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                x[i] = addresses[i].getHostAddress();
            }
            return x;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
