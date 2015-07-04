package com.qiniu.android.dns;

import android.test.AndroidTestCase;
import android.util.Log;

import com.qiniu.android.dns.local.AndroidDnsServer;

import junit.framework.Assert;

import java.net.InetAddress;

/**
 * Created by bailong on 15/6/16.
 */
public class LocalServerTest extends AndroidTestCase {
    public void testCmd(){
        long t1 = System.currentTimeMillis();
        InetAddress[] servers = AndroidDnsServer.getByCommand();
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        Assert.assertNotNull(servers);
        Assert.assertTrue(servers.length > 0);
        Log.v("localtest", servers[0].getHostAddress());
    }

    public void testReflection(){
        long t1 = System.currentTimeMillis();
        InetAddress[] servers = AndroidDnsServer.getByReflection();
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        Assert.assertNotNull(servers);
        Assert.assertTrue(servers.length>0);
        Log.v("localtest", servers[0].getHostAddress());
    }
}
