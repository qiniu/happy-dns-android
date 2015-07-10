package com.qiniu.android.dns.local;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * reference github/rtreffer/minidns.
 */
public final class AndroidDnsServer {
    //    15ms
    public static InetAddress[] getByCommand() {
        try {
            Process process = Runtime.getRuntime().exec("getprop");
            InputStream inputStream = process.getInputStream();
            LineNumberReader lnr = new LineNumberReader(
                    new InputStreamReader(inputStream));
            String line = null;
            ArrayList<InetAddress> servers = new ArrayList<InetAddress>(5);
            while ((line = lnr.readLine()) != null) {
                int split = line.indexOf("]: [");
                if (split == -1) {
                    continue;
                }
                String property = line.substring(1, split);
                String value = line.substring(split + 4, line.length() - 1);
                if (property.endsWith(".dns") || property.endsWith(".dns1") ||
                        property.endsWith(".dns2") || property.endsWith(".dns3") ||
                        property.endsWith(".dns4")) {

                    // normalize the address

                    InetAddress ip = InetAddress.getByName(value);

                    if (ip == null) continue;

                    value = ip.getHostAddress();

                    if (value == null) continue;
                    if (value.length() == 0) continue;

                    servers.add(ip);
                }
            }
            if (servers.size() > 0) {
                return servers.toArray(new InetAddress[servers.size()]);
            }
        } catch (IOException e) {
            Logger.getLogger("AndroidDnsServer").log(Level.WARNING, "Exception in findDNSByExec", e);
        }
        return null;
    }

    // 1ms
    public static InetAddress[] getByReflection() {
        try {
            Class<?> SystemProperties =
                    Class.forName("android.os.SystemProperties");
            Method method = SystemProperties.getMethod("get",
                    new Class<?>[]{String.class});

            ArrayList<InetAddress> servers = new ArrayList<InetAddress>(5);

            for (String propKey : new String[]{
                    "net.dns1", "net.dns2", "net.dns3", "net.dns4"}) {

                String value = (String) method.invoke(null, propKey);

                if (value == null) continue;
                if (value.length() == 0) continue;

                InetAddress ip = InetAddress.getByName(value);

                if (ip == null) continue;

                value = ip.getHostAddress();

                if (value == null) continue;
                if (value.length() == 0) continue;
                if (servers.contains(ip)) continue;

                servers.add(ip);
            }

            if (servers.size() > 0) {
                return servers.toArray(new InetAddress[servers.size()]);
            }
        } catch (Exception e) {
            // we might trigger some problems this way
            Logger.getLogger("AndroidDnsServer").log(Level.WARNING, "Exception in findDNSByReflection", e);
        }
        return null;
    }

    public static IResolver defaultResolver() {
//        the system dns ip would change after network changed.
        return new IResolver() {
            @Override
            public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
                InetAddress[] addresses = getByReflection();
                if (addresses == null) {
                    addresses = getByCommand();
                }
                if (addresses == null) {
                    throw new IOException("cant get local dns server");
                }
                IResolver resolver = new HijackingDetectWrapper(new Resolver(addresses[0]));
                Record[] records = resolver.resolve(domain, info);
                if (domain.hasCname) {
                    boolean cname = false;
                    for (Record r : records) {
                        if (r.isCname()) {
                            cname = true;
                            break;
                        }
                    }
                    if (!cname) {
                        throw new DnshijackingException(domain.domain,
                                addresses[0].getHostAddress());
                    }
                }
                if (domain.maxTtl != 0) {
                    for (Record r : records) {
                        if (!r.isCname()) {
                            if (r.ttl > domain.maxTtl) {
                                throw new DnshijackingException(domain.domain,
                                        addresses[0].getHostAddress(), r.ttl);
                            }
                        }
                    }
                }
                return records;
            }
        };
    }
}
