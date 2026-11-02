# DiPlay 0.3.12 — Stage 22: AP configuration readback and readiness audit

Date: 2026-11-02
Branch: `diplay-0312-assistant-fusion-stage10`
Input: original `DiPlay-v0.3.12-legacy-release(1).apk` (four DEX files)
Scope: static DEX method/invoke inspection only. No APK patch, build, signing, device execution, or hardware claim.

## 1. Verified method and call graph findings

Target classes are in `classes3.dex`:

- `ManualHotspotManager`
- `ManualHotspotReadiness`
- `CarHotspotTethering`
- `ManualHotspotReadinessKt`

The method `ManualHotspotManager.readApConfiguration()` invokes `readSoftApConfiguration()` and `readLegacyApConfiguration()`. The two methods use different reflection paths:

### Legacy path

`readLegacyApConfiguration()` invokes:

- `Class.getMethod(...)` and `Method.invoke(...)` for the hidden `getWifiApConfiguration` method;
- `Class.getField(...)` and `Field.getInt(...)` for legacy configuration fields;
- `legacyHotspotBandToSoftApBand(Integer)` to normalize the legacy band;
- `wifiChannelToFrequencyMhz(channel, band)` to derive frequency;
- `mapWifiConfigurationSecurity(WifiConfiguration)` to map security from configuration bitsets.

String references in this method include `getWifiApConfiguration`, `apChannel`, and `apBand`.

### Modern path

`readSoftApConfiguration()` invokes:

- hidden/reflected `getSoftApConfiguration`;
- API-model bridge methods for `SoftApConfiguration`, SSID, channel map, band, and security;
- `SparseIntArray.size/keyAt/valueAt` to iterate the channel map;
- `wifiChannelToFrequencyMhz(...)`;
- `mapSoftApSecurity(int)`.

String references include `getSoftApConfiguration`, `getChannels(...)`, `getBand`, and `getChannel`.

The code therefore has both legacy and modern readback implementations. Whether the hidden calls are accessible on a particular head unit remains device/API dependent. The existence of a method in DEX does not prove it succeeds on Android 4.x or a vendor-modified framework.

## 2. Frequency and channel evidence

The class contains two additional frequency sources:

- `frequencyFromConnectionInfo()`: obtains `WifiManager.getConnectionInfo()`, checks the connection SSID against the manager's own SSID (after unquoting), then reads the frequency through an API bridge.
- `frequencyFromScanResult(LocalHotspotInterface)`: obtains `WifiManager.getScanResults()` and matches a result by interface hardware address/BSSID before using its frequency.

This is useful corroboration, but it is not universally reliable for a SoftAP. A head unit may not expose the AP BSSID in scan results; scan permissions and vendor behavior may also block access. A null frequency must remain UNKNOWN. Do not infer 2.4 GHz from a missing value.

The start path contains a diagnostic fallback: when the active AP channel cannot be read through Android public APIs, it reports iAP2 channel 0 (auto), rather than echoing the configured channel. This is the correct direction: configured channel is not proof of actual channel.

## 3. Interface readiness selection

The invoke graph for `ManualHotspotReadiness.await(timeout)` calls `ManualHotspotReadinessKt.selectHotspotInterface(snapshot, ...)`.

The selector checks:

1. snapshot consistency;
2. AP-enabled state;
3. membership in the snapshot's AP-interface set;
4. exclusion of interfaces listed as Wi-Fi upstreams;
5. interface address candidates (site-local IPv4 and eligible IPv6);
6. interface up-state and index/address validity.

It does not simply accept the first interface named `wlan0`. It uses the AP-interface and Wi-Fi-upstream classifications supplied by the snapshot. This is materially safer than hard-coding `wlan0`, but the snapshot producer and its vendor classification still need a separate audit. The selector cannot be stronger than the accuracy of those supplied sets.

The readiness wait also re-samples and checks that the selected interface/address remains stable before publication. A changing interface or address is rejected.

