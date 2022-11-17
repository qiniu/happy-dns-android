package com.qiniu.android.dns;

import com.qiniu.android.dns.util.LruCache;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by bailong on 15/6/20.
 */
public class LruCacheTest extends TestCase {

    @Test
    public void testPut() {
        LruCache<String, String> x = new LruCache<>(2);
        x.put("1", "1");
        x.put("2", "2");
        x.put("3", "3");
        Assert.assertNull(x.get("1"));
        Assert.assertEquals("2", x.get("2"));
        Assert.assertEquals("3", x.get("3"));
        x.remove("2");
        x.put("1", "1");
        Assert.assertEquals("1", x.get("1"));
        Assert.assertNull(x.get("2"));
    }

    @Test
    public void testOut() {
        LruCache<String, String> x = new LruCache<>(2);
        x.put("1", "1");
        x.put("2", "2");
        x.get("1");
        x.put("3", "3");
        Assert.assertNull(x.get("2"));
        Assert.assertEquals("1", x.get("1"));
        Assert.assertEquals("3", x.get("3"));
        x.remove("2");
        x.put("1", "1");
        Assert.assertEquals("1", x.get("1"));
        Assert.assertNull(x.get("2"));
    }

    @Test
    public void testClear() {
        LruCache<String, String> x = new LruCache<>(2);
        x.put("1", "1");
        x.put("2", "2");
        x.put("3", "3");
        x.clear();
        Assert.assertNull(x.get("3"));
        x.put("1", "1");
        x.put("2", "2");
        x.put("3", "3");
        Assert.assertNull(x.get("1"));
        Assert.assertEquals("2", x.get("2"));
        Assert.assertEquals("3", x.get("3"));
    }
}
