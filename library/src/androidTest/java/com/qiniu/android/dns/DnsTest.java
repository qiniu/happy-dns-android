package com.qiniu.android.dns;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.qiniu.android.dns.dns.DnsUdpResolver;
import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.HijackingDetectWrapper;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Created by bailong on 15/6/21.
 */
@RunWith(AndroidJUnit4.class)
public class DnsTest extends TestCase {
    private boolean flag = false;

    @Test
    public void testDns() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = new DnsUdpResolver("8.8.8.8");
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        Record[] records = dns.queryRecords("www.qiniu.com");
        assertNotNull(records);
        assertTrue(records.length > 0);
    }

    @Test
    public void testQueryDnsRecords() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = new DnsUdpResolver("8.8.8.8");
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        Record[] records = dns.queryRecords("www.qiniu.com");
        assertNotNull(records);
        assertTrue(records.length > 0);
    }

    @Test
    public void testQueryDnsErrorHandler() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = new DnsUdpResolver("8.8.8.8");
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

    @Test
    public void testCnc() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = new DnsUdpResolver("8.8.8.8");
        resolvers[1] = new DnsUdpResolver("223.5.5.5");
        DnsManager dns = new DnsManager(new NetworkInfo(NetworkInfo.NetSatus.MOBILE, NetworkInfo.ISP_CNC), resolvers);

        dns.putHosts("hello.qiniu.com", "1.1.1.1");
        dns.putHosts("hello.qiniu.com", "2.2.2.2");
        dns.putHosts("qiniu.com", "3.3.3.3");

        dns.putHosts("qiniu.com", Record.TYPE_A,"4.4.4.4", NetworkInfo.ISP_CNC);
        Domain d = new Domain("qiniu.com", false, true);
        Record[] r = dns.queryRecords(d);
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("4.4.4.4", r[0].value);
    }

    @Test
    public void testTtl() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = new DnsUdpResolver("8.8.8.8");
        resolvers[1] = new HijackingDetectWrapper(new DnsUdpResolver("223.5.5.5"));
        DnsManager dns = new DnsManager(new NetworkInfo(NetworkInfo.NetSatus.MOBILE, NetworkInfo.ISP_CNC), resolvers);

        dns.putHosts("hello.qiniu.com", "1.1.1.1");
        dns.putHosts("hello.qiniu.com", "2.2.2.2");
        dns.putHosts("qiniu.com", "3.3.3.3");

        dns.putHosts("qiniu.com", Record.TYPE_A,"4.4.4.4", NetworkInfo.ISP_CNC);
        Domain d = new Domain("qiniu.com", false, true, 10);
        Record[] r = dns.queryRecords(d);
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("4.4.4.4", r[0].value);

        d = new Domain("qiniu.com", false, false, 1000);
        r = dns.queryRecords(d);
        Assert.assertEquals(1, r.length);
        Assert.assertTrue(!"4.4.4.4".equals(r[0].value));
    }

    @Test
    public void testCname() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = new DnsUdpResolver("8.8.8.8");
        resolvers[1] = new HijackingDetectWrapper(
                new DnsUdpResolver("114.114.115.115"));
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);

        dns.putHosts("hello.qiniu.com", "1.1.1.1");
        dns.putHosts("hello.qiniu.com", "2.2.2.2");
        dns.putHosts("qiniu.com", "3.3.3.3");

        dns.putHosts("qiniu.com", Record.TYPE_A, "4.4.4.4", NetworkInfo.ISP_CNC);

        Domain d = new Domain("qiniu.com", true, true, 120);
        Record[] r = dns.queryRecords(d);
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("3.3.3.3", r[0].value);

        d = new Domain("qiniu.com", false);
        r = dns.queryRecords(d);
        Assert.assertEquals(1, r.length);
        Assert.assertTrue(!"3.3.3.3".equals(r[0].value));
    }

    @Test
    public void testSort() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = new HijackingDetectWrapper(
                new DnsUdpResolver("114.114.115.115"));
        RecordSorter sorter = new RecordSorter() {
            @Override
            public Record[] sort(Record[] records) {
                if (records.length < 2)  {
                    return records;
                }
                if (flag) {
                    records[0] = new Record("2.2.2.2", Record.TYPE_A, 120);
                    records[1] = new Record("1.1.1.1", Record.TYPE_A, 120);
                } else {
                    records[0] = new Record("1.1.1.1", Record.TYPE_A, 120);
                    records[1] = new Record("2.2.2.2", Record.TYPE_A, 120);
                }
                return records;
            }
        };
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers, sorter);

        dns.putHosts("hello.qiniu.com", "1.1.1.1");
        dns.putHosts("hello.qiniu.com", "2.2.2.2");
        dns.putHosts("qiniu.com", "3.3.3.3");

        dns.putHosts("qiniu.com", Record.TYPE_A, "4.4.4.4", NetworkInfo.ISP_CNC);
        Domain d = new Domain("hello.qiniu.com", true, true);
        Record[] records = dns.queryRecords(d);
        Assert.assertEquals(2, records.length);
        Assert.assertEquals("1.1.1.1", records[0].value);
        Assert.assertEquals("2.2.2.2", records[1].value);
        flag = true;
        d = new Domain("hello.qiniu.com", true, true);
        records = dns.queryRecords(d);
        Assert.assertEquals(2, records.length);
        Assert.assertEquals("2.2.2.2", records[0].value);
        Assert.assertEquals("1.1.1.1", records[1].value);
    }

    @Test
    public void testNull() {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = new DnsUdpResolver("8.8.8.8");
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        IOException e = null;
        try {
            dns.queryRecords((String) null);
        } catch (IOException ex) {
            ex.printStackTrace();
            e = ex;
        }

        assertNotNull(e);
        e = null;
        try {
            dns.queryRecords((Domain) null);
        } catch (IOException ex) {
            ex.printStackTrace();
            e = ex;
        }

        assertNotNull(e);
        e = null;
        try {
            dns.queryRecords("");
        } catch (IOException ex) {
            ex.printStackTrace();
            e = ex;
        }
        assertNotNull(e);
    }

    @Test
    public void testIp() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = new DnsUdpResolver("8.8.8.8");
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        Record[] records = dns.queryRecords("1.1.1.1");
        assertEquals(records.length, 1);
        assertEquals(records[0].value, "1.1.1.1");
    }

    @Test
    public void testNeedHttpdns() {
        String id = TimeZone.getDefault().getID();
        if (id.equals("Asia/Shanghai")) {
            assertEquals(true, DnsManager.needHttpDns());
        } else {
            assertEquals(false, DnsManager.needHttpDns());
        }
    }
}
