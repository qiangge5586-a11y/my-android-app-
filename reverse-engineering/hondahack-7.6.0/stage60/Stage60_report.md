# Stage 60 — Native ELF / JNI export audit

## Input integrity
- APK: 本田黑科技 7.6.0
- APK size: 3,893,398 bytes
- APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe
- DEX SHA-256: 3c3a0ac9deac6fd33bf9a76dd338085896a301a0c9f4d9dfc7cb54eafc3595319

## Confirmed native libraries
All three packaged libraries are ELF32, little-endian ARM (armeabi-v7a).

| Library | Bytes | SHA-256 | JNI exports |
|---|---:|---|---:|
| libnative.so | 120,464 | bb6054b02fb133d8a432800cda84c75f76faca24554e8c70b3136054b3d69fd5 | 9 |
| libxposed.so | 108,124 | c241f274694c37cf5db0f5a3eb9baa9f7beca8ccc4ab2f911a91cb9661e1e9bb | 1 |
| libmp3lame.so | 157,368 | bb7c1a46782f04b68bbbfa0db3b58db00b8c0dabaff081e33957982f92c6cd72 | 4 |

## JNI export mapping evidence
- `libnative.so`: `Java_cn_autohack_hondahack_d_a`, `_d_b`, `_d_c`, `_d_d`, `_d_e`, `_d_f`, `_d_g`, `_d_q`, `_d_v`.
- `libxposed.so`: `Java_cn_autohack_hondahack_c_b`.
- `libmp3lame.so`: `Java_cn_autohack_voiceassistant_SimpleLame_init`, `_encode`, `_flush`, `_close`.
- `libnative.so` exports `JNI_OnLoad`; no `JNI_OnLoad` export was found in the other two libraries.
- Stage 59 independently recorded 14 DEX native declarations and 14 conventional JNI export matches. This stage confirms the packaged ELF-side export names and per-library placement.

## Dynamic dependencies
- `libnative.so` and `libxposed.so`: `liblog.so`, `libm.so`, `libdl.so`, `libc.so`.
- `libmp3lame.so`: `libdl.so`, `libc.so`, `libm.so`.

## Limits
- Export names establish JNI entry-point names, not complete native semantics.
- The APK ships only armeabi-v7a native binaries; no arm64-v8a/x86 ABI variant was found.
- ARM disassembly was not performed because this environment lacks an ARM-capable disassembler. ELF exports and dependencies were read statically; no library or APK code was executed.
- JNI_OnLoad may perform dynamic registration or initialization; its body remains to be disassembled and traced.
- No C/C++ source was recovered and no APK was rebuilt.

## Safety
Read-only static inspection. No Xposed injection, licensing/authentication bypass, system writes, vehicle commands, factory reset, or embedded scripts were executed or reproduced.
