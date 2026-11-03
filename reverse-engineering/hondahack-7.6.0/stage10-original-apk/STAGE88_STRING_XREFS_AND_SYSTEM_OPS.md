# Stage 88 — DEX string cross-references and behavior ownership

## Input identity
Original APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe

This pass scanned Dalvik code items for const-string / const-string-jumbo instructions and mapped referenced string IDs back to class and method names. The method names below are exact DEX names; obfuscated names remain unresolved.

## Endpoint call-site owners

| Literal | Class.method |
|---|---|
| http://%s/root/activate.php | cn.autohack.hondahack.Wb.a |
| http://%s/app/video/2/index.php | cn.autohack.hondahack.Wb.d |
| http://www.autohack.cn/app/hondahack.php?lang= | cn.autohack.hondahack.Wb.a |
| http://www.autohack.cn/app/appstore.php?lang= | cn.autohack.hondahack.Wb.b |
| http://www.autohack.cn/app/buy.php | cn.autohack.hondahack.Wb.b |
| http://www.autohack.org/app/buy.php | cn.autohack.hondahack.Wb.b |
| http://www.autohack.cn/app/vr.php | cn.autohack.hondahack.Wb.c |
| http://www.autohack.cn/app/crash.php | com.balsikandar.crashreporter.a.d.a |
| http://www.autohack.org/app/crash.php | com.balsikandar.crashreporter.a.d.a |
| http://www.autohack.org | cn.autohack.hondahack.MainActivity.onHelp |

The `%s` host is passed dynamically. Wb methods also reference both autohack.cn and autohack.org, so the actual selected host and fallback logic must be read from the full method body.

## Privileged operations mapped to code owners

| DEX string / operation | Owner |
|---|---|
| `rm -Rf /mnt/sdcard/!(Android) /mnt/sdcard/.*` | cn.autohack.hondahack.kb.onClick |
| `rm -Rf /data/dalvik-cache/*` | cn.autohack.hondahack.EmergencyActivity$a.run; cn.autohack.hondahack.FixActivity.c |
| `rm /system/app/HondaHack.apk` | cn.autohack.hondahack.ia.onClick |
| `chmod 777 /system/etc/factory_reset.sh` | cn.autohack.hondahack.FixActivity.e |
| `cp "%s" /system/etc/factory_reset.sh` | cn.autohack.hondahack.FixActivity.e |
| `chmod 777 /system/etc/permissions/platform.xml` | cn.autohack.hondahack.SdcardActivity.b; cn.autohack.hondahack.na.c |
| `cp -a /system/etc/permissions/platform.xml.orig ...` | cn.autohack.hondahack.SdcardActivity.b |
| `/system/etc/permissions/platform.xml 2>&1` | cn.autohack.hondahack.SdcardActivity.d |
| `chmod -R 755 /system/etc/hondahack` | cn.autohack.hondahack.AppsActivity.b |
| `mkdir /system/etc/hondahack` | cn.autohack.hondahack.AppsActivity.b |
| `/system/etc/hondahack/auth-code` | cn.autohack.hondahack.na.onCreate; cn.autohack.hondahack.ub.h |
| `/system/etc/hondahack/agent` | cn.autohack.hondahack.MainActivity.onHelp |
| `/system/bin/app_process`, `install-recovery2.sh`, `platform.xml` patch and backup commands | cn.autohack.hondahack.na.c |
| `su -c 'mount -o rw,remount ...'` | cn.autohack.utils.y.run |
| `chmod 666 /system/app/HondaHack.apk` and copy to system app | cn.autohack.hondahack.V.a |

## Critical safety finding

The cleanup handler `cn.autohack.hondahack.kb.onClick` contains a shell command targeting `/mnt/sdcard/!(Android) /mnt/sdcard/.*`. If run with shell expansion and sufficient permissions, this can remove a broad range of user-storage content. It must not be executed during analysis or testing on a device containing user data.

Other call sites manipulate /system, /data/system, dalvik-cache, platform.xml, app_process and recovery scripts. These are high-impact system modifications. This report records static references only and does not recommend running them.

## Other code owners identified

- `cn.autohack.hondahack.Wb`: owns most product/backend endpoint strings.
- `com.balsikandar.crashreporter.a.d`: owns crash-report upload URL strings.
- `cn.autohack.hondahack.na`: owns multiple root/system patch command strings.
- `cn.autohack.hondahack.AppsActivity.b`: owns system app directory setup and startup restoration commands.
- `cn.autohack.hondahack.SdcardActivity`: owns platform.xml backup/restore and permission patch strings.
- `cn.autohack.hondahack.kb`: owns broad external-storage cleanup command.
- `cn.autohack.hondahack.FixActivity`: owns factory-reset script copy/permission and dalvik-cache cleanup strings.
- `cn.autohack.hondahack.V`: owns system APK copy/permission strings.
- `cn.autohack.hondahack.ia`: owns system APK removal string.

## Confidence / remaining work

- String-to-method xrefs: high confidence for the const-string references found by this pass.
- Whether each command is reached, gated, or actually executed: not yet confirmed.
- URL request method, headers, payload, response format, host selection and authentication: not yet reconstructed.
- Root mechanism, privilege checks, rollback and failure handling: not yet reconstructed.

Next, decompile the identified owner methods and reconstruct control flow. Treat dangerous operations as static-analysis-only; do not run shell commands or apply system patches.
