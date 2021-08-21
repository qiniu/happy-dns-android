package com.qiniu.android.dns.http;

import android.annotation.SuppressLint;
import android.util.Base64;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;
import com.qiniu.android.dns.local.DnsMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SuppressLint("unused")
public class DohDns implements IResolver {
    private static final Random random = new Random();
    private final int timeout;
    private final String url;
    private boolean useIpv6;

    public DohDns() {
        this("https://dns.alidns.com/dns-query", DNS_DEFAULT_TIMEOUT, false);
    }

    public DohDns(String url) {
        this(url, DNS_DEFAULT_TIMEOUT, false);
    }

    public DohDns(String url, int timeout) {
        this(url, DNS_DEFAULT_TIMEOUT, false);
    }

    public DohDns(String url, int timeout, boolean useIpv6) {
        this.url = url;
        this.timeout = timeout;
        this.useIpv6 = useIpv6;
    }

    /**
     * set use ipv6
     *
     * @param useIpv6 true ues ipv6 false will not use.
     */

    public DohDns setUseIpv6(boolean useIpv6) {
        this.useIpv6 = useIpv6;
        return this;
    }

    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        List<Record> records = new ArrayList<>();
        if (useIpv6) {
            records.addAll(lookup(domain.domain, true));
        }

        records.addAll(lookup(domain.domain, false));
        return records.toArray(new Record[0]);
    }

    private List<Record> lookup(String domain, boolean ipv6) throws IOException {
        List<Record> records = new ArrayList<>();
        int id;
        int type = ipv6 ? Record.TYPE_AAAA : Record.TYPE_A;
        synchronized (random) {
            id = random.nextInt() & 0XFF;
        }
        String msg = Base64.encodeToString(DnsMessage.buildQuery(domain, type, id), Base64.NO_PADDING);
        URL u = new URL(url + "?dns=" + msg);
        //Log.e("DOH", msg);
        HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
        httpConn.setRequestProperty("Content-Type", "application/dns-message");
        httpConn.setRequestMethod("GET");
        httpConn.setConnectTimeout(3000);
        httpConn.setReadTimeout(timeout * 1000);
        int responseCode = httpConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return records;
        }
        int length = httpConn.getContentLength();
        if (length <= 0 || length > 1024) {
            return records;
        }
        InputStream is = httpConn.getInputStream();

        byte[] data = new byte[length];
        int read = is.read(data);
        is.close();
        if (read <= 0) {
            return records;
        }
        records.addAll(Arrays.asList(DnsMessage.parseResponse(data, id, domain)));
        return records;
    }


}
