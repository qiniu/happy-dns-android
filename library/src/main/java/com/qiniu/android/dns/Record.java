package com.qiniu.android.dns;

/**
 * Created by bailong on 15/6/12.
 */
public final class Record {
    public static final int TTL_MIN_SECONDS = 600;

    /**
     * A 记录 类型
     */
    public static final int TYPE_A = 1;

    /**
     * CName 类型
     */
    public static final int TYPE_CNAME = 5;

    /**
     * 具体的值，A 记录时为IP，CName时为指向的域名
     */
    public final String value;

    /**
     * 记录类型，A或者CName
     */
    public final int type;

    /**
     * TTL dns结果缓存时间
     */
    public final int ttl;

    /**
     * 时间戳，用来判断超时
     */
    public final long timeStamp;

    public Record(String value, int type, int ttl, long timeStamp) {
        this.value = value;
        this.type = type;
        this.ttl = ttl < TTL_MIN_SECONDS ? TTL_MIN_SECONDS : ttl;
        this.timeStamp = timeStamp;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Record)) {
            return false;
        }
        Record another = (Record) o;
        return this.value.equals(another.value)
                && this.type == another.type
                && this.ttl == another.ttl
                && this.timeStamp == another.timeStamp;
    }

    public boolean isA() {
        return type == TYPE_A;
    }

    public boolean isCname() {
        return type == TYPE_CNAME;
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis() / 1000);
    }

    public boolean isExpired(long time) {
        return timeStamp + ttl < time;
    }
}
