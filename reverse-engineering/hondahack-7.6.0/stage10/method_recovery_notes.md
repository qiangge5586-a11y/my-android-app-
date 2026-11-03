# Stage 10 — method recovery notes

> Static DEX evidence only. This document is not original Java source. Obfuscated method names and incomplete operand decoding limit semantic certainty.

## MyService.onStartCommand
- Logs service startup and reads the default SharedPreferences key `force_portrait`, then calls an internal method to apply orientation behavior.
- Creates a worker thread with an internal Runnable.
- Creates an internal BroadcastReceiver and IntentFilter. The registered actions include application actions, navigation audio actions and Mitsubishi HMI power-service lifecycle actions.
- GPS_FIX_CHANGE-related handling is also present. Exact runtime permission and execution paths need further instruction-level review.
- See `service_action_map.tsv` for the 40 observed registrations.

## MyService inner receiver
String and branch evidence spans startup video, orientation lock, HMI lifecycle, disclaimer display, audio focus/volume, source switching, media playback, seat-belt/hand-brake/low-voltage alerts, road-information voice, speed/camera voice, rear camera, voice assistant, meter display, AMAPAUTO lifecycle and GPS status. A string reference alone does not prove the exact side effect of every branch.

## MyApplication.onCreate
- Reads the default preference `enable_log_file` and then enters several initialization calls.
- Attempts binding service interfaces named IAvApService, IVehicleInfoManagerApService, ICameraAPService and IVideoManagerApService.
- Other evidence points to audio-source state, key remapping, navigation and log-monitoring tasks. The static slice does not prove every task always executes.

## SettingsActivity
- Loads a preference XML resource through PreferenceActivity.
- Attaches change listeners to `com.android.music`, `force_portrait`, `enable_debug`, `enable_test` and `enable_log_file`.
- Contains a conditional removal path for `adb_tcpip`.
- onSharedPreferenceChanged reads `car_model` and invokes refresh logic.
- The exact titles, summaries, widget types, defaults and dependency rules still need resource XML mapping.

## FixActivity safety boundary
Keys include `fix_extframework_error`, `restore_factory_reset`, `reboot`, `fix_driving_video` and `auto_fix_volume`. They are evidence-only. The safe rebuild must not execute factory reset, reboot, partition/whitelist writes or shell commands.

## Unresolved
- Cross-check obfuscated symbols and anonymous inner classes using JADX and Smali.
- Recover the complete preference XML tree and map resource IDs to labels/defaults/dependencies.
- Map JNI entry points to native symbols and ABI.
- Analyze dynamic reflection and runtime-loaded behavior without executing the APK.
