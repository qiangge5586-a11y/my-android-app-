# DiPlay 0.3.12 Hotspot Fusion — Stage 16 Boot Hook and Preference Gate Audit

## Scope and inputs

- Base APK: `DiPlay-v0.3.12-legacy-release(1).apk`
- SHA-256: `e8cc6ba9f87fed01380e4bae990f4df9232d84d299bbc0ee67c7ea08f99b5bab`
- DEX targets: `classes.dex` and `classes3.dex`
- Methods inspected: `BootReceiver.onReceive`, `BootReceiver.healHotspotIfOptedIn`, its generated worker/lambda, `CarHotspotSetup.shouldStartOnLaunch`, `CarHotspotSettings.enabled/shouldEnable`, and relevant `AirPlayPersistence` getters.

This is a static DEX instruction and call-reference audit. It is not a complete source decompilation or device runtime trace.

## Finding 1 — DiPlay already has one boot receiver; hotspot startup is separate from app auto-launch

`BootReceiver.onReceive` checks the broadcast action against `android.intent.action.BOOT_COMPLETED`. It then reads `AirPlayPersistence.loadAutoStartOnBoot()` to decide whether to launch `DiPlayActivity`.

Regardless of that app-launch branch, the method reaches `healHotspotIfOptedIn(context)` at the end of the receiver method. Therefore:

- Disabling DiPlay's UI auto-launch does not, by itself, disable the separate hotspot self-heal path.
- The hotspot path is not dependent on starting `DiPlayActivity`.
- The fusion must keep and extend this existing receiver; do not add the donor's second `BootReceiver`, duplicate boot filters, or duplicate retry services.

The inspected receiver checks `BOOT_COMPLETED`; this audit does not establish a `LOCKED_BOOT_COMPLETED` path.

## Finding 2 — hotspot auto-start is guarded by multiple independent persisted conditions

`BootReceiver.healHotspotIfOptedIn` first calls `CarHotspotSetup.shouldStartOnLaunch(context)`. That method checks:

1. `AirPlayPersistence.loadWirelessEnabled(context)` — SharedPreferences file `xcertplay_airplay`, key `wireless_enabled`.
2. `AirPlayPersistence.loadWirelessHotspotMode(context)` — the selected wireless mode.
3. `CarHotspotSettings.shouldEnable(context, mode)` — includes the dedicated hotspot auto-enable preference.
4. For manual hotspot configuration, it loads `manual_hotspot_ssid` and `manual_hotspot_passphrase` and rejects startup if `ManualHotspotValidation.error(...)` reports invalid settings.

The dedicated hotspot preference is stored separately:

- SharedPreferences file: `diplay_car_hotspot`
- Key: `auto_enable`
- Getter: `CarHotspotSettings.enabled(context)`
- Setter: `CarHotspotSettings.setEnabled(context, value)`
- The getter uses `getBoolean` with a false default.

Thus the boot self-heal is not simply “start hotspot whenever the device boots.” It is guarded by DiPlay's wireless-enabled state, selected hotspot mode, the dedicated hotspot auto-enable opt-in, and manual-configuration validation where applicable.

The mode enum contains `WIFI_P2P`, `LOCAL_ONLY_HOTSPOT`, `MANUAL`, `EXISTING_WIFI`, and `HOSTAPD`. The current `shouldEnable` method combines the dedicated opt-in with a mode check; the exact enum predicate is not treated as fully reconstructed from the compact instruction listing.

## Finding 3 — already-on is treated as a no-op; unknown state can proceed to recovery

After the preference gate, `healHotspotIfOptedIn` calls `CarHotspotStatus.isEnabled(context)` and compares the result with `Boolean.TRUE`.

- Confirmed ON: logs that the hotspot is already on and returns without starting it again.
- OFF or UNKNOWN/null: continues into the self-heal worker.

This is a useful idempotency guard, but it means the status probe must be trustworthy. In the fusion controller, UNKNOWN should be retained as a distinct state and the selected backend should perform its own bounded state confirmation before any disruptive recovery.

## Finding 4 — startup runs asynchronously while the broadcast is held open

The boot hook calls `goAsync()`, constructs a worker thread named `boot-hotspot-heal`, and starts it. The worker calls `CarHotspotTethering.enable(...)`, logs the result, and finishes the `PendingResult` on both success and exception paths.

This is preferable to performing a long hotspot startup directly on the broadcast receiver's main thread. Preserve the bounded/exception-safe completion behavior when replacing the old backend call.

However, the current worker still enters `CarHotspotTethering.enable`, whose first operation is `disableWifiBeforeHotspot` (Stage 14). Therefore the existing boot gate is sensible, but the current action behind it is still too disruptive for the fusion target.

## Preference and boot-flow map

| Purpose | Storage / method | Fusion behavior |
|---|---|---|
| Launch DiPlay UI after boot | `xcertplay_airplay:auto_start_on_boot` | Keep independent from hotspot startup |
| Enable wireless feature | `xcertplay_airplay:wireless_enabled` | Preserve as an existing gate |
| Select wireless/hotspot mode | `xcertplay_airplay:wireless_hotspot_mode` | Preserve; backend must respect selected mode |
| Enable hotspot auto-start | `diplay_car_hotspot:auto_enable` (default false) | Preserve as explicit opt-in |
| Manual SSID/passphrase | `manual_hotspot_ssid`, `manual_hotspot_passphrase` | Validate before starting; do not log secrets |
| Hotspot already-on check | `CarHotspotStatus.isEnabled` | Preserve no-op when confirmed ON |
| Boot execution | Existing `BootReceiver` + `goAsync` worker | Reuse; no duplicate receiver |

## Fusion implementation decision

1. Keep the existing `BootReceiver` and its `BOOT_COMPLETED` filter.
2. Keep app auto-launch and hotspot auto-start as separate settings.
3. Preserve the exact dedicated hotspot opt-in default of false; never silently enable boot hotspot after an update.
4. Preserve wireless-enabled and mode gates, plus manual SSID/passphrase validation.
5. Replace only the worker's backend action with the new serialized backend coordinator; do not create another boot worker or donor job service.
6. Preserve `goAsync` and guaranteed `PendingResult.finish()` on all completion/error/cancellation paths.
7. Do not call the current `CarHotspotTethering.enable` unchanged because it disables Wi-Fi before selecting a backend.
8. If the status probe is UNKNOWN, do not run global process cleanup or reset Bluetooth. Probe backend capabilities, then use the least disruptive supported backend.
9. Do not write SSID/passphrase to logs. Keep credentials in the existing secure/persisted configuration path and pass them only to the chosen backend.

## Status and limitations

- Static instruction sequences and preference keys were extracted from the exact 0.3.12 DEX.
- The exact enum predicate inside `CarHotspotSettings.shouldEnable` is not fully reconstructed; only its use of the dedicated enable setting and mode argument is asserted.
- The binary manifest's full receiver/exported/permission declaration has not been decoded in this stage.
- No DEX patch, APK rebuild, signing, install, or vehicle test has been performed.
