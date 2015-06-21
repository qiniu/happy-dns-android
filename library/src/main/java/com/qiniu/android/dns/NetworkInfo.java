package com.qiniu.android.dns;

import android.net.ConnectivityManager;

/**
 * Created by bailong on 15/6/21.
 */
public final class NetworkInfo {

    final int netType;

    public static final int TYPE_WIFI = 0;


    public NetworkInfo(android.net.NetworkInfo info){
        int main = info.getType();
        if (main == ConnectivityManager.TYPE_WIFI){
            netType = TYPE_WIFI;
        }else {
            netType = 1<<16 + info.getSubtype();
        }
    }

    /** {@hide}  only for test*/
    public NetworkInfo(int netType){
        this.netType = netType;
    }
}
