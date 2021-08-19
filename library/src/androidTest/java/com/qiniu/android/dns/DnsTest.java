package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.dns.DnsUdpResolver;
import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.HijackingDetectWrapper;

import junit.framework.Assert;

import java.io.IOException;
import java.net.InetAddress;
import java.util.TimeZone;

/**
 * Created by bailong on 15/6/21.
 */
public class DnsTest extends AndroidTestCase {
    private boolean flag = false;

    public void testDns() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = AndroidDnsServer.defaultResolver(getContext());
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        String[] ips = dns.query("www.qiniu.com");
        assertNotNull(ips);
        assertTrue(ips.length > 0);
    }

    public void testQueryDnsRecords() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = AndroidDnsServer.defaultResolver(getContext());
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        Record[] ips = dns.queryRecords("www.qiniu.com");
        assertNotNull(ips);
        assertTrue(ips.length > 0);
    }

    public void testQueryDnsErrorHandler() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = AndroidDnsServer.defaultResolver(getContext());
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        dns.queryErrorHandler = new DnsManager.QueryErrorHandler() {
            @Override
            public void queryError(Exception e, String host) {
                assertNotNull(e);
            }
        };
        Record[] ips = null;
        try {
            ips = dns.queryRecords("www.qiniu1.com");
        } catch (IOException e){
        }
        assertNull(ips);
    }

    public void testCnc() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = AndroidDnsServer.defaultResolver(getContext());
        resolvers[1] = new DnsUdpResolver("223.5.5.5");
        DnsManager dns = new DnsManager(new NetworkInfo(NetworkInfo.NetSatus.MOBILE, NetworkInfo.ISP_CNC), resolvers);

        dns.putHosts("hello.qiniu.com", "1.1.1.1");
        dns.putHosts("hello.qiniu.com", "2.2.2.2");
        dns.putHosts("qiniu.com", "3.3.3.3");

        dns.putHosts("qiniu.com", "4.4.4.4", NetworkInfo.ISP_CNC);
        Domain d = new Domain("qiniu.com", false, true);
        String[] r = dns.query(d);
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("4.4.4.4", r[0]);
    }

    public void testTtl() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = AndroidDnsServer.defaultResolver(getContext());
        resolvers[1] = new HijackingDetectWrapper(
                new DnsUdpResolver("223.5.5.5"));
        DnsManager dns = new DnsManager(new NetworkInfo(
                NetworkInfo.NetSatus.MOBILE, NetworkInfo.ISP_CNC), resolvers);

        dns.putHosts("hello.qiniu.com", "1.1.1.1");
        dns.putHosts("hello.qiniu.com", "2.2.2.2");
        dns.putHosts("qiniu.com", "3.3.3.3");

        dns.putHosts("qiniu.com", "4.4.4.4", NetworkInfo.ISP_CNC);
        Domain d = new Domain("qiniu.com", false, false, 10);
        String[] r = dns.query(d);
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("4.4.4.4", r[0]);

        d = new Domain("qiniu.com", false, false, 1000);
        r = dns.query(d);
        Assert.assertEquals(1, r.length);
        Assert.assertTrue(!"4.4.4.4".equals(r[0]));
    }

    public void testCname() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = AndroidDnsServer.defaultResolver(getContext());
        resolvers[1] = new HijackingDetectWrapper(
                new DnsUdpResolver("114.114.115.115"));
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);

        dns.putHosts("hello.qiniu.com", "1.1.1.1");
        dns.putHosts("hello.qiniu.com", "2.2.2.2");
        dns.putHosts("qiniu.com", "3.3.3.3");

        dns.putHosts("qiniu.com", "4.4.4.4", NetworkInfo.ISP_CNC);
        Domain d = new Domain("qiniu.com", true);
        String[] r = dns.query(d);
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("3.3.3.3", r[0]);

        d = new Domain("qiniu.com", false);
        r = dns.query(d);
        Assert.assertEquals(1, r.length);
        Assert.assertTrue(!"3.3.3.3".equals(r[0]));
    }

    public void testSort() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = new HijackingDetectWrapper(
                new DnsUdpResolver("114.114.115.115"));
        IpSorter sorter = new IpSorter() {
            @Override
            public String[] sort(String[] ips) {
                String[] ret = new String[ips.length];
                if (flag) {
                    ret[0] = "2.2.2.2";
                    ret[1] = "1.1.1.1";
                } else {
                    ret[0] = "1.1.1.1";
                    ret[1] = "2.2.2.2";
                }
                return ret;
            }
        };
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers, sorter);

        dns.putHosts("hello.qiniu.com", "1.1.1.1");
        dns.putHosts("hello.qiniu.com", "2.2.2.2");
        dns.putHosts("qiniu.com", "3.3.3.3");

        dns.putHosts("qiniu.com", "4.4.4.4", NetworkInfo.ISP_CNC);
        Domain d = new Domain("hello.qiniu.com", true, true);
        String[] ips = dns.query(d);
        Assert.assertEquals(2, ips.length);
        Assert.assertEquals("1.1.1.1", ips[0]);
        Assert.assertEquals("2.2.2.2", ips[1]);
        flag = true;
        d = new Domain("hello.qiniu.com", true, true);
        ips = dns.query(d);
        Assert.assertEquals(2, ips.length);
        Assert.assertEquals("2.2.2.2", ips[0]);
        Assert.assertEquals("1.1.1.1", ips[1]);
    }

    public void testNull() {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = AndroidDnsServer.defaultResolver(getContext());
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        IOException e = null;
        try {
            dns.query((String) null);
        } catch (IOException ex) {
            ex.printStackTrace();
            e = ex;
        }

        assertNotNull(e);
        e = null;
        try {
            dns.query((Domain) null);
        } catch (IOException ex) {
            ex.printStackTrace();
            e = ex;
        }

        assertNotNull(e);
        e = null;
        try {
            dns.query("");
        } catch (IOException ex) {
            ex.printStackTrace();
            e = ex;
        }
        assertNotNull(e);
    }

    public void testIp() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = AndroidDnsServer.defaultResolver(getContext());
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        String[] ips = dns.query("1.1.1.1");
        assertEquals(ips.length, 1);
        assertEquals(ips[0], "1.1.1.1");
    }

    public void testNeedHttpdns() {
        String id = TimeZone.getDefault().getID();
        if (id.equals("Asia/Shanghai")) {
            assertEquals(true, DnsManager.needHttpDns());
        } else {
            assertEquals(false, DnsManager.needHttpDns());
        }
    }
}
