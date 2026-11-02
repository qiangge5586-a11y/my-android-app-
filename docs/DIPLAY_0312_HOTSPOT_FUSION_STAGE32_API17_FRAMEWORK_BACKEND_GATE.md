# DiPlay 0.3.12 Hotspot Fusion — Stage 32: API-17 Framework Backend Gate

Date: 2026-11-03

## Scope

Add a buildable, fail-closed API-17 framework SoftAP decision helper to the independent checker source tree. This is not a patch to the original DiPlay APK and it does not start or stop a hotspot.

## Prior-art review

The Library already contains:
- the DiPlay 0.3.12 DEX/API audit and the Hotspot Assistant v2.0 controller inventory;
- the finding that the donor controller exposes Ecarx reflection, modern tethering, and legacy WifiManager paths;
- the Stage 27 audit identifying the foreground and boot activation seams;
- the Stage 28 audit establishing that this GitHub repository builds only the standalone checker, not DiPlay 0.3.12.

No original DiPlay Gradle source project or matching private signing key is available in the current repository.

## Source added

`app/src/main/java/com/diplay/hotspotcheck/SoftApBackendDecision.java`

The helper consumes a read-only capability snapshot and returns one of:
- `CANDIDATE_REQUIRES_DEVICE_VALIDATION`
- `BLOCKED_MISSING_PERMISSION`
- `BLOCKED_API_UNAVAILABLE`
- `UNKNOWN`

It requires both legacy AP method signatures and ordinary Wi-Fi permissions before identifying a candidate. It never invokes `setWifiApEnabled`, `setWifiApConfiguration`, hidden framework methods, hostapd, shell commands, or interface operations.

## Backend ordering decision

For a future DiPlay-owned integration, the source-level order remains:
1. Ecarx vendor API only when the target firmware exposes the expected class/service and its invocation is authorized.
2. API-appropriate Android framework tethering/legacy WifiManager path only when the API exists and authorization is proven.
3. No implicit fallback to Root, Shizuku, hostapd, Wi-Fi teardown, or global process cleanup.

API 17 has no modern `SoftApConfiguration` or `LocalOnlyHotspot`. A visible hidden method is not evidence that the firmware allows an ordinary application to call it.

## Safety and validation

- [x] Added Java source to the existing checker Gradle module.
- [x] Fail-closed outcomes for missing service, permission, or method signatures.
- [x] No radio state changes and no privileged execution.
- [ ] CI compile/build verification.
- [ ] Real target firmware vendor/framework authorization verification.
- [ ] Integration into DiPlay 0.3.12 source project (blocked: source project absent).
- [ ] Replacement DiPlay APK and signing (blocked: matching private key absent).

## Important limitation

The checker Gradle project is not the DiPlay application. This Stage 32 source is an implementation gate/reference helper, not a working hotspot controller and not a replacement APK. Do not interpret a candidate result as proof of hotspot operation.
