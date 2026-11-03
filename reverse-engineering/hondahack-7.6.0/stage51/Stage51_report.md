# Stage 51 — 存储、Preference 与字段关系候选分析

## 方法
将 Stage 48 存储 API 调用、有限字符串参数解析结果、Stage 38 字段访问点、Stage 50 Preference key 读写表，以及 Stage 36 内部方法调用边按精确方法描述符关联。重点输出同方法共现关系与存储方法的一跳直接调用邻居。

## 结果
- 存储 API 调用点：242；涉及方法：136
- 字段访问点：4,716
- Preference 读写调用点：136
- 同时出现存储 API 与字段访问的方法：77
- Preference key—字段候选配对：2,647
- 已解析存储字符串—字段候选配对：2,737
- 存储方法一跳调用邻居记录：277；涉及存储方法：81

## 输出
- same_method_storage_field_cooccurrence.tsv
- preference_key_field_candidates.tsv
- storage_string_field_candidates.tsv
- storage_method_direct_call_neighbors.tsv
- stage51_summary.json

## 限制
同方法共现只是候选关系，不能证明 Preference/path 值实际写入或来自某个字段；未进行寄存器级、堆对象级或跨基本块数据流求解；调用边为静态 invoke 引用，不包含反射、框架回调、JNI 与动态分派；一跳邻居不能证明运行时顺序或因果关系。本阶段不导出 Java/Smali 源码，不构建 APK。