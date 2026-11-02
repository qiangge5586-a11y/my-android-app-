# 车机底层 / 热点兼容性检测 APK

独立 Android 应用，最低支持 Android 4.2（API 17），用于 DiPlay 本田车机热点适配前的只读底层检测。

## 检测项目
- Android 版本、API、型号、板级、硬件、固件、内核
- 普通 shell 可读取的系统信息（不申请 Root）
- 网络接口、`wlan0`、`phy80211`、`/proc/net/wireless`
- `iw list` 是否明确列出 `* AP`（若设备允许普通应用读取）
- 常见系统目录中的 `hostapd`、`dnsmasq`、`iw`
- 内核模块列表

## 安全边界
检测过程不执行 `svc wifi`、`ifconfig`、`ip link`、`kill`，不启动热点进程，不写系统配置，不采集热点密码，不联网上传报告。

`iw list` 的 `* AP` 是强证据但不是成功保证；缺少 `iw` 只能判定未知，不能判定硬件不支持。最终仍需在单独确认后做 AP 启动、手机关联和 DHCP 实测。

## 构建
使用 JDK 17、Android SDK 35、Gradle 8.7：

```bash
gradle assembleDebug
```

APK 输出：`app/build/outputs/apk/debug/app-debug.apk`

此 APK 是独立诊断工具，不包含 DiPlay 热点启动或 CarPlay 功能。
