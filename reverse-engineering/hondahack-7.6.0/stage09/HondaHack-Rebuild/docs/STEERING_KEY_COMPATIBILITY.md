# Steering-wheel key compatibility boundary

## What this module does

The Stage 91 key-mapping screen records Android `KeyEvent` values only while `RemapKeysActivity` is visible in the foreground. A captured key can be mapped locally to one of four app-owned destinations: Navigation, Local Video, Voice Assistant, or App Catalog. Mappings are stored in this app's private SharedPreferences.

## What it cannot do

- It cannot globally intercept physical keys while another app is foreground.
- It cannot override keys consumed by the Honda head unit's MCU, launcher, system service, or vendor input dispatcher.
- It does not register privileged receivers, use Accessibility to capture unrelated input, invoke root, install Xposed, patch `app_process`, or write system partitions.
- It does not send commands to the vehicle CAN bus or change vehicle settings.

The original APK's Xposed hook and vendor service vocabulary show that global steering-wheel behavior depends on target-package hooks and OEM-specific integration. A normal third-party APK cannot reproduce that behavior with public Android APIs alone.

## Target vehicle caveat

The target profile is a 2019 Honda Civic 220TURBO CVT 劲动版 with the user's stated original center display. The prior compatibility assessment notes that the factory display/CarPlay feature does not prove the head unit runs Android. If it is the original CarPlay head unit, a normal APK cannot be installed directly on it. Android 4.4.2 (API 19) remains an unverified reported runtime, not a confirmed factory-head-unit specification.

Before claiming steering-key or APK-install compatibility, record the actual head-unit Android version/build, board/CPU ABI, MCU version, display resolution, and whether the unit is OEM or aftermarket. The APK's minSdk 17 permits installation on API 19 in principle; it does not prove that the user's display is an Android app host.

## Validation

1. Open the rebuild app and enter 按键映射.
2. Tap 开始捕获下一个按键.
3. Press a key that the Android input system delivers to the foreground app.
4. Select one of the four app-owned destinations and save.
5. Press the same key again while this screen is foreground; confirm it opens the mapped page.
6. Repeat with a physical steering-wheel key only if it is safe to do so while parked. If no event is received, record it as OEM/system interception rather than a rebuild defect.
