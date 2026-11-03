# Stage 50 — Preference 键读写关系与组件关联

## 结果
- 已解析 Preference key：62
- 读写调用点：136
- 同时存在读和写的 key：29
- 仅读 key：29
- 仅写 key：4
- 涉及调用类：43
- 归属 Manifest 组件类的调用点：34（Activity 12、Service 21、Receiver 1）

## 输出
preference_key_read_write_map.tsv、preference_key_summary.tsv、stage50_summary.json。

## 限制
只包括 Stage 48 线性传播解析出的 preference keys；组件归属按精确调用类描述符匹配，helper/间接调用不归属；不推断 preference 文件名、运行时值、控制流或完整数据来源；动态 key、反射和 native 访问未覆盖。本阶段不导出 Java/Smali 源码，不构建 APK。