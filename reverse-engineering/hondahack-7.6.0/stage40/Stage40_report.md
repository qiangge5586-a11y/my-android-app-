# Stage 40 — 控制流、异常与同步指令索引

## 方法
扫描语义化 DEX 指令，归类 monitor-enter/monitor-exit、throw/move-exception、return、goto、条件分支、switch 和数组数据填充指令。当前 Stage 13 语义清单将多种 invoke 指令统一规范为通用 invoke，因此本阶段不按 invoke-super/direct/virtual/interface/static 拆分统计。

## 结果
- 扫描文件：1540
- 索引指令点：23553
- 涉及类：1541
- 涉及方法：11104
- return：13738
- goto：7380
- monitor-enter：83
- monitor-exit：193
- move-exception：663
- throw：757
- switch：74
- fill-array-data：665

## 输出
- control_exception_sites.tsv：逐条指令索引
- class_control_exception_summary.tsv：类级汇总
- method_control_exception_summary.tsv：方法级汇总
- stage40_summary.json：机器可读摘要

## 限制
指令存在不代表运行时必然执行；本阶段不重建异常处理器完整结构（相关元数据已在前序阶段索引），不推断 monitor-enter/exit 配对及锁对象身份，也不覆盖框架内部和 native 控制流。invoke 细分受 Stage 13 语义规范化影响，不能由本阶段数据推断调用种类。本阶段不导出 Java/Smali 源码，不构建 APK。
