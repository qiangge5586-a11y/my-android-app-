# DiPlay 0.3.12 Hotspot Fusion — Stage 21
## Donor HotspotController vs DiPlay manual configuration: field adapter and backend compatibility

Date: 2026-11-02  
Baseline: DiPlay v0.3.12 legacy APK + donor `热点自启助手.apk.1`  
Status: static DEX method/field audit; no APK patch or device test

## 1. Executive result

The donor controller provides useful vendor/system SoftAP activation strategies, but it is **not a drop-in implementation** for DiPlay's manual hotspot configuration.

The key incompatibilities are:
- DiPlay exposes `AUTO`, `GHZ_2_4`, and `GHZ_5`; donor uses an integer band value, with vendor mapping `0 -> WIFIAP_FREQUENCY_2`, nonzero -> `WIFIAP_FREQUENCY_5`. No explicit donor AUTO behavior was found.
- DiPlay exposes `OPEN`, `WPA2`, `WPA3_TRANSITION`, and `WPA3`; donor legacy config hardcodes `WPA2_PSK`, while donor modern config passes a literal integer `2` to `SoftApConfiguration.Builder.setPassphrase`. The static donor code does not prove a correct mapping for all DiPlay security modes.
- DiPlay persists a manual channel. The donor modern builder calls `setSsid`, `setPassphrase`, and `setBand`, but no `setChannel` call was found. Donor legacy config sets `apBand`, but no explicit channel assignment was found.
- Donor controller's own `hotspot` SharedPreferences are separate from DiPlay's `xcertplay_airplay` settings. They must not be copied or used as the source of configuration.

Therefore, use the donor as a reference for backend invocation patterns only. Build a DiPlay-owned adapter with explicit field mapping and reject unsupported combinations rather than silently changing the user's requested hotspot.

## 2. DiPlay configuration contract

The values are loaded from `xcertplay_airplay` by `AirPlayPersistence`, then passed through `CarPlayHostActivity.loadConnectionSettings()` and `createRuntimeConfig()` into `CarPlayRuntimeConfig`.

| DiPlay field | Persisted key / type | Values or meaning |
|---|---|---|
| `manualHotspotSsid` | `manual_hotspot_ssid`, String | Requested SSID |
| `manualHotspotPassphrase` | `manual_hotspot_passphrase`, String | Requested passphrase; must not be logged |
| `manualHotspotBand` | `manual_hotspot_band`, enum name | `AUTO`, `GHZ_2_4`, `GHZ_5` |
| `manualHotspotChannel` | `manual_hotspot_channel`, Int | Requested channel; persistence clamps it to a bounded range |
| `manualHotspotSecurity` | `manual_hotspot_security`, enum name | `OPEN`, `WPA2`, `WPA3_TRANSITION`, `WPA3` |

DiPlay's MANUAL observer validates the observed AP against the requested SSID, channel (when both sides expose it), band, and security. It separately verifies that a requested 5 GHz or 2.4 GHz AP is actually observed on that band. This observer is an important acceptance gate and must be retained.

## 3. Donor configuration provenance

The donor `HotspotController.start()` reads:
- `Prefs.get(context)`, which opens SharedPreferences named `hotspot`;
- `custom` Boolean;
- `ssid` String, default `AutoHotspot`;
- `password` String, default empty;
- `band` Int;
- `allowWifiOff` Boolean.

When custom mode is enabled, donor calls `Rules.validate(password)`. The validation checks UTF-8 byte length (8–63) and rejects characters outside printable ASCII; the controller then passes the same SSID/password/band values into the backend methods.

The donor controller instance itself stores only:
- application `Context`;
- `WifiManager`;
- cancellation `AtomicBoolean`;
- vendor API object and a vendor-probed flag.

It does not own a richer typed configuration model. The donor's persisted preferences are not a suitable substitute for DiPlay's existing runtime config.

## 4. Backend invocation comparison

### 4.1 Vendor API

Donor behavior:
1. reflectively loads `com.ecarx.xui.adaptapi.wifiap.Wifi6Ap`;
2. reflectively invokes factory method `create`;
3. invokes `setWifi6ApConfiguration`;
4. invokes `setWifi6ApEnabled(true)`;
5. polls the framework hotspot state before reporting success.

The vendor band helper reflectively reads `com.ecarx.xui.adaptapi.wifiap.IWifiAPHost` constants:
- `WIFIAP_FREQUENCY_2` when donor band integer is zero;
- `WIFIAP_FREQUENCY_5` when donor band integer is nonzero.

This proves the donor's own integer convention for that helper. It does **not** prove the target Honda head unit has Ecarx classes or that the vendor API's full configuration argument order/semantics match DiPlay. Runtime class, factory, method, permission, and invocation checks remain mandatory.

### 4.2 Android legacy system API

Donor builds `WifiConfiguration` and:
- enables `WPA2_PSK` through reflective lookup;
- sets the relevant `BitSet` security flags;
- writes `apBand` from the donor band integer;
- calls `setWifiApConfiguration`;
- tries `startSoftAp`;
- falls back to legacy `setWifiApEnabled` when needed;
- polls hotspot state.

No explicit channel assignment was found in `legacyConfig()`.

The donor's legacy configuration path is therefore not a full adapter for DiPlay's manual channel or all four security modes.

### 4.3 Android modern system API

Donor `modernStart()` reflectively:
- loads `android.net.wifi.SoftApConfiguration` and its Builder;
- calls `setSsid`;
- calls `setPassphrase` with a literal security integer `2`;
- calls `setBand` with the donor integer;
- calls `build`;
- calls `WifiManager.setSoftApConfiguration`;
- calls `startTetheredHotspot`;
- polls state.

