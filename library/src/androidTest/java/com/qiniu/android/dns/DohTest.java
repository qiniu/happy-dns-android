package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.http.DohJson;
import com.qiniu.android.dns.http.DohDns;

import junit.framework.Assert;

import java.io.IOException;


public class DohTest extends AndroidTestCase {
    public void testDohJsonFound() throws IOException {
        DohJson resolver = new DohJson();
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"), null);
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length > 0);
            for (Record r : records) {
                Assert.assertTrue(r.ttl >= 600);
                Assert.assertTrue(r.isA()||r.isAAAA());
                Assert.assertFalse(r.isExpired());
            }

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
    public void testDohJsonIpv6() throws IOException {
        DohJson resolver = new DohJson("https://dns.alidns.com/resolve",10,true);
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"), null);
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length > 0);
            for (Record r : records) {
                Assert.assertTrue(r.ttl >= 600);
                Assert.assertTrue(r.isA());
                Assert.assertFalse(r.isExpired());
            }

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testDohJsonNotFound() throws IOException {
        DohJson resolver = new DohJson();
        try {
            Record[] records = resolver.resolve(new Domain("7777777.qiniu.com"), null);
            Assert.assertNull(records);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testDohJsonTimeout() throws IOException {
        DohJson resolver = new DohJson("https://www.google.com", 5);
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"), null);
            Assert.fail("no timeout");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testDohDnsFound() throws IOException {
        DohDns resolver = new DohDns();
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"), null);
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length > 0);
            for (Record r : records) {
                Assert.assertTrue(r.ttl >= 600);
                Assert.assertTrue(r.isA());
                Assert.assertFalse(r.isExpired());
            }

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testDohDnsIpv6() throws IOException {
        DohDns resolver = new DohDns("https://dns.alidns.com/dns-query",10,true);
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"), null);
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length > 0);
            for (Record r : records) {
                Assert.assertTrue(r.ttl >= 600);
                Assert.assertTrue(r.isA()||r.isAAAA());
                Assert.assertFalse(r.isExpired());
            }

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testDohDnsNotFound() throws IOException {
        DohDns resolver = new DohDns();
        try {
            Record[] records = resolver.resolve(new Domain("7777777.qiniu.com"), null);
            Assert.assertNull(records);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testDohDnsTimeout() throws IOException {
        DohDns resolver = new DohDns("https://www.google.com", 5);
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"), null);
            Assert.fail("no timeout");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
