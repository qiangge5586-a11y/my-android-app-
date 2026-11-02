# DiPlay 0.3.12 Hotspot Fusion — Stage 13 Bluetooth Reset and Startup Control-Flow Audit

## Scope and inputs

- Target: `DiPlay-v0.3.12-legacy-release(1).apk`
- APK SHA-256: `e8cc6ba9f87fed01380e4bae990f4df9232d84d299bbc0ee67c7ea08f99b5bab`
- Inspected DEX: `classes.dex` through `classes4.dex`
- Target methods: `HostapdHotspotManager.startHotspot`, `HostapdConfig.killStaleScript`, `HostapdConfig.removeGeneratedFilesScript`, and `BluetoothStackReset.reset`.
- Method code item for `startHotspot`: DEX code offset 3,573,092; 1,689 code units.

This is a static DEX instruction/call-reference audit. It is not a runtime trace, a complete semantic decompilation, a patch, or a device test. Branch targets and method calls were decoded from the DEX instruction stream; compiler-generated Kotlin control flow and exception edges are not fully reconstructed.

## New high-confidence finding: Bluetooth is actively reset after hotspot startup

The instruction sequence in `HostapdHotspotManager.startHotspot` shows:

1. Root availability and binary preflight are performed.
2. `HostapdConfig.killStaleScript` is invoked before the AP startup sequence.
3. The code deploys the hostapd configuration, launches hostapd, checks process/log evidence, configures `wlan0`, and starts dnsmasq.
4. It emits the success diagnostic `hostapd: hotspot ready iface=wlan0 addr=192.168.42.1`.
5. Immediately after the success path, it constructs callbacks and invokes `BluetoothStackReset.reset`.

The call at code-unit offset 790 is therefore on the successful hotspot path after the ready diagnostic, not merely part of a failure-only diagnostic branch.

### What BluetoothStackReset does

The `BluetoothStackReset.reset` method:

- Checks whether `BluetoothAdapter.getDefaultAdapter()` exists.
- Skips the reset if no adapter exists or the adapter is already off.
- If Bluetooth is enabled, first attempts the root command `svc bluetooth disable`.
- If that does not succeed, it attempts `BluetoothAdapter.disable()`.
- Waits for the adapter to turn off, then attempts `svc bluetooth enable` (with adapter API fallback in the surrounding control flow).
- Waits for Bluetooth to become enabled and emits diagnostic messages such as `bluetooth stack reset done`.

This means the current 0.3.12 root-hostapd success path intentionally cycles the Bluetooth adapter. On a vehicle head unit, that can interrupt hands-free calls, media audio, Bluetooth phone projection, and accessory connections. A successful hotspot startup must not be allowed to silently trigger this behavior in the DiPlay fusion target.

## Other confirmed startup hazards retained in the same method

- `killStaleScript` globally scans process listings for names `hostapd` and `dnsmasq`, then sends `kill -9` to matching PIDs. It does not establish that those processes belong to DiPlay.
- `removeGeneratedFilesScript` removes `/data/misc/wifi/hostapd.conf` as well as app-created temporary files. The system configuration is shared state, not an app-private artifact.
- The startup method stages `hostapd_full.conf` in app-private storage, copies it to `/data/local/tmp/hostapd_full.conf`, and then copies it to `/data/misc/wifi/hostapd.conf`.
- The method executes `ifconfig wlan0 down` before starting hostapd.
- It starts hostapd with `-B`, records the shell background PID, and then checks process count, PID/process evidence, and log output. A shell background PID is not by itself proof of the daemon's stable process identity.
- It uses a foreground `hostapd -dd -f` probe with a two-second liveness check. This is diagnostic evidence only; the probe's shell PID is not a robust daemon ownership mechanism.
- It configures `wlan0` as `192.168.42.1/24` and starts dnsmasq for DHCP. This does not, by itself, prove client internet forwarding, DNS reachability, or NAT.

## Fusion decision

**Do not invoke `BluetoothStackReset.reset` automatically after hotspot startup in the fusion build.** Preserve Bluetooth state. If a future vehicle-specific workaround proves that a Bluetooth cycle is necessary, it must be an explicit, opt-in, separately logged recovery action with a user-visible warning and a bounded restoration attempt—not part of the normal AP success path.

Recommended priority order:

1. Remove/guard the unconditional post-ready Bluetooth reset call in source-level integration.
2. Replace global process cleanup with exact process ownership validation: PID + `/proc/<pid>/stat` start time + command line/executable + per-session token.
3. Keep all PID, log, lease, and config files in a DiPlay-owned private runtime directory.
4. Never overwrite or delete `/data/misc/wifi/hostapd.conf` without a verified backup and hash-guarded restore; preferably use a private config path accepted by the target hostapd.
5. Do not bring `wlan0` down or disable unrelated system services as a default action.
6. Keep vendor SoftAP / Android tethering backends ahead of root hostapd.
7. Verify AP beacon, client association, DHCP, DNS, and NAT/forwarding as separate acceptance checks.

## Limitations and status

- The DEX was inspected statically; no source project for the exact 0.3.12 build was available in this work area.
- No DEX patch was applied.
- No APK was rebuilt, signed, installed, or tested on the vehicle.
- The donor hotspot assistant has not yet been merged into a new APK.
- The vehicle's exact Android build, Wi-Fi chipset/driver capabilities, vendor SoftAP API availability, and Bluetooth implementation remain to be confirmed on-device.

## Audit artifacts

The local analysis generated a focused instruction/call map for `startHotspot` and a method-level inspection of `BluetoothStackReset.reset`. The central new result is the post-ready Bluetooth adapter cycle described above.
