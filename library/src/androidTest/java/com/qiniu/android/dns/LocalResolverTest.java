package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.Resolver;

import junit.framework.Assert;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by bailong on 15/6/17.
 */
public class LocalResolverTest extends AndroidTestCase {
    public void testLocal(){
        Resolver resolver = AndroidDnsServer.defaultResolver();
        try {
            Record[] records = resolver.query(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length>0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
    public void testAli() throws UnknownHostException {
        Resolver resolver = new Resolver(InetAddress.getByName("223.5.5.5"));
        try {
            Record[] records = resolver.query(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length>0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void test114() throws UnknownHostException {
        Resolver resolver = new Resolver(InetAddress.getByName("114.114.114.114"));
        try {
            Record[] records = resolver.query(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length>0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testBaidu() throws UnknownHostException {
        Resolver resolver = new Resolver(InetAddress.getByName("180.76.76.76"));
        try {
            Record[] records = resolver.query(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length>0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
