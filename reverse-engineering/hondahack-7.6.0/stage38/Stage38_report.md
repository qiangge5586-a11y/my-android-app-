# Stage 38 — 字段访问与跨类共享状态索引

## 输入与方法
扫描 Stage 13 语义化 DEX TSV，提取 iget/iput/sget/sput 系列指令中的字段描述符，仅统计字段所有者位于 `Lcn/autohack/` 的访问。读写方向按 DEX opcode 分类；iget/iput 为实例字段，sget/sput 为静态字段。

## 结果
- 扫描语义文件：1540
- 字段访问指令：4716
- 唯一字段：1356
- 多调用类访问字段：357
- 可识别字段访问指令：4716
- 无法识别字段操作数：0
- 调用类到字段所有者耦合边：583

## 输出
- field_access_sites.tsv：逐条字段访问点
- field_access_summary.tsv：字段级读写汇总
- shared_state_fields.tsv：被多个调用类访问的字段
- class_field_coupling.tsv：调用类与字段所有者耦合
- stage38_summary.json：机器可读摘要

## 限制
这是基于反汇编操作数的静态字段访问索引，不是精确的 reaching-def/use 数据流分析。无法覆盖反射、JNI/native 直接内存访问、动态加载及运行时反射字段访问；共享字段分类仅按调用类数量判定，仍需结合对象实例、控制流和运行时路径验证。混淆字段名保持原样。本阶段不导出 Java/Smali 源码，不构建 APK。
