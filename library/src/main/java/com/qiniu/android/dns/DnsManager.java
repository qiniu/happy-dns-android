package com.qiniu.android.dns;

import android.content.Context;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;


/**
 * Created by bailong on 15/6/12.
 */
public final class DnsManager {

    private class Container implements Comparable{
        final IResolver resolver;
        final int weight;

        private Container(IResolver resolver, int weight) {
            this.resolver = resolver;
            this.weight = weight;

        }

        @Override
        public int compareTo(Object another) {
            if (this.weight > ((Container)another).weight){
                return 1;
            }else if (this.weight < ((Container)another).weight){
                return -1;
            }
            return 0;
        }
    }
    private PriorityQueue<Container> resolvers;

    //    增加一个 Dns 查询器实现，根据权重选择优先级顺序
    public void addQueryer(IResolver q, int weight) {
        resolvers.add(new Container(q, weight));
    }

    //    查询域名，返回IP列表
    public String[] query(String domain) {
        return query(new Domain(domain));
    }

    public String[] query(Domain domain){
        for (Container resolver : resolvers) {
            Record[] r = null;
            try {
                r = resolver.resolver.query(domain);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            String[] s = records2Ip(r);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    private static String[] records2Ip(Record[] records){
        ArrayList<String> a = new ArrayList<>(records.length);
        for (Record r:records){
            if (r != null && r.type == Record.TYPE_A){
                a.add(r.value);
            }
        }
        if (a.size() == 0){
            return null;
        }
        return a.toArray(new String[a.size()]);
    }

    public void onNetworkChange(NetworkInfo info, String deviceIp){
        if (info.getSubtype() != netType){
            if (deviceIp == null || (!deviceIp.equals(previousIp))){
                clearCache();
            }
        }
        netType = info.getSubtype();
        previousIp = deviceIp;
    }

    private String previousIp = null;
    private int netType = 0;
    private void clearCache(){

    }
}
