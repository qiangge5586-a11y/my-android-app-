# Stage 03 — DEX Evidence Ledger Template

> Status: template only. No new DEX decompilation findings are asserted in this file.

## Sample identity

| Field | Value |
|---|---|
| APK filename | 本田黑科技7.6.0_可联网_去注销_任意.apk |
| Package | cn.autohack.hondahack |
| Expected SHA-256 | e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe |
| Verified SHA-256 | Pending |
| File size | Pending |
| Analysis date | Pending |
| OS / image version | Pending |

## Toolchain record

| Tool | Version | Path | Status |
|---|---|---|---|
| Java | Pending | Pending | Not verified |
| JADX | Pending | Pending | Not verified |
| Apktool | Pending | Pending | Not verified |
| Smali/baksmali | Pending | Pending | Not verified |
| Androguard | Pending | Pending | Not verified |
| aapt / aapt2 | Pending | Pending | Not verified |
| apksigner | Pending | Pending | Not verified |
| Ghidra or radare2 | Pending | Pending | Not verified |

## Class and method inventory

Record one row per relevant class/method. Use exact decompiler output names and preserve obfuscated names.

| Class | Superclass / interfaces | Method signature | Entry source | Evidence location | Notes |
|---|---|---|---|---|---|
| cn.autohack.hondahack.MainActivity | Pending | Pending | Pending | Pending | Not yet inspected |
| cn.autohack.hondahack.MyService | Pending | Pending | Pending | Pending | Not yet inspected |
| cn.autohack.hondahack.MyBroadcastReceiver | Pending | Pending | Pending | Pending | Not yet inspected |
| np.manager.signature.PmsHookApplication | Pending | Pending | Pending | Pending | Not yet inspected |

## Call-path findings

For every edge, record caller, callee, dispatch type, and source location. Mark reflection, callbacks, JNI, and dynamically loaded code as unresolved until proven.

| ID | Caller class + method | Callee class + method / target | Dispatch | Evidence location | Confidence |
|---|---|---|---|---|---|
| CP-001 | Pending | factory_reset.sh execution path | Pending | Pending | Unknown |
| CP-002 | Pending | install_recovery2_clarion.sh path | Pending | Pending | Unknown |
| CP-003 | Pending | install_recovery2_mitsubishi.sh path | Pending | Pending | Unknown |
| CP-004 | Pending | Xposed initialization / hook path | Pending | Pending | Unknown |
| CP-005 | Pending | Network request construction | Pending | Pending | Unknown |

## Indicator cross-reference

| Indicator | Static evidence location | Referencing method | Runtime trigger known? | Assessment |
|---|---|---|---|---|
| /system/etc/factory_reset.sh | Pending | Pending | No | Caller not established |
| install_recovery2_clarion.sh | Pending | Pending | No | Installer/caller not established |
| install_recovery2_mitsubishi.sh | Pending | Pending | No | Installer/caller not established |
| whitelist.xml | Pending | Pending | No | Caller not established |
| /bin/XposedBridge.bin | Pending | Pending | No | Loader path not established |
| /bin/XposedBridge.jar | Pending | Pending | No | Loader path not established |
| /system/etc/hondahack/auth-code | Pending | Pending | No | Read/write path not established |
| autohack.cn endpoints | Pending | Pending | No | Request construction not established |
| autohack.org endpoints | Pending | Pending | No | Request construction not established |

## Evidence quality rules

- A string occurrence is not proof of execution.
- A method reference is not proof that the method is reachable.
- A call site is not proof that its branch condition is satisfied.
- A URL literal is not proof that a request was sent or that a server is available.
- A native library name or import is not proof of its complete behavior.
- Distinguish direct calls, reflection, callbacks, broadcast delivery, service lifecycle, JNI, and dynamic loading.
- Cite exact class, method, instruction or line, and tool version for each conclusion.
- Keep hypotheses in a separate section and label them explicitly.

## Open questions

1. Which component installs the recovery scripts into system paths?
2. Which method or lifecycle event invokes the factory-reset script?
3. What guards the destructive reset path?
4. Which Xposed package-load conditions activate the hook?
5. Which device identifiers and authorization values are collected or transmitted?
6. Which native methods are registered dynamically, if any?

## Stage 03 exit criteria

- [ ] APK hash independently verified.
- [ ] Tool versions recorded.
- [ ] Full DEX class inventory generated.
- [ ] Relevant classes and methods reviewed.
- [ ] Call graph exported with unresolved edges marked.
- [ ] Script and Xposed indicators cross-referenced to methods.
- [ ] Evidence reviewed for false positives.
- [ ] Findings report committed.

Until all exit criteria are met, Stage 03 remains in progress.
