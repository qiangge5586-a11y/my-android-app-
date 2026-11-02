# DiPlay 0.3.12 Hotspot Fusion — Stage 33 Ecarx Vendor API Discovery

## Objective
Add a safe, API-17-compatible, read-only discovery step for the Ecarx vendor SoftAP API family referenced by Hotspot Assistant v2.0.

## Evidence baseline
The Library's donor DEX audit records these strings:
- `com.ecarx.xui.adaptapi.wifiap.IWifiAPHost`
- `com.ecarx.xui.adaptapi.wifiap.Wifi6Ap`
- `setWifi6ApEnabled`
- `setWifi6ApConfiguration`

This is donor-APK evidence only. It does not establish that these classes or services exist on the target Honda head unit. No exact vendor service acquisition contract, constructor, binder name, method parameter contract, or required signature permission has been proven for that target.

## Implementation
Added `EcarxVendorProbe.java`:
- Uses `Class.forName(name, false, classLoader)` to avoid static class initialization.
- Reports whether each candidate class is present, absent, or unknown.
- Inventories relevant method signatures and return/parameter types.
- Catches class-loader, linkage, reflection and access failures as UNKNOWN.
- API 17 compatible.
- Does not instantiate vendor classes, bind services, call methods, enable/disable SoftAP, alter Wi-Fi, invoke shell, or request Root/Shizuku.

Integrated its report into the existing compatibility-checker UI as an additional read-only section.

## Safety and interpretation
Class/method presence is only a discovery hint. It does not prove:
- a vendor service is registered or reachable;
- the caller holds the required signature/privileged permission;
- the method is callable on this firmware;
- the chipset/driver supports AP mode;
- the requested configuration can be applied;
- a client can associate or receive DHCP/DNS/internet service.

No vendor setter is called. No backend is enabled based on reflection presence alone.

## Build and integration boundary
This repository remains a standalone compatibility-checker project, not the original DiPlay 0.3.12 Gradle source tree. This stage does not patch DiPlay's original DEX or manifest and does not produce a fused DiPlay APK. The original DiPlay private signing key and reproducible source/build project remain unavailable.

## Next gate
If the target firmware reports the exact vendor class and method signatures, a later stage can compare them with authoritative OEM service/permission documentation. Only after service acquisition, permission and target compatibility are established should an invocation adapter be considered. If signatures or authorization remain unknown, retain UNKNOWN and do not invoke.