No `setChannel` call was found. The hard-coded security integer and raw band integer must not be assumed to map correctly to every DiPlay enum or Android API level. The fused implementation should use symbolic API constants when available, explicitly validate supported security types, and keep a version-specific adapter.

### 4.4 Donor state and wait semantics

Donor `state()` tries reflective `getWifiApState()`, then `isWifiApEnabled()`. Invocation failure or inaccessible methods can yield an unknown/null observation; preserve this as `UNKNOWN`, not `OFF`.

Donor `await()` polls the controller state with a bounded elapsed-realtime deadline. This verifies framework AP state only. It does not prove client association, DHCP, DNS, NAT, or internet connectivity.

## 5. Required DiPlay adapter mapping

The following is the safe adapter contract, not a claim that the donor currently implements it:

| DiPlay value | Vendor backend | Android backend | Fusion rule |
|---|---|---|---|
| SSID | Pass the DiPlay SSID only after vendor signature/argument contract is verified | `setSsid` / `WifiConfiguration.SSID` | Validate UTF-8 byte length 1–32; no donor default SSID |
| Passphrase | Pass only for a secured mode, through the verified vendor configuration schema | Passphrase only with a matching symbolic security type | Never log or store in donor Prefs |
| `AUTO` band | No proven vendor AUTO constant mapping | Omit explicit band or use a verified platform AUTO/default constant | Do not translate AUTO to 2.4 GHz by assumption |
| `GHZ_2_4` | Resolve vendor constant `WIFIAP_FREQUENCY_2` at runtime | Use the API-level symbolic 2.4 GHz constant | Verify observed band after start |
| `GHZ_5` | Resolve vendor constant `WIFIAP_FREQUENCY_5` at runtime | Use the API-level symbolic 5 GHz constant | Verify observed band after start |
| Channel = auto | Leave channel unset where backend supports automatic selection | Leave channel unset | Report observed channel separately |
| Fixed channel | Vendor channel argument mapping not proven | Donor did not call `setChannel`; only use a backend with verified channel support | If unsupported, fail clearly; do not silently ignore |
| `OPEN` | Vendor security schema not proven | Donor legacy path hardcodes WPA2; modern donor path has a hard-coded security integer | Do not reuse donor security setup; require explicit backend support |
| `WPA2` | Vendor security schema not proven | Legacy donor path shows WPA2_PSK; modern path still requires symbolic constant correction | Map only after API-specific tests |
| `WPA3_TRANSITION` | No verified donor mapping | No verified donor transition-mode mapping | Mark unsupported until explicitly implemented |
| `WPA3` | No verified donor mapping | Donor modern path's literal `2` is not an adequate cross-version contract | Use symbolic WPA3-SAE only when runtime API supports it |

For OPEN mode, do not pass an empty passphrase to a backend API that requires a secured passphrase. For WPA3 modes, do not silently downgrade to WPA2.

## 6. Recommended backend adapter interface

Keep DiPlay's typed config at the boundary:

```kotlin
data class ManualSoftApConfig(
    val ssid: String,
    val passphrase: CharArray?,
    val band: ManualHotspotBand,
    val channel: Int,
    val security: ManualHotspotSecurity
)

interface SoftApBackend {
    fun capability(config: ManualSoftApConfig): CapabilityResult
    fun start(config: ManualSoftApConfig, token: OperationToken): BackendStartResult
    fun state(): HotspotObservation
    fun stop(token: OperationToken): BackendStopResult
}
```

Backend-specific adapters must convert the typed enum values into runtime-verified constants. Do not put numeric band/security constants in shared business logic. The Root hostapd backend remains separate and last-resort; it must satisfy the process/config ownership rules from Stage 17 before it is eligible.

The MANUAL path becomes:
1. build config from DiPlay `CarPlayRuntimeConfig`;
2. validate SSID/passphrase and requested mode;
3. capability-check candidate backend for that exact config;
4. start via the selected backend;
5. keep `ManualHotspotManager` as the post-start observer and config verifier;
6. report READY only when observed SSID/security/band/channel satisfy the request;
7. report a distinct unsupported/partial/unknown result when any requested property cannot be verified.

## 7. Donor behaviors explicitly excluded from fusion

- Do not copy donor `Prefs` or its `hotspot` SharedPreferences.
- Do not adopt `allowWifiOff` as a default or implicit fallback.
- Do not blindly reuse the donor modern API's numeric security/band arguments.
- Do not assume vendor Ecarx classes exist on this target.
- Do not claim AP state means internet sharing.
- Do not copy donor Activities, services, boot receivers, retry receivers, or jobs.

## 8. Stage 21 implementation readiness

| Item | Result |
|---|---|
| DiPlay MANUAL field provenance | Confirmed |
| DiPlay band/security enum values | Confirmed |
| Donor preference source and defaults | Confirmed |
| Donor vendor/system method sequence | Confirmed |
| Donor vendor 2.4/5 integer mapping | Confirmed for donor helper |
| Donor modern API channel mapping | No channel setter found |
| Donor legacy API channel mapping | No channel assignment found |
| Donor security mapping across all DiPlay modes | Incomplete / unsafe to reuse |
| Target vehicle runtime vendor/API support | Unverified |
| Actual internet tethering | Unverified |

## 9. Next stage

Stage 22 should audit the DiPlay `ManualHotspotManager` readback implementations and the framework/vendor configuration reflection helpers in detail. The objective is to determine which requested properties can actually be read back on the target Android generation, and to define truthful partial-success behavior when channel/security information is unavailable.

No APK code has been patched, rebuilt, signed, or tested in this stage.
