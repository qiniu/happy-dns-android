package com.qiniu.android.dns;

import java.util.Objects;

/**
 * Created by bailong on 15/6/12.
 */
public final class Record {
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

    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (o == null || !(o instanceof Record)){
            return  false;
        }
        Record another = (Record)o;
        return this.value.equals(another.value)
                && this.type == another.type
                && this.ttl == another.ttl
                && this.timeStamp == another.timeStamp;
    }

    public boolean isA(){
        return type == TYPE_A;
    }

    public boolean isCname(){
        return type == TYPE_CNAME;
    }
}
