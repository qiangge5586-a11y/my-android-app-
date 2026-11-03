# Stage 09 — 核心类控制流与源码重建笔记

- DEX SHA-256: `3c3a0ac9deac6fd33bf9a76dd338085896a3010c9f4d9dfc7cb54eafc3595319`
- 数据来源：Stage 08 重点类 Dalvik 指令清单；静态证据，不是原始 Java 源码。

## MainActivity
- 5 个方法记录。
- onCreate 配置 ActionBar 自定义视图，并加载混淆 Fragment 类 na。
- onHelp 检查 Wb.g()，一分支读取 /system/etc/hondahack/agent 并构建帮助弹窗，另一分支打开 autohack.org。

## MyApplication
- 32 个方法记录。
- onCreate 读取 SharedPreferences 的 enable_log_file 并初始化日志入口。
- 随后调用混淆类 d、F、ub 等入口；具体副作用未确认，不在重建版执行。
- 存在 ServiceConnection 回调。

## MyBroadcastReceiver
- 2 个方法记录。
- onReceive 读取 Intent action 并记录日志；DEX 显示 PACKAGE_REPLACED 等分支。
- 重建版接收器保持空操作。

## MyService
- 34 个方法记录。
- 存在 Handler、BroadcastReceiver、LocationManager、Timer 等调用。
- 启动逻辑注册自定义 action，并注册 GPS_FIX_CHANGE；action 字符串涉及音量、媒体、导航、语音助手、后视摄像头、仪表等功能域。
- 重建版服务不主动运行、不访问车载系统接口。

## FixActivity
- 14 个方法记录。
- 可见 restore_factory_reset、reboot、restore_whitelist、fix_system_errors 等危险入口，全部禁用。

## SettingsActivity
- 14 个方法记录。
- 注册 SharedPreferences 变更监听；可见 enable_log_file、enable_test、force_portrait、select_car_model 等设置键。

## 安全禁用项
- 不移植 factory reset、reboot、分区写入、whitelist 修改、USB recovery 脚本执行。
- 不复现 Xposed 注入、驾驶状态伪造、授权绕过或付费校验绕过。
- MyService 与 MyBroadcastReceiver 仅保留生命周期空壳。
