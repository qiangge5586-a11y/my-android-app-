# Honda Car Monitor 阶段 2 与阶段 4

版本：0.3.0  
模块：`:hondamonitor`  
应用包名：`com.honda.carmonitor`

## 阶段 2：CPU 信息与负载检测

已实现：
- 从 `/proc/cpuinfo` 读取 CPU 型号/硬件字段；不可读时回退到 Android `Build.HARDWARE`。
- 读取 CPU 核心数（优先枚举 `/sys/devices/system/cpu/cpuN`）。
- 显示系统 CPU ABI 架构。
- 尝试读取 CPU 当前频率与最高频率；厂商未开放 sysfs 时显示不可读取。
- 对 `/proc/stat` 做约 700ms 两次采样，计算整机 CPU 使用率；不支持时明确提示。
- CPU 采样在后台线程执行，不阻塞主界面。

## 阶段 4：硬件与系统兼容性报告

报告包含：
- Android API 最低版本检查（本 APP minSdk 17）。
- ABI/CPU 架构信息。
- 屏幕横竖方向提示。
- RAM 总量与 1GB 参考阈值。
- 内部存储可用空间与 512MB 参考阈值。
- 触摸屏、Wi-Fi 系统功能声明。
- 汇总通过项、注意项和基础兼容性结论。

这些是通用 Android 安装/运行条件检查，不代表已验证与本田原厂车机、Honda ECU 或车辆 CAN 总线兼容。需要在目标车机实测安装与运行。

## 既有功能保留
- Android 版本、API Level、安全补丁、构建版本和 Build 指纹。
- 设备型号、品牌、制造商、主板、硬件平台。
- Root 检测：仅在 `su -c id` 返回 `uid=0` 时确认 Root；线索与授权状态分开显示。
- 屏幕、RAM、内部存储信息。

## 安全与边界
- 本地只读，不联网、不上传数据、不修改系统分区。
- 不读取发动机转速、车速、水温、故障码等 ECU 数据。没有 OBD2 适配器时，不能声称具备车辆实时数据能力。
- Root 检测可能触发授权管理器提示；超时或拒绝均不作为 Root 成功。

## 构建
GitHub Actions 单独构建 `:hondamonitor:assembleDebug`，避免旧仓库中的另一个 `:app` 模块阻塞本 APK。
APK 产物：`hondamonitor/build/outputs/apk/debug/hondamonitor-debug.apk`  
Artifact：`Honda-Car-Monitor-Chinese-v0.3.0`

## 当前验证状态
代码已提交，等待 GitHub Actions 完成编译和 APK 产物验证。只有 workflow 成功且 artifact 可下载后，才标记 APK 构建完成。
