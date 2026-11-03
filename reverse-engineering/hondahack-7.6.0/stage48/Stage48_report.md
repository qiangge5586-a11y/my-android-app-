# Stage 48 — 存储与持久化读写关系索引

## 方法
扫描 1,540 个语义化 DEX 文件中的 SharedPreferences、File/FileInputStream/FileOutputStream/RandomAccessFile、SQLiteDatabase 与 ContentResolver 调用点；对部分 String 参数使用同方法内 const-string/move 线性传播，尝试解析键名、路径或查询参数。

## 结果
- 扫描指令行：196,173
- 匹配存储 API 调用点：242
- 涉及调用类：82
- 涉及方法：136
- 可解析字符串参数位置：157

### 分类
- SharedPreferences 读取：115
- SharedPreferences 写入：54
- File 构造：45
- FileInputStream 打开：15
- FileOutputStream 打开：5
- ContentResolver：7
- RandomAccessFile：1

## 输出
- storage_data_access_sites.tsv：逐条存储 API 调用点
- resolved_storage_path_key_strings.tsv：有限寄存器传播解析出的字符串参数
- storage_data_class_summary.tsv：调用类汇总
- storage_data_category_summary.tsv：分类汇总
- stage48_summary.json：机器可读摘要

## 限制
字符串传播仅限同方法线性 const-string/move，不含 CFG 合流、跨方法、堆对象与对象身份；参数位置按 API 描述符/名称启发式选择，需复核重载；动态拼接/加密路径、native I/O 和框架内部持久化未解析。API 引用不证明运行时执行或成功写入。本阶段不导出 Java/Smali 源码，不构建 APK。
