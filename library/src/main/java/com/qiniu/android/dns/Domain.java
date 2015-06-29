package com.qiniu.android.dns;

/**
 * Created by bailong on 15/6/17.
 */
public final class Domain {
    public final String domain;
    public final boolean hasCname;
    public final int type;
    public final boolean hostsFirst;

    public Domain(String domain, boolean hasCname, int type) {
        this(domain, hasCname, type, false);
    }

    public Domain(String domain, boolean hasCname, int type, boolean hostsFirst) {
        this.domain = domain;
        this.hasCname = hasCname;
        this.type = type;
        this.hostsFirst = hostsFirst;
    }

    public Domain(String domain, boolean hasCname) {
        this(domain, hasCname, 0);
    }

    public Domain(String domain) {
        this(domain, false, 0);
    }
}
