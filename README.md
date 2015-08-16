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
    IResolver[] resolvers = new IResolver[2];
    resolvers[0] = AndroidDnsServer.defaultResolver();
    resolvers[1] = new Resolver(InetAddress.getByName("114.114.115.115"));
    DnsManager dns = new DnsManager(NetworkInfo.normal(), resolvers);
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


## 代码许可

The MIT License (MIT).详情见 [License文件](https://github.com/qiniu/happy-dns-android/blob/master/LICENSE).
