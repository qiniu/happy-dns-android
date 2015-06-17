package com.qiniu.android.dns;

/**
 * Created by bailong on 15/6/17.
 */
public class Domain {
    public final String domain;
    public final boolean hasCname;

    public Domain(String domain, boolean hasCname) {
        this.domain = domain;
        this.hasCname = hasCname;
    }

    public Domain(String domain) {
        this(domain, false);
    }
}
