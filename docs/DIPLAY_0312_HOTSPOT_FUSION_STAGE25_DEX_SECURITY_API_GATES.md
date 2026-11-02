# DiPlay 0.3.12 Hotspot Fusion — Stage 25 DEX Security Mapping & API Gate Findings

## Scope and status
Read-only inspection of the four DEX files extracted from the supplied DiPlay v0.3.12 APK. This stage decoded the two ManualHotspotManager security mappers, the Wi-Fi key-management bit checks, realSdkInt(), the legacy tether-interface regex matcher, and the wireless interface name heuristic. No APK bytes were modified; no build, signing, install, or device test was performed.

## 1. Exact ManualHotspotManager.mapSoftApSecurity(int) mapping

The method is a linear equality chain, not a packed/sparse switch. The argument is compared against integer values 0 through 3:

| Input | DiPlay result |
|---:|---|
| 0 | Iap2WirelessSecurity.NONE |
| 1 | Iap2WirelessSecurity.WPA_WPA2 |
| 2 | Iap2WirelessSecurity.WPA3_ONLY |
| 3 | Iap2WirelessSecurity.WPA3_TRANSITION |
| other | Iap2WirelessSecurity.WPA_WPA2 (fallback) |

This is the app's readback interpretation for SoftApConfiguration.getSecurityType(); it is not a general proof that all Android versions support all four modes. The fallback to WPA_WPA2 for unknown values is potentially lossy. Fusion code should not reuse this fallback for validation: unknown/unrecognized platform values must remain UNKNOWN/unsupported and must not be reported as WPA2.

## 2. Exact legacy WifiConfiguration.allowedKeyManagement reads

ManualHotspotManager.mapWifiConfigurationSecurity() reads BitSet indexes 0, 4, and 8.

Decoded branch behavior:
1. If bit 0 is set while bits 4 and 8 are both clear, return NONE.
2. Otherwise, if bits 4 and 8 are both set, return WPA3_TRANSITION.
3. Otherwise, if bit 0 is set, return WPA_WPA2.
4. Otherwise, if bit 8 is set, return WPA3_ONLY.
5. All remaining combinations return WPA_WPA2.

This is not a complete enumeration of every Android key-management bit. It does not inspect every possible flag, and several combinations collapse to WPA_WPA2. This method is a compatibility-oriented readback mapper, not a security policy validator. Fusion must validate the requested security against the exact backend and readback evidence before claiming success. Do not assume an unrecognized or mixed key-management bitset is safe WPA2.

The separate LocalOnlyHotspotManager mapper is not identical: it reads bits 0, 4, 8, and 9, has distinct unsupported-key-management/OWE errors, and maps to its own DiPlay enums. It must not be substituted for the Manual mapper.

## 3. realSdkInt() behavior

The helper reads Build.VERSION.SDK_INT and Build.VERSION.RELEASE. When the release string is non-null, it recognizes these prefixes and returns the historical API level:

| Release prefix | Returned API level |
|---|---:|
| 4.4 | 19 |
| 5.0 | 21 |
| 5.1 | 22 |
| 6.0 | 23 |

If no listed prefix matches, it returns Build.VERSION.SDK_INT. It is a narrow compatibility correction for a few old release strings, not a generic Android-version detector.

## 4. ManualHotspotInterfaces.sample() API gates

Decoded gates include:
- realSdkInt() >= 36: try the public tethered-interface source (PublicTethering.getInterfaces()), if the stored provider object is available; otherwise use the legacy tethered-interface reflection path.
- realSdkInt() >= 21: use newer ConnectivityManager network enumeration/capabilities/link-properties for Wi-Fi upstream discovery. Below API 21 it uses legacy Wi-Fi upstream discovery.
- realSdkInt() >= 23: use ConnectivityManager.getActiveNetwork() and associated link properties where available; older versions use getActiveNetworkInfo() fallback logic.

The class also has API-model bridge calls for newer platform classes. Their presence in DEX does not make those classes available on older Android releases. The >=36 gate is restrictive: public tethered-interface enumeration is not the fallback for older platforms; older devices rely on legacy hidden reflection.

SoftApConfiguration readback is not an API-17 facility. LocalOnlyHotspot is likewise unavailable on API 17. These paths must be excluded by runtime API/capability checks before class resolution or invocation on legacy devices.

## 5. Legacy tethered interface matching

legacyHotspotInterfaces(tethered, wifiRegexes):
- requires both lists;
- compiles each supplied Wi-Fi tetherable-interface regex into a Regex;
- iterates tethered interface names;
- matches each tethered name using Regex.matches() (whole-string match, not substring search);
- returns matched names as a set.

An empty result only means no interface was matched by the successfully observed lists; it is not proof of no AP when reflection failed or the platform did not expose those lists. The caller must preserve its observed/unobservable distinction.

## 6. Wireless interface name heuristic

wirelessInterfaceName(name) uses case-sensitive prefix checks for wlan, swlan, ap, and wifi, plus a contains("softap") check and a prefix check for p2p. It is only a candidate-name heuristic. It does not establish that the interface is currently in AP mode, belongs to tethering, has an address, or is distinct from the Wi-Fi upstream.

## 7. Fusion backend consequences

1. Do not copy the app's unknown-security fallback as an authoritative success mapping. Unknown platform security values must fail closed.
2. Do not silently map OPEN to WPA2, WPA3 to WPA2, or transition mode to WPA2.
3. On a confirmed API-17 target, use only APIs actually present on that firmware. Reflective getWifiApConfiguration() is not proof that a corresponding start method or driver capability exists.
4. Public TetheringManager interface enumeration is only attempted behind the API-36 gate. API-17 relies on legacy hidden ConnectivityManager methods and must report UNKNOWN when those methods cannot be observed.
5. Interface-name heuristics are not sufficient for interface selection. Keep AP membership, AP enabled state, upstream exclusion, interface-up/index, usable address, consistent snapshot, and stable resampling.
6. Keep DiPlay runtime configuration as the source of truth. Do not adopt donor defaults or donor preference storage.

## 8. Remaining blockers

- Target head-unit Android release/API level remains unconfirmed.
- Exact vehicle SoftAP capability is untested.
- No target-device evidence confirms driver AP mode, hostapd compatibility, channel/regulatory behavior, DHCP, DNS, NAT, or client internet reachability.
- Donor helper backend compatibility with the Honda head unit is unproven.
- No merged APK exists yet; no patch/build/sign/install has been performed.

## Verification summary

- Manual SoftApConfiguration security integer mapping: decoded.
- Manual WifiConfiguration bitset branch logic: decoded.
- realSdkInt() compatibility overrides: decoded.
- Interface sampling API gates: decoded at relevant branch sites.
- Legacy tether regex semantics: decoded (Regex.matches, whole-string match).
- Wireless interface name heuristic: decoded.
- Device capability and end-to-end internet sharing: not tested.
- APK modification/build/signing: not performed.
