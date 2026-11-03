# Stage 56 — Activity/Service 启动调用点与显式目标候选

## 结果
- Activity 启动 API 调用点：16
- Service 绑定/启动 API 调用点：3
- 涉及调用类：15
- 位于 Manifest 组件类本身的启动调用点：4
- 同方法字符串与 Manifest 组件全类名精确匹配候选：1

## 输出
component_launch_call_sites.tsv、explicit_component_target_string_candidates.tsv、stage56_summary.json。

## 限制
启动 API 不证明组件成功启动；同方法字符串匹配不是寄存器/Intent 对象数据流；只匹配完整组件类名字面量，动态目标和隐式 Intent 可能遗漏；静态调用边不模拟框架分派、反射、native 或运行时权限。本阶段不导出 Java/Smali 源码，不构建 APK。