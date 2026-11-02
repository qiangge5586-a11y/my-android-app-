# DiPlay 0.3.12 Hotspot Fix — Stage 30: API-17 Safe SoftAP Preflight

Date: 2026-11-02
Branch: `diplay-0312-hotspot-fix-stage30`
Base: `diplay-0312-assistant-fusion-stage10`

## Objective

Address the first blocking fault before attempting another hotspot launch: establish whether this Android 4.2.2 / API 17 image exposes a usable legacy framework SoftAP path. Do not repeat the unsupported Wi-Fi Direct path or assume that finding a hostapd binary proves AP support.

## Source changes

- Added `LegacyHotspotProbe.java`, a Java/API-17-compatible, read-only capability inventory.
- Integrated its report into the existing compatibility checker.
- Added this branch to the existing GitHub Actions debug-build trigger.

The probe reports:
- FEATURE_WIFI and FEATURE_WIFI_DIRECT declarations;
- app-visible CHANGE_WIFI_STATE and ACCESS_WIFI_STATE permission grants;
- Wi-Fi service availability and current enabled state;
- concrete WifiManager implementation class;
- presence/visibility of legacy getWifiApState, getWifiApConfiguration, isWifiApEnabled and setWifiApEnabled signatures.

It does not invoke any setter or hidden method. It does not toggle Wi-Fi, stop supplicant, alter wlan0, launch hostapd/dnsmasq, execute su, or change OEM files.

## Why this is the first fix gate

The prior report records API 17, Wi-Fi Direct unavailable, and repeated hostapd exits with no PID alive and no hostapd log. The previous fusion audit also found that the old tethering entry point unconditionally disables system Wi-Fi before its fallback orchestration. Repeating that sequence is unsafe and does not diagnose the root cause.

A framework SoftAP method being present is only a hint. It is not proof of caller authorization or successful AP operation. The next stage must combine this output with driver capability evidence and, only after an explicitly authorized device test, verify AP beacon, client association and DHCP separately.

## Safety and compatibility rules

- No root or Shizuku requirement is introduced by this preflight.
- No global process termination and no blanket Wi-Fi/Bluetooth reset.
- No hostapd config overwrite.
- Unknown capability remains UNKNOWN; missing tools are not interpreted as unsupported hardware.
- Do not silently downgrade requested band/channel/security.
- Keep the original DiPlay 0.3.12 APK untouched.

## Build/delivery boundary

This branch still builds the independent diagnostic checker APK, not a replacement DiPlay APK. The original 0.3.12 Gradle source project and matching signing private key remain absent. No integrated DiPlay APK is claimed.

## Stage 30 acceptance

- [x] API-17 read-only framework SoftAP inventory added.
- [x] Probe wired into diagnostic report.
- [x] Branch-specific CI build trigger added.
- [ ] GitHub Actions build passes.
- [ ] Run on the actual MY16ADA head unit and collect the new report.
- [ ] Confirm a permitted legacy framework/vendor backend before writing any AP-start operation.
