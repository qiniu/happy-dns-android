package com.qiniu.android.dns;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by bailong on 15/7/3.
 */
public final class Network {
    private static String previousIp = "";

    //    use udp socket connect, tcp socket would connect when new.
    public static String getIp() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress addr = InetAddress.getByName("114.114.114.114");
            socket.connect(addr, 53);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        InetAddress local = socket.getLocalAddress();
        socket.close();
        return local.getHostAddress();
    }


    public static synchronized boolean isNetworkChanged() {
        String nowIp = getIp();
        if (nowIp.equals(previousIp)) {
            return false;
        }
        previousIp = nowIp;
        return true;
    }
}
