package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.local.AndroidDnsServer;

import junit.framework.Assert;

/**
 * Created by bailong on 15/7/4.
 */
public class NetworkTest extends AndroidTestCase {
    public void testIpChange(){
        long t1 = System.currentTimeMillis();
        String ip = Network.getIp();
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.println(ip);
        Assert.assertNotNull(ip);
        Assert.assertTrue(!"".equals(ip));
    }
}
