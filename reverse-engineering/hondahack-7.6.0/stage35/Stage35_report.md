# HondaHack 7.6.0 — Stage 35：界面事件回调与监听器绑定线索

## 范围
对已有语义化 DEX 指令清单中的全部类方法进行静态扫描，定位 Android View、Preference、Adapter、Menu 等监听器注册调用，并建立回调方法名索引。本阶段不导出源码、不修改 APK。

## 结果
- Semantic DEX 类文件：1540
- 扫描方法：11104
- 命名匹配的回调方法记录：131
- 监听器注册调用点：93
- 识别监听器类型：checked_change、editor_action、item_click、item_selected、key、menu_click、preference_change、preference_click、text_change、touch、view_click、view_long_click
- 含回调命名方法的类：125

## 证据解释与限制
- 回调方法名仅是候选，未单独证明对应类实现了特定 listener interface。
- 监听器注册点来自 invoke 目标名称匹配。
- 最近出现的资源常量仅是候选关联，不能证明它就是监听器注册的 View ID 或参数；需要寄存器级数据流分析。
- 匿名类、lambda、反射、动态注册监听器和 XML android:onClick 可能未被完整识别。
- 本阶段只增加反推证据，不提取 Java/Smali，不构建 APK。
