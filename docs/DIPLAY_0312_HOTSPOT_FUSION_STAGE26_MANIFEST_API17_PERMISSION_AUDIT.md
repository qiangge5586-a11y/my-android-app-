# DiPlay 0.3.12 Hotspot Fusion — Stage 26 Manifest & API-17 Permission Audit

## Scope
Parsed the binary AndroidManifest.xml directly from the supplied DiPlay v0.3.12 APK. This is static package metadata inspection only. No device-side install or permission behavior was tested.

## 1. Manifest identity and SDK declarations

- Package: com.shihab.diplay
- Version name: 0.3.12
- minSdkVersion: 17
- targetSdkVersion: 37
- compileSdkVersion: 37
- android.hardware.wifi is declared required=false.
- USB host, Bluetooth, location, microphone, and fake-touch features are also declared optional.

The APK's declared minimum SDK is compatible with Android API 17 at the package metadata level. This does not prove that every execution path or bundled dependency works on a specific Android 4.2.2 vendor build. The vehicle OS/API remains unconfirmed.

## 2. Relevant declared permissions

The manifest declares ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, ACCESS_NETWORK_STATE, CHANGE_NETWORK_STATE, INTERNET, RECEIVE_BOOT_COMPLETED, WRITE_SETTINGS, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, and newer-version permissions including NEARBY_WIFI_DEVICES, Bluetooth runtime permissions, and foreground-service permissions.

No declaration was found for privileged tethering/system permissions such as TETHER_PRIVILEGED, NETWORK_SETTINGS, OVERRIDE_WIFI_CONFIG, or WRITE_SECURE_SETTINGS.

## 3. Permission implications for a rooted head unit

CHANGE_WIFI_STATE and ACCESS_WIFI_STATE are ordinary app-level Wi-Fi permissions. They do not by themselves authorize hidden WifiManager SoftAP methods or tethering-service operations that are protected by signature/privileged permissions on a given Android build.

Root access is a separate execution capability. It only helps if the app actually launches a correctly scoped privileged helper/shell operation and the vendor image allows the required commands, SELinux domains, driver controls, and service interactions. Merely having a rooted device does not elevate the APK's manifest permission grants or make its process a system app.

The fusion design should separate app-level configuration/UI permissions, root-helper execution capability, platform/vendor API availability, kernel/firmware SoftAP capability, and tethering/DHCP/DNS/NAT functionality. Each layer needs independent evidence.

## 4. Impact on the API-17 candidate

The minSdkVersion=17 declaration removes one packaging-level concern: the APK was intentionally built with API 17 as its minimum install level. It does not establish SoftAP support on the head unit.

For a confirmed API-17 target:
- modern SoftApConfiguration, TetheringManager, LocalOnlyHotspot, and NEARBY_WIFI_DEVICES behavior must not be assumed;
- use legacy WifiConfiguration/hidden WifiManager routes only after runtime reflection and vendor behavior checks;
- normal Wi-Fi permissions are insufficient evidence for privileged tethering operations;
- root helper behavior must be bounded, owned, reversible, and must not globally kill Wi-Fi daemons or overwrite shared system configuration.

## 5. Fusion gate update

A candidate backend is not READY merely because the device is rooted, the APK installs, CHANGE_WIFI_STATE is granted, a hidden method exists by reflection, or an interface name looks wireless.

Backend readiness requires explicit proof of AP start, stable AP interface, verified requested security/band/channel, client association, DHCP lease, DNS resolution, NAT/upstream routing, and client internet reachability. Keep these as separate states and report the first failing stage.

## 6. Verification status

- Binary manifest parsed: yes.
- Package/version/SDK declarations: confirmed from supplied APK.
- Optional Wi-Fi hardware feature: confirmed.
- Ordinary Wi-Fi/network permissions: confirmed.
- Privileged tethering permissions declared: none found in manifest.
- Root helper execution and actual permission grants on vehicle: not tested.
- Vehicle OS/API identity: not confirmed.
- SoftAP and internet sharing: not tested.
- APK patch/build/sign/install: not performed.
