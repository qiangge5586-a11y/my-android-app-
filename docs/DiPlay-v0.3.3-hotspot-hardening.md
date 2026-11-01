# DiPlay v0.3.3 Root Hotspot Hardening

> Status: a patched APK has been rebuilt and signature-verified in GitHub Actions. It is **not yet validated on the actual Civic head unit**; build success is not a claim of working on-device hotspot operation.

## Scope and compatibility invariants

- Baseline: the exact `DiPlay0.3.3.apk` stored at the repository root.
- Preserve the already-tested Root hostapd hotspot start path.
- 2.4 GHz only: retain `hw_mode=g`; do not add 5 GHz selection, fallback, or band switching.
- Preserve the current network contract unless device testing proves a change is necessary:
  - interface: `wlan0`
  - gateway: `192.168.42.1/24`
  - DHCP pool: `192.168.42.10-192.168.42.50`
  - WPA2-PSK / CCMP
  - maximum clients: 8
- A running AP is not proof that the phone has obtained a DHCP lease or that CarPToCarL has completed its session.

## Reverse-engineering findings from the v0.3.3 APK

The hotspot implementation is located in DEX classes:

- `com.shilapi.xcertplay.network.HostapdConfig`
- `com.shilapi.xcertplay.network.HostapdHotspotManager`

The current implementation has several unsafe cleanup behaviors that must be corrected before reliability tuning:

1. `killStaleScript()` scans the global process list for any process whose basename is `hostapd` or `dnsmasq`, then uses `kill -9`. This can terminate services not started by DiPlay.
2. The same cleanup removes global `/data/local/tmp/dnsmasq.pid` and `dnsmasq.leases`, which may belong to another session.
3. `removeGeneratedFilesScript()` unconditionally removes `/data/misc/wifi/hostapd.conf`, a shared system Wi-Fi configuration path.
4. `startHotspot()` copies a generated config over that shared path, then brings `wlan0` down before starting hostapd.
5. `startHotspot()` records the prior framework Wi-Fi state. It issues `svc wifi disable` only when Wi-Fi was previously on; `teardown()` issues `svc wifi enable` only when that recorded state was on. This temporary radio handoff is part of the current SoftAP strategy and must be tested on the Civic. It must not be replaced with unconditional disable/enable.
6. `teardown()` also brings `wlan0` down before restoring the previous framework Wi-Fi state. This ordering remains a device-validation point.
7. Current readiness checks do not distinguish AP readiness, DHCP readiness, phone lease, and the CarPToCarL application session.

## Required implementation changes

### P0 — Process and session ownership

- Generate a unique session ID for every start attempt.
- Store DiPlay-owned runtime files under a session-specific directory, not shared fixed PID/lease/log names.
- Record PID, `/proc/<pid>/stat` start time, and `/proc/<pid>/cmdline` for each launched process.
- Before signaling a process, verify PID + start time + executable/arguments + session-specific config path.
- Stop only processes positively identified as belonging to the active DiPlay session.
- Do not use global process-name scans, broad `killall`/`pkill`, or unconditional `kill -9`.

### P0 — Conflict-safe hostapd configuration

- Back up the pre-existing shared hostapd config before touching it, including metadata and a content hash.
- Prefer a session-specific hostapd config path if the device's hostapd build accepts it.
- If the shared path is unavoidable, serialize ownership and refuse to overwrite a config changed by another service.
- Restore only when the current file still matches the content written by this DiPlay session.
- Never blindly delete the shared config during cleanup.

### P0 — Bounded rollback

- Make start and teardown idempotent.
- Track which resources this session actually changed.
- On failure, roll back only those resources, in reverse order, with bounded waits.
- Do not disable system Wi-Fi unconditionally. Restore prior Wi-Fi/interface state only when DiPlay can prove it changed that state and still owns the change.

### P1 — Accurate connection diagnostics

Expose separate states and timestamps:

- `AP_READY`: hostapd is alive and the AP interface is configured.
- `DHCP_READY`: the session-owned DHCP service is alive and responding.
- `PHONE_LEASED`: a lease for the expected phone is present in this session's lease file.
- `CARPLAY_CONFIRMED`: CarPToCarL has completed its application-level connection/session handshake.

Do not report a complete connection based only on `AP_READY`. Include a bounded diagnostic snapshot with interface state, owned process identity, DHCP state, lease state, and relevant logs. Avoid collecting unrelated personal traffic or credentials.

### P2 — Stability and performance

- Keep the tested 2.4 GHz band and network parameters unchanged initially.
- Add bounded health checks and recovery only for session-owned services.
- Measure startup time, DHCP lease acquisition time, reconnect success, and session uptime before changing channel or power behavior.
- Do not introduce automatic 5 GHz fallback or band switching.

## Read-only head-unit capability check

Before changing the hotspot startup sequence, collect the actual vehicle driver's capabilities. The repository now includes `scripts/collect_civic_hotspot_diag.sh`. It is read-only: it reports the rooted Android build, wlan0 driver, hostapd/dnsmasq binaries and versions, advertised wireless interface modes, relevant kernel messages, and current Wi-Fi service state. It does not change Wi-Fi state, launch services, or print hotspot passwords.

On the rooted head unit, copy the script to the device and run:

