package com.qiniu.android.dns.http;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.Record;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bailong on 15/6/12.
 */
public final class DnspodFree implements IResolver {
    @Override
    public Record[] query(Domain domain) throws IOException {
        URL url = new URL("http://119.29.29.29/d?ttl=1&dn=" + domain.domain);
        HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
        int responseCode = httpConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK){
            return null;
        }
        int length = httpConn.getContentLength();
        if (length > 1024 || length == 0){
            return null;
        }
        InputStream is = httpConn.getInputStream();
        byte[] data = new byte[length];
        int read =  is.read(data);
        is.close();
        String response = new String(data,0, read);
        String[] r1 = response.split(",");
        if (r1.length != 2){
            return null;
        }
        int ttl;
        try {
            ttl = Integer.parseInt(r1[1]);
        } catch (Exception e){
            return null;
        }
        String[] ips = r1[0].split(";");
        if (ips.length == 0){
            return null;
        }
        Record[] records = new Record[ips.length];
        long time = System.currentTimeMillis()/1000;
        for (int i = 0; i < ips.length; i++) {
            records[i] = new Record(ips[i], Record.TYPE_A, ttl, time);
        }
        return records;
    }

//// //设置连接属性
//    httpConn.setDoOutput(true);// 使用 URL 连接进行输出
//    httpConn.setDoInput(true);// 使用 URL 连接进行输入
//    httpConn.setUseCaches(false);// 忽略缓存
//    httpConn.setRequestMethod("POST");// 设置URL请求方法
//    String requestString = "客服端要以以流方式发送到服务端的数据...";
//
//
//    // 设置请求属性
//// 获得数据字节数据，请求数据流的编码，必须和下面服务器端处理请求流的编码一致
//    byte[] requestStringBytes = requestString.getBytes(ENCODING_UTF_8);
//    httpConn.setRequestProperty("Content-length", "" + requestStringBytes.length);
//    httpConn.setRequestProperty("Content-Type", "application/octet-stream");
//    httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
//    httpConn.setRequestProperty("Charset", "UTF-8");
//    //
//    String name = URLEncoder.encode("黄武艺", "utf-8");
//    httpConn.setRequestProperty("NAME", name);
//
//
//    // 建立输出流，并写入数据
//    OutputStream outputStream = httpConn.getOutputStream();
//    outputStream.write(requestStringBytes);
//    outputStream.close();
//    // 获得响应状态
//    int responseCode = httpConn.getResponseCode();
//
//
//    if (HttpURLConnection.HTTP_OK == responseCode) {// 连接成功
//// 当正确响应时处理数据
//        StringBuffer sb = new StringBuffer();
//        String readLine;
//        BufferedReader responseReader;
//// 处理响应流，必须与服务器响应流输出的编码一致
//        responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), ENCODING_UTF_8));
//        while ((readLine = responseReader.readLine()) != null) {
//            sb.append(readLine).append("\n");
//        }
//        responseReader.close();
//        tv.setText(sb.toString());
//    }
//} catch (Exception ex) {
//        ex.printStackTrace();
//        }
//
//
//        }
        }
