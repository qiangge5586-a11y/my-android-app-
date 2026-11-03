# HondaHack 7.7.0 Enhancement Center — Stage 09

## Objective

Add a separate **增强功能** entry/page while preserving the original HondaHack 7.6.0 UI and existing behavior. This is a source prototype and integration specification, not a claim that the original APK has already been modified.

## Verified baseline

- Package: `cn.autohack.hondahack`
- Version: `7.6.0` / versionCode `1`
- minSdk / targetSdk: `17 / 17`
- Launcher activity: `cn.autohack.hondahack.MainActivity`
- Existing modules already cover app installation/listing, autorun management, navigation, dashboard/meter, key remapping, startup media, optimization/fix and settings.
- The APK contains ARM native libraries, Xposed-related resources and system-level integration clues. Preserve the original APK as an immutable baseline.

## New page scope

The first enhancement page is deliberately low-risk and does not duplicate existing HondaHack modules:

1. **设备信息** — Android release/API, manufacturer, model, ABI and build fingerprint.
2. **网络诊断** — current connectivity and Wi-Fi state; read-only, no toggling Wi-Fi or modifying network settings.
3. **存储信息** — available and total app-visible storage.
4. **应用快捷入口** — enumerate launchable apps and open the selected app; do not silently install, uninstall or modify packages.
5. **系统设置快捷入口** — open standard Android Wi-Fi/settings screens through public intents.
6. **文件选择入口** — use Android's document/content picker where supported; never browse protected system directories.

## Compatibility rules

- Java-only implementation; no AndroidX, Kotlin, Compose, lambdas or API newer than Android 4.2 in the initial prototype.
- Guard optional APIs at runtime.
- Use platform Holo widgets already available on API 17.
- No root commands, Xposed hooks, hidden APIs, signature spoofing, authorization bypass, shell execution, partition writes, factory reset, or background persistence.
- No vehicle CAN/MCU, reverse-camera, DSP, thermal or driving-control commands.
- No network upload or telemetry.

## Integration strategy

1. Build and test the enhancement Activity as a standalone Java source component.
2. Verify API-17 compilation and inspect its DEX references.
3. Add an explicit menu item to the original `MainActivity` only after its exact layout/menu construction path is confirmed from DEX evidence.
4. Register the enhancement Activity in the original manifest.
5. Merge the new class into the original DEX using a DEX-aware toolchain; do not append raw ZIP entries or edit DEX bytes.
6. Rebuild resources/manifest, preserve all original native ABIs and assets, align and sign.
7. Verify package identity and signing constraints before any installation attempt.

## Known hard blockers

- The supplied APK does not contain the original source project.
- The original signing private key is not present in the APK. A newly signed APK is not a normal in-place update and may lose signature-level privileges.
- Previous reverse-engineering records establish DEX inventories and call graphs, but do not provide complete Java source or a verified MainActivity UI insertion point.
- The current GitHub repository contains a separate hotspot-checker Android project plus HondaHack reverse-engineering records; it is not the original HondaHack Gradle project.
- Apktool/JADX/Smali/aapt/apksigner availability must be verified in the build environment before claiming an APK build.

## Stage 09 result

- Enhancement page scope defined.
- Compatibility and safety boundaries defined.
- Standalone Java Activity prototype is being added in this branch.
- Original APK has **not** been modified, rebuilt, signed, installed or device-tested.

## Acceptance criteria

- Existing 7.6.0 functions remain unchanged.
- Enhancement entry opens a separate page and Back returns to the original screen.
- All controls use public Android APIs and fail gracefully when unavailable.
- No privileged operation is performed without an explicit, separately reviewed implementation.
- API-17 build and runtime smoke test pass.
- Package/signing migration risk is documented before producing an installable candidate.
