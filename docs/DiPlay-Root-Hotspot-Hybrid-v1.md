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
