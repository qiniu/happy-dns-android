package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.local.Hosts;

import junit.framework.Assert;

import java.io.IOException;

/**
 * Created by bailong on 15/6/18.
 */
public class HostsTest extends AndroidTestCase {
    public void testQuery() throws IOException {
        Hosts hosts = new Hosts();
        hosts.put("hello.qiniu.com", "1.1.1.1");
        hosts.put("hello.qiniu.com", "2.2.2.2");
        hosts.put("qiniu.com", "3.3.3.3");
        String[] r = hosts.query(new Domain("hello.qiniu.com"), NetworkInfo.normal());
        Assert.assertEquals(2, r.length);
        Assert.assertTrue(r[0].equals("1.1.1.1") || r[0].equals("2.2.2.2"));
        Assert.assertTrue(r[1].equals("1.1.1.1") || r[1].equals("2.2.2.2"));
        Assert.assertTrue(!r[0].equals(r[1]));
        String[] r1 = hosts.query(new Domain("hello.qiniu.com"), NetworkInfo.normal());
        String[] r2 = hosts.query(new Domain("hello.qiniu.com"), NetworkInfo.normal());
        String[] r3 = hosts.query(new Domain("hello.qiniu.com"), NetworkInfo.normal());
        String[] r4 = hosts.query(new Domain("hello.qiniu.com"), NetworkInfo.normal());
        Assert.assertTrue(!(r[0].equals(r1[0]) && r[0].equals(r2[0])
                && r[0].equals(r3[0]) && r[0].equals(r4[0])));
    }

    public void testCnc() throws IOException {
        Hosts hosts = new Hosts();
        hosts.put("hello.qiniu.com", "1.1.1.1");
        hosts.put("hello.qiniu.com", "2.2.2.2");
        hosts.put("qiniu.com", "3.3.3.3");

        hosts.put("qiniu.com", new Hosts.Value("4.4.4.4", NetworkInfo.ISP_CNC));

        String[] r = hosts.query(new Domain("qiniu.com"), new NetworkInfo(NetworkInfo.NetSatus.MOBILE, NetworkInfo.ISP_CNC));
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("4.4.4.4", r[0]);
    }
}
