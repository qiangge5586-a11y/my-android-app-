# Stage 45 — Intent、Bundle 与 Android 组件通信索引

## 方法
扫描 DEX invoke 操作数中的 Intent 构造/修改、Extra 读写、Activity/Service 启动、广播发送、Receiver 注册、PendingIntent、Bundle 读写和 ComponentName API；另收集关键词筛选的相关常量字符串候选。

## 结果
- 扫描语义文件：1540
- 匹配 API 调用点：562
- 涉及调用类：113
- 相关字符串候选位置：467

### 分类计数
- bundle_read：46
- bundle_write：60
- component_name：22
- receiver_register：18
- intent_construction：137
- intent_mutation：34
- activity_launch：16
- intent_extras_write：90
- pending_intent：3
- broadcast_send：49
- intent_extras_read：84
- service_launch：3

## 输出
- intent_component_api_sites.tsv
- intent_caller_summary.tsv
- intent_related_strings.tsv
- stage45_summary.json

## 限制
API 引用不是跨过程 Intent 数据流；字符串只按关键词筛选，不能证明是 action/extra 的实际参数；未重建隐式 Intent 解析、运行时动态注册、反射和 native 通信。本阶段不导出 Java/Smali 源码，不构建 APK。
