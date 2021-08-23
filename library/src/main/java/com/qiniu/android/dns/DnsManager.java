package com.qiniu.android.dns;

import android.os.Build;

import com.qiniu.android.dns.http.DomainNotOwn;
import com.qiniu.android.dns.local.Hosts;
import com.qiniu.android.dns.util.LruCache;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * DNS解析管理类，可以重复使用
 */
public final class DnsManager {

    private final IResolver[] resolvers;
    private final LruCache<String, Record[]> cache;
    private final Hosts hosts = new Hosts();
    private final RecordSorter sorter;
    private volatile NetworkInfo info = null;
    private volatile int index = 0;

    public QueryErrorHandler queryErrorHandler;

    /**
     * @param info      当前的网络信息，从Android context中获取
     * @param resolvers 具体的dns 解析示例，可以是local或者httpdns
     */
    public DnsManager(NetworkInfo info, IResolver[] resolvers) {
        this(info, resolvers, null);
    }

    /**
     * @param info      当前的网络信息，从Android context中获取
     * @param resolvers 具体的dns 解析示例，可以是local或者httpdns
     * @param sorter    外部接口，对获取的IP数组进行排序
     */
    public DnsManager(NetworkInfo info, IResolver[] resolvers, RecordSorter sorter) {
        this.info = info == null ? NetworkInfo.normal : info;
        this.resolvers = resolvers.clone();
        cache = new LruCache<>();
        this.sorter = sorter == null ? new DummySorter() : sorter;
    }

