# DiPlay 0.3.12 / hotspot helper — Stage 12 method-string audit

Date: 2026-11-02
Base branch: `diplay-0312-assistant-fusion-stage10`
Base commit: `b459f81427be2474fad0e29f82d827ce427421da`

## Purpose

Continue the v0.3.12 audit using the exact DEX files in the supplied APKs. This stage checks which relevant method bodies contain the hotspot command and API string references. It does **not** claim a complete control-flow reconstruction, patch, rebuild, signing, or vehicle test.

## New v0.3.12 finding: foreground hostapd probe already exists

The exact method `Lcom/shilapi/xcertplay/network/HostapdHotspotManager;->startHotspot` in `classes3.dex` contains both the ordinary daemon launch string and a foreground diagnostic probe string.

The probe command contains these operations:

- remove the fixed log `/data/local/tmp/hapd-fg.log`;
- run hostapd with `-dd -f /data/local/tmp/hapd-fg.log` against `/data/misc/wifi/hostapd.conf`;
- background the command and record `$!` as `FG`;
- sleep 2 seconds and test `kill -0 $FG`;
- if still alive, print `FG_ALIVE`, kill that PID and sleep 1 second;
- otherwise wait for it and print `FG_DEAD FG_EXIT=$?`;
- inspect the temporary directory and read the fixed foreground log.

This means a foreground diagnostic mechanism is already represented in v0.3.12. Do not add a duplicate probe blindly.

### Additional instruction-order reconstruction

A focused DEX instruction scan of `startHotspot` (code item offset 3,573,092; 1,689 code units) shows these relevant operations in method order. This is an instruction-order reconstruction, not a claim that every conditional branch executes on every attempt:

1. Check root availability and probe the hostapd/dnsmasq binary paths.
2. Invoke `HostapdConfig.killStaleScript()` before the later Wi-Fi/radio checks (around code-unit index 196).
3. Read framework Wi-Fi state. If it is off, issue `svc wifi enable` (around index 229), then wait for the interface.
4. If `wlan0` is still absent, call `WifiRadioHelper.run(...)` and wait again.
5. Render a hostapd config, write an app-private `hostapd_full.conf`, then stage/copy it to the shared `/data/misc/wifi/hostapd.conf`.
6. Issue `ifconfig wlan0 down`, then launch `hostapd -B` using the shared config.
7. Poll the global hostapd process count, parse the launch PID, and inspect PID/process/log evidence.
8. The method then contains the foreground `-dd -f` probe and appends its output as `fgProbe=` in the `hostapd: launch failed evidence:` diagnostic before constructing the `hostapd did not stay up` failure.

The key safety finding is stronger than a mere string inventory: the method invokes global stale-process cleanup before startup and deliberately takes `wlan0` down before the daemon launch. These are live startup-path operations in the method body and must be addressed in any safe patch. The exact conditions governing each branch still require a complete control-flow decode.

### Limits and defects to investigate

- The foreground probe is a two-second liveness test, not a general bounded-time supervisor.
- The alive branch kills the probe PID and does not record a post-termination exit status.
- The PID is the shell's background job PID; process identity after any daemonization must not be assumed.
- The log path is fixed and shared between attempts.
- The probe uses the shared system config path `/data/misc/wifi/hostapd.conf`.
- The normal startup path still uses global process-name cleanup and brings `wlan0` down before launch.
- The DEX instruction scan identifies method order and invokes, but does not yet fully decode all branch predicates or exception edges.

Do not add a duplicate diagnostic probe. The next source-level task is to replace the unsafe process/interface mutations, then improve ownership, timeout, log isolation, and result reporting around the existing probe.

## Confirmed v0.3.12 method string references

### `HostapdConfig`

- `killStaleScript` contains a `ps` scan for process argument basenames `hostapd` and `dnsmasq`, then `kill -9` on matching PIDs. It also removes the fixed dnsmasq lease and PID files.
- `removeGeneratedFilesScript` contains deletion of `/data/misc/wifi/hostapd.conf` and fixed files under `/data/local/tmp/`.
- `dnsmasqCommand` uses fixed `/data/local/tmp/dnsmasq.pid`, `dnsmasq.leases`, and `dns.log`; it binds `wlan0` and assigns DHCP range `192.168.42.10–192.168.42.50/24`, gateway/DNS `192.168.42.1`.
- `renderConf` builds an nl80211, 2.4-GHz-style WPA2 configuration for `wlan0`.

### `HostapdHotspotManager`

- `startHotspot` contains Wi-Fi enable recovery, interface probing, shared-config staging, `ifconfig wlan0 down`, the `hostapd -B` launch, process/log checks, and the foreground diagnostic probe above.
- `teardown` contains `ifconfig wlan0 down`, `svc wifi disable`, and `svc wifi enable` references.
- The method also contains Bluetooth-reset diagnostics. This does not prove Bluetooth reset is required for AP startup.

### `CarHotspotTethering`

- `startWifiApFallback` contains the legacy `WifiManager.setWifiApEnabled` path.
- `enable$lambda$1` contains hidden tethering service / `startTethering` references.
- The class has separate state observation and fallback-related methods; do not remove its fallback callback without reconstructing the `enableUntil` state machine.

## Donor helper method inventory reconfirmed

The supplied helper APK package is `app.autohotspot.helper`. Its `HotspotController` method bodies contain:

- `vendorStart`: reflective Ecarx `Wifi6Ap` / `setWifi6ApConfiguration` / `setWifi6ApEnabled` path.
- `vendorBand`: `IWifiAPHost` and vendor 2.4/5-GHz frequency constants.
- `modernStart`: `SoftApConfiguration.Builder`, `setSsid`, `setPassphrase`, `setBand`, `setSoftApConfiguration`, and `startTetheredHotspot`.
- `systemStart`: legacy `setWifiApConfiguration`, `startSoftAp`, and `setWifiApEnabled`.
- `state` / `stateText`: AP state observation.
- `stop`: vendor and system stop paths.

These references support selectively reusing the helper's backend strategy, but do not establish that the actual head unit implements the Ecarx APIs or grants the needed permissions. Runtime feature detection and post-start state verification remain mandatory.

Do not copy the helper's Activity, preferences, boot receiver, retry receiver, or JobService into DiPlay. Keep DiPlay's existing UI, opt-in preference, lifecycle, and boot handling.

## Safe next patch order

1. Reconstruct the exact v0.3.12 branch flow around the existing foreground probe and standard daemon launch.
2. Replace global process-name cleanup with exact session ownership checks; never kill another service's hostapd/dnsmasq.
3. Give each attempt a unique private config/log/PID/lease directory. Avoid the shared system config path where the binary permits it; otherwise use backup + hash-guarded restore.
4. Do not unconditionally disable Wi-Fi or bring `wlan0` down. Determine AP/STA concurrency and driver support before changing radio state.
5. Preserve `CarHotspotTethering` fallback semantics until its state machine is reconstructed.
6. Integrate the helper's vendor → modern system → legacy system backend strategy with runtime API checks, retaining root hostapd only as a last-resort owned-process backend.
7. Validate AP beacon, client association, DHCP, DNS, and internet forwarding separately.

## Build and verification status

- The DEX string-reference scan completed against the exact v0.3.12 APK and the exact helper APK.
- No smali/DEX instructions were modified.
- No APK was rebuilt or signed.
- No APK was uploaded to GitHub.
- No vehicle runtime test was performed.
- The current working environment still has no verified Apktool/Smali/Baksmali/JADX/d8/apksigner toolchain or Android SDK/Gradle cache.

This stage is an audit update, not a deliverable APK.
