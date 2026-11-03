# Stage 84 — Original APK revalidation and extraction baseline

## Scope correction

This stage analyzes only the uploaded original file `本田黑科技7.6.0_可联网_去注销_任意.apk`. The unrelated factory-screen / CarPlay note created in error was removed from this branch. The safe rebuild is not the reverse-engineering target.

## Input identity

- File: `本田黑科技7.6.0_可联网_去注销_任意.apk`
- Size: 3,893,398 bytes
- SHA-256: `e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe`
- ZIP entries: 986
- Total uncompressed entry bytes: 5,526,302

## DEX inventory

- DEX files: 1 (`classes.dex`)
- DEX version: 035
- DEX bytes: 1,909,752
- DEX SHA-256: `3c3a0ac9deac6fd33bf9a76dd338085896a3010c9f4d9dfc7cb54eafc3595319`
- String IDs: 12,302
- Type IDs: 2,555
- Method IDs: 15,752
- Class definitions: 1,770
- Class definitions under `cn.autohack.hondahack`: 353

Representative app classes confirmed in DEX include:
`MainActivity`, `SettingsActivity`, `AboutActivity`, `AdvancedActivity`, `AppsActivity`, `AppsInstallLocalActivity`, `AppsInstallOnlineActivity`, `AutorunManagerActivity`, `CleanInternalSdActivity`, `EmergencyActivity`, `FixActivity`, `LogActivity`, `MeterActivity`, `MyApplication`, `MyBroadcastReceiver`, `MyService`, `NavigatorActivity`, `OptimizeActivity`, `RemapKeysActivity`, `SdcardActivity`, `SelectLocalVideoActivity`, `SelectOnlineVideoActivity`, `StartupActivity`, `StartupVideoActivity`, `VoiceAssistantActivity`, and `Xposed`.

Many classes are obfuscated to short identifiers. Names alone are not treated as proof of their behavior; the next stages must map method bodies, call sites, resources, manifest components and native calls.

## Resources and assets

- `res/` entries: 975
- `resources.arsc`: 648,100 bytes
- `AndroidManifest.xml`: 15,644 bytes (binary Android XML)
- `assets/VERSION`
- `assets/xposed_init`

Resource inventory includes UI layouts, drawables, localized voice prompts/audio and other app assets. These should be decoded and preserved rather than recreated from screenshots.

## Native libraries

All three libraries are packaged only for `armeabi-v7a`:

| Library | Bytes | SHA-256 |
|---|---:|---|
| `libnative.so` | 120,464 | `bb6054b02fb133d8a432800cda84c75f76faca24554e8c70b3136054b3d69fd5` |
| `libmp3lame.so` | 157,368 | `bb7c1a46782f04b68bbbfa0db3b58db00b8c0dabaff081e33957982f92c6cd72` |
| `libxposed.so` | 108,124 | `c241f274694c37cf5db0f5a3eb9baa9f7beca8ccc4ab2f911a91cb9661e1e9bb` |

Static string evidence:
- `libnative.so` contains JNI export-name strings for `Java_cn_autohack_hondahack_d_a` through selected obfuscated methods, `JNI_OnLoad`, and `COM_TENCENT_QQMUSICCAR_ACTION` (file offset 0x1d18). The presence of the action string alone does not prove a receiver is registered or that a QQMusic integration works.
- `libmp3lame.so` exports/contains LAME encoder API strings including `lame_encode_buffer` and version/configuration functions. This indicates MP3 encoding support, not a Honda backend.
- `libxposed.so` contains a JNI entry name `Java_cn_autohack_hondahack_c_b`; exact runtime behavior still needs disassembly and Java call-site tracing.

## What is and is not recovered

This stage establishes a reproducible, hash-identified extraction baseline and verifies the APK's DEX/resource/native composition. It does **not** claim that all Java source has been recovered or that native functions have been fully decompiled. DEX bytecode is not original Java source; decompiler output will be reconstructed source with possible control-flow and naming inaccuracies.

## Next stages

1. Decode binary manifest and resources; enumerate all components, permissions, intent filters, resource IDs and layouts.
2. Decompile every DEX class, preserving package paths and a mapping from obfuscated descriptors to recovered names.
3. Trace app-owned call graphs and annotate behavior for every Activity, Service, Receiver and Xposed entry point.
4. Analyze each ARM32 ELF library: headers, sections, dynamic symbols, relocations, JNI registration, function boundaries and disassembly.
5. Cross-reference native calls with Java declarations and call sites; record confidence and unresolved behavior.
6. Only after analysis coverage is measured, consolidate source output and build an independent reproducible project.

## Reproducibility

The APK SHA-256 above is the identity key for all subsequent reports. Re-run inventory against the same original APK before comparing any future extraction. Do not substitute the safe rebuild APK or another version.
