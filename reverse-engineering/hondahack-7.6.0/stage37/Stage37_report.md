# HondaHack 7.6.0 — Stage 37：Manifest 入口到业务类的静态可达性

以已映射到 DEX 的 27 个 Manifest 组件作为入口，在 Stage 36 直接跨类调用图上进行多源广度优先遍历。

- Manifest 组件入口根节点：27
- 调用图涉及类：364
- 从组件入口可达类：350
- 未被直接静态边触达的类：16
- 组件入口逐项可达性记录：27
- 图中跨类边：610

限制：仅使用直接静态跨类 invoke 边。Android framework 回调、XML 事件、反射、JNI、动态加载、隐式 Intent 和 native callback 未建模。未触达不代表死代码；可达也不代表运行时一定执行。本阶段不提取源码、不修改或执行 APK。