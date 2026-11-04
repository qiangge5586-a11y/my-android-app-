# Stage 87 — DEX string reconnaissance: endpoints, privileged paths and integration actions

## Input identity
Original APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe

## Network endpoint strings found in classes.dex

The DEX string table contains these literal endpoint templates/domains:

| Endpoint | Initial interpretation (not yet call-site verified) |
|---|---|
| http://www.autohack.cn/app/appstore.php?lang= | app catalog/store |
| http://www.autohack.cn/app/buy.php | purchase |
| http://www.autohack.cn/app/crash.php | crash report |
| http://www.autohack.cn/app/hondahack.php?lang= | product/service page or API |
| http://www.autohack.cn/app/vr.php | VR-related page/API |
| http://%s/app/video/2/index.php | video service using a runtime host |
| http://%s/root/activate.php | root activation endpoint using a runtime host |
| http://www.autohack.org | secondary domain |
| http://www.autohack.org/app/buy.php | purchase endpoint |
| http://www.autohack.org/app/crash.php | crash report endpoint |
| www.autohack.cn | domain string |
| google.com | generic host string |

These are DEX string literals, not proof that the endpoints are reachable, currently operated, authenticated, or successfully called. The `%s` host is dynamically supplied; do not infer the actual server address from the placeholder.

No credentials, bearer token values or passwords are included in this report. A generic UI string such as `password` is not a credential.

## Privileged filesystem / shell strings

The DEX string table includes the following system paths and shell command templates:

- `/system/app/HondaHack.apk`
- `/system/etc/hondahack/agent`
- `/system/etc/hondahack/auth-code`
- `mkdir /system/etc/hondahack`
- `chmod -R 755 /system/etc/hondahack`
- `chmod 666 /system/app/HondaHack.apk`
- `cp "%s" /system/app/HondaHack.apk`
- `rm /system/app/HondaHack.apk`
- `echo %04d > /system/etc/hondahack/restore-apps`
- `touch /data/data/cn.autohack.hondahack/conf/disabled`
- `rm /data/data/cn.autohack.hondahack/conf/disabled`
- `if [ ! -d /sdcard/HondaHack ]; then mkdir /sdcard/HondaHack; fi; touch /sdcard/HondaHack/.nomedia`
- `rm -Rf /mnt/sdcard/!(Android) /mnt/sdcard/.*`
- BaiduMapAuto directory copy/removal commands

Important safety note: the broad `rm -Rf` string is potentially destructive if executed with matching shell semantics and permissions. This is static evidence only. Do not execute any extracted shell string as part of reverse engineering. Later stages must identify the exact caller, guard conditions, privilege mechanism, target path expansion and whether the command is reachable.

## Automotive / navigation / media integrations

DEX strings include package identifiers:
- `com.baidu.carlifevehicle`
- `com.tencent.qqmusiccar`
- `com.tencent.wecarnavi`
- `cn.kuwo.kwmusiccar`
- `cn.autohack.hondainstaller`

And app-specific broadcast/action constants, including:
- `cn.autohack.hondahack.START_NAVI`, `STOP_NAVI`
- `UPDATE_NAVI`, `UPDATE_NAVI_AMAP`, `UPDATE_NAVI_WAZE`
- `SHOW_METER`, `HIDE_METER`, `UPDATE_METER`, `TEST_METER`
- `ENG_COOLANT_TEMP`, `AVG_FUEL_EFF`, `DISTANCE_TO_EMPTY`, `TRIP_A_DISTANCE`, `UPDATE_VOLTAGE`
- `TOGGLE_REAR_CAMERA`
- `PLAY_STARTUP_VIDEO`, `STOP_STARTUP_VIDEO`
- `VOICE_ASSISTANT`, `VOICE_ASSISTANT_SET_STATE`, `VOICE_ASSISTANT_SIMULATE`
- `GET_SMART_PHONE_INFO`, `GET_SOURCE`, `SET_SOURCE`, `CHANGE_SOURCE`
- `REQUEST_AUDIO_FOCUS`, `ABANDON_AUDIO_FOCUS`

These constants suggest app features and integration targets, but their actual semantics must be verified against sendBroadcast/receive paths and native bridge calls.

## Location and device behavior evidence

Strings include `requestLocationUpdates`, `getLastPositionUpdateTime`, `onLocationChanged`-related method metadata, and location permission declarations. This is consistent with the MyService location callbacks seen in Stage 86.

## Next steps

1. Resolve each endpoint string to the DEX class/method that references it.
2. Trace URL construction, runtime host selection, query parameters, request method, headers, response parsing and error handling.
3. Identify any authentication or licensing flow without publishing secret values.
4. Trace every shell command to its execution wrapper, root detection and user-trigger path; statically inspect only.
5. Map each automotive action to sendBroadcast/receiver handling and native function calls.
6. Preserve the original obfuscated class names alongside any semantic labels assigned during analysis.

## Confidence
- Literal strings and paths: high confidence (directly present in classes.dex).
- Intended endpoint role: medium/low until call sites are traced.
- Actual server behavior, authorization and successful execution: unknown.
