package com.qiniu.android.dns.http;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by bailong on 15/6/12.
 */
public final class DnspodFree implements IResolver {
    private final String ip;

    public DnspodFree(String ip) {
        this.ip = ip;
    }

    public DnspodFree() {
        this("119.29.29.29");
    }

    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        URI uri = null;
        try {
            uri = new URI("http://" + ip + "/d?ttl=1&dn=" + domain.domain);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        request.setURI(uri);
        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode != 200) {
            return null;
        }
        HttpEntity entity = response.getEntity();
        long length = entity.getContentLength();
        if (length > 1024 || length == 0) {
            return null;
        }
        InputStream is = entity.getContent();
        byte[] data = new byte[(int) length];
        int read = 0;
        try {
            read = is.read(data);
        } catch (IOException e) {
            throw e;
        } finally {
            is.close();
        }
        String body = new String(data, 0, read);
        String[] r1 = body.split(",");
        if (r1.length != 2) {
            return null;
        }
        int ttl;
        try {
            ttl = Integer.parseInt(r1[1]);
        } catch (Exception e) {
            return null;
        }
        String[] ips = r1[0].split(";");
        if (ips.length == 0) {
            return null;
        }
        Record[] records = new Record[ips.length];
        long time = System.currentTimeMillis() / 1000;
        for (int i = 0; i < ips.length; i++) {
            records[i] = new Record(ips[i], Record.TYPE_A, ttl, time);
        }
        return records;
    }
}
