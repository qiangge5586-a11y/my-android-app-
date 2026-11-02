# DiPlay 0.3.12 Hotspot Fusion — Stage 19
## Foreground mode dispatcher, settings choices, and concrete network managers

Date: 2026-11-02  
Baseline: DiPlay v0.3.12 legacy APK  
Status: static DEX call-site and dispatcher audit; no APK patch or device test

## 1. Executive result

The foreground wireless startup path is a mode dispatcher, not a single SoftAP starter. `CarPlayController.startWirelessHotspot()` reads `CarPlayRuntimeConfig.wirelessHotspotMode`, performs mode-specific setup, constructs a concrete `WirelessHotspotManager`, and invokes its `start()`.

Confirmed routing:

| Mode | Foreground manager / path | Configuration source |
|---|---|---|
| `HOSTAPD` | `HostapdHotspotManager` | `hostapdSsid`, `hostapdPassphrase`, `hostapdChannel`, `hostapdAppleVendorIe` |
| `MANUAL` | `ManualHotspotManager` | `manualHotspotSsid`, `manualHotspotPassphrase`, `manualHotspotBand`, `manualHotspotChannel`, `manualHotspotSecurity` |
| `EXISTING_WIFI` | `ExistingWifiManager` | `existingWifiSsid`, `existingWifiPassphrase` |
| `LOCAL_ONLY_HOTSPOT` | `LocalOnlyHotspotManager` | Android local-only hotspot API path |
| `WIFI_P2P` | `WifiP2pGroupManager`; older SDK path delegates to `startLegacyWifiDirectWithFallback()` | `wifiP2pPreferredChannel` |

This confirms that the controller fusion must preserve the manager boundary and must not route every selected mode into SoftAP.

## 2. Foreground dispatcher evidence

Static call-site analysis identifies:
- `CarPlayController.runWireless()` as part of the wireless startup orchestration;
- `CarPlayController.startWirelessHotspot()` as the concrete mode-dispatch method;
- a read of `CarPlayRuntimeConfig.getWirelessHotspotMode()` before backend selection;
- mode-specific manager constructors followed by the common `WirelessHotspotManager.start()` interface;
- manager `close()` on failed/cancelled startup.

The dispatcher also has a legacy Wi-Fi Direct compatibility branch: when the selected mode is `WIFI_P2P` and the runtime SDK is below 29, it delegates to `startLegacyWifiDirectWithFallback()` rather than using the normal manager path.

## 3. Exact mode-to-manager branches

The dispatcher obtains the enum ordinal through a generated Kotlin enum-switch mapping. The branch bodies contain these constructors and getters:

### 3.1 HOSTAPD

Constructs `HostapdHotspotManager` with:
- application context;
- `CarPlayRuntimeConfig.hostapdSsid`;
- `hostapdPassphrase`;
- `hostapdChannel`;
- `hostapdAppleVendorIe`;
- diagnostic/cancellation callbacks.

If the configured hostapd SSID is absent, it throws `WirelessStartupException("Hostapd hotspot SSID is not configured")`.

This path does not use the donor helper's `Prefs`. It uses DiPlay's own runtime config. The existing HostapdHotspotManager still has the previously audited shared-config, wlan0, global-process-cleanup, and Bluetooth-reset hazards; do not treat this as a safe backend simply because the dispatcher selects it.

### 3.2 MANUAL

Constructs `ManualHotspotManager` with:
- application context;
- `manualHotspotSsid`;
- `manualHotspotPassphrase`;
- `manualHotspotBand`;
- `manualHotspotChannel`;
- `manualHotspotSecurity`;
- callbacks.

A missing SSID raises `WirelessStartupException("Manual hotspot SSID is not configured")`.

Before the mode-specific manager is started, `startWirelessHotspot()` also evaluates `CarHotspotSettings.shouldEnable(...)`. If that predicate is true, it invokes `CarHotspotTethering.enable(...)`. Stage 18 established that this opt-in predicate is MANUAL-only. This means the current MANUAL foreground path can run the separate car-hotspot enable step before the ManualHotspotManager path. The current tethering method calls `disableWifiBeforeHotspot()`; this remains a fusion blocker and must not be copied unchanged into the new controller.

### 3.3 EXISTING_WIFI

Constructs `ExistingWifiManager` using:
- `existingWifiSsid`;
- `existingWifiPassphrase`;
- connectivity/network callbacks.

