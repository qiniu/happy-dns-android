package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.dns.DnsUdpResolver;

import junit.framework.Assert;

import java.io.IOException;
import java.util.Arrays;

public class UdpResolverTest extends AndroidTestCase {
    public void testSimpleDns() {
        String host = "en.wikipedia.org";
        String server = "114.114.114.114";
        int[] typeArray = new int[]{Record.TYPE_A, Record.TYPE_AAAA};

        for (int type : typeArray) {
            DnsUdpResolver resolver = new DnsUdpResolver(server, type, 5);
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
            DnsUdpResolver resolver = new DnsUdpResolver(server, type, 5);
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
        String[] servers = new String[]{"114.114.114.114", "8.8.8.8"};
        int[] typeArray = new int[]{Record.TYPE_A, Record.TYPE_AAAA};

        for (int type : typeArray) {
            DnsUdpResolver resolver = new DnsUdpResolver(servers, type, 5);
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
