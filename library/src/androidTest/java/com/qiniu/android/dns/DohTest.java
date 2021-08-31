package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.dns.DohResolver;

import junit.framework.Assert;

import java.io.IOException;
import java.util.Arrays;

public class DohTest extends AndroidTestCase {

    public void testSimpleDns() {
        String host = "en.wikipedia.org";
        String server = "https://dns.alidns.com/dns-query";
        int[] typeArray = new int[]{Record.TYPE_A, Record.TYPE_AAAA};

        for (int type : typeArray) {
            DohResolver resolver = new DohResolver(server, type, 5);
            try {
                Record[] records = resolver.resolve(new Domain(host), NetworkInfo.normal);
                System.out.println("=== records:" + Arrays.toString(records));
                Assert.assertTrue("records is empty", records != null && records.length > 0);
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail("query records error:" + e);
            }
        }

        for (int type : typeArray) {
            DohResolver resolver = new DohResolver(server, type, 5);
            DnsManager manager = new DnsManager(null, new IResolver[]{resolver});
            try {
                Record[] records = manager.queryRecords(host);
                System.out.println("=== dns manager records:" + Arrays.toString(records));
                Assert.assertTrue(" dns manager records is empty", records != null && records.length > 0);
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail("query dns manager records error:" + e);
            }
        }
    }

    public void testMultiDnsServer() {
        String host = "en.wikipedia.org";
        String[] servers = new String[]{"https://dns.alidns.com/dns-query", "https://dns.google/dns-query"};
        int[] typeArray = new int[]{Record.TYPE_A, Record.TYPE_AAAA};

        for (int type : typeArray) {
            DohResolver resolver = new DohResolver(servers, type, 5);
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
}
