package com.qiniu.android.dns;

import java.util.Date;
import java.util.Locale;

/**
 * Created by bailong on 15/6/12.
 */
public final class Record {
    public static final int TTL_MIN_SECONDS = 600;
    public static final int TTL_Forever = -1;

    public static class Source {
        public static final int Unknown = 0;
        public static final int Custom = 1;
        public static final int DnspodEnterprise = 2;
        public static final int System = 3;
        public static final int Udp = 4;
        public static final int Doh = 5;
    }

    /**
     * A 记录 类型
     */
    public static final int TYPE_A = 1;

    /**
     * AAAA 记录 类型
     */
    public static final int TYPE_AAAA = 28;

    /**
     * CName 类型
     */
    public static final int TYPE_CNAME = 5;

    /**
     * TXT 记录 类型
     */
    public static final int TYPE_TXT = 16;

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
     * 时间戳，用来判断超时，单位：秒
     */
    public final long timeStamp;

    /**
     * 记录来源， httpDns 或者 System
     * {@link Source}
     */
    public final int source;

    public final String server;

    public Record(String value, int type, int ttl) {
        this.value = value;
        this.type = type;
        this.ttl = ttl;
        this.timeStamp = new Date().getTime() / 1000;
        this.source = Source.Unknown;
        this.server = null;
    }

    public Record(String value, int type, int ttl, long timeStamp, int source) {
        this.value = value;
        this.type = type;
        this.ttl = Math.max(ttl, TTL_MIN_SECONDS);
        this.timeStamp = timeStamp;
        this.source = source;
        this.server = null;
    }

    public Record(String value, int type, int ttl, long timeStamp, int source, String server) {
        this.value = value;
        this.type = type;
        this.ttl = ttl < TTL_MIN_SECONDS ? TTL_MIN_SECONDS : ttl;
        this.timeStamp = timeStamp;
        this.source = source;
        this.server = server;
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

    public boolean isAAAA() {
        return type == TYPE_AAAA;
    }

    public boolean isCname() {
        return type == TYPE_CNAME;
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis() / 1000);
    }

    public boolean isExpired(long time) {
        if (ttl == TTL_Forever) {
            return false;
        }
        return timeStamp + ttl < time;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),"{type:%s, value:%s, source:%s, server:%s, timestamp:%d, ttl:%d}", type, value, source, server, timeStamp, ttl);
    }
}
