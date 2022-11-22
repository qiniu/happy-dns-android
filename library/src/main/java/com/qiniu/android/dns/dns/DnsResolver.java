package com.qiniu.android.dns.dns;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.R;
import com.qiniu.android.dns.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class DnsResolver implements IResolver {

    private static ScheduledExecutorService timeoutExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static ExecutorService defaultExecutorService = Executors.newFixedThreadPool(4);

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
        this.recordType = recordType;
        this.timeout = timeout > 0 ? timeout : DNS_DEFAULT_TIMEOUT;
        this.servers = servers;
        this.executorService = null;
    }

    public DnsResolver(String[] servers, int recordType, int timeout, ExecutorService executorService) {
        if (servers != null && servers.length > 1 && executorService == null) {
            executorService = defaultExecutorService;
        }
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

        final RequestCanceller canceller = new RequestCanceller();
        if (servers.length == 1 || executorService == null) {
            DnsResponse response = null;
            for (String server : servers) {
                response = request(canceller, server, host, recordType);
                if (response != null) {
                    break;
                }
            }
            return response;
        } else {
            final ResponseComposition responseComposition = new ResponseComposition();

            // 超时处理
            timeoutExecutorService.schedule(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    synchronized (responseComposition) {
                        responseComposition.notify();
                        responseComposition.exception = new IOException("resolver timeout for server:" + servers + " host:" + host);
                    }
                    return null;
                }
            }, timeout, TimeUnit.SECONDS);

            // 返回一个即结束
            List<Future<?>> futures = new ArrayList<>();
            for (final String server : servers) {
                Future<?> future = executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        DnsResponse response = null;
                        IOException exception = null;

                        try {
                            response = request(canceller, server, host, recordType);
                        } catch (Exception e) {
                            e.printStackTrace();
                            exception = new IOException(e);
                        }

                        synchronized (responseComposition) {
                            responseComposition.completedCount += 1;

                            if (responseComposition.response == null) {
                                responseComposition.response = response;
                            }

                            if (responseComposition.exception == null) {
                                responseComposition.exception = exception;
                            }

                            if (responseComposition.completedCount == servers.length || responseComposition.response != null) {
                                responseComposition.notify();
                            }
                        }
                    }
                });
                futures.add(future);
            }

            synchronized (responseComposition) {
                try {
                    responseComposition.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            canceller.cancel();

            if (responseComposition.exception != null && responseComposition.response == null) {
                throw responseComposition.exception;
            }

            return responseComposition.response;
        }

    }

    private static class ResponseComposition {
        DnsResponse response;
        IOException exception;
        int completedCount;

        ResponseComposition() {
            this.completedCount = 0;
        }
    }

    static class RequestCanceller {
        Queue<Runnable> cancelActions = new ConcurrentLinkedQueue<>();

        void addCancelAction(Runnable cancelAction) {
            if (cancelAction != null) {
                cancelActions.add(cancelAction);
            }
        }

        void cancel() {
            for (Runnable cancelAction : cancelActions) {
                if (cancelAction != null) {
                    cancelAction.run();
                }
            }
        }
    }

    abstract DnsResponse request(RequestCanceller canceller, String server, String host, int recordType) throws IOException;
}
