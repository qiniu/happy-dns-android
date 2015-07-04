package com.qiniu.android.dns;

/**
 * Created by bailong on 15/6/21.
 */
public final class NetworkInfo {
    /**
     * 默认网络供应商
     */
    public static final int ISP_GENERAL = 0;
    /**
     * 中国电信
     */
    public static final int ISP_CTC = 1;
    /**
     * 中国电信
     */
    public static final int ISP_DIANXIN = ISP_CTC;
    /**
     * 中国联通
     */
    public static final int ISP_CNC = 2;
    /**
     * 中国联通
     */
    public static final int ISP_LIANTONG = ISP_CNC;
    /**
     * 中国移动
     */
    public static final int ISP_CMCC = 3;
    /**
     * 中国移动
     */
    public static final int ISP_YIDONG = ISP_CMCC;
    /**
     * 其他运营商
     */
    public static final int ISP_OTHER = 999;
    public static final NetworkInfo noNetwork =
            new NetworkInfo(NetSatus.NO_NETWORK, ISP_GENERAL);
    public static final NetworkInfo normal =
            new NetworkInfo(NetSatus.WIFI, ISP_GENERAL);
    /**
     * 运营商
     */
    public final int provider;
    /**
     * 网络状态
     */
    public final NetSatus netStatus;

    public NetworkInfo(NetSatus status, int provider) {
        this.netStatus = status;
        this.provider = provider;
    }


    /**
     * 网络状态，没有网络，wifi，或者2G/3G/4G网络
     */
    public enum NetSatus {
        NO_NETWORK, WIFI, MOBILE;
    }
}