```sh
su
sh /sdcard/Download/collect_civic_hotspot_diag.sh
```

If the script is stored elsewhere, adjust only the script path. Review the output before sharing it; it is designed to omit SSIDs, passwords, MAC addresses, and DHCP lease contents. The most important checks are whether hostapd exists, whether the driver advertises AP mode, and whether wlan0 is the correct AP interface. The revised launcher runs hostapd in the foreground as a child process and records its shell-launched PID, so it does not depend on hostapd's optional `-P` PID-file argument.

## Acceptance tests

1. Starting DiPlay does not terminate a pre-existing, unrelated hostapd or dnsmasq process.
2. Stopping DiPlay does not remove another service's PID, lease, or hostapd configuration files.
3. If the shared hostapd config changes during a session, teardown does not overwrite the newer file.
4. Repeated start/stop and partial-start failure are safe and idempotent.
5. A failed DHCP start is reported as `AP_READY` without falsely claiming `PHONE_LEASED`.
6. A phone lease is not reported as `CARPLAY_CONFIRMED` until the application-level handshake succeeds.
7. The generated hostapd config remains 2.4 GHz (`hw_mode=g`) and the tested IP/DHCP/WPA2 contract remains intact.
8. Test on the actual head unit and phone before distributing a rebuilt APK.

## Implemented bytecode changes and build status

The repository does not contain the developer's original Gradle/Kotlin/Java project. JADX output is reconstructed source, so the current implementation is a narrowly scoped Smali patch against the exact supplied v0.3.3 APK.

The GitHub Actions build has completed successfully once. It decoded the supplied APK with Apktool, patched `HostapdConfig` and `HostapdHotspotManager` Smali, rebuilt, zip-aligned, signed, and signature-verified the APK, and packaged the patched APK plus reverse-engineering output as a workflow artifact.

The current patch:
- namespaces DiPlay PID, log, lease, and staged-config paths under `/data/local/tmp/diplay-root-hotspot`;
- associates hostapd/dnsmasq with a per-run session ID, PID, and `/proc` start time;
- uses bounded TERM followed by identity-checked KILL for only recorded DiPlay-owned processes;
- checks startup readiness against the current session's recorded hostapd PID, session ID, `/proc` start time, and executable identity, rather than treating any system-wide `hostapd` process as success;
- disables the old global `wpa_supplicant` kill command;
- backs up `/data/misc/wifi/hostapd.conf` and restores it only if its content hash still matches the file written by this session;
- retains 2.4 GHz-only configuration, `wlan0`, gateway `192.168.42.1`, DHCP pool `192.168.42.10-192.168.42.50`, WPA2/CCMP, and eight-client limit;
- preserves conditional Wi-Fi radio handoff: disable framework Wi-Fi only if it was on before start, then re-enable only in that same case during teardown.

**Important remaining risks / not yet verified:**
1. The `wlan0` down/up and framework Wi-Fi restoration sequence must be tested on the actual 2019 Civic head unit. Android vendor Wi-Fi drivers differ; an APK build cannot prove driver support or successful hostapd startup.
2. The current readiness probe has been narrowed to DiPlay's own recorded hostapd process. It still needs device validation to confirm the target hostapd writes the configured PID file and the driver reaches AP mode.
3. The exact hostapd binary's support for `-P` PID-file mode must be confirmed on the head unit.
4. DHCP lease acquisition and phone reconnect have not yet been verified.
5. CarPToCarL's application-level session handshake is not proven by DiPlay's AP-ready state.
6. The rebuilt APK uses a test signing key, not the original developer key. It may not install as an in-place update over the original app; back up DiPlay settings/data before considering uninstall/reinstall.

The previous Actions run (run ID `41267603857`) completed successfully, but the generated artifact is only a build artifact and has not passed physical-device acceptance tests. The next release gate is on-device diagnostics, not another speculative feature change.


## 2026-11-01 log-driven diagnostic update

The uploaded DiPlay log reports Android 4.2.2 / API 17 and a device label of
`MITSUBISHI ELECTRIC MY16ADA`, while a firmware property contains
`Honda/Andromeda/vcm30t30`. These identifiers conflict, so the log must not be
treated as confirmed evidence from the previously described 2019 Civic.

The observed hostapd launch returned a PID from the background shell, but the
process was no longer alive on the subsequent check. The log did not capture
hostapd stderr/stdout, so it does not establish whether the cause is an
unsupported driver/AP mode, an unsupported hostapd option, or a config error.
The DHCP stage was not reached.

The read-only collector now additionally reports DiPlay runtime logs and PID
metadata, the shared/staged hostapd configuration with SSID and passphrase
fields redacted, and relevant kernel Wi-Fi errors. It prints the full hostapd
help output so the presence or absence of `-P` can be checked. It does not
start or stop Wi-Fi, hostapd, or dnsmasq.

Important remaining validation:
- The launcher no longer depends on hostapd `-P`; it runs hostapd in the foreground and tracks the exact child PID. The target binary and driver still require device validation.
- The runtime directory is a dedicated DiPlay-owned directory guarded by a
  lock and session token; it is not yet a unique nested directory per run.
- No AP/phone/DHCP success has been verified on the vehicle.
- The log's conflicting device identifiers must be resolved before attributing
  its results to the Civic.

