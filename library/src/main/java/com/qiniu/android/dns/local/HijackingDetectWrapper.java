package com.qiniu.android.dns.local;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;
import com.qiniu.android.dns.dns.DnsUdpResolver;

import java.io.IOException;

/**
 * Created by bailong on 15/7/10.
 */
public final class HijackingDetectWrapper implements IResolver {
    private final DnsUdpResolver resolver;

    public HijackingDetectWrapper(DnsUdpResolver r) {
        this.resolver = r;
    }

    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        Record[] records = resolver.resolve(domain, info);
        if (domain.hasCname) {
            boolean cname = false;
            String server = null;
            for (Record r : records) {
                if (r.isCname()) {
                    cname = true;
                    server = r.server;
                    break;
                }
            }
            if (!cname) {
                throw new DnshijackingException(domain.domain, server);
            }
        }
        if (domain.maxTtl != 0) {
            for (Record r : records) {
                if (!r.isCname()) {
                    if (r.ttl > domain.maxTtl) {
                        throw new DnshijackingException(domain.domain, r.server, r.ttl);
                    }
                }
            }
        }
        return records;
    }
}
