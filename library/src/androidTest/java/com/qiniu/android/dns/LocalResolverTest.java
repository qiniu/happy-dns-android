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
        IResolver resolver = AndroidDnsServer.defaultResolver();
        try {
            Record[] records = resolver.query(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length>0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

//    http://www.alidns.com/
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

//    https://www.114dns.com/
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

//    http://dudns.baidu.com/
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

//    http://www.sdns.cn/
    public void testCnnic() throws UnknownHostException {
        Resolver resolver = new Resolver(InetAddress.getByName("1.2.4.8"));
        try {
            Record[] records = resolver.query(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length>0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testGoogle() throws UnknownHostException {
        Resolver resolver = new Resolver(InetAddress.getByName("8.8.4.4"));
        try {
            Record[] records = resolver.query(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length>0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

//    http://www.dnspai.com/
    public void testDnspai() throws UnknownHostException {
        Resolver resolver = new Resolver(InetAddress.getByName("101.226.4.6"));
        try {
            Record[] records = resolver.query(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length>0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
