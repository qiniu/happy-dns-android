package com.qiniu.android.dns.dns;

import com.qiniu.android.dns.Record;


import java.io.IOException;
import java.net.IDN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DnsResponse extends DnsMessage {

    private long timestamp;
    private Record.Source source;
    private String server;
    private DnsRequest request;
    private byte[] recordData;

    private int aa;
    private int rCode;
    private List<Record> answerArray;
    private List<Record> authorityArray;
    private List<Record> additionalArray;

    public DnsResponse(String server, Record.Source source, DnsRequest request, byte[] recordData) throws IOException {
        if (recordData == null || recordData.length == 0) {
            throw new IOException("response data is empty");
        }

        this.server = server;
        this.source = source;
        this.request = request;
        this.recordData = recordData;
        this.timestamp = new Date().getTime() / 1000;
        this.parse();
    }

    private void parse() throws IOException {
        if (recordData.length < 12) {
            throw new IOException("response data too small");
        }

        // Header
        parseHeader();

        // Question
        int index = parseQuestion();

        // Answer
        RecordResource answer = new RecordResource("answer", readRecordDataInt16(6), index);
        parseResourceRecord(answer);
        answerArray = answer.records;
        index += answer.length;

        // Authority
        RecordResource authority = new RecordResource("authority", readRecordDataInt16(8), index);
        parseResourceRecord(authority);
        authorityArray = authority.records;
        index += authority.length;

        // Additional
        RecordResource additional = new RecordResource("additional", readRecordDataInt16(10), index);
        parseResourceRecord(additional);
        additionalArray = additional.records;
    }

    private void parseHeader() throws IOException {
        messageId = readRecordDataInt16(0);

        // question id 不匹配
        if (messageId != request.messageId) {
            throw new IOException("question id error");
        }

        // |00|01|02|03|04|05|06|07|
        // |QR|  OPCODE   |AA|TC|RD|
        int field0 = readRecordDataInt8(2);
        int qr = readRecordDataInt8(2) & 0x80;
        // 非 dns 响应数据
        if (qr == 0) {
            throw new IOException("not a response data");
        }

        opCode = (field0 >> 3) & 0x07;
        aa = (field0 >> 2) & 0x01;
        rd = field0 & 0x01;

        // |00|01|02|03|04|05|06|07|
        // |RA|r1|r2|r3| RCODE     |
        int field1 = readRecordDataInt8(3);
        ra = (field1 >> 7) & 0x01;
        rCode = field1 & 0x0F;
    }

    private int parseQuestion() throws IOException {
        int index = 12;
        int qdCount = readRecordDataInt16(4);
        while (qdCount > 0) {
            RecordName recordName = getNameFrom(index);
            if (recordName == null) {
                throw new IOException("read Question error");
            }

            index += recordName.skipLength + 4;
            qdCount--;
        }
        return index;
    }

    private void parseResourceRecord(RecordResource resource) throws IOException {
        int index = resource.from;
        int count = resource.count;

        while (count > 0) {
            RecordName recordName = getNameFrom(index);
            if (recordName == null) {
                throw new IOException("read " + resource.name + " error");
            }

            index += recordName.skipLength;

            int type = readRecordDataInt16(index);
            index += 2;
            int clazz = readRecordDataInt16(index);
            index += 2;
            int ttl = readRecordDataInt32(index);
            index += 4;
            int rdLength = readRecordDataInt16(index);
            index += 2;
            String value = readData(type, index, rdLength);

            if (clazz == 0x01 && (type == Record.TYPE_CNAME || type == request.getRecordType())) {
                Record record = new Record(value, type, ttl, timestamp, source, server);
                resource.addRecord(record);
            }

            index += rdLength;
            count--;
        }
        resource.length = index - resource.from;
    }

    private RecordName getNameFrom(int from) throws IOException {
        int partLength = 0;
        int index = from;
        StringBuilder name = new StringBuilder();
        RecordName recordName = new RecordName();

        int maxLoop = 128;
        do {
            partLength = readRecordDataInt8(index);
            if ((partLength & 0xc0) == 0xc0) {
                // name pointer
                if (recordName.skipLength < 1) {
                    recordName.skipLength = index + 2 - from;
                }
                index = (partLength & 0x3f) << 8 | readRecordDataInt8(index + 1);
                continue;
            } else if ((partLength & 0xc0) > 0) {
                return null;
            } else {
                index++;
            }

            if (partLength > 0) {
                if (name.length() > 0) {
                    name.append(".");
                }

                byte[] nameData = Arrays.copyOfRange(recordData, index, index + partLength);
                name.append(IDN.toUnicode(new String(nameData)));
                index += partLength;
            }
        } while (partLength > 0 && (--maxLoop) > 0);

        recordName.name = name.toString();
        if (recordName.skipLength < 1) {
            recordName.skipLength = index - from;
        }
        return recordName;
    }

    private String readData(int recordType, int from, int length) throws IOException {
        String dataString = null;
        switch (recordType) {
            case Record.TYPE_A: {
                if (length == 4) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(readRecordDataInt8(from));
                    for (int i = 1; i < 4; i++) {
                        builder.append(".");
                        builder.append(readRecordDataInt8(from + i));
                    }
                    dataString = builder.toString();
                }
            }
            break;
            case Record.TYPE_AAAA: {
                if (length == 16) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < 16; i += 2) {
                        builder.append(i > 0 ? ":" : "");
                        builder.append(readRecordDataInt8(from + i));
                        builder.append(readRecordDataInt8(from + i + 1));
                    }
                    dataString = builder.toString();
                }
            }
            break;
            case Record.TYPE_CNAME: {
                if (length > 1) {
                    RecordName name = getNameFrom(from);
                    dataString = name.name;
                }
            }
            break;
            case Record.TYPE_TXT: {
                if (length > 0 && (from + length) < recordData.length) {
                    byte[] data = Arrays.copyOfRange(recordData, from, from + length);
                    String dataValue = new String(data);
                    dataString = IDN.toUnicode(dataValue);
                }
            }
            break;
            default:
                break;
        }
        return dataString;
    }


    private int readRecordDataInt8(int from) throws IOException {
        if (from >= recordData.length) {
            throw new IOException("read response data out of range");
        }
        return recordData[from] & 0xFF;
    }

    private short readRecordDataInt16(int from) throws IOException {
        if ((from + 1) >= recordData.length) {
            throw new IOException("read response data out of range");
        }
        int a = (recordData[from] & 0xFF) << 8;
        int b = recordData[from + 1] & 0xFF;
        return (short) (a + b);
    }

    private int readRecordDataInt32(int from) throws IOException {
        if ((from + 3) >= recordData.length) {
            throw new IOException("read response data out of range");
        }
        return ((recordData[from] & 0xFF) << 32) + ((recordData[from + 1] & 0xFF) << 24) + ((recordData[from + 2] & 0xFF) << 16) + ((recordData[from + 3] & 0xFF) << 8);
    }


    public int getAA() {
        return aa;
    }

    public int getRCode() {
        return rCode;
    }

    public List<Record> getAnswerArray() {
        return answerArray;
    }

    public List<Record> getAdditionalArray() {
        return additionalArray;
    }

    public List<Record> getAuthorityArray() {
        return authorityArray;
    }

    private static class RecordResource {
        private final String name;
        private final int count;
        private final int from;
        private int length;
        private final List<Record> records;

        private RecordResource(String name, int count, int from) {
            this.name = name;
            this.count = count;
            this.from = from;
            this.length = 0;
            this.records = new ArrayList<>();
        }

        private void addRecord(Record record) {
            if (record != null) {
                records.add(record);
            }
        }
    }

    private static class RecordName {
        private int skipLength;
        private String name;
    }
}
