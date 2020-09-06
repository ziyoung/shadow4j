# shadow4j

A proxy server written in Java.

> 考虑科学上网时才用这个，直接用中文介绍吧！

### 原理

[Shadowsocks](https://shadowsocks.org/en/spec/Protocol.html) 网站对该解析有非常详细的介绍。代理工具的工作流程如下图所示。

<img src="/doc/connect.svg" />

### 测试

在开发过程中如何保证程序的正确性呢？[go-shadowsocks2](https://github.com/shadowsocks/go-shadowsocks2) 是官方的 go 语言实现，是可信的。用 go-shadowsocks
来测试我们的程序即可。具体体现在以下两点。

##### 1. 单元测试

在加密算法与秘钥一致时，相同的内容进行加密或者解密，输出必然一样。因此先记录 go 语言的输出结果，然后在对比 Java 语言的输出结果。
结果相同，证明代码逻辑正确。`shadow` 模块下的单元测试就是这么做的。

##### 2. 程序流程的测试

为了测试整个程序运行是否正常，我写了一个 [socks5-dump](https://github.com/ziyoung/socks5-dump) 来发送 socks5 请求，通过对比 socks5-dump 的输出来验证程序。

另外我将这些用来测试的程序都打包成 docker 镜像了，在开发时可以直接使用。使用了 docker 镜像，后续可以添加更多的自动化测试。

