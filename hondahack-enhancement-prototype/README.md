# HondaHack Enhancement Center prototype

This is a standalone test harness for the proposed 7.7.0 Enhancement Center. It is **not** the modified HondaHack APK and must not be installed as a replacement for HondaHack 7.6.0.

## Features in this prototype

- Read-only Android device/build information
- Read-only active network status
- Shared storage capacity information
- List and launch installed launcher apps
- Open Android Wi-Fi settings
- Select a file through the system content picker

## Compatibility

- minSdk 17 (Android 4.2)
- Java 7 source compatibility
- Android Gradle Plugin 8.6.1
- compileSdk 35 / targetSdk 28
- No AndroidX or third-party runtime libraries

## Build

Open this directory as an independent Gradle project in Android Studio, or run:

```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Integration warning

The original HondaHack APK still needs a DEX-aware class merge, manifest registration, a proven MainActivity entry point, rebuild/resource validation, signing and device tests. Its original signing private key is not contained in the APK. This standalone prototype does not satisfy those integration steps.
