# Stage 83 — 2019 Civic CVT Jindong target profile

## Vehicle profile inferred from the user's description

- Market/model: Dongfeng Honda Civic, 2019 model year, tenth generation.
- Trim: 220TURBO CVT 劲动版 (Jindong), subject to confirmation against the vehicle registration/trim plate.
- This is the 1.5T CVT mid/entry configuration in the 2019 lineup, not the higher 劲擎/燃擎 Honda CONNECT navigation configurations.
- Public 2019 road-test and trim-comparison material describes the lower 220TURBO configurations including 劲动版 as having a 7-inch display and Apple CarPlay; higher 劲擎/燃擎 configurations are described with Honda CONNECT/GPS navigation. Sources are listed below.

## Important distinction: vehicle model does not identify the head-unit OS

The factory 7-inch display/CarPlay feature does **not** establish that the factory head unit runs Android, nor does it establish Android 4.4.2. Public trim specifications and reviews found for this vehicle do not publish an Android build fingerprint, Android API level, head-unit board model, or firmware version.

Therefore:
- Android 4.4.2 (API 19) is treated as the user's reported target runtime, not as a version independently verified from the 2019 Civic trim.
- If the vehicle still has its factory CarPlay head unit, a normal APK cannot be installed onto it merely because the car has a touchscreen. CarPlay is phone projection, not an Android app-install environment.
- If an aftermarket Android head unit has been installed, its Android version and board/MCU are determined by that replacement unit, not by the Civic trim. The user's reported Android 4.4.2 can then be used as the compatibility target.

## Rebuild compatibility assessment

Current app Gradle configuration:
- minSdk 17 (Android 4.2)
- targetSdk 28
- Java source/target compatibility 1.8
- applicationId: cn.autohack.hondahack.rebuild

API 19 is above minSdk 17, so the app's declared minimum SDK permits installation on Android 4.4.2. This is not a guarantee of full runtime compatibility: the specific head-unit firmware, CPU ABI, display density/resolution, package installer, and available map/media/TTS apps still need device testing.

Existing compatibility choices retained:
- API 19 document picker path with a fallback for API 17/18.
- Text-to-speech uses compatible speak overloads.
- No runtime permissions are declared.
- No vehicle CAN, steering-wheel key, instrument cluster, privileged settings, root, or Xposed operations are implemented.

## Required hardware validation

Do not label the target device as verified until the actual head unit is inspected. On the head unit, open Settings → About device / System information and record:
1. Android version and build number.
2. Model/device/board name.
3. MCU version (if shown).
4. CPU architecture/ABI (if shown).
5. Screen resolution and density (if shown).
6. Whether it is the original Honda factory unit or an aftermarket replacement.

Then install only the rebuild package `cn.autohack.hondahack.rebuild` on a compatible Android head unit and run `docs/DEVICE_SMOKE_TEST.md`. Do not uninstall or overwrite the original `cn.autohack.hondahack` package.

## Public research references

- 2019 Civic launch and trim lineup (Autohome, 2019-05-17): https://www.autohome.com.cn/news/201905/936268.html
- 2019 Civic lower-trim CarPlay vs. higher-trim Honda CONNECT comparison (Sohu automotive review): https://www.sohu.com/a/346038777_215942
- Similar report syndicated by Phoenix Auto: https://auto.ifeng.com/c/7qf0UJh6urW

These sources support trim/display/connectivity distinctions only. They do not prove the factory head unit runs Android 4.4.2.