    private static Record[] trimCname(Record[] records) {
        ArrayList<Record> a = new ArrayList<>(records.length);
        for (Record r : records) {
            if (r != null && (r.isA() || r.isAAAA())) {
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

    private static Record[] filterInvalidRecords(Record[] records) {
        if (records == null || records.length == 0) {
            return null;
        }
        ArrayList<Record> ret = new ArrayList<>(records.length);
        for (Record r : records) {
            if (r != null && r.value != null && r.value.length() > 0 && !r.isExpired()) {
                ret.add(r);
            }
        }
        if (ret.size() == 0) {
            return null;
        }
        return ret.toArray(new Record[ret.size()]);
    }

    public static boolean validIP(String ip) {
        if (ip == null || ip.length() < 7 || ip.length() > 15) return false;
        if (ip.contains("-")) return false;

        try {
            int x = 0;
            int y = ip.indexOf('.');

            if (y != -1 && Integer.parseInt(ip.substring(x, y)) > 255) return false;

            x = ip.indexOf('.', ++y);
            if (x != -1 && Integer.parseInt(ip.substring(y, x)) > 255) return false;

            y = ip.indexOf('.', ++x);
            return !(y != -1 && Integer.parseInt(ip.substring(x, y)) > 255 &&
                    Integer.parseInt(ip.substring(++y, ip.length() - 1)) > 255 &&
                    ip.charAt(ip.length() - 1) != '.');

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 简单通过时区判断是否在大陆
    public static boolean needHttpDns() {
        try {
            TimeZone zone = TimeZone.getDefault();
            String id = zone.getID();
            return ("Asia/Shanghai".equals(id) || "Asia/Chongqing".equals(id) ||
                    "Asia/Harbin".equals(id) || "Asia/Urumqi".equals(id));

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 查询域名
     *
     * @param domain 域名参数
     * @return ip 记录 列表
     * @throws IOException 网络异常或者无法解析抛出异常
     */
    public Record[] queryRecords(String domain) throws IOException {
        return queryRecords(new Domain(domain));
    }

    public Record[] queryRecords(Domain domain) throws IOException {
        if (domain == null) {
            throw new IOException("null domain");
        }
        if (domain.domain == null || domain.domain.trim().length() == 0) {
            throw new IOException("empty domain " + domain.domain);
        }

        if (validIP(domain.domain)) {
            Record record = new Record(domain.domain, Record.TYPE_A, Record.TTL_MIN_SECONDS, (new Date()).getTime(), Record.Source.Unknown);
            return new Record[]{record};
        }

        Record[] records = queryRecordInternal(domain);
        return sorter.sort(records);
    }

    private Record[] queryRecordInternal(Domain domain) throws IOException {

        if (domain.hostsFirst) {
            Record[] ret = hosts.query(domain, info);
            ret = filterInvalidRecords(ret);
            if (ret != null && ret.length > 0) {
                return ret;
            }
        }

        synchronized (cache) {
            if (info.equals(NetworkInfo.normal) && Network.isNetworkChanged()) {
                cache.clear();
                synchronized (resolvers) {
                    index = 0;
                }
            } else {
                Record[] records = cache.get(domain.domain);
                records = filterInvalidRecords(records);
                if (records != null && records.length > 0) {
                    return records;
                }
            }
        }

        Record[] records = null;
        IOException lastE = null;
        int firstOk = index;
        for (int i = 0; i < resolvers.length; i++) {
            int pos = (firstOk + i) % resolvers.length;
            NetworkInfo before = info;
            String ip = Network.getIp();
            try {
                records = resolvers[pos].resolve(domain, info);
            } catch (DomainNotOwn e) {
                continue;
            } catch (IOException e) {
                lastE = e;
                e.printStackTrace();
                if (queryErrorHandler != null) {
                    queryErrorHandler.queryError(e, domain.domain);
                }
            } catch (Exception e2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    lastE = new IOException(e2);
                }
                e2.printStackTrace();
                if (queryErrorHandler != null) {
                    queryErrorHandler.queryError(e2, domain.domain);
                }
            }
            String ip2 = Network.getIp();
            if (info == before && (records == null || records.length == 0) && ip.equals(ip2)) {
                synchronized (resolvers) {
                    if (index == firstOk) {
                        index++;
                        if (index == resolvers.length) {
                            index = 0;
                        }
                    }
                }
            } else {
                break;
            }
        }

        if (records == null || records.length == 0) {
            if (!domain.hostsFirst) {
                Record[] rs = hosts.query(domain, info);
                rs = filterInvalidRecords(rs);
                if (rs != null && rs.length != 0) {
                    return rs;
                }
            }
            if (lastE != null) {
                throw lastE;
            }
            IOException e = new UnknownHostException(domain.domain);
            if (queryErrorHandler != null) {
                queryErrorHandler.queryError(e, domain.domain);
            }
            throw e;
        }

        records = trimCname(records);
        if (records.length == 0) {
            throw new UnknownHostException("no A/AAAA records");
        }
        synchronized (cache) {
            cache.put(domain.domain, records);
        }
        return records;
    }

    public InetAddress[] queryInetAdress(Domain domain) throws IOException {
        Record[] records = queryRecords(domain);
        String[] ips = records2Ip(records);
        if (ips == null || ips.length == 0) {
            return null;
        }

        InetAddress[] addresses = new InetAddress[ips.length];
        for (int i = 0; i < ips.length; i++) {
            addresses[i] = InetAddress.getByName(ips[i]);
        }
        return addresses;
    }

    /**
     * 当网络发生变化时，通知当前的网络信息
     *
     * @param info 网络信息
     */
    public void onNetworkChange(NetworkInfo info) {
        clearCache();
        this.info = info == null ? NetworkInfo.normal : info;
        synchronized (resolvers) {
            index = 0;
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
     * @param record   record 记录, source 配置为 @{@link Record.Source#Custom}
     * @param provider 运营商，见 NetworkInfo
     * @return 当前的DnsManager，便于链式调用
     */
    public DnsManager putHosts(String domain, Record record, int provider) {
        hosts.put(domain, new Hosts.Value(record, provider));
        return this;
    }

    /**
     * 插入指定运营商的hosts配置
     *
     * @param domain   域名
     * @param type     ip 类别，{@link Record#TYPE_A} / {@link Record#TYPE_AAAA}
     * @param ip       ip
     * @param provider 运营商，见 NetworkInfo
     * @return 当前的DnsManager，便于链式调用
     */
    public DnsManager putHosts(String domain, int type, String ip, int provider) {
        putHosts(domain, new Record(ip, type, Record.TTL_Forever, new Date().getTime()/1000, Record.Source.Custom), provider);
        return this;
    }

    /**
     * 插入指定运营商的hosts配置
     *
     * @param domain 域名
     * @param type   ip 类别，{@link Record#TYPE_A} / {@link Record#TYPE_AAAA}
     * @param ip     ip
     * @return 当前的DnsManager，便于链式调用
     */
    public DnsManager putHosts(String domain, int type, String ip) {
        putHosts(domain, type, ip, NetworkInfo.ISP_GENERAL);
        return this;
    }

    /**
     * 插入指定运营商的hosts配置
     *
     * @param domain 域名
     * @param ipv4   ipv4 ip
     * @return 当前的DnsManager，便于链式调用
     */
    public DnsManager putHosts(String domain, String ipv4) {
        putHosts(domain, Record.TYPE_A, ipv4);
        return this;
    }

    private static class DummySorter implements RecordSorter {
        private AtomicInteger pos = new AtomicInteger();

        @Override
        public Record[] sort(Record[] records) {
            return records;
        }
    }


    public interface QueryErrorHandler {
        void queryError(Exception e, String host);
    }
}
