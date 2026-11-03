# HondaHack Rebuild — Android Studio project

An independent, safe reconstruction of selected user-facing functions observed in HondaHack 7.6.0. This is **not** the original source code and is not a drop-in replacement for the original APK.

## Implemented modules

| Module | Current behavior |
|---|---|
| Dashboard | Local home screen and safe-mode notice |
| Device information | Read-only Android, hardware and internal-storage summary |
| App management | Lists launcher-visible apps and opens them through Android's public launch API |
| Navigation | Sends a user-entered destination to installed map apps using a standard geo URI |
| Local video | User selects one video through the system document picker; an installed player opens it |
| Diagnostics | Builds a local device/app summary; sharing is user-initiated |
| Local assistant | Text command shortcuts with optional Android TextToSpeech output |
| Settings | Persists two local display preferences |

## Build environment

- JDK 17
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- Android SDK Platform 35 / Build Tools 35.0.0
- minSdk 17; targetSdk 28
- Java 8 language compatibility

Open this directory in Android Studio:

`reverse-engineering/hondahack-7.6.0/stage09/HondaHack-Rebuild`

Or build from a shell with Gradle 8.9 installed:

```bash
gradle assembleDebug
```

Output:

`app/build/outputs/apk/debug/app-debug.apk`

## Automated checks

The GitHub Actions workflow `.github/workflows/hondahack-safe-rebuild.yml` runs the static safety/feature checks, compiles the debug APK and uploads it as `hondahack-safe-rebuild-debug`.

Run the static checks locally:

```bash
python tests/test_safe_rebuild.py
```

## Privacy and safety boundary

- The app currently declares no Android runtime permissions.
- It does not use root, Xposed, shell execution, privileged settings writes, vehicle CAN/HMI commands, or destructive factory-reset/partition operations.
- Navigation uses only the destination the user enters and a standard Android Intent.
- Video access is limited to the user-selected content URI.
- Diagnostic sharing occurs only after the user taps Share.
- The local assistant uses typed commands and system TTS; it does not capture microphone audio or upload speech.

## Known limitations

- Original DEX is obfuscated. The native and DEX reports document evidence but do not recover all original Java/Kotlin/C/C++ source.
- Original private backend APIs, credentials, server-side behavior, proprietary assets and device-specific integrations are not recreated.
- Dashboard instrumentation, vehicle repair/optimization actions, Root/Xposed modules, privileged boot behavior, and system modification are intentionally not implemented.
- Map/player/TTS behavior depends on installed apps and OEM firmware.
- CI compilation is not a substitute for installation and testing on the target Honda head unit.

See `docs/DEVICE_SMOKE_TEST.md` before using on a vehicle.
