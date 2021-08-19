package com.qiniu.android.dns.dns;

import com.qiniu.android.dns.Record;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.HttpsURLConnection;

public class DohResolver extends DnsResolver {

    public DohResolver(String server) {
        super(server);
    }

    public DohResolver(String server, int timeout) {
        super(server, timeout);
    }

    public DohResolver(String server, int recordType, int timeout) {
        super(server, recordType, timeout);
    }

    public DohResolver(String[] servers, int recordType, int timeout) {
        super(servers, recordType, timeout);
    }

    public DohResolver(String[] servers, int recordType, int timeout, ExecutorService executorService) {
        super(servers, recordType, timeout, executorService);
    }

    @Override
    DnsResponse request(String server, String host, int recordType) throws IOException {
        double d = Math.random();
        short messageId = (short) (d * 0xFFFF);
        DnsRequest request = new DnsRequest(messageId, recordType, host);
        byte[] requestData = request.toDnsQuestionData();

        HttpsURLConnection httpConn = (HttpsURLConnection) new URL(server).openConnection();
        httpConn.setConnectTimeout(3000);
        httpConn.setReadTimeout(timeout * 1000);
        httpConn.setDoOutput(true);
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("Content-Type", "application/dns-message");
        httpConn.setRequestProperty("Accept", "application/dns-message");
        httpConn.setRequestProperty( "Accept-Encoding", "");

        DataOutputStream bodyStream = new DataOutputStream(httpConn.getOutputStream());
        bodyStream.write(requestData);
        bodyStream.close();

        int responseCode = httpConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return null;
        }

        int length = httpConn.getContentLength();
        if (length <= 0 || length > 1024 * 1024) {
            return null;
        }
        InputStream is = httpConn.getInputStream();
        byte[] responseData = new byte[length];
        int read = is.read(responseData);
        is.close();
        if (read <= 0) {
            return null;
        }

        return new DnsResponse(server, Record.Source.Doh, request, responseData);
    }
}
