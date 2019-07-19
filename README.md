# Qiniu Happy DNS for Android

[![@qiniu on weibo](http://img.shields.io/badge/weibo-%40qiniutek-blue.svg)](http://weibo.com/qiniutek)
[![Software License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](LICENSE.md)
[![Build Status](https://travis-ci.org/qiniu/happy-dns-android.svg?branch=master)](https://travis-ci.org/qiniu/happy-dns-android)
[![Latest Stable Version](http://img.shields.io/maven-central/v/com.qiniu/happy-dns.svg)](https://github.com/qiniu/happy-dns-android/releases)


## 安装

### 直接安装


### 通过maven

## 使用方法
DnsManager 可以创建一次，一直使用。
```java
    IResolver[] resolvers = new IResolver[3];
    resolvers[0] = AndroidDnsServer.defaultResolver(); //系统默认 DNS 服务器
    resolvers[1] = new Resolver(InetAddress.getByName("119.29.29.29")); //自定义 DNS 服务器地址
    resolvers[2] = new QiniuDns(accountId, encryptKey, expireTimeMs); //七牛 http dns 服务
    DnsManager dns = new DnsManager(NetworkInfo.normal(), resolvers);
```

其中，七牛 http dns 服务所需的参数如下：

| 参数             | 描述                                    |
|------------------|-----------------------------------------|
| accountId        |  账户名称，从七牛控制台获取             |
| encryptKey       | 加密所需的 key，从七牛控制台获取        |
| expireTimeSecond | Unix 时间戳，单位为秒，该时间后请求过期 |


`QiniuDns` 提供了 `setHttps` 与 `setEncrypted` 两个方法，用于设置是否启用 SSL，与请求的 URL 是否加密。

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
    resolvers[0] = new DnspodFree();
    resolvers[1] = AndroidDnsServer.defaultResolver();
    dns = new DnsManager(NetworkInfo.normal, resolvers);
}else{
	IResolver[] resolvers = new IResolver[2];
    resolvers[0] = AndroidDnsServer.defaultResolver();
    resolvers[1] = new Resolver(InetAddress.getByName("8.8.8.8"));
    dns = new DnsManager(NetworkInfo.normal, resolvers);
}
```
## 代码许可

The MIT License (MIT).详情见 [License文件](https://github.com/qiniu/happy-dns-android/blob/master/LICENSE).
