package com.qiniu.android.dns.local;


import com.qiniu.android.dns.DnsException;

/**
 * Created by bailong on 15/6/19.
 */
public class DnshijackingException extends DnsException {
    public DnshijackingException(String domain, String server) {
        super(domain, "has hijacked by " + server);
    }
}
