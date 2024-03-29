#Changelog
## 2.0.1 (2021-08-19)
* 优化多 Server 并发解析

## 2.0.0 (2021-08-19)
* 处理加密模式 ECB 不安全问题
* 删除 com.qiniu.android.dns.http.DnspodEnterprise
* 删除 com.qiniu.android.dns.util.BitSet.java
* 删除 com.qiniu.android.dns.util.DES.java
* 删除 com.qiniu.android.dns.util.MD5.java


## 1.0.1 (2022-06-02)

### 修改
*  udp 和 doh resolver 共享 ThreadPool

## 1.0.0 (2021-09-02)

### 增加
* 增加 Doh Resolver
* 增加 Udp Resolver

### 修改
*  调整 Record api
*  调整 DnsManager api，支持返回 IPv6

### 删除
* 删除 Resolver
* 删除 Qiniu Dns
* 删除 DnspodFree 

## 0.2.18 (2021-05-11)

### 修改
* 处理 connectivityManager.registerNetworkCallback 可能出现的异常

## 0.2.17 (2020-10-14)

### 修改
* 处理getByCommand偶现异常问题

## 0.2.16 (2020-07-06)

### 增加
* DnsManager增加queryRecords方法
* Record增加source属性

## 0.2.15 (2018-05-06)

### 修正
*  更新Dns

## 0.2.14 (2018-04-03)

### 增加
* qiniu dns 支持

## 0.2.12 (2017-07-05)

### 修正
* 对 host 添加并发锁

## 0.2.11 (2016-11-14)

### 增加
* 根据时区判断是否使用httpdns的API

## 0.2.10 (2016-08-02)

### 修正
* http访问 系统底层可能抛出 SecurityException 或者 NPE，需要catch

## 0.2.9 (2016-07-06)

### 增加
* 可以配置timeout

## 0.2.8 (2016-07-06)

### 修正
* 优化 lrucache
* ip 不采用随机，使用固定轮换

## 0.2.7 (2016-03-05)

### 修正
* record cache 过期后不应返回 by 小影
* record 默认 600s 最小 by 小影

## 0.2.6 (2016-03-02)

### 修正
* 传入null的行为改为抛出IOException
* 传入IP 直接返回该IP

## 0.2.5 (2015-12-24)

### 修正
* httpdns 增加 timeout设置

## 0.2.4 (2015-10-09)

### 修正
* 将HttpClient改为 HttpUrlConnection, 兼容android 6.0

## 0.2.3.2 (2015-08-07)

### 修正
* 过期判断

## 0.2.3.1 (2015-07-30)

### 修正
* 修正 跳过resolver 的bug 

## 0.2.3 (2015-07-26)

### 增加
* 增加外部IP排序接口

## 0.2.2 (2015-07-23)

### 增加
* 支持dnspod 企业版

## 修改
* 调整内部 resolver 选择

## 0.2.1 (2015-07-10)

### 修正
* 从hosts取不出来需要抛出异常

## 0.2.0 (2015-07-10)

### 修改
* 调整接口为惯用方式，无数据时抛出异常

### 增加
* 增加处理httpdns 不去解析域名的判断

## 0.1.1 (2015-06-21)

### 增加
* 发布到maven

## 0.0.1 (2015-06-21)

### 增加
* 基本功能完成
* 支持httpdns
* 支持localdns

