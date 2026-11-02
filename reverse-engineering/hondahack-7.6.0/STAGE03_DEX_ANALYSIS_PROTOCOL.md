# HondaHack 7.6.0 — Stage 03 DEX Analysis Protocol

## Status
Planning and evidence-collection protocol only. No DEX decompilation or binary analysis has been performed in this stage.

## Objective
Map the APK's Java/DEX structure, identify the application entry points, and establish evidence-backed call paths to the already observed scripts, network endpoints, and Xposed integration.

## Inputs
- APK: `本田黑科技7.6.0_可联网_去注销_任意.apk`
- Package: `cn.autohack.hondahack`
- SHA-256: `e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe`
- Prior report: `STAGE02_STATIC_RESOURCE_AND_SCRIPT_AUDIT.md`

Before analysis, verify the APK hash matches the value above. If it differs, stop and record the new sample identity rather than mixing evidence.

## Required outputs
1. JADX project export, preserving package/class names and decompiler warnings.
2. Smali export for classes that JADX fails to decompile or renders ambiguously.
3. Class inventory with superclass, interfaces, fields, and method signatures.
4. Call graph for:
   - `cn.autohack.hondahack.MainActivity`
   - `cn.autohack.hondahack.MyService`
   - `cn.autohack.hondahack.MyBroadcastReceiver`
   - `np.manager.signature.PmsHookApplication`
5. Cross-reference report for these indicators:
   - `factory_reset.sh`
   - `install_recovery2_clarion.sh`
   - `install_recovery2_mitsubishi.sh`
   - `whitelist.xml`
   - `XposedBridge.bin`
   - `XposedBridge.jar`
   - `auth-code`
   - previously identified `autohack.cn` and `autohack.org` endpoint strings.
6. Evidence ledger: each conclusion must cite a class, method, instruction/string location, and tool/version. Separate observed facts from hypotheses.

## Procedure
1. Verify the input hash and record file size and tool versions.
2. Run JADX into a new output directory; retain logs and warnings.
3. Decode the DEX to Smali independently for cross-checking.
4. Build a class/method inventory.
5. Trace references from manifest components and application initialization.
6. Trace all references to the indicators listed above.
7. Identify dynamic loading, reflection, native method declarations, and Xposed entry points.
8. Export a call graph. Mark unresolved reflective or native edges explicitly.
9. Review findings without executing the APK or its embedded scripts.

## Safety constraints
- Do not install or run the APK on a personal phone, vehicle head unit, or production device.
- Do not execute embedded shell scripts.
- Do not connect the sample to a vehicle, CAN adapter, or diagnostic interface.
- Treat USB recovery scripts and factory-reset paths as potentially destructive.
- Do not send requests to the hard-coded service endpoints. Network behavior must be assessed offline from code first.
- Any dynamic testing must be isolated, disposable, and separately approved.

## Completion criteria
Stage 03 is complete only when the required exports, logs, class inventory, call graph, and evidence ledger are committed to this repository. A tool installation script or a plan alone does not satisfy completion.

## Next stages
- Stage 04: Xposed initialization, hook targets, and system modification paths.
- Stage 05: network request construction, authorization, and device-data flow.
- Stage 06: ARM native library analysis and JNI registration.
