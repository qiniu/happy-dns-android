package com.qiniu.android.dns.local;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.Record;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * Created by bailong on 15/6/16.
 */
public final class Resolver implements IResolver {
    private static final Random random = new Random();

    private final InetAddress address;

    public Resolver(InetAddress address) {
        this.address = address;
    }

    @Override
    public Record[] query(Domain domain) throws IOException {
        short id;
        synchronized (random) {
            id = (short) random.nextInt();
        }
        byte[] query = DnsMessage.buildQuery(domain.domain, id);
        byte[] answer = udpCommunicate(query);
        if (answer == null) {
            return null;
        }
        Record[] r = DnsMessage.parseResponse(answer, id);
        return r;
    }

    private byte[] udpCommunicate(byte[] question) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(question, question.length,
                    address, 53);
            socket.setSoTimeout(5000);
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

//    todo
//    public byte[] tcpQuery(byte[] question){
//
//    }
}
