# HondaHack 7.6.0 — Stage 10: core component evidence recovery

## Input baseline
- DEX SHA-256: `3c3a0ac9deac6fd33bf9a76dd338085896a3010c9f4d9dfc7cb54eafc3595319`
- Evidence: Stage 05 DEX, Stage 08 focused Dalvik disassembly, Stage 09 control-flow notes.

## Results
- Indexed 94 methods across MyService, MyApplication, SettingsActivity and FixActivity.
- Identified 40 IntentFilter action registrations directly in MyService.onStartCommand.
- Collected 59 candidate preference/string keys for follow-up resource mapping.
- Produced method evidence index, broadcast action inventory, preference inventory and control-flow notes.

## Findings
- MyService touches audio focus, media, navigation, voice assistant, rear camera, meter display, GPS and Mitsubishi HMI service lifecycle actions.
- MyApplication attempts to bind four Mitsubishi service interfaces: IAvApService, IVehicleInfoManagerApService, ICameraAPService and IVideoManagerApService.
- SettingsActivity is a PreferenceActivity. It attaches change listeners to com.android.music, force_portrait, enable_debug, enable_test and enable_log_file. A car_model preference change triggers refresh logic.
- FixActivity contains factory-reset, reboot and system-repair related keys. These remain evidence-only and are not implemented in the safe rebuild.

## Artifacts
- service_action_map.tsv
- preference_inventory.tsv
- method_evidence_index.tsv
- method_recovery_notes.md
- stage10_extract.py

## Limits
This is evidence-based reconstruction, not complete original Java source. Obfuscated names, local variables, reflection, JNI, runtime-loaded code and resource-to-preference relationships still require mature DEX/Smali tooling and cross-validation. The current environment did not have JADX, Android SDK or Gradle available.

## Safety
Static analysis only. No original APK or embedded shell scripts are executed. No root/Xposed, license bypass, vehicle-control execution, factory reset, partition writes or dangerous repair operations are reproduced.
