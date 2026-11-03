# Stage 54 — 类职责证据画像与功能模块候选

## 结果
- 类级证据画像：1,520 个类
- Manifest 组件画像：27 个组件
- 有 API 上下文记录的类：121
- 有字符串使用记录的类：553
- 有字段访问记录的类：379
- 有回调方法候选的类：125
- Manifest 组件中有 API 上下文：14 个
- Manifest 组件中有字段访问：14 个

## 输出
class_responsibility_evidence_profiles.tsv、manifest_component_evidence_profiles.tsv、stage54_summary.json。

## 限制
画像按类聚合静态证据，不等于已确认的功能语义；邻近字符串不是 API 参数证明；API 调用数不是运行时执行次数；helper 类不会自动归属组件；混淆类名保持原样。本阶段不导出 Java/Smali 源码，不构建 APK。