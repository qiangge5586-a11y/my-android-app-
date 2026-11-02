# DiPlay 0.3.12 + Hotspot Assistant Fusion — Stage 17
## Controller contract, mode routing, boot handoff, and safety invariants

Date: 2026-11-02  
Baseline: DiPlay v0.3.12 legacy APK + `热点自启助手.apk.1` donor  
Status: static-analysis design gate; **not an APK patch or device test**

## 1. Stage 17 outcome

Stage 17 turns the Stage 10–16 findings into an implementable controller boundary. It defines:
- one DiPlay-owned request/configuration model (no donor preferences or duplicate receiver);
- a serialized start/stop/reconcile state machine;
- backend selection and explicit fallback rules;
- boot-worker handoff that preserves DiPlay's existing opt-in gates;
- strict ownership and non-interference rules for the Root/hostapd fallback;
- independent success criteria for AP, client, DHCP/DNS, and internet forwarding.

This is an implementation contract. The current APK has **not** been modified.

## 2. Existing preference and mode facts

### 2.1 Confirmed preference locations

| Purpose | Preference file | Key | Evidence / behavior |
|---|---|---|---|
| Wireless feature enabled | `xcertplay_airplay` | `wireless_enabled` | `AirPlayPersistence.loadWirelessEnabled()`; default false |
| Selected hotspot mode | `xcertplay_airplay` | `wireless_hotspot_mode` | `AirPlayPersistence.loadWirelessHotspotMode()`; stored as enum name |
| Manual SSID | `xcertplay_airplay` | `manual_hotspot_ssid` | loaded only for manual configuration |
| Manual passphrase | `xcertplay_airplay` | `manual_hotspot_passphrase` | loaded only for manual configuration; never log |
| Independent hotspot boot opt-in | `diplay_car_hotspot` | `auto_enable` | `CarHotspotSettings.enabled()`; default false |
| Root hotspot setting | `xcertplay_airplay` | `loadRootHotspotEnabled()` | consulted in mode-loading normalization; exact mode policy needs full control-flow decode |
| App UI boot start | `xcertplay_airplay` | `auto_start_on_boot` | separate from hotspot boot opt-in |

The existing boot path first checks wireless enabled, mode, independent `auto_enable`, and manual config validity as applicable. The receiver then uses `goAsync()`, a worker thread, and `PendingResult.finish()`. Preserve this receiver and its preference namespace; replace only the worker's backend action after the controller is implemented.

### 2.2 Mode enum values observed

`WirelessHotspotMode` declares, in order:
1. `WIFI_P2P`
2. `LOCAL_ONLY_HOTSPOT`
3. `MANUAL`
4. `EXISTING_WIFI`
5. `HOSTAPD`

The enum names are confirmed. The exact `CarHotspotSettings.shouldEnable(context, mode)` branch predicate and every mode-to-backend UI mapping are **not yet fully decoded** from the compact DEX instruction listing. Do not infer that all five modes mean “start an internet-sharing SoftAP”.

### 2.3 Safe routing semantics to implement

| DiPlay mode | Controller interpretation | Must not do |
|---|---|---|
| `WIFI_P2P` | Leave the existing P2P/CarPlay transport path to its current owner | Do not start SoftAP as a substitute |
| `LOCAL_ONLY_HOTSPOT` | Request a local-only AP only if the current DiPlay product semantics require it | Do not claim cellular/WAN internet sharing |
| `MANUAL` | Use validated DiPlay SSID/passphrase as SoftAP config, then select a supported AP backend | Do not copy credentials into donor Prefs/logs |
| `EXISTING_WIFI` | Treat as “use an already available Wi-Fi network” unless source-level audit proves otherwise | Do not turn off the client Wi-Fi or start an AP automatically |
| `HOSTAPD` | Permit the isolated Root hostapd backend as a last resort after capability preflight | Do not kill global daemons, bring wlan0 down, or overwrite shared config without guarded backup |

This table is the proposed fusion contract, not a claim that the original UI already implements each row exactly. Before patching, Stage 18 must decode the UI selection and call sites to verify the mapping.

## 3. Controller API contract

Use a single application-owned facade, conceptually:

```kotlin
interface HotspotController {
    fun state(): HotspotSnapshot
    fun start(request: HotspotRequest, origin: StartOrigin): StartResult
    fun stop(origin: StopOrigin): StopResult
    fun reconcile(origin: ReconcileOrigin): ReconcileResult
}
```

