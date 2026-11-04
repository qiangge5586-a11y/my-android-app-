# HondaHack 7.6.0 — feature parity matrix

This matrix compares the features identified in the supplied reverse-engineering report with the independent safe rebuild. “Implemented” means the app contains a corresponding safe user-facing flow; it does not imply byte-for-byte or behavioral equivalence with the original.

| Original feature area | Safe rebuild status | Current implementation / boundary |
|---|---|---|
| Main dashboard | Implemented | Rebuild dashboard with links to local modules |
| Device information | Implemented | Read-only Android/device/storage summary |
| App catalog | Partial | Launcher-visible app list, search, launch and system app-info hand-off |
| Local APK installation | Partial | User-selected APK passed to Android package installer; user confirmation required |
| Online app store | Not implemented | Original private backend is not recreated |
| Steering-wheel key remapping | Partial | Foreground-only Android KeyEvent capture and local app-page mapping; no global/OEM input hooks |
| Instrument meter | Partial | Visual gauge with explicitly fixed demo values; no live telemetry |
| Navigation | Partial | Standard geo: hand-off to installed map app; no proprietary navigation overlay integration |
| Navigation audio ducking | Partial | User-triggered transient AudioManager focus test; not automatic external-navigation detection |
| Voice assistant | Partial | Typed local commands and on-device TTS; no microphone recording or wake-word service |
| Local video | Implemented | User-selected content URI and external player hand-off |
| Startup video | Partial | API 19+ persistent local video preset, opened manually; no boot-time playback |
| Diagnostics | Implemented | Read-only local summary and user-initiated share |
| Operation log | Partial | Last 100 app screen-open events, local clear/export; no system logcat |
| App display preferences | Implemented | Local summary/count preferences and app-only keep-screen-on flag |
| Startup app manager / autorun | Not implemented | No boot persistence or third-party app auto-launch |
| SD-card permission repair | Not implemented | No platform.xml replacement or global permission changes |
| System optimization / cache cleanup | Not implemented | No cache clearing, process killing or system writes |
| Emergency mode | Not implemented | No vehicle-motion inference or emergency control path |
| Rear camera toggle | Not implemented | No OEM camera service or vehicle-control broadcast |
| Screen casting / instrument projection | Not implemented | No Xposed target hooks, OEM display services or CAN integration |
| Vehicle telemetry | Not implemented | No CAN/OBD/vendor AIDL reads; meter values remain demo-only |
| Mitsubishi / Clarion integration | Not implemented | No private AIDL bindings or vendor broadcasts |
| Root / Xposed installer | Intentionally excluded | No su, app_process replacement, Xposed injection or system partition writes |
| Factory reset / repair scripts | Intentionally excluded | No destructive shell commands or recovery scripts |
| Licensing / signature spoofing / remote revoke | Intentionally excluded | No auth bypass, certificate spoofing, private license protocol or remote device destruction |
| Online video service | Not implemented | No private video backend or remote media fetch |

## Compatibility baseline

- Application ID: `cn.autohack.hondahack.rebuild` (kept distinct from the original package).
- minSdk: 17 (Android 4.2); targetSdk: 28.
- Android 4.4.2 is API 19 and is above minSdk, but runtime compatibility still depends on the actual head-unit firmware and hardware.
- A factory Honda CarPlay display is not an Android APK host merely because it has a touchscreen. Confirm the exact head-unit OS before attempting installation.

## Remaining acceptance work

1. Finish CI validation for the latest branch head and retain the APK artifact/checksum.
2. Confirm the exact target head-unit OS/build, board/CPU ABI, MCU and screen resolution.
3. Run the documented device smoke test on compatible Android hardware.
4. Record each feature as device-verified, unsupported by firmware, or not tested.
