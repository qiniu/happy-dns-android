package com.qiniu.android.dns.http;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;
import com.qiniu.android.dns.util.Hex;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by bailong on 15/7/22.
 */
public final class DnspodEnterprise implements IResolver {
    private final String id;
    private final String ip;
    private final SecretKeySpec key;

    public DnspodEnterprise(String id, String key, String ip) {
        this.id = id;
        this.ip = ip;
        byte[] k = new byte[0];
        try {
            k = key.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        this.key = new SecretKeySpec(k, "DES");
    }

    public DnspodEnterprise(String id, String key) {
        this(id, key, "119.29.29.29");
    }

    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        URI uri = null;
        try {
            uri = new URI("http://" + ip + "/d?ttl=1&dn=" + encrypt(domain.domain)
                    + "&id=" + id);
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
        String result = decrypt(body);
        String[] r1 = result.split(",");
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

    private String encrypt(String domain) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedString = cipher.doFinal(domain.getBytes("utf-8"));
            return Hex.encodeHexString(encryptedString) + "&id=" + id;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String decrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Hex.decodeHex(data.toCharArray()));

            return new String(decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
