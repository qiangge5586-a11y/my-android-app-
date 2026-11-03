# Stage 39 — 对象分配与运行时类型使用索引

## 方法
扫描语义化 DEX 文件中的 new-instance、new-array、filled-new-array、check-cast、instance-of、const-class 指令，提取操作数中的类型描述符，构建类型使用和调用类统计。

## 结果
- 扫描文件：1540
- 类型相关指令：9345
- 不同目标类型：1666
- 出现类型操作的调用类：988
- 未解析目标类型：0

### Opcode 分布
- instance-of：514
- check-cast：2609
- new-instance：4239
- new-array：1718
- const-class：265

## 输出
- object_type_sites.tsv：逐条对象/类型操作指令
- type_usage_summary.tsv：类型级分布
- class_allocation_type_usage.tsv：调用类级分布
- stage39_summary.json：机器可读摘要

## 限制
静态指令索引不能确定分配对象与后续使用之间的对象身份关系；反射、JNI/native 分配、Android 框架创建对象及动态加载未建模。类型描述符从操作数文本提取。本阶段不导出 Java/Smali 源码，不构建 APK。
