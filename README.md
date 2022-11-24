# Qiniu Happy DNS for Android

[![@qiniu on weibo](http://img.shields.io/badge/weibo-%40qiniutek-blue.svg)](http://weibo.com/qiniutek)
[![LICENSE](https://img.shields.io/github/license/qiniu/happy-dns-android.svg)](https://github.com/qiniu/happy-dns-android/blob/master/LICENSE)
[![Build Status](https://travis-ci.org/qiniu/happy-dns-android.svg?branch=master)](https://travis-ci.org/qiniu/happy-dns-android)
[![GitHub release](https://img.shields.io/github/v/tag/qiniu/happy-dns-android.svg?label=release)](https://github.com/qiniu/happy-dns-android/releases)
[![codecov](https://codecov.io/gh/qiniu/happy-dns-android/branch/master/graph/badge.svg)](https://codecov.io/gh/qiniu/happy-dns-android)
[![Latest Stable Version](http://img.shields.io/maven-central/v/com.qiniu/happy-dns.svg)](https://github.com/qiniu/happy-dns-android/releases)

## 用途
可以使用114 等第三方dns解析，也可以使用 Doh 协议的 Dns 解析方案，也可以集成 dnspod 等httpdns。另外也有丰富的hosts 域名配置。

## 安装

### 直接安装
将sdk jar文件 复制到项目中去，[jar包下载地址](https://search.maven.org/search?q=com%2Fqiniu%2Fqiniu-android-sdk) 下载对应的jar包

### 通过maven
* Android Studio中添加dependencies 或者 在项目中添加maven依赖
```
implementation 'com.qiniu:happy-dns:2.0.1'
```


## 使用方法
DnsManager 可以创建一次，一直使用。
```java
    IResolver[] resolvers = new IResolver[3];
    resolvers[0] = AndroidDnsServer.defaultResolver(getContext()); //系统默认 DNS 服务器
    resolvers[1] = new DnsUdpResolver("8.8.8.8"); //自定义 DNS 服务器地址
    resolvers[2] = new DohResolver("https://dns.alidns.com/dns-query");
    DnsManager dns = new DnsManager(NetworkInfo.normal(), resolvers);
    Record[] records = dns.queryRecords("www.qiniu.com");
```

## 测试

``` bash
$ ./gradlew connectedAndroidTest
```

## 运行环境

Android 最低要求 2.3

## 代码贡献

详情参考[代码提交指南](https://github.com/qiniu/happy-dns-android/blob/master/CONTRIBUTING.md)。

## 贡献记录

- [所有贡献者](https://github.com/qiniu/happy-dns-android/contributors)

## 联系我们

- 如果需要帮助，请提交工单（在portal右侧点击咨询和建议提交工单，或者直接向 support@qiniu.com 发送邮件）
- 如果有什么问题，可以到问答社区提问，[问答社区](http://qiniu.segmentfault.com/)
- 如果发现了bug， 欢迎提交 [issue](https://github.com/qiniu/happy-dns-android/issues)
- 如果有功能需求，欢迎提交 [issue](https://github.com/qiniu/happy-dns-android/issues)
- 如果要提交代码，欢迎提交 pull request
- 欢迎关注我们的[微信](http://www.qiniu.com/#weixin) [微博](http://weibo.com/qiniutek)，及时获取动态信息。

## 常见问题
- 如果软件有国外的使用情况时，建议初始化程序采取这样的方式，下面代码只是根据时区做简单判断，开发者可以根据自己需要使用更精确的判断方式

```java
DnsManager dns;
if(DnsManager.needHttpDns()){
	IResolver[] resolvers = new IResolver[2];
    // dohResolver 需要配置一个支持 Doh(Dns over http) 协议的 url
    resolvers[0] = new DnhResolver("https://dns.alidns.com/dns-query");
    resolvers[1] = AndroidDnsServer.defaultResolver(getContext());
    dns = new DnsManager(NetworkInfo.normal, resolvers);
}else{
	IResolver[] resolvers = new IResolver[2];
    resolvers[0] = AndroidDnsServer.defaultResolver(getContext());
    resolvers[1] = new DnsUdpResolver("8.8.8.8");
    dns = new DnsManager(NetworkInfo.normal, resolvers);
}
```
## 代码许可

The MIT License (MIT).详情见 [License文件](https://github.com/qiniu/happy-dns-android/blob/master/LICENSE).
