# Stage 91 — Safe foreground key-mapping parity slice

## Evidence baseline

- Original APK identity: SHA-256 `e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe`.
- The reverse-engineering report identifies a RemapKeysActivity and MyService key-action mapping, including navigation restart, rear-camera toggle, voice-assistant toggle, screen-cast and app-launch actions.
- Stage 85–90 evidence shows that global behavior relies on a mixture of app broadcasts, vendor service interfaces, and Xposed hooks. Those privileged mechanisms are not reproduced in this safe rebuild.

## Implemented in this stage

- Added `RemapKeysActivity` to the Android manifest and dashboard.
- Captures only `KeyEvent` values delivered while the mapping Activity is foreground.
- Persists local mappings in app-private SharedPreferences.
- Restricts actions to four app-owned destinations: Navigation, Local Video, Voice Assistant, and App Catalog.
- Added a static CI check for the new Activity and key-event mapping code.
- Added a compatibility note that distinguishes public Android key events from OEM/MCU steering-wheel routing.

## Explicitly not implemented

- Global steering-wheel key interception.
- Xposed/LSPosed injection or target-app hooks.
- Vendor Mitsubishi/Clarion AIDL binding, OEM HMI service calls, CAN-bus access, rear-camera control, screen casting, or instrument-cluster commands.
- Root, shell commands, app_process replacement, platform.xml replacement, or system partition modification.

## Why this is the safe parity boundary

The original app's global key mapping is not merely a UI preference: it depends on privileged input delivery and OEM-specific services. A normal APK cannot claim equivalent behavior unless the target head unit exposes a documented public integration. This stage implements the user-visible mapping workflow as a local foreground feature without pretending it controls the vehicle.

## Build and acceptance

- CI must run the static safety checks and assembleDebug.
- Device acceptance is pending. Test only while parked and only with keys that are safely available to the app.
- If the original Honda factory CarPlay head unit is still installed, APK installation is not assumed to be possible. Verify the head-unit OS and board first.

## Next stage

Stage 92 should reconstruct the instrument-meter UI as a demo/data-provider interface, with clearly labeled simulated values until a documented and read-only OEM data source is available.
