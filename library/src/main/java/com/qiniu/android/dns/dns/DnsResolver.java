package com.qiniu.android.dns.dns;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class DnsResolver implements IResolver {

    private static ScheduledExecutorService timeoutExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static ExecutorService defaultExecutorService = new ThreadPoolExecutor(0, 4,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private final int recordType;
    private final String[] servers;
    private final ExecutorService executorService;

    protected final int timeout;

    public DnsResolver(String server) {
        this(server, Record.TYPE_A, DNS_DEFAULT_TIMEOUT);
    }

    public DnsResolver(String server, int timeout) {
        this(server, Record.TYPE_A, timeout);
    }

    public DnsResolver(String server, int recordType, int timeout) {
        this(server == null ? null : new String[]{server}, recordType, timeout, null);
    }

    public DnsResolver(String[] servers, int recordType, int timeout) {
        this(servers, recordType, timeout, (servers != null && servers.length > 0) ? defaultExecutorService : null);
    }

    public DnsResolver(String[] servers, int recordType, int timeout, ExecutorService executorService) {
        this.recordType = recordType;
        this.timeout = timeout > 0 ? timeout : DNS_DEFAULT_TIMEOUT;
        this.servers = servers;
        this.executorService = executorService;
    }

    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        DnsResponse response = lookupHost(domain.domain);
        if (response == null) {
            throw new IOException("response is null");
        }

        List<Record> answers = response.getAnswerArray();
        if (answers == null || answers.size() == 0) {
            return null;
        }

        List<Record> records = new ArrayList<>();
        for (Record record : answers) {
            if (record.isA() || record.isCname() || record.isAAAA()) {
                records.add(record);
            }
        }
        return records.toArray(new Record[0]);
    }

    private DnsResponse lookupHost(String host) throws IOException {
        return request(host, recordType);
    }

    private DnsResponse request(final String host, final int recordType) throws IOException {
        if (servers == null || servers.length == 0) {
            throw new IOException("server can not empty");
        }

        if (host == null || host.length() == 0) {
            throw new IOException("host can not empty");
        }

        if (servers.length == 1 || executorService == null) {
            DnsResponse response = null;
            for (String server : servers) {
                response = request(server, host, recordType);
                if (response != null) {
                    break;
                }
            }
            return response;
        } else {
            final DnsResponse[] response = {null};
            final IOException[] exceptions = {null};
            final int[] completedCount = {0};
            final Object waiter = new Object();

            // 超时处理
            timeoutExecutorService.schedule(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    synchronized (waiter) {
                        waiter.notify();
                        exceptions[0] = new IOException("resolver timeout for server:" + servers.toString() + " host:" + host);
                    }
                    return null;
                }
            }, timeout, TimeUnit.SECONDS);

            // 返回一个即结束
            for (final String server : servers) {
                final String serverP = server;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (waiter) {
                            try {
                                response[0] = request(serverP, host, recordType);
                            } catch (Exception e) {
                                e.printStackTrace();
                                exceptions[0] = new IOException(e);
                            }
                            completedCount[0] += 1;

                            if (completedCount[0] == servers.length || response[0] != null) {
                                waiter.notify();
                            }
                        }
                    }
                });
            }

            synchronized (waiter) {
                try {
                    waiter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (exceptions[0] != null) {
                throw exceptions[0];
            }

            return response[0];
        }

    }

    abstract DnsResponse request(String server, String host, int recordType) throws IOException;
}
