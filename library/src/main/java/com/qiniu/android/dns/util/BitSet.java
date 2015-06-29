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

    public boolean isSet(int index) {
        return (set & (1 << index)) != 0;
    }

    public boolean noneIsSet(int index) {
        return set == 0;
    }

    // 30 bits
    public boolean allIsSet(int index) {
        return (set + 1) == (1 << index);
    }

    public int leadingZeros() {
        int y;
        int n = 32;
        y = set >> 16;
        if (y != 0) {
            n = n - 16;
            set = y;
        }
        y = set >> 8;
        if (y != 0) {
            n = n - 8;
            set = y;
        }
        y = set >> 4;
        if (y != 0) {
            n = n - 4;
            set = y;
        }
        y = set >> 2;
        if (y != 0) {
            n = n - 2;
            set = y;
        }
        y = set >> 1;
        if (y != 0) {
            return n - 2;
        }

        return n - set;
    }

    public BitSet clear() {
        set = 0;
        return this;
    }

    public int value() {
        return set;
    }
}
