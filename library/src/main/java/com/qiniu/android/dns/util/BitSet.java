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

    public boolean isSet(int index){
        return (set & (1<<index)) != 0;
    }

    public boolean noneIsSet(int index){
        return  set == 0;
    }

    // 30 bits
    public boolean allIsSet(int index){
        return (set +1 ) == (1<<index);
    }

    public BitSet clear(){
        set = 0;
        return this;
    }

    public int value() {
        return set;
    }
}
