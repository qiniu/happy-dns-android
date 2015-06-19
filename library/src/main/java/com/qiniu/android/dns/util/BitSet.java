package com.qiniu.android.dns.util;

/**
 * Created by bailong on 15/6/16.
 */
public final class BitSet {
    private int set;

    public BitSet() {
        set = 0;
    }

    public BitSet set(int bitIndex) {
        set |= (1 << bitIndex);
        return this;
    }

    public int value() {
        return set;
    }
}
