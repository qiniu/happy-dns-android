package com.qiniu.android.dns.local;

import com.qiniu.android.dns.DnsException;
import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by bailong on 15/6/16.
 */
public final class Resolver implements IResolver {
    private static final Random random = new Random();

    final InetAddress address;
    private final int timeout;
    private boolean useIpv6;

    public Resolver(InetAddress address) {
        this(address, DNS_DEFAULT_TIMEOUT, false);
    }

    public Resolver(InetAddress address, int timeout) {
        this(address, timeout, false);
    }

    public Resolver(InetAddress address, int timeout, boolean useIpv6) {
        this.address = address;
        this.timeout = timeout;
        this.useIpv6 = useIpv6;
    }

    public Resolver setUseIpv6(boolean useIpv6) {
        this.useIpv6 = useIpv6;
        return this;
    }


    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        List<Record> records = new ArrayList<>();
        if (useIpv6) {
            records.addAll(lookup(domain, true));
        }
        records.addAll(lookup(domain, false));
        return records.toArray(new Record[0]);
    }


    private List<Record> lookup(Domain domain, boolean ipv6) throws IOException {
        int id;
        int type = ipv6 ? Record.TYPE_AAAA : Record.TYPE_A;
        synchronized (random) {
            id = random.nextInt() & 0XFF;
        }
        byte[] query = DnsMessage.buildQuery(domain.domain, type, id);
        byte[] answer = udpCommunicate(query);
        if (answer == null) {
            throw new DnsException(domain.domain, "cant get answer");
        }

        DnsMessage.parseResponse(answer, id, domain.domain);
        return new ArrayList<>(Arrays.asList(DnsMessage.parseResponse(answer, id, domain.domain)));
    }

    private byte[] udpCommunicate(byte[] question) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(question, question.length,
                    address, 53);
            socket.setSoTimeout(timeout * 1000);
            socket.send(packet);
            packet = new DatagramPacket(new byte[1500], 1500);
            socket.receive(packet);

            return packet.getData();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
