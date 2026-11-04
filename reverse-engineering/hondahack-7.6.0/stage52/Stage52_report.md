# Stage 52 — 网络端点与 Manifest 可达性关联

## 结果
- 网络 API 调用点：177；涉及方法：53
- URL 字符串位置：12；涉及方法：10
- 同方法 URL/API 候选配对：3
- 位于 Manifest 根直接静态可达类中的网络 API 方法：24
- 位于 Manifest 组件类本身的网络 API 方法：7
- 同方法未匹配网络 API 的 URL 字符串位置：9

## 输出
same_method_url_network_api_candidates.tsv、network_method_reachability.tsv、url_literal_without_network_api_same_method.tsv、stage52_summary.json。

## 限制
URL 与 API 仅按精确调用方法关联，不是参数数据流或真实 socket 目的地；Manifest 可达性是全局直接静态可达类集合，不能确定具体哪个组件到达某方法；同方法共现不证明该 URL 被该 API 使用。动态 URL、反射、JNI/native、框架网络及运行时行为未解析。本阶段不导出 Java/Smali 源码，不构建 APK。