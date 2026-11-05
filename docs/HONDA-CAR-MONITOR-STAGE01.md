# Honda Car Monitor — 第一阶段

## 本阶段目标
先实现不依赖 OBD2 的车机自身信息检测，重点显示 Android 系统版本与 Root 状态。

## 已实现
- 中文横屏车机界面
- Android 版本、API Level、安全补丁、Build 指纹/标签/版本
- 设备型号、品牌、制造商、设备代号、主板、硬件平台
- 屏幕分辨率与密度
- RAM 总量/可用量、内部存储总量/可用量
- Root 检测：执行 su -c id，只有返回 uid=0 才报告已获取 Root；超时、拒绝或无返回显示未能确认/未获取
- 只读、不联网、不修改系统

## 构建
GitHub Actions 自动构建：
hondamonitor/build/outputs/apk/debug/hondamonitor-debug.apk
工作流产物名称：Honda-Car-Monitor-Chinese-debug

## 限制
普通应用不能保证读取所有厂商私有车机属性。Root 检测会受 su 管理器授权策略影响。此版本不读取车辆 ECU 数据。
