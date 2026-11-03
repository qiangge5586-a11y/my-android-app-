# HondaHack 7.6.0 — Stage 11: DEX/资源恢复与反编译准备

## 输入校验
- APK SHA-256：`e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe`
- DEX SHA-256：`3c3a0ac9deac6fd33bf9a76dd338085896a3010c9f4d9dfc7cb54eafc3595319`
- DEX 类记录：1,770
- DEX 方法记录：11,787
- APK ZIP 条目：986
- 原生库：3 个（仅清点和哈希，不执行）

## 本阶段实际交付
- 原始 DEX 与全量类/方法索引保存在 Stage 11 归档包。
- APK 全部条目清单、资源清单与哈希。
- 已解码的非 shell 资源。
- 原始 shell 脚本隔离在 audit_only_scripts，仅静态审计，不执行。
- 原生库仅保留路径、大小和哈希。
- 提供 run_jadx_when_available.sh，可在安装 JADX 后复现 DEX 导出。

## 反编译阻塞
当前执行环境没有 JADX、APKTool、dexdump、Androguard 等工具，且无法从 GitHub 下载 JADX 发行包。因此本阶段没有生成 Java 源码；Stage 08 仅为重点类的部分语义化 Dalvik 指令恢复，不是完整 Smali/Java。官方 JADX 支持 DEX/APK 到 Java 的反编译，后续需在具备该工具的环境完成导出与逐类校验。

## 安全边界
只进行静态读取、清点、哈希和反编译准备；不运行 APK、脚本或原生库。不实现许可/授权绕过、root/Xposed 注入、车机写入、恢复出厂设置或白名单篡改。

## 下一阶段
Stage 12：运行 JADX，导出 Java/Smali，统计成功率与错误类，并逐个对照 DEX 方法索引；再整合安全可重建的应用层源码。
