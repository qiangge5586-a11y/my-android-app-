# Honda Car Monitor 0.5.0 — 报告导出修复

## 问题
部分车机没有 Android 文档创建界面，也没有可处理分享 Intent 的应用，导致 0.4.0 的导出按钮无法完成操作。

## 修复
- Android 10（API 29）及以上：通过 MediaStore 直接写入公共 Download/HondaCarMonitor 目录。
- Android 9（API 28）及以下：按需申请 WRITE_EXTERNAL_STORAGE 权限，再写入公共 Download/HondaCarMonitor 目录。
- 不再把分享应用作为导出的后备路径。
- 分享按钮仍保留；没有分享应用时显示明确提示。
- 报告使用 UTF-8 文本，文件名带时间戳。

## 使用
点击“导出 TXT 报告”，成功后在车机文件管理器中打开 Download/HondaCarMonitor 目录。

## 限制
尚未在用户的 2019 款思域原厂中控屏上实机验证。旧版 Android 若拒绝存储权限，公共下载目录写入会失败并显示原因。
