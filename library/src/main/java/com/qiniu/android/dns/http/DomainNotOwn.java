package com.qiniu.android.dns.http;

import com.qiniu.android.dns.DnsException;

/**
 * 一些httpdns 只能解析自己管理的域名
 */
public class DomainNotOwn extends DnsException {
    public DomainNotOwn(String domain) {
        super(domain, "dns not own");
    }
}
