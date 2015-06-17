package com.qiniu.android.dns;

import android.content.Context;

/**
 * Created by bailong on 15/6/12.
 */
public class DnsManager {
    // 使用Application context 进行初始化，监听网络变化，并作dns 缓存清理，
//    config 为json 字符串
    public DnsManager(Context c, String config) {

    }

    //    增加一个 Dns 查询器实现，根据权重选择优先级顺序
    public void addQueryer(IResolver q, int weight) {

    }

    //    查询域名，返回IP列表
    public String[] query(String domain) {
        return null;
    }
}
