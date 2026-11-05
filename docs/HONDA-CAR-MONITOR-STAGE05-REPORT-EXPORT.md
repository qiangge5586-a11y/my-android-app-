# Honda Car Monitor 0.4.0 — 检测报告导出

## 新增功能
- 主界面提供“导出 TXT 报告”和“分享检测报告”两个入口。
- Android 4.4（API 19）及以上通过系统文档选择器选择保存位置，生成 UTF-8 TXT 文件。
- 较旧 Android 版本自动使用系统分享面板，可将报告复制、发送到文件管理器或其他支持文本的应用。
- 报告从当前界面检测结果提取，包含设备、Android、Root、CPU、内存、存储、屏幕、兼容性检测项和检测范围说明。
- 文件名附带日期时间，便于区分多次检测。

## 隐私与限制
- 仅在用户点击导出或分享后生成报告。
- 不联网、不自动上传、不申请存储权限；保存由 Android 系统文档选择器完成。
- 报告不会包含 ECU 发动机数据；没有 OBD2 或厂商授权接口时，不会生成车速、转速、水温或故障码数据。
- API 19 以下不支持系统文档创建器，使用分享面板作为兼容回退。

## 构建
- 模块：:hondamonitor
- 包名：com.honda.carmonitor
- 版本：0.4.0
- APK：hondamonitor/build/outputs/apk/debug/hondamonitor-debug.apk
- GitHub Actions Artifact：Honda-Car-Monitor-Chinese-v0.4.0

## 验证状态
源码和构建配置已提交。须等待 GitHub Actions 编译成功、检查 APK 产物并下载验证后，才能标记本版本构建完成。