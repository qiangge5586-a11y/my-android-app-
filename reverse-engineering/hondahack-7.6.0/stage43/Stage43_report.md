# Stage 43 — 存储、系统设置、进程与 IPC API 索引

## 方法
扫描 invoke 操作数中的 File/文件流、SharedPreferences、SQLite/SQL、ContentResolver/Provider、PackageManager、Settings、Runtime.exec/ProcessBuilder、System properties 和 Binder/Parcel API 引用。

## 结果
- 扫描语义文件：1540
- 匹配 API 调用点：1154
- 涉及调用类：161
- Binder/Parcel IPC：559
- File/文件流：261
- SharedPreferences：267
- PackageManager：45
- ContentResolver/Provider：14
- Runtime.exec/ProcessBuilder：6
- System properties：2
- SQLite/SQL、Settings：本轮描述符规则未匹配到记录

## 输出
- storage_system_api_sites.tsv：逐条 API 调用点
- storage_system_class_summary.tsv：调用类统计
- stage43_summary.json：机器可读摘要

## 限制
仅按 API 引用匹配，不证明操作成功或可达；文件路径和参数未进行完整数据流解析；未推断权限要求及 Android 版本行为；DEX 之外的 native/system_server 操作不覆盖。本阶段不导出 Java/Smali 源码，不构建 APK。
