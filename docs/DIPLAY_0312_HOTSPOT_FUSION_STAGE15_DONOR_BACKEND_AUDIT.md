# DiPlay 0.3.12 Hotspot Fusion — Stage 15 Donor Backend Control-Flow Audit

## Scope and exact inputs

- DiPlay base: `DiPlay-v0.3.12-legacy-release(1).apk`, SHA-256 `e8cc6ba9f87fed01380e4bae990f4df9232d84d299bbc0ee67c7ea08f99b5bab`.
- Donor: `热点自启助手.apk.1`, SHA-256 `9fc3eea63d19a2928b9e2f540361eec0e4ae98a8ae20b730649e11806f822daf`.
- Donor DEX extracted from `classes.dex`; package `app.autohotspot.helper`.
- Inspected methods: `HotspotController.start/vendor/vendorStart/systemStart/modernStart/legacyConfig/state/stop/await/call`.

This is a static DEX instruction and call-reference audit. It is not a complete source decompilation, runtime trace, merge, or device test.

## Finding 1 — donor's backend priority and runtime verification

The donor `HotspotController.start()` follows this broad sequence:

1. Read current hotspot state.
2. If hotspot state is already confirmed on, return without restarting or overwriting the existing configuration.
3. Read SSID/password/band preferences and validate custom configuration when enabled.
4. Try the vendor backend.
5. If vendor start does not confirm success, try the Android system backend.
6. Only when the saved `allowWifiOff` compatibility option is enabled, consider the Wi-Fi-off compatibility retry path.
7. If no backend confirms hotspot state, report failure rather than claiming success.

The vendor backend is a runtime reflection probe for `com.ecarx.xui.adaptapi.wifiap.Wifi6Ap`, calls its static `create` factory, then looks up `setWifi6ApConfiguration` and `setWifi6ApEnabled`. It waits for the reported hotspot state before treating the operation as successful. The mere presence of these class-name strings in the donor does not prove the target Honda head unit has the Ecarx service or compatible implementation.

## Finding 2 — donor's Android system backend is a compatibility ladder, not one API

The donor system path contains:

- Legacy `WifiManager` reflection for `setWifiApConfiguration`, `startSoftAp`, and/or `setWifiApEnabled`, depending on API availability.
- A modern reflection path that looks for `android.net.wifi.SoftApConfiguration` and its `Builder`, sets SSID/passphrase/band, applies `setSoftApConfiguration`, then invokes `startTetheredHotspot`.
- A state poll after requesting startup; a successful method return alone is not treated as final proof.

The modern method is API-dependent. On older Android builds, the class lookup will fail and the code proceeds through its compatibility path. Conversely, on a modern Android build, method existence does not guarantee the app has the required system privilege. Runtime invocation can still fail due to permission/signature restrictions.

## Finding 3 — donor has a safer pre-existing-hotspot guard, but its radio retry is still opt-in destructive behavior

The donor explicitly avoids restarting an already-confirmed hotspot or replacing its existing settings. This is worth preserving.

However, the donor's `allowWifiOff` option can disable station Wi-Fi and retry the vendor/system backend. It later contains several Wi-Fi restoration branches. That is better than an unconditional common-entry Wi-Fi shutdown, but it is still not suitable as an automatic default for a vehicle: restoration may fail, state can change concurrently, and the code does not prove that DiPlay was the only actor changing Wi-Fi.

Fusion rule: do not import `allowWifiOff` as an enabled default. The default backend attempt must preserve station Wi-Fi. Any radio-exclusive retry must be separately user-opted-in, record the exact prior state, recheck state ownership before restoring, and abort if the user/system changes Wi-Fi during the operation.

## Finding 4 — donor state detection uses legacy Wi-Fi AP reflection

The donor `state()` method reflectively tries `getWifiApState` and `isWifiApEnabled`, mapping legacy AP state values to on/off/unknown. This is useful for older framework builds, but it is not a universal state API for modern tethering or vendor implementations.

The fused controller must combine backend-specific state confirmation with framework/network observations and preserve an explicit UNKNOWN state. A failed reflection call must never be interpreted as hotspot OFF or ON.

## Fusion mapping to DiPlay 0.3.12

| Donor method | Port decision | Reason |
|---|---|---|
| `vendor()` / `vendorStart()` | Port conceptually, reimplement with runtime capability checks | Ecarx presence is unconfirmed on target |
| `modernStart()` | Port as a distinct backend | API and privilege checks must be runtime-gated |
| `systemStart()` / `legacyConfig()` | Port only compatible calls supported by target SDK/runtime | Legacy reflection is device/API-sensitive |
| `state()` | Port as tri-state backend probe, not a single universal truth | Reflection may be absent or restricted |
| `await()` | Port bounded polling and cancellation | Avoid indefinite waits |
| `start()` | Do not transplant wholesale | Contains Wi-Fi-off retry and donor preferences |
| `stop()` | Reimplement with backend ownership | Must not stop a hotspot owned by another system component |
| donor `BootReceiver`, jobs, retry services, UI, preferences | Do not copy wholesale | DiPlay already has its own boot hook and lifecycle |

## Proposed fused backend contract

Each backend should implement the same small contract:

- `probe(): ON | OFF | UNKNOWN`
- `canStart(): capability result with reason`
- `start(config): START_REQUESTED | REJECTED | UNSUPPORTED`
- `awaitState(deadline, cancellation): ON | OFF | UNKNOWN`
- `stopIfOwned(sessionToken): STOPPED | NOT_OWNED | FAILED`
- `restore(changesOwnedByThisSession): RESTORED | SKIPPED | FAILED`

Backend order remains:

1. Verified vehicle vendor SoftAP.
2. Android modern/system tethering.
3. Android legacy Wi-Fi AP.
4. Root hostapd as last resort, with process/config ownership isolation.

Do not treat `START_REQUESTED` as success. A backend only succeeds after state confirmation, and internet sharing requires separate DHCP, DNS, and NAT/forwarding checks.

## Immediate implementation decisions

1. Keep DiPlay's existing `BootReceiver` and opt-in preference.
2. Reuse DiPlay's existing UI and settings rather than importing donor UI/preferences.
3. Add vendor API reflection only behind class/factory/method availability checks and a real state confirmation.
4. Do not call the donor `allowWifiOff` retry automatically.
5. Do not transplant donor `stop()` without proving the active backend/session is owned by DiPlay.
6. Do not use root hostapd until the safer framework/vendor backends have returned UNSUPPORTED or a verified failure.
7. Keep all backend attempts serialized to avoid concurrent Wi-Fi state changes.

## Status and limitations

- Both APK inputs were identified by their recorded SHA-256 values.
- Donor `HotspotController` methods were extracted and statically inspected.
- No DEX class was relocated or merged.
- No APK was patched, rebuilt, signed, installed, or tested on the vehicle.
- The target head unit's Android API level, Ecarx class availability, SoftAP privilege grants, and radio coexistence behavior remain unknown.
