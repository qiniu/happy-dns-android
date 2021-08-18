package com.qiniu.android.dns.dns;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class DnsUdpResolver extends DnsResolver {

    public DnsUdpResolver(String serverIP) {
        super(serverIP);
    }

    public DnsUdpResolver(String serverIP, int recordType, int timeout) {
        super(serverIP, recordType, timeout);
    }

    public DnsUdpResolver(String[] serverIPs, int recordType, int timeout) {
        super(serverIPs, recordType, timeout);
    }

    public DnsUdpResolver(String[] serverIPs, int recordType, int timeout, ExecutorService executorService) {
        super(serverIPs, recordType, timeout, executorService);
    }

    @Override
    DnsResponse request(String server, String host, int recordType) throws IOException {

        return null;
    }
}