The manager has an SDK split:
- modern path: `ExistingWifiManager.startModern()`, using ConnectivityManager network discovery/callback behavior;
- legacy path: `LegacyExistingWifi.start()`.

This is a client-network attachment path, not an AP creation path. It must never be replaced with hostapd or SoftAP startup.

### 3.4 LOCAL_ONLY_HOTSPOT

Constructs `LocalOnlyHotspotManager` on supported Android versions. The dispatcher explicitly rejects the path below Android 8 with:
`Local hotspot requires Android 8; choose Wi-Fi Direct or Car hotspot`.

This is a local-only AP path. It does not promise internet sharing/NAT.

### 3.5 WIFI_P2P

Uses `WifiP2pGroupManager` on the regular dispatcher path, configured with `wifiP2pPreferredChannel`. For older SDKs, it uses `startLegacyWifiDirectWithFallback()`, which delegates to `LegacyWifiP2pGroupManager`.

This is a Wi-Fi Direct group path, not SoftAP internet tethering.

## 4. Settings UI choices and root option

The `DiPlayActivity.wirelessLinkControls()` method constructs the selectable mode list as:
1. `MANUAL`;
2. `WIFI_P2P`;
3. `EXISTING_WIFI`;
4. `HOSTAPD` only when `AirPlayPersistence.loadRootHotspotEnabled()` is true.

Notably, `LOCAL_ONLY_HOTSPOT` is not added to this selector's option list, even though the dispatcher still has a backend branch and the home/status label mapping contains its enum case. Treat it as a retained/internal/other-entry mode until all mode-setting call sites are traced; do not add it to the visible list during fusion without a product decision.

The HOSTAPD option is gated by the Root Hotspot preference, not by a verified runtime driver/API capability check. Therefore:
- UI visibility does not prove the head unit supports AP mode;
- `loadRootHotspotEnabled()` is not equivalent to `RootShell.isRootAvailable()`;
- actual backend preflight must still be performed at runtime.

## 5. Mode setup UX facts

The same settings method renders mode-specific help:
- MANUAL displays car-hotspot setup instructions and an “open car hotspot settings” action; it also exposes saved SSID editing/use controls.
- EXISTING_WIFI displays instructions and an “open car Wi-Fi settings” action.
- HOSTAPD has its own mode description.
- WIFI_P2P has a Wi-Fi Direct description.

This reinforces that the five enum values are not interchangeable network transports.

## 6. Fusion impact

The Stage 17 generic controller should sit behind the existing `WirelessHotspotManager` mode contract or be called by each concrete manager, rather than replacing the entire dispatcher.

Recommended separation:

- Keep `WIFI_P2P` and its legacy fallback under their current Wi-Fi Direct managers.
- Keep `EXISTING_WIFI` under the existing client-network manager.
- Keep `LOCAL_ONLY_HOTSPOT` semantically local-only.
- For `MANUAL`, remove the duplicated/destructive pre-step only after identifying whether `CarHotspotTethering` is responsible for enabling the OEM AP or merely probing/repairing it; preserve the manual manager's own setup/validation.
- For `HOSTAPD`, route through the new safe Root backend only after ownership-safe implementation; do not call the current HostapdHotspotManager's unsafe startup/teardown unchanged.
- Do not change boot eligibility in this stage: Stage 18 confirmed the current boot auto-enable predicate remains MANUAL-only.

## 7. New risks / blockers identified

1. **Two-step MANUAL path:** the dispatcher may invoke `CarHotspotTethering.enable()` before constructing/starting `ManualHotspotManager`. This needs a deeper call/data-flow audit to determine whether it is duplicate AP activation or an OEM preparation stage.
2. **HOSTAPD preference is not capability detection:** UI can expose HOSTAPD without confirming hostapd binary/driver/permission support.
3. **LOCAL_ONLY mode is implemented but absent from the observed selector:** its reachability and persistence source remain unresolved.
4. **Root hostapd remains unsafe:** the current backend still includes shared system config mutation and radio/process side effects already recorded in earlier stages.

## 8. Next stage

Stage 20 should resolve the MANUAL two-step path and runtime configuration provenance:
- trace how `CarPlayRuntimeConfig` is built from DiPlay persistence;
- identify the exact caller/meaning of `CarHotspotTethering.enable()` in MANUAL mode;
- inspect `ManualHotspotManager` and `HostapdHotspotManager` common-interface contracts;
- compare donor `HotspotController` config construction with DiPlay's existing configuration fields.

No APK code has been patched, rebuilt, signed, or tested in this stage.
