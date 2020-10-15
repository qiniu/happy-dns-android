package com.qiniu.android.dns.local;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;

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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * reference github/rtreffer/minidns.
 */
public final class AndroidDnsServer {

    static class AndroidResolver implements IResolver {

        private List<InetAddress> dnsServers = new ArrayList<>();
        private boolean networkCallback;

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        AndroidResolver(Context context){
            networkCallback = false;

            List<InetAddress> addresses = getByReflection();
            if (addresses == null) {
                addresses = getByCommand();
            }

            if(addresses == null){
                ///Android 8 , net.dns* was disabled, query dns servers must use network callback
                ///@see https://developer.android.com/about/versions/oreo/android-8.0-changes.html
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ConnectivityManager connectivityManager =
                            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                    NetworkRequest.Builder builder = new NetworkRequest.Builder();

                    connectivityManager.registerNetworkCallback(builder.build(),
                            new ConnectivityManager.NetworkCallback(){

                                @Override
                                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {

                                    if(linkProperties != null)
                                        dnsServers.addAll(linkProperties.getDnsServers());

                                    networkCallback = true;
                                }

                            });
                }
            }
            else {
                dnsServers.addAll(addresses);
            }


        }

        @Override
        public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
            if(dnsServers.isEmpty() && !networkCallback) {
                ///Network callback may delay, wait for 1000 ms
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (dnsServers == null || dnsServers.isEmpty()) {
                throw new IOException("cant get local dns server");
            }

            InetAddress dnsServer  = dnsServers.get(0);

            IResolver resolver = new HijackingDetectWrapper(new Resolver(dnsServer));
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
                            dnsServer.getHostAddress());
                }
            }
            if (domain.maxTtl != 0) {
                for (Record r : records) {
                    if (!r.isCname()) {
                        if (r.ttl > domain.maxTtl) {
                            throw new DnshijackingException(domain.domain,
                                    dnsServer.getHostAddress(), r.ttl);
                        }
                    }
                }
            }
            return records;
        }
    };

    //    15ms
    public static List<InetAddress> getByCommand() {
        try {
            Process process = Runtime.getRuntime().exec("getprop");
            InputStream inputStream = process.getInputStream();
            LineNumberReader lnr = new LineNumberReader(
                    new InputStreamReader(inputStream));
            String line = null;
            ArrayList<InetAddress> servers = new ArrayList<InetAddress>(5);
            while ((line = lnr.readLine()) != null) {
                int split = line.indexOf("]: [");
                if (split <= 1 || line.length() - 1 <= split + 4) {
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
                return servers;
            }
        } catch (IOException e) {
            Logger.getLogger("AndroidDnsServer").log(Level.WARNING, "Exception in findDNSByExec", e);
        }
        return null;
    }

    // 1ms
    public static List<InetAddress> getByReflection() {
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
                return servers;
            }
        } catch (Exception e) {
            // we might trigger some problems this way
            Logger.getLogger("AndroidDnsServer").log(Level.WARNING, "Exception in findDNSByReflection", e);
        }
        return null;
    }

    public static IResolver defaultResolver(Context context) {
//        the system dns ip would change after network changed.
        return new AndroidResolver(context);

      /*
        return new IResolver() {
            @Override
            public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
                List<InetAddress> addresses = getByReflection();
                if (addresses == null) {
                    addresses = getByCommand();
                }
                if (addresses == null) {
                    throw new IOException("cant get local dns server");
                }
                IResolver resolver = new HijackingDetectWrapper(new Resolver(addresses.get(0)));
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
        */
    }
}
