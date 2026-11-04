# Stage 59 — Java native 声明、JNI 导出与调用者交叉索引

## 结果
- DEX native 方法声明：14
- 常规 JNI 导出符号匹配：14
- 存在 native 反汇编符号块的方法：14
- 存在直接 Java 调用者的方法：10
- Java→native 静态直接调用边：10
- DEX 加载库边界记录：3

## 输出
jni_java_native_method_cross_reference.tsv、java_callers_of_native_methods.tsv、stage59_summary.json。

## 限制
按常规 JNI 导出命名匹配，动态 RegisterNatives 可能遗漏；库归属依据加载库/导出符号清单；native 反汇编不等于 C/C++ 源码或完整控制/数据流；Java 调用边不证明运行时可达或 native 成功执行。本阶段不导出 Java/Smali 源码，不构建 APK。