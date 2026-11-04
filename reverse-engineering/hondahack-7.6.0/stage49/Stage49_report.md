# Stage 49 — 序列化、结构化数据与编码 API 索引

## 结果
- 扫描语义文件：1,540
- 匹配 API 调用点：914
- 涉及调用类：83

### API 分类
- XML：304
- Base64：5
- SharedPreferences 相关：267
- JSON：57
- 压缩：14

### 字符串标记候选
- JSON 标记：75
- XML 标记：27
- 序列化标记：1

## 输出
serialization_format_api_sites.tsv、serialization_class_summary.tsv、format_string_candidates.tsv、stage49_summary.json。

## 限制
API 匹配不证明实际载荷格式或运行时使用；字符串标记仅为启发式候选，可能是界面文本/文档；未恢复数据 schema 和对象到载荷的映射；native 编解码与动态加载库不覆盖。本阶段不导出 Java/Smali 源码，不构建 APK。