The names are design names; no such class has been inserted into the APK.

### 3.1 Request model

```kotlin
data class HotspotRequest(
    val mode: WirelessHotspotMode,
    val ssid: String?,
    val passphrase: CharArray?,
    val bandPreference: BandPreference,
    val allowClientWifiInterruption: Boolean = false,
    val allowRootHostapdFallback: Boolean = false,
    val requireInternetSharing: Boolean
)
```

Rules:
- Values come from DiPlay's existing persistence and validated setup object.
- Donor `Prefs`, `Rules`, and `MainActivity` are not imported.
- Never serialize passphrases into diagnostics, crash reports, command-line arguments, or persistent operation journals.
- The default for both disruptive fallbacks is false.
- `requireInternetSharing` is false for local-only AP and true only where the selected mode explicitly promises internet sharing.

### 3.2 Snapshot and result model

```kotlin
enum class HotspotState {
    OFF, STARTING, AP_ENABLED, CLIENT_CONNECTED,
    DHCP_READY, INTERNET_READY, STOPPING, FAILED, UNKNOWN
}

data class HotspotSnapshot(
    val state: HotspotState,
    val backend: String?,
    val ssid: String?,
    val lastErrorCode: String?,
    val observedAtElapsedMs: Long
)
```

Keep `UNKNOWN` distinct from `OFF`. A missing or inaccessible framework API is not proof that the AP is disabled. Results should include a stable error code and backend stage, but no credentials.

## 4. Backend selection and fallback

The controller tries only backends that pass a runtime capability check:

1. **Vehicle vendor SoftAP API** — reflectively detect the exact vendor classes/methods; call only if class, factory, methods, and required permission path are all present. Ecarx `Wifi6Ap` is a donor clue, not a confirmed capability on this Honda head unit.
2. **Android modern/system tethering** — detect `SoftApConfiguration` and the required `WifiManager` methods; distinguish method presence from permission/privilege success.
3. **Android legacy AP API** — only when the runtime actually exposes the legacy API and the call can be made without disrupting the client Wi-Fi path.
4. **Root hostapd** — last resort, only when mode explicitly allows it and a preflight confirms binary, driver/interface, configuration support, and a safe isolated execution path.

A backend is not “supported” merely because reflection finds a method. Record capability as `PRESENT`, `ABSENT`, or `UNKNOWN`; record invocation as `ACCEPTED`, `REJECTED`, or `THREW`; and independently verify the resulting AP state.

### 4.1 Fallback stop conditions

Do not fall through to a more disruptive backend after:
- user cancellation;
- a permission/security denial that cannot be corrected by the next backend;
- a configuration validation error;
- a detected conflict with an existing Wi-Fi/P2P/CarPlay session;
- an ownership mismatch or uncertain state from a prior Root-hostapd run.

A backend may fall through only when it reports a classified “unsupported/unavailable” result and the next backend is permitted by the request. Never use “try every method” as a substitute for capability classification.

## 5. Serialized lifecycle state machine

All lifecycle operations run through one process-wide serialized executor / mutex. Each operation receives a monotonically increasing operation ID and cancellation token.

| Current observation | Requested action | Required behavior |
|---|---|---|
| AP already confirmed ON | start/reconcile | Return already-on; do not reset Bluetooth or restart daemons |
| OFF | start | Validate request, preflight backend, then start |
| UNKNOWN | start | Probe boundedly; do not assume OFF and do not blindly spawn another AP |
| STARTING | second start | Join/return current operation; do not create a second backend |
| STARTING | stop | Cancel where possible, then stop only resources owned by that operation |
| AP_ENABLED | start | Verify existing configuration/ownership; do not replace a foreign AP |
| AP_ENABLED | stop | Stop only if this controller owns the AP or framework explicitly grants safe stop semantics |
| STOPPING | start | Wait for completion or return BUSY; never overlap teardown and startup |
| FAILED | reconcile | Run read-only probes first; clean only positively identified owned resources |

Bound every probe and wait. Use elapsed realtime deadlines rather than wall-clock time. A timed-out start returns `UNKNOWN` unless an independent state query confirms OFF.

## 6. Boot receiver handoff

Preserve the existing DiPlay `BOOT_COMPLETED` receiver and its current gates:
1. `wireless_enabled == true`;
2. selected mode passes the existing `CarHotspotSetup.shouldStartOnLaunch()` checks;
3. `diplay_car_hotspot/auto_enable == true`;
4. manual mode credentials pass `ManualHotspotValidation`.

