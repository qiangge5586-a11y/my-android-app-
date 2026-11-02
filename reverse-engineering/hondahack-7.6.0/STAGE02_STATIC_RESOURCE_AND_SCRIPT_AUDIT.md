# HondaHack 7.6.0 — Stage 02 静态资源与脚本审计

> 样本仅用于离线静态分析；没有安装、运行 APK，也没有在车机上执行脚本。

## 样本身份与完整性

- 文件：`本田黑科技7.6.0_可联网_去注销_任意.apk`
- SHA-256：`e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe`
- 大小：3,893,398 bytes
- Package：`cn.autohack.hondahack`
- Version：7.6.0 (versionCode 1)
- DEX：单个 `classes.dex`，1,909,752 bytes
- Native：`armeabi-v7a` 下有 `libnative.so`、`libxposed.so`、`libmp3lame.so`

本轮从资料库重新 materialize 原始 APK，SHA-256 与 Stage 01 记录一致。

## Stage 02 已实际检查内容

1. 解压 APK 并枚举 `res/raw` 文件。
2. 逐行读取四类系统脚本：Clarion/Mitsubishi 安装脚本、恢复启动脚本和 factory reset 脚本。
3. 检查 DEX 字符串中的类描述符、系统路径、URL、Xposed 标识和进程执行线索。
4. 使用 ELF 动态符号表初查三个 Native 库。
5. 未执行 APK、脚本、网络请求或任何车机写操作。

## 新确认的行为

### A. `factory_reset.sh`

实际脚本执行/包含以下动作：

- 调用厂商工具：`lmem_clear_dctr_data`、`sh2a_audio_onoff 0`、`submicom_reset -f`。
- 卸载 `/mnt/shell/emulated`。
- 对 `/dev/block/mtdblock25` 写入 `0000`。
- 备份 EEPROM、默认 MIB、CarPlay/Android Auto 属性和若干应用 SharedPreferences。
- 挂载 `/dev/block/mmcblk0p8` 到 `/tmp/mnt`，检查 `data_org.tar.gz`。
- 若存在恢复包：写入 MTD 标记、复制配置备份并创建 `factory_reset` 标记文件；归档解压命令处于注释状态。
- 若不存在恢复包：执行 `rm -rf /data/*`，从 `/system/etc/data.org/*` 恢复数据目录，恢复部分配置，并清理 `/var/log/*`。
- 随后对 `/dev/block/mtdblock16`（仅在无恢复包分支）和 `/dev/block/mtdblock22` 执行 zero-fill，最后 reboot。

**风险判断：严重/不可在普通车机试运行。** 该脚本高度依赖特定分区布局和厂商命令；误用可能清空用户数据、破坏设备专属分区或导致无法启动。当前只确认脚本内容，尚未确认 APK 中触发入口及目标车型适配条件。

### B. USB recovery 启动钩子

- `install_recovery2_clarion.sh`：最多检查 5 次，查找 `/mnt/usb3/recovery.sh` 或 `/mnt/usb6/recovery.sh`；发现后复制到 `/data/local/tmp/recovery.sh`、chmod 755、后台执行并删除临时文件。
- `install_recovery2_mitsubishi.sh`：每次 sleep 60 秒，最多 5 次，查找 `/mnt/usbdrive1/recovery.sh` 或 `/mnt/usbdrive2/recovery.sh`，之后同样执行。

**风险判断：高。** 若安装流程把该脚本放入系统 recovery 启动路径，USB 文件可成为启动期任意命令执行入口。尚需追踪写入 `/system/etc/install-recovery2.sh` 的方法和调用条件。

### C. 厂商白名单与 APK 安装

- Clarion 修改 `/data/system/whitelist.xml`。
- Mitsubishi 修改 `/data/data/whitelist-1.0.xml`。
- 脚本拼接包名、签名、版本范围、音频控制配置，再复制覆盖白名单。
- 安装动作使用 `pm install -r -d`。
- 两份脚本都存在 chmod 666、未经 XML 转义的字符串拼接和临时文件处理问题。

**风险判断：中高。** 只应在已确认对应厂商固件、白名单格式、权限和备份回滚方案的环境中分析；不建议在思域车机上执行。

## DEX 字符串新增线索

- 应用核心类可见：`cn.autohack.hondahack.MainActivity`、`MyService`、`MyBroadcastReceiver`。
- Xposed：`de.robv.android.xposed.IXposedHookLoadPackage`、`XposedBridge`、`XposedHelpers`。
- Hook 类：`np.manager.signature.PmsHookApplication`。
- 系统路径：`/bin/XposedBridge.bin`、`/bin/XposedBridge.jar`、`/system/etc/factory_reset.sh`、`/system/etc/hondahack/auth-code`。
- URL 线索包括：
  - `http://www.autohack.cn/app/vr.php`
  - `http://www.autohack.cn/app/buy.php`
  - `http://www.autohack.org/app/buy.php`
  - `http://www.autohack.cn/app/crash.php`
  - `http://www.autohack.cn/app/appstore.php?lang=`
  - `http://www.autohack.cn/app/hondahack.php?lang=`
  - `/app/whitelist.php?lang=`

URL 字符串只能证明端点被编入程序，不能证明当前服务器可用、实际请求内容或数据是否上传。

## Native 初查

- `libmp3lame.so` 导出大量 LAME 编码 API，支持其音频 MP3 编码用途判断。
- `libnative.so` 与 `libxposed.so` 动态符号中未见明显自定义 JNI 导出（初步筛选）；这不排除符号隐藏、静态注册或通过其他机制调用。
- 三个库的 SHA-256：
  - `libnative.so`: `bb6054b02fb133d8a432800cda84c75f76faca24554e8c70b3136054b3d69fd5`
  - `libxposed.so`: `c241f274694c37cf5db0f5a3eb9baa9f7beca8ccc4ab2f911a91cb9661e1e9bb`
  - `libmp3lame.so`: `bb7c1a46782f04b68bbbfa0db3b58db00b8c0dabaff081e33957982f92c6cd72`

## 尚未完成 / 不应误报为已完成

- DEX 尚未反汇编成逐类 Java/smali 源码。
- 尚未得到完整 Manifest 组件清单及权限的逐项调用证据。
- 尚未追踪 reset/recovery 脚本的 Java 调用点和触发条件。
- 尚未还原授权请求参数、设备标识采集和“去注销”修改点。
- Native 尚未进行 ARM 反汇编、JNI 注册追踪或动态分析。
- 未生成可完整重建的原始 Gradle 工程；APK 不包含原作者完整源码和签名私钥。

## Stage 03 下一步

1. 获取可用的 JADX 或 baksmali/dexlib2 工具链，导出完整类清单与 smali。
2. 重点跟踪 `MainActivity`、`MyService`、`MyBroadcastReceiver`、`PmsHookApplication`。
3. 对脚本资源 ID 做 DEX 引用交叉映射，确认安装、恢复和 recovery 钩子调用点。
4. 提取签名证书、公钥与 APK Signing Scheme 信息。
5. 解析 Native JNI_OnLoad、动态注册和 ARM 函数调用。
6. 单独审计网络请求参数与设备信息流；不尝试绕过远端授权或移除付费/访问控制。
7. 以逐阶段源码、证据和风险标记构建可复现的独立分析工程。

## 工具限制

当前环境只有 unzip、strings、readelf 等基础工具；未发现 jadx、apktool、aapt/aapt2、smali/baksmali、dex2jar、d8 或 apksigner。因此本阶段是可复核的静态资源/字符串/ELF 初查，不是完整反编译。
