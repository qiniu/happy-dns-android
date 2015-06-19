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
        Record[] r = hosts.query(new Domain("hello.qiniu.com"));
        Assert.assertEquals(2, r.length);
        Assert.assertTrue(r[0].value.equals("1.1.1.1") || r[0].value.equals("2.2.2.2"));
        Assert.assertTrue(r[1].value.equals("1.1.1.1") || r[1].value.equals("2.2.2.2"));
        Assert.assertTrue(!r[0].equals(r[1]));
        Record[] r1 = hosts.query(new Domain("hello.qiniu.com"));
        Record[] r2 = hosts.query(new Domain("hello.qiniu.com"));
        Record[] r3 = hosts.query(new Domain("hello.qiniu.com"));
        Record[] r4 = hosts.query(new Domain("hello.qiniu.com"));
        Assert.assertTrue(!(r[0].equals(r1[0]) && r[0].equals(r2[0])
        && r[0].equals(r3[0]) && r[0].equals(r4[0])));
    }
}
