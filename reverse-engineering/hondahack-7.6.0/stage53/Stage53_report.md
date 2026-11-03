# Stage 53 — UI 事件回调到业务方法关联

## 结果
- 回调方法候选：131
- 监听器注册调用点：93
- 回调方法直接指向内部业务方法的静态边：108
- 存在直接内部调用的回调类：62
- Manifest 组件：27；组件类中有监听器注册调用的组件：14
- 组件类中有 Intent/Bundle API 调用的类：12

## 输出
callback_to_business_method_edges.tsv、listener_registration_component_context.tsv、component_ui_business_link_summary.tsv、stage53_summary.json。

## 限制
回调方法按名称候选识别，不证明接口实现；业务边只统计直接静态 invoke，不含继承/框架回调、反射、JNI 和动态分派；资源常量仍是最近前置常量启发式，不是寄存器级参数证明；组件归属按精确类描述符。本阶段不导出 Java/Smali 源码，不构建 APK。