# DiPlay Root Hotspot Hybrid v1

This branch combines the safer ownership/rollback work from the hardened APK patch with the Android 4.2 compatibility and failure-evidence approach from DiPlay Legacy Android.

## Scope

- The head unit is the access point (SoftAP); the phone is the client.
- Root is required on the head unit.
- Target profile: wlan0, 2.4 GHz (802.11g/n), WPA2-PSK/CCMP, 192.168.42.1/24, DHCP 192.168.42.10-192.168.42.50, 12-hour leases, maximum 8 associated stations.
- No Shizuku, phone hotspot, screen mirroring, or unrelated AADisplay work is in scope.

## Design: retain A's safety, import B's compatibility

### Process ownership (retain A)

Every process started by this backend must have a PID file and an owner record containing PID, /proc start time, and session token. Stop only when all three still match and the executable identity is hostapd or dnsmasq. Send TERM first, wait a bounded interval, re-check identity, then KILL only the still-owned process. Never use global killall/pkill or process-name-wide cleanup.

### Legacy compatibility (adopt B's approach)

Do not rely on grep, wc, tail, head, sed, or GNU-specific options in a shell pipeline on Android 4.2. Parse ps and logs in Kotlin/Python where available, or use POSIX shell/awk with graceful fallbacks. Probe multiple binary locations rather than assuming /system/bin. Treat a successful svc exit code as inconclusive: verify wlan0 actually appears. Only consider the framework radio helper as a fallback after svc fails to produce the interface, and only if the helper is present in the APK/runtime.

### Startup and readiness

1. Capture the framework Wi-Fi state before touching system state.
2. Verify root, interface/driver evidence, and both executables.
3. Back up the shared hostapd config before replacing it; record whether it existed and hash the exact DiPlay-written version.
4. Launch hostapd with captured output and a recorded PID; verify the PID is alive and still belongs to this session.
5. Assign 192.168.42.1/24 and verify the address from interface output.
6. Launch dnsmasq; verify its PID and process identity, not merely the shell command's exit status.
7. Publish hotspot-ready only after hostapd, dnsmasq, and interface checks pass. A real DHCP lease is the definitive end-to-end proof that a phone joined; process checks alone do not prove client connectivity.

### Rollback

Stop only owned processes. Restore the previous shared config only if its current hash still equals the hash written by this session. Never overwrite an external change. Restore the original Wi-Fi radio state, and avoid taking wlan0 down unless this session demonstrably brought it up and owns the AP lifecycle. Cleanup must be idempotent.

## Implementation sequence

- Phase 1: branch isolation, compatibility-safe read-only diagnostics, and static checks.
- Phase 2: merge binary discovery and richer foreground failure evidence into the APK patch without weakening process ownership.
- Phase 3: strengthen dnsmasq PID/readiness checks and rollback ordering.
- Phase 4: build/sign/inspect the APK and attach a reproducible artifact.
- Phase 5: on-device validation when the user is back at the car.

## Acceptance criteria

1. Rooted head unit starts its own SoftAP.
2. Phone sees and joins the head-unit SSID.
3. Phone receives a 192.168.42.x lease with gateway/DNS 192.168.42.1.
4. Closing the backend stops only its own processes and restores the previous Wi-Fi/config state.
5. Logs clearly distinguish binary missing, driver/AP unsupported, hostapd config/driver failure, dnsmasq failure, and DHCP/client failure.

No real-car validation is claimed until the acceptance tests are performed on the vehicle.


## Implemented in the hybrid v1 test build

- Retained A's PID + `/proc/<pid>/stat` start-time + session-token ownership checks and hash-guarded shared-config restoration.
- Kept B's existing binary-probe/parse flow and expanded the probe directories to `/system/bin`, `/system/xbin`, `/vendor/bin`, `/vendor/bin/hw`, `/system_ext/bin`, `/odm/bin`, and `/bin`.
- Hostapd is launched in foreground mode under the background shell job, with its PID and owner identity recorded. Startup failure evidence is captured in the dedicated DiPlay log; log-tail output uses POSIX awk rather than `tail`.
- dnsmasq startup now has an owner record written only after its PID, process start time, executable identity, and session token are checked. Startup aborts if the root-shell command reports that validation failed.
- `validateReady()` now checks the owned hostapd process, the owned dnsmasq process, and the wlan0 address before allowing publication.
- Framework Wi-Fi restoration now mirrors the captured `settings get global wifi_on` state: teardown enables the framework radio when it was originally ON and explicitly disables it when it was originally OFF. If the query returns anything other than exactly `0` or `1`, startup aborts before changing system Wi-Fi state. The AP teardown still brings `wlan0` down; restoration of its exact pre-start administrative UP/DOWN state is not tracked and is not claimed as complete.
- The read-only diagnostics script is compatible with older toolboxes by using graceful command fallbacks and avoiding GNU-only tail/head/sed dependencies.

## CI result

The hybrid v1 test build at commit `762f3aa500e503a15fd4d05b2da52b21665f480f` passed GitHub Actions:
- APK decode and Smali patch
- Smali rebuild and APK archive integrity
- zipalign, signing, and v1/v2/v3 signature verification
- package identity check (`com.shihab.diplay`, versionName `0.3.3`)

This is a CI-verified test APK, not an on-car validated release. The APK retains the upstream package version metadata. The CI signing key is ephemeral per run, so this build may not update an installation signed by a different key; uninstalling an existing DiPlay install may be required and can erase its local data. No real-car SoftAP, phone association, DHCP lease, or Wi-Fi restoration test has been performed yet.
