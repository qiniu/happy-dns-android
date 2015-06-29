package com.qiniu.android.dns;

/**
 * Created by bailong on 15/6/17.
 */
public final class Domain {
    /**
     * 域名
     */
    public final String domain;
    /**
     * 是否有cname, 用来做劫持判断, 劫持的dns解析经常直接返回IP
     */
    public final boolean hasCname;
    /**
     * 最大的ttl长度，劫持及异常dns判断，劫持的dns解析经常会有超大的ttl；
     * 某些运营商dns server为了减少请求数，也会将ttl设置非常大，比如一年。
     */
    public final int maxTtl;

    /**
     * 是否先查hosts文件，再去网络请求，一般调试使用
     */
    public final boolean hostsFirst;

    public Domain(String domain, boolean hasCname, boolean hostsFirst) {
        this(domain, hasCname, hostsFirst, 0);
    }

    public Domain(String domain, boolean hasCname, boolean hostsFirst, int maxTtl) {
        this.domain = domain;
        this.hasCname = hasCname;
        this.hostsFirst = hostsFirst;
        this.maxTtl = maxTtl;
    }

    public Domain(String domain, boolean hasCname) {
        this(domain, hasCname, false, 0);
    }

    public Domain(String domain) {
        this(domain, false, false, 0);
    }
}
