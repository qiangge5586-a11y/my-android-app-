# Stage 57 — 广播 API 与 Manifest Action 字符串关联

## 结果
- Manifest Action 字面量：22 个
- 广播发送/动态 Receiver 注册 API 调用点：67
- 同方法精确字符串候选：9
- Receiver onReceive 方法内 Action 候选：8
- 广播方法涉及不同 Action 候选：3 个

## 输出
broadcast_api_manifest_action_candidates.tsv、receiver_onreceive_manifest_action_candidates.tsv、stage57_summary.json。

## 限制
仅做同方法字符串精确匹配，不能证明字符串作为 Intent action 参数；Manifest 扁平 XML 未保留 action 与具体 receiver filter 的父子关系；未模拟动态注册、隐式 Intent 解析和框架投递；native/反射生成广播未覆盖。本阶段不导出 Java/Smali 源码，不构建 APK。