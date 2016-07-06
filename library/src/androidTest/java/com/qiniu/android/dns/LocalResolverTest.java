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

    private void template(String ip) throws UnknownHostException {
        Resolver resolver = new Resolver(InetAddress.getByName(ip));
        template(resolver);
    }

    private void template(IResolver resolver) {
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"), null);
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length > 0);
            records = resolver.resolve(new Domain("www.qiniu.com"), null);
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length >= 3);

            for (Record r : records) {
                Assert.assertTrue(r.value, r.ttl >= 600);
                Assert.assertTrue(r.value, r.isA() || r.isCname());
                Assert.assertFalse(r.value, r.isExpired());
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testLocal() {
        template(AndroidDnsServer.defaultResolver());
    }

    //    http://www.alidns.com/
//    public void testAli() throws UnknownHostException {
//        template("223.5.5.5");
//    }

    //    https://www.114dns.com/
    public void test114() throws UnknownHostException {
        template("114.114.115.115");
    }

    //    http://dudns.baidu.com/
//    public void testBaidu() throws UnknownHostException {
//        template("180.76.76.76");
//    }

    //    http://www.sdns.cn/ cnnic
//    public void testCnnic() throws UnknownHostException {
//        template("1.2.4.8");
//    }

    public void testGoogle() throws UnknownHostException {
        template("8.8.4.4");
    }

    public void testDnspod() throws UnknownHostException {
        template("119.29.29.29");
    }

    //    http://www.dnspai.com/ 360dns
//    public void testDnspai() throws UnknownHostException {
//        template("101.226.4.6");
//    }
}
