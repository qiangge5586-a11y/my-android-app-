# Stage 42 — 网络 API 与端点字符串索引

## 方法
扫描 DEX invoke 操作数中的 HttpURLConnection/URL、Socket、TLS/SSL、WebView、OkHttp、Apache HTTP、ConnectivityManager/Wi-Fi 和输入输出流/DownloadManager API；同时提取 const-string 操作数中的 HTTP(S) 字面量。

## 结果
- 扫描语义文件：1540
- 网络 API 调用点：177
- 涉及调用类：34
- URL 字符串位置：12
- 唯一 URL 字面量：11

### API 分类
- URL/URI：48
- HttpURLConnection：49
- TLS/SSL：6
- 网络状态：9
- WebView：14
- 下载/上传流：7
- Socket：44

## 输出
- network_api_sites.tsv：网络 API 调用点
- network_class_summary.tsv：调用类统计
- url_string_sites.tsv：URL 字符串位置
- stage42_summary.json：机器可读摘要

## 限制
API 引用和 URL 字面量不等于运行时真实网络流量；字符串可能是测试、文档或未启用代码。TLS 信任行为需要进一步结合调用链和配置核查；native 网络及动态拼接端点未覆盖。本阶段不导出 Java/Smali 源码，不构建 APK。
