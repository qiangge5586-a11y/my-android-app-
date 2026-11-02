# DiPlay 0.3.12 + Hotspot Assistant v2.0 — Stage 10 Integration Contract

## Scope

The target is one integrated DiPlay 0.3.12 APK. The hotspot-assistant APK is the donor of hotspot-control behavior only. Do not merge DiPlay 0.3.3, do not ship a second helper APK, and do not replace the target APK with the compatibility-checker project.

## Repository inventory checked on 2026-11-02

The connected repository's default branch contains a standalone read-only compatibility-checker Android app:
- `app/build.gradle` declares applicationId `com.diplay.hotspotcheck`.
- `app/src/main/AndroidManifest.xml` declares the checker launcher activity.
- `README.md` explicitly says this app does not contain DiPlay hotspot startup or CarPlay features.
- The repository does not contain DiPlay 0.3.12 source, the DiPlay 0.3.12 APK, or the hotspot-assistant donor APK.

Therefore this repository is not currently a source/build baseline for the requested merged product. This branch records the integration contract; it does not claim to contain an APK patch.

## Required backend order

1. Ecarx vendor backend, only when the runtime factory and methods exist and are callable.
2. Android modern tethering / Soft AP backend.
3. Android legacy Wi-Fi AP backend.
4. Root + hostapd as a last resort, with strict ownership of processes and files.

The donor's Ecarx flow is reflective:
- Load `com.ecarx.xui.adaptapi.wifiap.Wifi6Ap`.
- Resolve and invoke `create(Context)`.
- Resolve band constants from `com.ecarx.xui.adaptapi.wifiap.IWifiAPHost`.
- Apply AP configuration, request enable, then confirm observed AP state with a bounded wait.

The presence of these strings in the donor DEX does not prove that a target head-unit firmware ships the Ecarx classes or grants access. Runtime absence, reflection failure, permission denial, or explicit rejection must fall through safely.

## DiPlay lifecycle integration requirements

- Keep DiPlay's existing opt-in and boot-recovery lifecycle; do not blindly import a second BootReceiver, RetryReceiver, JobService, or donor UI.
- Remove unconditional Wi-Fi shutdown from the hotspot startup path.
- Remove LocalAdb / runtime-ADB fallback from this startup path. Do not substitute Shizuku.
- Fix the unknown-initial-state branch so it still invokes the selected backend and polls for state; removing an ADB callback alone must not short-circuit startup as UNSUPPORTED.
- Do not globally kill `wpa_supplicant`, `hostapd`, or `dnsmasq`.
- Do not alter OEM Bluetooth, MCU/CAN, reverse-camera, DSP/audio, thermal, or projection services.
- Root fallback may stop only a process whose PID, start time, executable/cmdline, and DiPlay session ownership have been revalidated.
- Shared system configuration must not be overwritten or deleted without a guarded backup/restore strategy.

## Validation gates before release

- DEX and manifest merge verified; no duplicate class definitions.
- Original DiPlay four-ABI native library set preserved.
- APK zip integrity, alignment, and signature verified.
- Ecarx-unavailable path falls back without crashing.
- AP state confirmed independently of API return value.
- On-device tests: enable, client association, DHCP lease, reconnect, disable, and restoration of the pre-existing Wi-Fi state.
- Explicitly label whether the build can update the installed DiPlay or requires uninstall/reinstall. A different signing key normally prevents in-place update.

## Current status / limitations

This commit is an integration contract only. No DEX bytecode has been patched, no APK has been rebuilt or signed, and no vehicle test has been performed.

The GitHub repository currently holds the diagnostic checker project, not the two APK inputs needed for a reproducible merge. A workflow run in this repository cannot access files that exist only in a separate local ChatGPT execution workspace. The original DiPlay signing key is also not present in this repository inventory.

Do not publish an artifact from this repository as the merged DiPlay product.
