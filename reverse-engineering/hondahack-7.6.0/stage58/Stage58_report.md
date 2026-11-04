# Stage 58 — DEX 命名空间与包依赖关系索引

## 结果
- 当前可由调用图/语义类头识别的类集合：365
- 包聚类：4
- 包级依赖边：4
- 命名空间族依赖边：3
- 跨命名空间族静态调用点：68

## 输出
class_namespace_inventory.tsv、package_dependency_edges.tsv、namespace_family_dependency_edges.tsv、stage58_summary.json。

## 限制
当前类集合基于可识别的语义类头和调用图，不代表全部 1,770 个 DEX 类；包聚类截取前三段路径；命名空间族基于前缀规则，未知类不能据此认定第三方来源；调用次数是静态直接 invoke 统计，不是运行时频率。本阶段不导出 Java/Smali 源码，不构建 APK。