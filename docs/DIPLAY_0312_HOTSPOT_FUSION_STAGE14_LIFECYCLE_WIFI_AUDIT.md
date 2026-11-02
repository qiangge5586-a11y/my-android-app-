# DiPlay 0.3.12 Hotspot Fusion — Stage 14 Lifecycle and Wi-Fi Disruption Audit

## Scope

Static DEX call-flow audit of the same DiPlay 0.3.12 APK recorded in Stage 13. This stage follows the entry point `CarHotspotTethering.enable`, its Wi-Fi preflight, and the root hostapd manager teardown path. It does not patch or execute the APK.

## Finding 1 — the shared hotspot entry point disables Wi-Fi before backend selection

The method `CarHotspotTethering.enable` begins by calling `disableWifiBeforeHotspot` at code-unit offset 15. Only after that call does it proceed to state observation and `enableUntil`.

The helper's instruction/string references show it:

- obtains the Wi-Fi system service;
- checks whether Wi-Fi is enabled;
- calls `WifiManager.setWifiEnabled(false)`;
- polls the Wi-Fi state with a bounded retry loop and emits diagnostics if Wi-Fi remains on.

The call occurs at the common `enable` entry point, before the later backend-selection/state-machine work. Consequently, the current implementation can switch off the existing Wi-Fi connection even if a later backend path would have used Android tethering or a vendor SoftAP API.

### Fusion decision

Do not make Wi-Fi-off a mandatory precondition at the common entry point. Move radio-conflict handling into the specific backend that has proven it requires exclusive radio ownership. Prefer vendor SoftAP or Android tethering paths that manage radio coexistence themselves. If a device-specific chipset truly requires station-mode shutdown, gate it on a positive capability/requirement check, record the prior state, and restore it safely on stop or failure.

## Finding 2 — hostapd teardown is not process-private

`HostapdHotspotManager.teardown` contains cleanup branches that call both:

- `HostapdConfig.killStaleScript`
- `HostapdConfig.removeGeneratedFilesScript`

The same global cleanup methods are used by startup and teardown. The script implementations previously inspected show that process cleanup scans for process names `hostapd` and `dnsmasq` and issues `kill -9` to matching PIDs, without validating DiPlay ownership. The generated-file cleanup removes the shared `/data/misc/wifi/hostapd.conf` path in addition to temporary files.

One teardown branch also contains `svc wifi disable` and `ifconfig wlan0 down`; another contains a `svc wifi enable` recovery command. These operations affect system-wide radio state and must not be used as generic app cleanup.

The manager's `close` method calls `teardown`, so lifecycle closure can reach this destructive cleanup path. The manager's `start` wrapper also has a teardown call in its exception-handling path; this is not evidence that every normal start unconditionally tears down first.

## Combined confirmed hazards in the 0.3.12 baseline

| Lifecycle point | Confirmed behavior | Integration risk |
|---|---|---|
| Common hotspot entry | Wi-Fi is disabled before backend selection | Existing station connection can be lost even when using another backend |
| Root AP startup | Global hostapd/dnsmasq name-based kill | Can terminate services not owned by DiPlay |
| Root AP startup | Shared hostapd config is overwritten | Can corrupt or replace system configuration |
| Root AP startup | `wlan0` is brought down | Can disrupt other network services |
| AP success path | Bluetooth adapter is cycled | Can interrupt hands-free/media/accessory links |
| Root AP teardown | Global kill + shared config deletion | Can terminate unrelated services and remove system files |
| Root AP teardown | Wi-Fi disable/enable commands | Can leave radio state changed after app lifecycle events |

## Required fusion architecture changes

1. Keep `CarHotspotTethering.enable` side-effect free until a backend is selected.
2. Implement a backend capability contract: `canStartWithoutDisablingSta`, `requiresExclusiveRadio`, `ownsInterface`, and `restorePlan`.
3. Prefer vendor SoftAP, then Android modern tethering, then legacy framework AP. Use root hostapd only as a last-resort backend.
4. Root hostapd must own a unique session directory, process identity, config, PID metadata, log, and DHCP lease file.
5. Teardown must stop only processes whose PID, process start time, executable/cmdline, and session token match DiPlay's recorded ownership.
6. Never delete or overwrite the system `/data/misc/wifi/hostapd.conf` without a verified backup and guarded restore; prefer a private config path.
7. Do not run `svc wifi disable`, `svc bluetooth disable`, `ifconfig wlan0 down`, or global process kills as generic lifecycle cleanup.
8. Restore only state that DiPlay itself changed, and only if the current state still matches the state DiPlay last set.
9. Validate AP beacon, client association, DHCP, DNS, and NAT/forwarding separately.

## Status

- New static call-flow findings recorded; no APK bytes modified.
- No source-level patch, DEX rebuild, signing, install, or vehicle runtime test has been performed.
- Exact target head-unit radio coexistence behavior remains unverified.

## Primary instruction evidence

- `CarHotspotTethering.enable`: common entry calls `disableWifiBeforeHotspot` at code-unit offset 15.
- `disableWifiBeforeHotspot`: includes `WifiManager.isWifiEnabled` and `WifiManager.setWifiEnabled(false)`.
- `HostapdHotspotManager.teardown`: calls `killStaleScript` and `removeGeneratedFilesScript` in cleanup branches; contains `svc wifi disable`, `ifconfig wlan0 down`, and `svc wifi enable` strings/calls.
- `HostapdHotspotManager.close`: invokes `teardown`.
- `HostapdHotspotManager.start`: teardown appears in the exception-handling path after the `startHotspot` invocation; this audit does not characterize it as unconditional normal-start cleanup.
