# Stage 55 — Activity 布局与界面控件关联证据

## 结果
- Activity—layout 候选：5
- 对应布局文件候选记录：6
- 布局中的 ID/XML onClick 控件记录：60
- Manifest Activity：23
- 有布局候选的 Activity：5
- 监听器注册索引：93

## 输出
activity_layout_evidence.tsv、layout_controls_for_linked_activities.tsv、activity_layout_listener_summary.tsv、stage55_summary.json。

## 限制
布局关联使用最近前置资源常量启发式；资源限定符可能产生多个变体；XML 控件结构不证明运行时监听器绑定；监听器计数仅按类统计，未绑定到具体 View ID；动态布局可能遗漏。本阶段不导出 Java/Smali 源码，不构建 APK。