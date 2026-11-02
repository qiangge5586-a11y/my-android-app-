# DiPlay 0.3.12 Hotspot Fusion — Stage 18
## Exact boot eligibility predicate and mode-routing consequence

Date: 2026-11-02  
Baseline: DiPlay v0.3.12 legacy APK  
Status: static DEX control-flow audit; no APK patch or device test

## 1. New confirmed result

The method `CarHotspotSettings.shouldEnable(context, wirelessEnabled, mode)` is not a generic “hotspot mode enabled” predicate. Its decoded branch structure returns true only when all of the following are true:

1. `CarHotspotSettings.enabled(context)` is true (`diplay_car_hotspot/auto_enable`, default false);
2. the caller-supplied `wirelessEnabled` argument is true (the caller passes `AirPlayPersistence.loadWirelessEnabled()`);
3. `mode == WirelessHotspotMode.MANUAL`.

The DEX method has four incoming register words (receiver + Context + Boolean + enum). Its branch sequence checks the preference result, checks the Boolean argument, loads the static enum field `WirelessHotspotMode.MANUAL`, compares it with the mode argument, and returns true only on equality.

This resolves the Stage 16 uncertainty around the exact predicate. It also corrects the overly broad reading of the boot gate in earlier planning.

## 2. Caller chain and effect

`CarHotspotSetup.shouldStartOnLaunch(context)`:
1. loads `AirPlayPersistence.loadWirelessEnabled()`;
2. loads `AirPlayPersistence.loadWirelessHotspotMode()`;
3. passes context, wireless-enabled Boolean, and selected mode to `CarHotspotSettings.shouldEnable(...)`;
4. returns false immediately when that predicate is false;
5. only after the predicate succeeds, loads the manual SSID and passphrase and calls `ManualHotspotValidation.error(...)`;
6. returns true only when manual validation reports no error.

Therefore, the existing `BOOT_COMPLETED -> healHotspotIfOptedIn -> shouldStartOnLaunch` path is **MANUAL-only** under the current predicate. In particular:
- `HOSTAPD` does not pass this boot gate;
- `LOCAL_ONLY_HOTSPOT`, `WIFI_P2P`, and `EXISTING_WIFI` do not pass this boot gate;
- the manual SSID/passphrase validation is downstream of the mode gate, not a generic validation for every mode.

This is a code-path fact for the audited v0.3.12 APK. It does not establish what the UI labels promise or whether another independent service starts other modes elsewhere.

## 3. Fusion implementation consequence

Do not simply preserve the current predicate and expect the new controller to auto-start `HOSTAPD` on boot. Also do not silently broaden it during fusion.

Before changing behavior, choose and document one product policy:
- **Compatibility policy:** preserve MANUAL-only boot auto-start exactly; controller receives a MANUAL request only.
- **Explicit expanded policy:** add an explicit per-mode boot opt-in and mode-specific validation. This is a user-visible behavior change and must not be smuggled into the controller refactor.

Recommended initial fusion behavior is compatibility policy. It minimizes unexpected hotspot activation and preserves the existing user's opt-in semantics. A later UI change can deliberately add a HOSTAPD boot toggle after device capability and backend safety are proven.

## 4. Updated mode / boot matrix

| Mode | Passes current boot gate? | Reason |
|---|---:|---|
| `MANUAL` | Yes, conditionally | `auto_enable` + `wireless_enabled` + manual credential validation |
| `HOSTAPD` | No | mode comparison is explicitly against `MANUAL` |
| `LOCAL_ONLY_HOTSPOT` | No | mode comparison fails |
| `WIFI_P2P` | No | mode comparison fails |
| `EXISTING_WIFI` | No | mode comparison fails |

“Yes, conditionally” still requires `BOOT_COMPLETED`, wireless enabled, independent hotspot opt-in enabled, and valid manual credentials.

## 5. Safety and compatibility requirements

- Keep app UI auto-start (`auto_start_on_boot`) separate from hotspot auto-start (`diplay_car_hotspot/auto_enable`).
- Do not copy donor boot receivers, retry receivers, jobs, or preferences.
- Keep `PendingResult.finish()` in every worker exit path.
- For compatibility-first fusion, do not auto-start Root hostapd merely because the selected mode is `HOSTAPD`.
- If a future version adds HOSTAPD boot auto-start, it must have its own explicit opt-in and must pass Root ownership/capability preflight before launching anything.
- Never log manual passphrases.

## 6. Evidence boundaries

Confirmed by static DEX:
- `shouldEnable` loads the independent `auto_enable` preference;
- the caller passes `loadWirelessEnabled()`;
- the enum comparison is against the static `MANUAL` field;
- `shouldStartOnLaunch` validates manual SSID/passphrase only after this gate succeeds.

Not established by this stage:
- every UI mode-selection handler and all foreground/manual start call sites;
- whether an unrelated service or receiver starts another mode independently;
- actual device behavior, framework privilege, vendor APIs, or SoftAP driver support.

## 7. Next stage

Stage 19 should trace all foreground mode-selection and start call sites, especially `HOSTAPD`, `LOCAL_ONLY_HOTSPOT`, and `EXISTING_WIFI`, and map each one to its concrete backend. This determines whether the current foreground path has a separate mode policy from the MANUAL-only boot path.
