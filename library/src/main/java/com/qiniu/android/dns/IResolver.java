package com.qiniu.android.dns;

import java.io.IOException;

// 同步的域名查询接口，没有独立线程, 可以有local dns, httpdns 等实现

/**
 * 同步的域名查询接口，可以有local dns, httpdns 等实现
 */
public interface IResolver {
    int DNS_DEFAULT_TIMEOUT = 10; //seconds
    /**
     * 根据域名参数进行查询
     *
     * @param domain 域名参数
     * @param info   网络信息，便于根据运营商选择不同策略
     * @return dns记录列表
     * @throws IOException 劫持或者网络异常
     */
    Record[] resolve(Domain domain, NetworkInfo info) throws IOException;
}
