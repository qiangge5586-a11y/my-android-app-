# HondaHack Rebuild — Android Studio project

Independent, safe reconstruction of selected user-facing functions observed in HondaHack 7.6.0. This is **not** the original source code and is not a drop-in replacement.

## Important installation detail

The rebuild uses application ID `cn.autohack.hondahack.rebuild`, while keeping Java namespace `cn.autohack.hondahack`. This allows it to be installed beside the original package and avoids silently replacing the original app or its data.

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

Or run `gradle assembleDebug`. Output:
`app/build/outputs/apk/debug/app-debug.apk`

## Automated checks

GitHub Actions runs static safety/feature checks, compiles the debug APK and uploads `hondahack-safe-rebuild-debug`.
Run locally with `python tests/test_safe_rebuild.py`.

## Privacy and safety

- No Android runtime permissions are declared.
- No root, Xposed, shell execution, privileged settings writes, vehicle CAN/HMI commands, or destructive system operations.
- Navigation uses only a user-entered destination and a standard Android Intent.
- Video access is limited to a user-selected content URI.
- Diagnostics are shared only after the user taps Share.
- The local assistant uses typed commands and system TTS; no microphone recording or speech upload.

## Known limitations

Original DEX is obfuscated. Native/DEX reports document evidence but do not recover all Java/Kotlin/C/C++ source. Original private backend APIs, credentials, server-side behavior, proprietary assets and device-specific integrations are not recreated. Dashboard instrumentation, vehicle repair/optimization actions, Root/Xposed modules, privileged boot behavior and system modification are intentionally not implemented.

Map/player/TTS behavior depends on installed apps and OEM firmware. CI compilation is not a substitute for physical testing on the target Honda head unit. See `docs/DEVICE_SMOKE_TEST.md`.
