package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.dns.DnsResponse;
import com.qiniu.android.dns.dns.DnsUdpResolver;
import com.qiniu.android.dns.dns.DohResolver;

import junit.framework.Assert;

import java.io.IOException;
import java.util.Arrays;

public class UdpResolverTest extends AndroidTestCase {
    public void testSimpleDns() {
        String host = "qiniu.com";
        String server = "114.114.114.114";
        int[] typeArray = new int[]{Record.TYPE_A, Record.TYPE_CNAME, Record.TYPE_AAAA, Record.TYPE_TXT};

        for (int type : typeArray) {
            DnsUdpResolver resolver = new DnsUdpResolver(server, type, 5);
            try {
                DnsResponse response = resolver.lookupHost(host);
                System.out.println("=== response:" + response);
                Assert.assertTrue("response is null", response != null);
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail("testSimpleDns error" + e);
            }
        }


        DnsUdpResolver resolver = new DnsUdpResolver(server);
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
        String[] servers = new String[]{"114.114.114.114", "8.8.8.8"};
        int[] typeArray = new int[]{Record.TYPE_A, Record.TYPE_CNAME, Record.TYPE_AAAA, Record.TYPE_TXT};

        for (int type : typeArray) {
            DnsUdpResolver resolver = new DnsUdpResolver(servers, type, 5);
            try {
                DnsResponse response = resolver.lookupHost(host);
                System.out.println("=== response:" + response);
                Assert.assertTrue("response is null", response != null);
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail("testSimpleDns error" + e);
            }
        }


        DnsUdpResolver resolver = new DnsUdpResolver(servers, Record.TYPE_A, 5);
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
