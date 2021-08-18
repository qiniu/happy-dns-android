package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.dns.DnsResponse;
import com.qiniu.android.dns.dns.DohResolver;

import junit.framework.Assert;

import java.io.IOException;

public class DohTest extends AndroidTestCase {

    public void testA() {
        String host = "upload.qiniup.com";
        String server = "https://dns.alidns.com/dns-query";
        DohResolver resolver = new DohResolver(server, Record.TYPE_A, 5);
        try {
            DnsResponse response = resolver.lookupHost(host);
            Assert.assertTrue("response is null", response != null);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("testSimpleDns error" + e);
        }
    }

    public void testSimpleDns() {
        String host = "qiniu.com";
        String server = "https://dns.alidns.com/dns-query";
        int[] typeArray = new int[]{Record.TYPE_A, Record.TYPE_CNAME, Record.TYPE_AAAA, Record.TYPE_TXT};

        for (int type : typeArray) {
            DohResolver resolver = new DohResolver(server, type, 5);
            try {
                DnsResponse response = resolver.lookupHost(host);
                Assert.assertTrue("response is null", response != null);
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail("testSimpleDns error" + e);
            }
        }


        DohResolver resolver = new DohResolver(server);
        try {
            Record[] records = resolver.resolve(new Domain(host), NetworkInfo.normal);
            Assert.assertTrue("records is empty", records != null && records.length > 0);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("query records error:" + e);
        }
    }
}
