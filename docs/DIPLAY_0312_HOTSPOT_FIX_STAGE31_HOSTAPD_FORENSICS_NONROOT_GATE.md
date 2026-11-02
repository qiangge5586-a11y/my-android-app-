# DiPlay 0.3.12 Hotspot Fix — Stage 31: Hostapd Failure Forensics and Non-Root Backend Gate

Date: 2026-11-03
Branch: `diplay-0312-hotspot-fix-stage30`
Related PR: #2

## Scope

This stage converts the existing vehicle report and prior DEX audits into a safe implementation gate. It does not claim to patch or rebuild the original DiPlay APK: the repository currently contains the independent API-17 compatibility checker, not the original DiPlay Gradle project or its matching signing key.

## Evidence carried forward

The MY16ADA target is Android 4.2.2 / API 17. The vehicle report repeatedly records this sequence:

1. DiPlay releases the system Wi-Fi stack with `svc wifi disable`.
2. It kills stale `wpa_supplicant` processes and takes `wlan0` down.
3. It launches `/system/bin/hostapd -B /data/misc/wifi/hostapd.conf` in the background.
4. The later check reports `pidAlive=false`, `psLine=none`, and a missing/empty `hapd.log`.

The report proves the process was not observed alive at the check. It does not establish why hostapd exited.

A prior investigation found the background-launch check is weak: `$!` can identify a shell/background launcher rather than the final daemon after daemonization. The missing log also prevents separating daemon exit from redirection, permissions, or observation races.

The v0.3.3 disassembly is only a control-flow reference. The v0.3.12 DEX contains matching command/path literals, but the precise v0.3.12 method-level sequence has not been fully reconstructed. Do not treat the older bytecode as proof of identical implementation.

## Stage 31 decision

**Do not repeat or extend the current hostapd launch sequence.** In particular, do not globally kill supplicant/hostapd/dnsmasq, do not force `wlan0` down, do not overwrite the OEM shared `/data/misc/wifi/hostapd.conf`, and do not cycle Bluetooth as part of a generic hotspot attempt.

The requested no-Root/no-Shizuku direction means the privileged hostapd backend is not an acceptable default solution. The next implementation must first determine whether a supported vendor or Android framework SoftAP API is callable by the ordinary DiPlay application on this exact firmware. API-17 does not provide `SoftApConfiguration` or `LocalOnlyHotspot`; hidden legacy `WifiManager` methods are only candidates, not proof of authorization or operation.

## Required backend gate

1. Keep all capability checks read-only.
2. Prefer a documented vendor API only if the MY16ADA firmware actually exposes it to this application.
3. Otherwise inspect the API-17 legacy framework path. Method presence alone is insufficient; authorization and vendor implementation must be established.
4. If neither non-root path is available, report the target as unsupported under the no-root constraint rather than silently invoking `su` or changing global Wi-Fi state.
5. Only after a permitted backend is proven should a separately authorized test verify, in order: AP interface/beacon, phone association, DHCP lease, DNS, and routing.

## Implementation requirements for any future hostapd backend

If a separately approved privileged backend is ever considered, it must be isolated and opt-in, with:
- unique per-session config, log, PID and lease paths;
- foreground launch with captured stdout/stderr and real exit status;
- process ownership validated by PID, process start time, executable/cmdline and session token;
- no global process-name kill;
- no shared OEM config overwrite without verified backup and restore;
- no automatic Wi-Fi, interface, Bluetooth, or supplicant teardown.

This is a future safety specification, not authorization to introduce a Root dependency.

## Stage 31 deliverable and limits

- [x] Reconciled the vehicle log with prior DEX findings.
- [x] Documented why `$!` plus a one-second process check is not reliable daemon proof.
- [x] Established a non-root framework/vendor capability gate.
- [x] Explicitly blocked destructive global cleanup as a diagnostic step.
- [ ] Original DiPlay v0.3.12 source-level patch.
- [ ] Build and sign a replacement DiPlay APK.
- [ ] Device validation of a supported non-root AP backend.

The remaining unchecked items require the original application project and/or firmware capabilities that are not present in the current checker repository.