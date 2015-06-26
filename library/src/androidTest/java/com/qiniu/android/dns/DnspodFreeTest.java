package com.qiniu.android.dns;

import android.test.AndroidTestCase;

import com.qiniu.android.dns.http.DnspodFree;

import junit.framework.Assert;

import java.io.IOException;

/**
 * Created by bailong on 15/6/17.
 */
public class DnspodFreeTest extends AndroidTestCase {
    public void testFound() throws IOException {
        DnspodFree resolver = new DnspodFree();
        try {
            Record[] records = resolver.query(new Domain("baidu.com"), null);
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length > 0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testNotFound() throws IOException {
        DnspodFree resolver = new DnspodFree();
        try {
            Record[] records = resolver.query(new Domain("7777777.qiniu.com"), null);
            Assert.assertNull(records);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
