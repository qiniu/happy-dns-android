package com.qiniu.android.dns.http;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * DoH JSON
 */
@SuppressWarnings("unused")
public final class DohJson implements IResolver {
    /**
     * doh json 的地址
     */
    private final String url;
    /**
     * 超时
     */
    private final int timeout;
    /**
     * 是否使用ipv6
     */
    private boolean useIpv6;

    public DohJson() {
        this.url = "https://dnh.alidns.com/resolve";
        this.timeout = DNS_DEFAULT_TIMEOUT;
        this.useIpv6 = false;
    }

    public DohJson(String url) {
        this.url = url;
        this.timeout = DNS_DEFAULT_TIMEOUT;
        this.useIpv6 = false;

    }

    public DohJson(String url, int timeout) {
        this.url = url;
        this.timeout = timeout;
        this.useIpv6 = false;

    }

    public DohJson(String url, int timeout, boolean useIpv6) {
        this.url = url;
        this.timeout = timeout;
        this.useIpv6 = true;

    }

    public DohJson setUseIpv6(boolean useIpv6) {
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
        if (records.size() == 0) {
            return null;
        }
        return records.toArray(new Record[0]);
    }

    /**
     * DohJson 查询dns
     *
     * @param domain 域名
     * @param ipv6   是否使用ipv6
     * @return 返回Record列表
     * @throws IOException 当出错时抛出
     */

    private List<Record> lookup(String domain, boolean ipv6) throws IOException {
        List<Record> records = new ArrayList<>();
        //A 类型的值为1，AAAA 类型的值为28
        int type = ipv6 ? Record.TYPE_AAAA : Record.TYPE_A;
        URL u = new URL(url + "?name=" + domain + "&type=" + type);
        HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
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
        String response = new String(data, 0, read);
        long time = System.currentTimeMillis() / 1000;
        try {
            JSONArray ja = new JSONObject(response).getJSONArray("Answer");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject item = ja.getJSONObject(i);
                Record record = new Record(item.getString("data"), item.getInt("type"), item.getInt("TTL"), time, Record.Source.DohJson);
                records.add(record);
            }

        } catch (JSONException e) {
            return records;
        }
        return records;
    }
}
