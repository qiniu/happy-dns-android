package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.dns.DnsResponse;
import com.qiniu.android.dns.dns.DohResolver;

import junit.framework.Assert;

import java.io.IOException;
import java.util.Arrays;

public class DohTest extends AndroidTestCase {

    public void testSimpleDns() {
        String host = "qiniu.com";
        String server = "https://dns.alidns.com/dns-query";
        int[] typeArray = new int[]{Record.TYPE_A, Record.TYPE_CNAME, Record.TYPE_AAAA, Record.TYPE_TXT};

        for (int type : typeArray) {
            DohResolver resolver = new DohResolver(server, type, 5);
            try {
                DnsResponse response = resolver.lookupHost(host);
                System.out.println("=== response:" + response);
                Assert.assertTrue("response is null", response != null);
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail("testSimpleDns error" + e);
            }
        }


        DohResolver resolver = new DohResolver(server);
        try {
            Record[] records = resolver.resolve(new Domain(host), NetworkInfo.normal);
            System.out.println("=== records:" + Arrays.toString(records));
            Assert.assertTrue("records is empty", records != null && records.length > 0);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("query records error:" + e);
        }
    }

    public void testMultiDnsServer() {
        String host = "qiniu.com";
        String[] servers = new String[]{"https://dns.alidns.com/dns-query", "8.8.8.8"};
        int[] typeArray = new int[]{Record.TYPE_A, Record.TYPE_CNAME, Record.TYPE_AAAA, Record.TYPE_TXT};

        for (int type : typeArray) {
            DohResolver resolver = new DohResolver(servers, type, 5);
            try {
                DnsResponse response = resolver.lookupHost(host);
                System.out.println("=== response:" + response);
                Assert.assertTrue("response is null", response != null);
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail("testSimpleDns error" + e);
            }
        }


        DohResolver resolver = new DohResolver(servers, Record.TYPE_A, 5);
        try {
            Record[] records = resolver.resolve(new Domain(host), NetworkInfo.normal);
            System.out.println("=== records:" + Arrays.toString(records));
            Assert.assertTrue("records is empty", records != null && records.length > 0);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("query records error:" + e);
        }
    }
}
