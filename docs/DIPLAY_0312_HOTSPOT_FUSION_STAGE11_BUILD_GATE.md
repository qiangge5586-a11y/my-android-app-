# DiPlay 0.3.12 + Hotspot Assistant v2.0 — Stage 11 build gate

Date: 2026-11-02

## Inputs verified locally

- Host: DiPlay-v0.3.12-legacy-release(1).apk
  - Size: 18,128,817 bytes
  - SHA-256: e8cc6ba9f87fed01380e4bae990f4df9232d84d299bbc0ee67c7ea08f99b5bab
  - ZIP/APK structure valid; AndroidManifest.xml and four DEX files present.
- Donor: 热点自启助手.apk.1
  - Size: 33,251 bytes
  - SHA-256: 9fc3eea63d19a2928b9e2f540361eec0e4ae98a8ae20b730649e11806f822daf
  - ZIP/APK structure valid; AndroidManifest.xml and classes.dex present.
  - Package: app.autohotspot.helper.

## Integration target

DiPlay 0.3.12 remains the sole host application. The helper is a donor of hotspot-control logic only. Do not merge the helper's Activity, preferences UI, boot receiver, retry receiver, JobService, or manifest wholesale. Reuse the existing DiPlay lifecycle and user settings.

Donor controller method inventory:
- HotspotController.start / stop / state / stateText / check
- vendorStart / systemStart / modernStart / legacyConfig
- await / call / vendor / vendorBand

Potential vendor API symbols discovered in donor:
- IWifiAPHost
- Wifi6Ap
- setWifi6ApEnabled
- setWifi6ApConfiguration

Android API paths discovered:
- setWifiApEnabled
- startTetheredHotspot
- setSoftApConfiguration

All vendor APIs must be discovered and capability-checked at runtime; symbol presence in donor DEX does not prove the target head unit implements them.

## Safety constraints for the host

Do not transplant donor boot/retry scheduling blindly. Preserve DiPlay's existing BootReceiver and opt-in preference. Integrate controller behind DiPlay's existing hotspot control entry points.

Do not:
- globally kill hostapd, dnsmasq, or wpa_supplicant by process name;
- run svc wifi disable or bring wlan0 down unconditionally;
- overwrite or delete shared /data/misc/wifi/hostapd.conf without verified ownership and hash-guarded restoration;
- flush global firewall rules;
- alter MCU/CAN, reverse-camera/AVM, DSP/audio, thermal, or projection services.

Backend preference:
1. Runtime-verified vehicle vendor SoftAP API.
2. Android system tethering/modern SoftAP API where available.
3. Android legacy Wi-Fi AP API where supported.
4. Root hostapd fallback only after non-destructive capability preflight and process/config ownership registration.

A successful AP start is not equivalent to working internet sharing. Validate beaconing, client association, DHCP, DNS, and forwarding/NAT separately.

## Build and release gate

The current execution environment has Java but no verified Apktool, Baksmali/Smali, JADX, Android build-tools d8, or apksigner installation. No method-level DEX patch, rebuild, signing, or runtime test has been performed in this stage.

The connected GitHub interface supports text/blob operations but exposes no binary upload or release-asset upload operation. The two APKs have NOT been uploaded to this public repository. User authorization to publish was given, but the transport limitation remains.

Do not mark this project as fused or publish a fabricated APK. Resume only when a real binary transfer route and a verified Android APK rebuild/signing toolchain are available.

## Evidence boundary

The method inventory and APK hashes are verified. Exact v0.3.12 method control flow still requires disassembly; v0.3.3 decompiled control flow must not be represented as bytecode-equivalent to v0.3.12.
