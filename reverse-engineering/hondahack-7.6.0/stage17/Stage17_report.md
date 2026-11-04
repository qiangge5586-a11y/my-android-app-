# HondaHack 7.6.0 — Stage 17: DEX/JNI/native boundary mapping

DEX SHA-256: `3c3a0ac9deac6fd33bf9a76dd338085896a3010c9f4d9dfc7cb54eafc3595319`

- DEX native method declarations: 14.
- Exported JNI symbols across bundled ELF libraries: 14.
- Native declaration to exported symbol matches: 14.
- System.loadLibrary call sites recovered from Stage 13 semantic listings: 2.
- Directly recovered library names: `native`, `mp3lame`.
- Bundled ARM ELF libraries inspected: 3.
- Parser/tool errors: 0.

Outputs: dex_native_methods.tsv, native_exports.tsv, native_library_metadata.tsv, load_library_calls.tsv, native_boundary_map.tsv, and stage17_summary.json. The builder build_stage17.py is included in the stage archive.

Interpretation: JNI symbol presence is static evidence only. A library without a recovered System.loadLibrary call may be loaded by a different mechanism or remain unused on this code path; absence of a direct call is not proof of non-use. Export-name matching does not establish runtime reachability or function semantics.

Safety: ELF files were inspected with metadata tools only; no native code, APK, embedded script, or vehicle interaction was executed. No root/Shizuku/Xposed injection, vehicle/HMI writes, authorization bypass, or destructive system operations are implemented.
