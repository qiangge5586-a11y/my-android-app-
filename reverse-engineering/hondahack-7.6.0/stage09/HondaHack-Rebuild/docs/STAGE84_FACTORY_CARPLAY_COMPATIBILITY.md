# Stage 84 — Factory-screen installation and compatibility decision

## Decision

For the user's 2019 Dongfeng Honda Civic 220TURBO CVT Jindong (劲动版), proceed on the factory-screen assumption as a CarPlay host, not as an Android head unit.

A 2019 road-test comparison states that the lower 2019 Civic trims (劲动版/燃动版) support CarPlay, while the higher 劲擎版/燃擎版 use Honda CONNECT/GPS navigation and do not provide the same CarPlay setup. A separate trim review describes the 7-inch display and CarPlay in the lower trim. This is useful public evidence but does not identify a firmware build or prove Android is installed.

## Can the HondaHack APK be installed on the factory display?

No supported direct installation path has been established.

The reconstruction is an Android APK (cn.autohack.hondahack.rebuild). The factory display's CarPlay function is a phone projection / host interface, not an Android package installer. Android APK files cannot be copied to a CarPlay screen and launched as native apps.

Do not attempt:
- sideloading an APK into the factory head unit using a USB stick;
- Android ADB commands or Android settings paths against the factory unit without evidence it runs Android;
- flashing generic Android firmware or an unrelated Honda head-unit image;
- CAN-bus, steering-control, or vehicle firmware writes to try to expose an app launcher.

## Practical options

### Option A — Keep the factory screen and use supported CarPlay apps (recommended, least invasive)

- Use an iPhone and the factory wired CarPlay connection.
- Install compatible navigation/audio/communication apps on the iPhone; only apps supported by CarPlay appear on the car display.
- Use the HondaHack Android reconstruction on an Android phone separately. It can provide phone-side diagnostics and local functions but will not appear as an arbitrary app on the factory CarPlay screen.
- If an Android phone is used, Bluetooth may support calls/audio depending on the vehicle, but this does not mean Android Auto or Android screen mirroring is supported.

### Option B — Investigate a wired-CarPlay Android AI box (conditional, not yet approved)

Some aftermarket boxes use the vehicle's wired-CarPlay interface and provide a separate Android runtime on the box. This is not the same as installing an APK into the Honda factory unit. Compatibility is model-specific and must be confirmed by the vendor for the exact 2019 Civic factory head unit, USB protocol, display resolution, touch input, audio routing, boot behavior and firmware.

Do not purchase based only on a generic Honda-compatible label. Confirm return policy and exact model compatibility. The box's own Android version/ABI must meet the APK requirements; the vehicle's CarPlay display itself still does not run Android.

### Option C — Replace the head unit with a compatible Android head unit (most invasive)

This may create a true Android APK installation environment, but can affect factory integration: steering-wheel controls, reverse camera, USB, microphone, factory amplifier, vehicle settings, boot behavior and trim fitment. Use a vehicle-specific harness and a qualified installer. Retain the original unit and avoid modifications to airbag, SRS, immobilizer or vehicle control wiring.

## Current rebuild APK compatibility

The rebuild uses minSdk 17, so Android 4.4.2 (API 19) meets the declared minimum API level. That does not make it installable on the factory CarPlay unit. It can only be evaluated on an actual Android runtime such as a compatible aftermarket Android head unit or Android AI box. CPU ABI, firmware, screen dimensions, storage and system WebView/TTS/maps must be checked.

## Verification checklist before any purchase or install

1. Confirm the head unit is factory original and not a previous aftermarket replacement.
2. Confirm CarPlay works by connecting a compatible iPhone to the factory USB data port.
3. Photograph the main menu, USB-port labels and any unit information screen.
4. Record the head-unit part number / serial label if safely accessible without dismantling.
5. If considering an AI box, give its vendor the exact vehicle year/trim and photos; get written confirmation that it supports this wired-CarPlay head unit and touch/audio controls.
6. If considering APK installation, first verify that the target device itself boots Android and record Android version, build number, board/device, CPU ABI and screen resolution.
7. Install only the separate package cn.autohack.hondahack.rebuild; do not replace the original app or flash the vehicle's factory firmware.

## Sources

- 2019 Civic infotainment comparison, including lower trims 劲动版/燃动版 and CarPlay: https://www.sohu.com/a/346038777_215942
- Reproduced automotive report with same trim distinction: https://auto.ifeng.com/c/7qf0UJh6urW
- 2019 Civic 220TURBO CVT 劲动版 configuration listing: https://www.shenyangcar.com/chexing/39311.htm
- Apple Support: connect iPhone to CarPlay; wired USB connection and supported vehicle requirements: https://support.apple.com/zh-cn/102521
- Apple Support: only compatible apps appear in CarPlay: https://support.apple.com/zh-cn/guide/iphone/-iph206c570e3/ios
- Apple Developer: CarPlay integration is entitlement/category-based, not arbitrary Android APK execution: https://developer.apple.com/documentation/carplay/requesting-carplay-entitlements

## Acceptance status

- Research-based factory-screen path decision: complete.
- APK direct install on original CarPlay display: not supported.
- AI box path: conditional; exact hardware identification and vendor confirmation required.
- Actual in-car test: pending; no physical vehicle access was available during this work.