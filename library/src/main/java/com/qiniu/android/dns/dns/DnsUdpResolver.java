package com.qiniu.android.dns.dns;

import com.qiniu.android.dns.Record;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

public class DnsUdpResolver extends DnsResolver {
    private static final int DnsUdpPort = 53;

    public DnsUdpResolver(String serverIP) {
        super(serverIP);
    }

    public DnsUdpResolver(String serverIP, int timeout) {
        super(serverIP, timeout);
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
    DnsResponse request(RequestCanceller canceller, String server, String host, int recordType) throws IOException {
        double d = Math.random();
        short messageId = (short) (d * 0xFFFF);
        DnsRequest request = new DnsRequest(messageId, recordType, host);
        byte[] requestData = request.toDnsQuestionData();

        InetAddress address = InetAddress.getByName(server);
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(requestData, requestData.length,
                    address, DnsUdpPort);
            socket.setSoTimeout(timeout * 1000);

            final DatagramSocket finalSocket = socket;
            canceller.addCancelAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        finalSocket.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        finalSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            socket.send(packet);
            packet = new DatagramPacket(new byte[1500], 1500);
            socket.receive(packet);
            return new DnsResponse(server, Record.Source.Udp, request, packet.getData());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
