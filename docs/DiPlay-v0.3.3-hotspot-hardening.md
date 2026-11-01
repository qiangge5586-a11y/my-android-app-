# DiPlay v0.3.3 Root Hotspot Hardening

> Status: reverse-engineering baseline and implementation specification. This document does **not** claim that the APK has been patched or rebuilt.

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
5. `teardown()` includes `svc wifi disable`, which can disrupt unrelated system Wi-Fi state.
6. Current readiness checks do not distinguish AP readiness, DHCP readiness, phone lease, and the CarPToCarL application session.

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

## Acceptance tests

1. Starting DiPlay does not terminate a pre-existing, unrelated hostapd or dnsmasq process.
2. Stopping DiPlay does not remove another service's PID, lease, or hostapd configuration files.
3. If the shared hostapd config changes during a session, teardown does not overwrite the newer file.
4. Repeated start/stop and partial-start failure are safe and idempotent.
5. A failed DHCP start is reported as `AP_READY` without falsely claiming `PHONE_LEASED`.
6. A phone lease is not reported as `CARPLAY_CONFIRMED` until the application-level handshake succeeds.
7. The generated hostapd config remains 2.4 GHz (`hw_mode=g`) and the tested IP/DHCP/WPA2 contract remains intact.
8. Test on the actual head unit and phone before distributing a rebuilt APK.

## Build and source status

The repository currently contains the original APK and a GitHub Actions workflow that decompiles it for analysis. It does not contain the developer's original Gradle/Kotlin/Java project. JADX output is reconstructed source and may be incomplete; it is not equivalent to the original project.

Therefore, this branch is the tracked hardening specification and analysis baseline. No APK bytecode has been changed, no APK has been rebuilt, and no installable replacement is produced by this commit. The next implementation step is to use the generated reverse-engineering artifact to identify the exact smali methods and apply/test narrowly scoped bytecode patches, or obtain the original v0.3.3 source project for a maintainable source-level fix.
