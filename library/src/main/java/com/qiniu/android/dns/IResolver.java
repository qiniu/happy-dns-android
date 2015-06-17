package com.qiniu.android.dns;

import java.io.IOException;

/**
 * Created by bailong on 15/6/12.
 */

// 同步的域名查询接口，没有独立线程, 可以有local dns, httpdns 等实现
public interface IResolver {
    Record[] query(Domain domain) throws IOException;
}
