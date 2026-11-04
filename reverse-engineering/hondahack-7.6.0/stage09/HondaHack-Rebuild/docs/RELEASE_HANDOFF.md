# HondaHack Rebuild — build handoff

## Current verified build

- GitHub Actions run: https://github.com/qiangge5586-a11y/my-android-app-/actions/runs/41692507933
- Workflow result: SUCCESS
- Artifact name: `hondahack-safe-rebuild-debug`
- Artifact archive size: 19,235 bytes
- APK SHA-256: `ebd9bbf6d35ea1ae5c838aebc7a0a5d1c75b2bb39f11c9fe5fe58ef628749c69`
- Package ID: `cn.autohack.hondahack.rebuild`
- Java namespace: `cn.autohack.hondahack`
- minSdk: 17
- targetSdk: 28
- compileSdk: 35

The artifact includes `app-debug.apk`, `apk-badging.txt`, and `app-debug.apk.sha256`.

## Download and install

1. Open the successful Actions run linked above.
2. Open the `hondahack-safe-rebuild-debug` artifact and download its ZIP.
3. Extract the ZIP and verify the APK checksum against `app-debug.apk.sha256`.
4. Copy `app-debug.apk` to the head unit and install it, or use ADB: `adb install app-debug.apk`.
5. Keep the original HondaHack APK installed. The rebuild has a distinct application ID and is designed to coexist with it.

If installing an updated debug build over an earlier rebuild fails due to a different debug signing key, uninstall only `cn.autohack.hondahack.rebuild` and install the new APK. Do not uninstall the original `cn.autohack.hondahack` package.

## Implemented

Dashboard, device information, launcher-visible app catalog/search/details, map hand-off, local video picker, diagnostics/share, typed local assistant with system TTS, and local preferences.

## Not implemented

Original private backend, account/authentication system, full proprietary Java/Kotlin/C/C++ source, vehicle CAN/HMI controls, instrument telemetry, Root/Xposed modules, privileged boot persistence, factory reset, partition writes, and authorization bypass.

## Acceptance status

**Build verified:** GitHub CI compiled the current project, verified APK package identity and minSdk, and uploaded the artifact.

**Device acceptance pending:** No target Honda head-unit model, Android version, display dimensions, or physical test results were supplied. Complete `docs/DEVICE_SMOKE_TEST.md` on the target device before treating this as a vehicle-ready replacement.
