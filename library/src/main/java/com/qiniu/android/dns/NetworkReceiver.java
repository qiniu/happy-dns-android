package com.qiniu.android.dns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by bailong on 15/6/19.
 */
public final class NetworkReceiver extends BroadcastReceiver {
    private static final Uri PREFERRED_APN_URI = Uri
            .parse("content://telephony/carriers/preferapn");
    private static DnsManager mdnsManager;

    public static NetworkInfo createNetInfo(android.net.NetworkInfo info, Context context) {
        if (info == null) {
            return NetworkInfo.noNetwork;
        }

        NetworkInfo.NetSatus net;
        int provider = NetworkInfo.ISP_GENERAL;
        int main = info.getType();
        if (main == ConnectivityManager.TYPE_WIFI) {
            net = NetworkInfo.NetSatus.WIFI;
            provider = NetworkInfo.ISP_GENERAL;
        } else {
            net = NetworkInfo.NetSatus.MOBILE;
//            参考 http://blog.csdn.net/yinkai1205/article/details/8983861

//            判断是否电信:

            final Cursor c = context.getContentResolver().query(
                    PREFERRED_APN_URI, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                final String user = c.getString(c
                        .getColumnIndex("user"));
                if (!TextUtils.isEmpty(user)) {
                    if (user.startsWith("ctwap") || user.startsWith("ctnet")) {
                        provider = NetworkInfo.ISP_CTC;
                    }
                }
            }
            c.close();
            if (provider != NetworkInfo.ISP_CTC) {
// 判断是移动联通wap:
                String netMode = info.getExtraInfo();
                if (netMode != null) {
                    // 通过apn名称判断是否是联通和移动wap
                    netMode = netMode.toLowerCase(Locale.getDefault());

                    if (netMode.equals("cmwap") || netMode.equals("cmnet")) {
                        provider = NetworkInfo.ISP_CMCC;
                    } else if (netMode.equals("3gnet")
                            || netMode.equals("uninet")
                            || netMode.equals("3gwap")
                            || netMode.equals("uniwap")) {
                        provider = NetworkInfo.ISP_CNC;
                    }
                }
            }
        }

        return new NetworkInfo(net, provider);
    }

    public static void setDnsManager(DnsManager dnsManager) {
        mdnsManager = dnsManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mdnsManager == null) {
            return;
        }
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeInfo = manager.getActiveNetworkInfo();

        NetworkInfo info = createNetInfo(activeInfo, context);
        mdnsManager.onNetworkChange(info);
    }
}
