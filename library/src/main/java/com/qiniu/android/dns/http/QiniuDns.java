package com.qiniu.android.dns.http;

import com.qiniu.android.dns.DnsException;
import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;
import com.qiniu.android.dns.util.DES;
import com.qiniu.android.dns.util.MD5;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class QiniuDns implements IResolver{
    private static final String ENDPOINT_SSL = "https://httpdns.qnydns.net:18443/";
    private static final String ENDPOINT = "http://httpdns.qnydns.net:18302/";


    private static String mAccountId;
    private static String mEncryptKey;
    private static int mExpireTimeSecond = 0;
    private static boolean mIsEncrypted = true;
    private static boolean mIsHttps = true;

    public QiniuDns(String accountId, String encryptKey, int expireTimeSecond) {
        mAccountId = accountId;
        mEncryptKey = encryptKey;
        mExpireTimeSecond = expireTimeSecond;
    }

    public void setEncrypted(boolean encrypted) {
        mIsEncrypted = encrypted;
    }

    public void setHttps(boolean https) {
        mIsHttps = https;
    }

    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        if (mAccountId == null || mEncryptKey == null) {
            throw new DnsException(domain.domain, "Invalid account id or encrypt key");
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(mIsHttps ? ENDPOINT_SSL : ENDPOINT + mAccountId
                + "/d?dn=" + (mIsEncrypted ? DES.encrypt(domain.domain, mEncryptKey) : domain.domain)
                + "&e=" + Integer.toString(mExpireTimeSecond)
                + "&s=" + MD5.encrypt(domain.domain + "-" + mEncryptKey + "-" + mExpireTimeSecond)
                + "&ttl=1" + "&echo=1").openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(10000);
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        try {
            JSONArray result = mIsEncrypted ?
                    new JSONArray(DES.decrypt(new JSONObject(sb.toString()).optString("data"),
                            mEncryptKey)).optJSONArray(0) :
                    new JSONObject(sb.toString()).optJSONArray("data").optJSONArray(0);
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
