# DiPlay 0.3.12 Hotspot Fusion — Stage 20
## MANUAL mode two-phase startup and persisted runtime configuration provenance

Date: 2026-11-02  
Baseline: DiPlay v0.3.12 legacy APK  
Status: static DEX data-flow and call-site audit; no APK patch or device test

## 1. Key resolution: MANUAL is a two-phase flow, not two AP starters

Stage 19 identified that `CarPlayController.startWirelessHotspot()` invokes `CarHotspotTethering.enable()` for the MANUAL opt-in path and later constructs `ManualHotspotManager`. Stage 20 traced `ManualHotspotManager.start()` and resolves the ambiguity:

- `CarHotspotTethering.enable()` is the AP activation/preparation step.
- `ManualHotspotManager.start()` is an observation, validation, and network-interface discovery step. It does **not** call a SoftAP start API.

Evidence in `ManualHotspotManager.start()`:
1. calls `readApConfiguration()`;
2. if an active AP configuration is available, compares its SSID to the configured MANUAL SSID and fails if they differ;
3. calls `validateApConfiguration()` to compare the observed configuration with the requested channel, band, and security;
4. calls `ManualHotspotReadiness.await()` to wait for a hotspot interface;
5. resolves the interface by name, obtains address/hardware address, and derives observed channel/frequency;
6. builds `WirelessHotspotInfo` from the observed AP/interface information.

The method contains no `WifiManager.startLocalOnlyHotspot`, `startTetheredHotspot`, legacy `setWifiApEnabled`, or hostapd process launch. Therefore the two calls are not evidence of duplicate AP startup. The second manager is the verifier/observer for the AP brought up by the first stage.

## 2. The actual MANUAL startup sequence

The audited path in `CarPlayController.startWirelessHotspot()` is:

1. Read selected mode and wireless transport from `CarPlayRuntimeConfig`.
2. Evaluate `CarHotspotSettings.shouldEnable(...)`. Stage 18 confirmed this predicate is true only for MANUAL, when the independent opt-in and wireless-enabled conditions are true.
3. If true, call `CarHotspotTethering.enable(context, isCancelled, log)`.
4. Query `CarHotspotStatus.isEnabled()` and classify the tethering result. The controller has explicit handling for `UNSUPPORTED`, `PERMISSION_REQUIRED`, `READY`, and `CANCELLED`.
5. Continue into the mode dispatcher.
6. For MANUAL, construct `ManualHotspotManager` using the requested SSID, passphrase, band, channel, and security.
7. Call the common `WirelessHotspotManager.start()`.
8. The MANUAL manager waits for and verifies the already active AP and returns its observed network information.

This explains why simply deleting the `CarHotspotTethering.enable()` call would break MANUAL startup: the subsequent manager expects an active AP to observe.

## 3. Confirmed persisted configuration mapping

`CarPlayHostActivity.loadConnectionSettings()` reads these DiPlay persistence values and stores them in the connection/runtime settings object:

| Runtime field | DiPlay persistence getter |
|---|---|
| `wirelessEnabled` | `loadWirelessEnabled()` |
| `wirelessHotspotMode` | `loadWirelessHotspotMode()` |
| `existingWifiSsid` | `loadExistingWifiSsid()` |
| `existingWifiPassphrase` | `loadExistingWifiPassphrase()` |
| `manualHotspotSsid` | `loadManualHotspotSsid()` |
| `manualHotspotPassphrase` | `loadManualHotspotPassphrase()` |
| `manualHotspotBand` | `loadManualHotspotBand()` |
| `manualHotspotChannel` | `loadManualHotspotChannel()` |
| `manualHotspotSecurity` | `loadManualHotspotSecurity()` |
| `hostapdSsid` | `loadHostapdSsid()` |
| `hostapdPassphrase` | `loadHostapdPassphrase()` |
| `hostapdChannel` | `loadHostapdChannel()` |
| `hostapdAppleVendorIe` | `loadHostapdAppleVendorIe()` |

The same loader also reads MFi, transport, and other unrelated CarPlay settings. Preserve those fields and the existing configuration object; do not replace it with donor preferences.

The settings are loaded by `CarPlayHostActivity.loadConnectionSettings()` and passed through `createRuntimeConfig()` into `CarPlayRuntimeConfig`. The wireless mode dispatcher then consumes the dedicated getters on that runtime config.

## 4. Manual AP observation and validation requirements

The existing MANUAL observer enforces more than “SSID exists”:
- SSID must match the active local AP configuration.
- Configured channel must match the observed AP channel when both are known.
- Configured band must match the observed band.
- Configured security must match the active AP security.
- 5 GHz configuration must actually be observed on 5 GHz.
- 2.4 GHz configuration must actually be observed on 2.4 GHz.
- A secured AP must have a passphrase.
- Interface and host address information must be discoverable before it can return the final `WirelessHotspotInfo`.

If public Android APIs cannot expose the active AP channel, the current manager logs a warning and reports iAP2 channel 0 (auto) rather than falsely claiming the configured channel was observed. Preserve this distinction between configured and observed values.

## 5. Integration point for the donor controller

The correct replacement seam is now much clearer:

- Keep `CarPlayHostActivity.loadConnectionSettings()` and `CarPlayRuntimeConfig` as the single source of DiPlay settings.
- In MANUAL mode, replace the AP activation behavior behind the current `CarHotspotTethering.enable()` seam with the fused controller's backend selection.
- Pass MANUAL SSID/passphrase/band/channel/security from DiPlay's runtime config to that controller; do not read donor `Prefs`.
- Keep `ManualHotspotManager.start()` as the post-start observer/validator, or move its validation into a shared verification component without losing any of its checks.
- Only return READY when the active AP configuration and observed interface agree with the requested settings.
- If controller reports AP enabled but the observer cannot verify it, return a distinct partial/unknown result rather than treating it as successful CarPlay readiness.

This allows donor vendor/system SoftAP start strategies to replace the current disruptive enable implementation while retaining DiPlay's richer AP observation and CarPlay network verification.

## 6. Safety consequence

The current `CarHotspotTethering.enable()` begins by calling `disableWifiBeforeHotspot()`. This is still the principal risk in the MANUAL path. A replacement must not inherit that unconditional pre-step.

Do not remove the call and leave a null/no-op callback: `CarHotspotTethering.enable()` also orchestrates state observation, cancellation, ADB fallback, and permission/error classification. Replace the entire AP activation seam with a controller that preserves those contracts, then retain the MANUAL observer stage.

## 7. What remains unverified

- Exact mapping from every DiPlay preference value to donor `HotspotController` configuration defaults/options.
- Whether the target head unit exposes a vendor SoftAP API or modern/legacy Android tethering APIs with sufficient privilege.
- Whether the active AP is internet-tethered or local-only on the target vehicle.
- Actual DHCP, DNS, NAT, client association, and internet forwarding.
- Reproducible DEX rebuild/signing path and original DiPlay signing-key availability.

## 8. Next stage

Stage 21 should compare the donor `HotspotController` start/config contract against the DiPlay MANUAL runtime config, including SSID/passphrase validation, band/channel conversion, vendor API invocation, Android modern/legacy paths, state polling, and error semantics. The result should be a field-by-field adapter specification, not a wholesale donor app merge.

No APK code has been patched, rebuilt, signed, or tested in this stage.