Then:
- retain `goAsync()` and the `boot-hotspot-heal` worker;
- worker calls `controller.reconcile(BOOT)`, not the current destructive `CarHotspotTethering.enable()` path;
- always call `PendingResult.finish()` in a `finally` block;
- if state is confirmed ON, return without touching it;
- if state is UNKNOWN, perform read-only probes and do not launch a second AP process;
- if start is appropriate, construct the request from DiPlay persistence and invoke the controller;
- do not start the UI Activity merely to start the hotspot;
- do not add a second BootReceiver, JobService, or donor RetryReceiver.

## 7. Root hostapd ownership and rollback contract

The existing DiPlay hostapd path has confirmed risky operations/call sites: global stale-process cleanup, `wlan0 down`, shared `/data/misc/wifi/hostapd.conf` replacement, and Bluetooth stack reset on a success path. These are excluded from the fused default path.

Root fallback must:
- use a private per-operation config/log/PID directory where the daemon supports it;
- never `killall`/globally kill `hostapd`, `dnsmasq`, or `wpa_supplicant`;
- never bring down `wlan0` or disable Wi-Fi as an automatic first step;
- identify the exact process by PID plus `/proc/<pid>/stat` start time, executable/cmdline, and an unguessable operation token;
- send TERM, wait with a deadline, revalidate identity, and only then KILL that exact process;
- back up shared files before any unavoidable mutation and restore only if the current file hash still matches the controller's written version;
- never stop or rewrite resources when ownership cannot be proven;
- never reset Bluetooth as part of hotspot startup.

If the platform only supports hostapd by replacing shared system config or taking over the system interface, classify Root fallback as unsafe/unsupported rather than forcing it.

## 8. Success is a layered result, not one Boolean

Report these separately:
1. `AP_ENABLED`: SoftAP state or beacon observed.
2. `CLIENT_CONNECTED`: at least one client association observed.
3. `DHCP_READY`: client obtained a lease and gateway route.
4. `DNS_READY`: client can resolve a test name through the advertised DNS path.
5. `INTERNET_READY`: client can reach a bounded external connectivity check through the expected upstream.

An enabled AP is not proof of DHCP, DNS, NAT, or internet forwarding. The UI must not display “internet shared” until the last required layer succeeds. Do not use external connectivity checks during boot unless the user opted into that behavior and the check is bounded.

## 9. Non-interference invariants

The fused controller must not:
- restart/disable Bluetooth after AP startup;
- stop CarPlay, P2P, projection, MCU/CAN, AVM/reverse camera, DSP/audio, thermal, or display services;
- kill system Wi-Fi daemons by process name;
- overwrite shared Wi-Fi configuration without backup/hash-guarded rollback;
- claim internet sharing from AP state alone;
- log SSID passphrases or client identifiers unnecessarily.

When the current Wi-Fi/P2P state cannot be distinguished safely, fail closed and preserve the existing connection.

## 10. Implementation gates before any APK patch

| Gate | Required evidence | Status |
|---|---|---|
| Exact mode routing | Full decode of `shouldEnable`, UI mode selection, and all mode call sites | OPEN |
| Donor config mapping | Trace donor constructor/config fields to DiPlay SSID/passphrase/band | PARTIAL |
| Runtime vendor API | Actual head-unit class/method/permission detection | UNVERIFIED |
| Android API privilege | Actual framework version and callable API path | UNVERIFIED |
| Root hostapd safe isolation | Exact binary/driver/interface capability and non-disruptive ownership test | UNVERIFIED |
| Source-level patchability | Obtain source or DEX rebuild toolchain and prove reproducible rebuild | OPEN |
| Signature compatibility | Original DiPlay signing key or an accepted installation/update path | OPEN |
| Device validation | AP, client, DHCP, DNS, NAT, CarPlay/Bluetooth regression tests | OPEN |

## 11. Next stage

Stage 18 should close the two code-level uncertainties that can be resolved statically:
1. decode `CarHotspotSettings.shouldEnable()` and the exact mode normalization/selection branches;
2. trace the DiPlay setup/request construction and donor `HotspotController` constructor fields so the config mapping is evidence-based.

Only after those are closed should implementation work begin. The current deliverable remains an audit/design artifact, not an installable APK.
