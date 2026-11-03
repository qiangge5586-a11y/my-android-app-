# Stage 41 — 反射与动态分派边界索引

## 方法
扫描 invoke 操作数中对 Java 反射、Class 查询、ClassLoader/DexClassLoader、Proxy、MethodHandle 和 ServiceLoader API 的引用，形成调用点及调用类汇总。

## 结果
- 扫描语义文件：1540
- 匹配调用点：376
- 涉及调用类：75
- reflect_method：153
- reflect_field：62
- class_lookup：138
- classloader：22
- proxy：1
- method_handles：0（未发现匹配）
- service_loader：0（未发现匹配）

## 输出
- dynamic_dispatch_sites.tsv：逐条匹配调用点
- class_dynamic_dispatch_summary.tsv：调用类汇总
- stage41_summary.json：机器可读摘要

## 限制
本阶段仅按 API 描述符和名称匹配，不代表调用一定可达；未解析反射目标、动态加载类名或代理目标。MethodHandle/Proxy 可能形成间接调用，Android 框架、运行时和 native 的动态分派不在覆盖范围内。本阶段不导出 Java/Smali 源码，不构建 APK。
