package com.qiniu.android.dns.http;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;
import com.qiniu.android.dns.util.DES;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class QiniuDns implements IResolver{
    private static final String ENDPOINT = "https://httpdns.qnydns.net:18443/";

    private static String mAccountId;
    private static String mEncryptKey;
    private static long mExpireTimeMs = 0;

    public QiniuDns(String accountId, String encryptKey, long expireTimeMs) {
        mAccountId = accountId;
        mEncryptKey = encryptKey;
        mExpireTimeMs = expireTimeMs;
    }

    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(ENDPOINT + mAccountId
                + "/d?dn=" + (mEncryptKey == null ? domain.domain
                    : DES.encrypt( domain.domain
                        + "?e=" + Long.toString((System.currentTimeMillis()
                        + mExpireTimeMs) / 1000), mEncryptKey))
                + "&ttl=1" + "&echo=1").openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(10000);
        if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        try {
            JSONArray result = mEncryptKey == null ?
                    new JSONObject(sb.toString()).optJSONArray("data").optJSONArray(0) :
                    new JSONArray(DES.decrypt(new JSONObject(sb.toString()).optString("data"),
                            mEncryptKey)).optJSONArray(0);
            if (result.length() <= 0) {
                return null;
            }
            Record[] records = new Record[result.length()];
            for (int i = 0; i < records.length;++i) {
                JSONObject item = result.optJSONObject(i);
                records[i] = new Record(item.optString("data"), Record.TYPE_A,
                        item.optInt("TTL"), System.currentTimeMillis() / 1000);
            }
            return records;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
