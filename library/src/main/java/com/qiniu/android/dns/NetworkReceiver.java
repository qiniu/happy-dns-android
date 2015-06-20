package com.qiniu.android.dns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by bailong on 15/6/19.
 */
public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (mdnsManager == null){
            return;
        }
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        if (activeInfo != null){
            mdnsManager.onNetworkChange(activeInfo, null);
        }
    }

    private static DnsManager mdnsManager;

    public static void setDnsManager(DnsManager dnsManager){
        mdnsManager = dnsManager;
    }
}
