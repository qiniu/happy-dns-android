package com.qiniu.android.dns;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by bailong on 15/7/4.
 */
public class NetworkTest extends TestCase {

    @Test
    public void testIpChange() {
        long t1 = System.currentTimeMillis();
        String ip = Network.getIp();
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.println(ip);
        Assert.assertNotNull(ip);
        Assert.assertTrue(!"".equals(ip));
    }
}
