package com.qiniu.android.dns;

/**
 * Created by bailong on 15/6/12.
 */
public class Record {
    public static final int TYPE_A = 1;
    public static final int TYPE_CNAME = 5;
    public final String value;
    public final int type;
    public final int ttl;
    public final long timeStamp;
    public Record(String value, int type, int ttl, long timeStamp) {
        this.value = value;
        this.type = type;
        this.ttl = ttl;
        this.timeStamp = timeStamp;
    }
}
