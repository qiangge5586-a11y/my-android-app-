# HondaHack 7.6.0 — Stage 09：核心控制流梳理与安全源码骨架

## 本阶段完成
- 整理 144 个重点类方法记录。
- 输出核心类控制流笔记与应用内广播 action 清单。
- 创建 Android Java 工程骨架：MainActivity、SettingsActivity、MyApplication、MyService、MyBroadcastReceiver。
- 默认安全模式：不申请 root/系统日志/挂载/位置权限，不注册车载广播，不调用 JNI/Xposed，不执行 shell，不自动启动后台服务。

## 结构性证据
- MainActivity.onCreate 配置 ActionBar 自定义视图，并提交 FragmentTransaction 加载混淆类 na。
- MyApplication.onCreate 读取默认 SharedPreferences 的 enable_log_file，随后调用多个尚未明确语义的混淆初始化入口。
- MyService 存在 Handler、BroadcastReceiver、LocationManager、Timer 等依赖，以及车载/导航/语音相关 action 字符串。
- SettingsActivity 注册 SharedPreferences 变更监听。
- FixActivity 的字符串/资源中出现工厂重置、重启、白名单恢复等高风险入口，均不移植。

## 工程状态
- 工程是可继续开发的安全壳，不是完整原始源码。
- Android Gradle Plugin 8.7.3；compileSdk 35；minSdk 17；targetSdk 28。
- 本阶段未构建 APK，未进行设备验证。

## 下一阶段
- 逐项分析 MyService 广播 action 与处理函数映射。
- 拆解设置项并按风险分级。
- 建立 JNI Java/native 参数边界映射，但不执行 native 代码。
