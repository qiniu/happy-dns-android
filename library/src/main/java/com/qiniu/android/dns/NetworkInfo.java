package com.qiniu.android.dns;

import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

/**
 * Created by bailong on 15/6/21.
 */
public final class NetworkInfo {

    public final int provider;

    public final NetSatus netStatus;

    public enum NetSatus{
        NO_NETWORK,WIFI,MOBILE;
    }

    public static final int ISP_GENERAL = 0;
    public static final int ISP_CTC = 1;
    public static final int ISP_DIANXIN = ISP_CTC;
    public static final int ISP_CNC = 2;
    public static final int ISP_LIANTONG = ISP_CNC;
    public static final int ISP_CMCC = 3;
    public static final int ISP_YIDONG = ISP_CMCC;
    public static final int ISP_OTHER = 999;

    public NetworkInfo(NetSatus status, int provider){
        this.netStatus = status;
        this.provider = provider;
    }

    public static NetworkInfo noNetwork(){
        return new NetworkInfo(NetSatus.NO_NETWORK, ISP_GENERAL);
    }

    public static NetworkInfo normal(){
        return new NetworkInfo(NetSatus.WIFI, ISP_GENERAL);
    }
}
