package com.qiniu.android.dns;

import com.qiniu.android.dns.local.Hosts;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by bailong on 15/6/18.
 */
public class HostsTest extends TestCase {

    @Test
    public void testQuery() throws IOException {
        Hosts hosts = new Hosts();
        hosts.put("hello.qiniu.com", new Record("1.1.1.1", Record.TYPE_A, 120));
        hosts.put("hello.qiniu.com", new Record("2.2.2.2", Record.TYPE_A, 120));
        hosts.put("qiniu.com", new Record("3.3.3.3", Record.TYPE_A, 120));
        Record[] r = hosts.query(new Domain("hello.qiniu.com"), NetworkInfo.normal);
        Assert.assertEquals(2, r.length);
        Assert.assertTrue(r[0].value.equals("2.2.2.2"));
        Assert.assertTrue(r[1].value.equals("1.1.1.1"));

        Record[] r2 = hosts.query(new Domain("hello.qiniu.com"), NetworkInfo.normal);
        Assert.assertEquals(2, r2.length);
        Assert.assertTrue(r2[1].value.equals("2.2.2.2"));
        Assert.assertTrue(r2[0].value.equals("1.1.1.1"));
    }

    @Test
    public void testCnc() throws IOException {
        Hosts hosts = new Hosts();
        hosts.put("hello.qiniu.com", new Record("1.1.1.1", Record.TYPE_A, 120));
        hosts.put("hello.qiniu.com", new Record("2.2.2.2", Record.TYPE_A, 120));
        hosts.put("qiniu.com", new Record("3.3.3.3", Record.TYPE_A, 120));

        hosts.put("qiniu.com", new Hosts.Value(new Record("4.4.4.4", Record.TYPE_A, 120), NetworkInfo.ISP_CNC));

        Record[] r = hosts.query(new Domain("qiniu.com"), new NetworkInfo(NetworkInfo.NetSatus.MOBILE, NetworkInfo.ISP_CNC));
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("4.4.4.4", r[0].value);
    }
}
