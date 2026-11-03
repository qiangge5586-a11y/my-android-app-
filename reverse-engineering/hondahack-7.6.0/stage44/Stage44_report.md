# Stage 44 — 权限敏感 Android API 索引

## 方法
扫描 DEX invoke 操作数中的定位、相机、录音/音频、电话/SMS、蓝牙、联系人/日历、账户以及无障碍/输入事件相关 API。

## 结果
- 扫描语义文件：1540
- 匹配 API 调用点：193
- 涉及调用类：40
- 定位：21
- 音频管理：24
- 无障碍/输入事件：134
- 蓝牙：6
- 音频采集：8
- 本轮未匹配到相机、电话/SMS、联系人/日历、账户 API

## 输出
- sensitive_api_sites.tsv：逐条 API 调用点
- sensitive_api_class_summary.tsv：调用类统计
- stage44_summary.json：机器可读摘要

## 限制
API 引用不等于权限声明或运行时授权；匹配结果可能来自无害或未使用代码。本阶段未将每个调用点精确映射到 Manifest 权限，也未覆盖 native/framework 行为及动态调用。本阶段不导出 Java/Smali 源码，不构建 APK。
