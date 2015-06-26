package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.Resolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by bailong on 15/6/21.
 */
public class DnsTest extends AndroidTestCase {
    public void testDns() throws UnknownHostException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = AndroidDnsServer.defaultResolver();
        resolvers[1] = new Resolver(InetAddress.getByName("223.5.5.5"));
        DnsManager dns = new DnsManager(NetworkInfo.normal(), resolvers);
        String[] ips = dns.query("www.qiniu.com");
        assertNotNull(ips);
        assertTrue(ips.length > 0);
    }
}
