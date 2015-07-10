package com.qiniu.android.dns.local;

import com.qiniu.android.dns.DnsException;
import com.qiniu.android.dns.Record;
import com.qiniu.android.dns.util.BitSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 * reference github/rtreffer/minidns.
 */
public final class DnsMessage {

    public static byte[] buildQuery(String domain, int id) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        DataOutputStream dos = new DataOutputStream(baos);
        BitSet bits = new BitSet();

//        recursionDesired
        bits.set(8);

        try {
            dos.writeShort((short) id);
            dos.writeShort((short) bits.value());

//        questions count
            dos.writeShort(1);

//      no  answer
            dos.writeShort(0);

//      no nameserverRecords
            dos.writeShort(0);

//      no  additionalResourceRecords
            dos.writeShort(0);

            dos.flush();
            writeQuestion(baos, domain);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        return baos.toByteArray();
    }

    private static void writeDomain(OutputStream out, String domain) throws IOException {
        for (String s : domain.split("[.\u3002\uFF0E\uFF61]")) {
            byte[] buffer = IDN.toASCII(s).getBytes();
            out.write(buffer.length);
            out.write(buffer, 0, buffer.length); // ?
        }
        out.write(0);
    }

    private static void writeQuestion(OutputStream out, String domain) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        writeDomain(out, domain);
//        type A
        dos.writeShort(1);
//        class internet
        dos.writeShort(1);
    }

    public static Record[] parseResponse(byte[] response, int id, String domain) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(response);
        DataInputStream dis = new DataInputStream(bis);
        int answerId = dis.readUnsignedShort();
        if (answerId != id) {
            throw new DnsException(domain, "the answer id " + answerId + " is not match " + id);
        }
        int header = dis.readUnsignedShort();
        boolean recursionDesired = ((header >> 8) & 1) == 1;
        boolean recursionAvailable = ((header >> 7) & 1) == 1;
        if (!(recursionAvailable && recursionDesired)) {
            throw new DnsException(domain, "the dns server cant support recursion ");

        }

        int questionCount = dis.readUnsignedShort();
        int answerCount = dis.readUnsignedShort();
//        nameserver Count
        dis.readUnsignedShort();
//        additionalResourceRecordCount
        dis.readUnsignedShort();

//         ignore questions
        readQuestions(dis, response, questionCount);

        return readAnswers(dis, response, answerCount);
//        ignore auth
//        ignore additional
    }

    /**
     * Parse a domain name starting at the current offset and moving the input
     * stream pointer past this domain name (even if cross references occure).
     *
     * @param dis  The input stream.
     * @param data The raw data (for cross references).
     * @return The domain name string.
     * @throws IOException Should never happen.
     */
    private static String readName(DataInputStream dis, byte data[])
            throws IOException {
        int c = dis.readUnsignedByte();
        if ((c & 0xc0) == 0xc0) {
            c = ((c & 0x3f) << 8) + dis.readUnsignedByte();
            HashSet<Integer> jumps = new HashSet<Integer>();
            jumps.add(c);
            return readName(data, c, jumps);
        }
        if (c == 0) {
            return "";
        }
        byte b[] = new byte[c];
        dis.readFully(b);
        String s = IDN.toUnicode(new String(b));
        String t = readName(dis, data);
        if (t.length() > 0) {
            s = s + "." + t;
        }
        return s;
    }

    /**
     * Parse a domain name starting at the given offset.
     *
     * @param data   The raw data.
     * @param offset The offset.
     * @param jumps  The list of jumps (by now).
     * @return The parsed domain name.
     * @throws IOException on cycles.
     */
    private static String readName(
            byte data[],
            int offset,
            HashSet<Integer> jumps
    ) throws IOException {
        int c = data[offset] & 0xff;
        if ((c & 0xc0) == 0xc0) {
            c = ((c & 0x3f) << 8) + (data[offset + 1] & 0xff);
            if (jumps.contains(c)) {
                throw new DnsException("", "Cyclic offsets detected.");
            }
            jumps.add(c);
            return readName(data, c, jumps);
        }
        if (c == 0) {
            return "";
        }
        String s = new String(data, offset + 1, c);
        String t = readName(data, offset + 1 + c, jumps);
        if (t.length() > 0) {
            s = s + "." + t;
        }
        return s;
    }

    private static void readQuestions(DataInputStream dis, byte[] data, int count) throws IOException {
        while (count-- > 0) {
            readName(dis, data);
//            type
            dis.readUnsignedShort();
//            class
            dis.readUnsignedShort();
        }
    }

    private static Record[] readAnswers(DataInputStream dis, byte[] data, int count) throws IOException {
        int offset = 0;
        Record[] ret = new Record[count];
        while (count-- > 0) {
            ret[offset++] = readRecord(dis, data);
        }
        return ret;
    }

    private static Record readRecord(DataInputStream dis, byte[] data) throws IOException {
        readName(dis, data);
        int type = dis.readUnsignedShort();
//            class
        dis.readUnsignedShort();

        long ttl = (((long) dis.readUnsignedShort()) << 16) +
                dis.readUnsignedShort();
        int payloadLength = dis.readUnsignedShort();
        String payload = null;
        switch (type) {
            case Record.TYPE_A:
                byte[] ip = new byte[4];
                dis.readFully(ip);
                payload = InetAddress.getByAddress(ip).getHostAddress();
                break;
            case Record.TYPE_CNAME:
                payload = readName(dis, data);
                break;
            default:
                payload = null;
                for (int i = 0; i < payloadLength; i++) {
                    dis.readByte();
                }
                break;
        }
        if (payload == null) {
            throw new UnknownHostException("no record");
        }
        return new Record(payload, type, (int) ttl, System.currentTimeMillis() / 1000);
    }
}