## 4. Security readback boundary

Two mapping methods exist:

- `mapWifiConfigurationSecurity(WifiConfiguration)` reads three BitSet positions from legacy configuration.
- `mapSoftApSecurity(int)` maps the modern platform security integer.

The method names and call sites are verified. This inspection did not decode the complete switch/constant mapping, so it does not establish a reliable OPEN/WPA2/WPA3/WPA3-transition mapping for every API level. In particular, do not infer WPA3 support from the existence of the mapper. A readback result that is null, unknown, or unsupported must remain UNKNOWN/UNSUPPORTED, not be silently downgraded to WPA2.

## 5. Readiness is not internet sharing

The manager's readiness path confirms an AP interface/address selection and validates the observed AP configuration. The static call graph does not establish that a client has associated, received DHCP, resolved DNS, obtained NAT/forwarding, or reached the internet.

Keep the states separate:

- `AP_STARTED`: framework/vendor AP state reports active.
- `AP_INTERFACE_READY`: a stable AP-classified interface and usable address are observed.
- `CONFIG_VERIFIED`: SSID and requested supported band/channel/security match readback.
- `CLIENT_CONNECTED`: a client association is independently observed.
- `INTERNET_SHARED`: DHCP/DNS/forwarding and an external connectivity probe succeed.

Never report `INTERNET_SHARED` from AP state or interface readiness alone.

## 6. Fusion implications

The safest fusion seam remains the activation step behind `CarHotspotTethering.enable()`, with the existing `ManualHotspotManager` retained as observer/validator. The current `enable()` method directly invokes `disableWifiBeforeHotspot()`, which calls `WifiManager.setWifiEnabled(false)`; this must not be inherited by the fused activation backend.

Recommended minimum backend READY gate:

- AP state positively observed (unknown is not OFF and not READY);
- AP interface positively classified as AP, up, stable, with a usable address;
- SSID readback matches the requested SSID;
- requested band/security/channel are either positively verified or explicitly reported unsupported;
- no unsupported field is silently ignored;
- no claim of internet sharing until client/DHCP/DNS/NAT/connectivity checks pass.

For the Honda target, AP-interface snapshot provenance, hidden API availability, security mapping constants, and actual driver/hostapd capability remain unverified. Do not assume Ecarx APIs exist.

## 7. Evidence classification

| Field / behavior | Classification | Reason |
|---|---|---|
| Legacy configuration getter path | VERIFIED in DEX | Reflective `getWifiApConfiguration` call path exists |
| Modern configuration getter path | VERIFIED in DEX | Reflective `getSoftApConfiguration` call path exists |
| Legacy AP band/channel fields | VERIFIED in DEX | `apBand` / `apChannel` field reads |
| Modern channel map/band/security access | VERIFIED in DEX | API bridge calls and channel map iteration |
| Actual runtime access to hidden APIs | BEST_EFFORT | Depends on OS/vendor restrictions |
| Actual channel/frequency | BEST_EFFORT | Android readback or MAC-matched scan result can be unavailable |
| Security enum mapping across API versions | UNAVAILABLE as a universal guarantee | Full switch constants not established in this audit |
| AP interface classification | BEST_EFFORT | Selector uses snapshot AP/upstream sets; snapshot producer not yet audited |
| Stable interface/address check | VERIFIED in DEX | Await path rechecks selection/address stability |
| DHCP/DNS/NAT/internet sharing | UNAVAILABLE from this readiness path | No end-to-end client internet proof in the audited gate |

## 8. Explicit non-claims

- No APK bytecode was modified.
- No merged APK was built or signed.
- No runtime test was performed on the Honda head unit.
- No actual AP capability, channel, WPA3 support, DHCP, NAT, or internet access is claimed.

Next technical stage: trace the producer of `HotspotNetworkSnapshot` and its `apInterfaces` / `wifiUpstreams` sets, then inspect the complete security mapping switch and exact API-level guards.
