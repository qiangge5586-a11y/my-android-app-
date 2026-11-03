# Stage 61 — JNI_OnLoad entry-point control-flow audit

## Input
- APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe
- Library: `lib/armeabi-v7a/libnative.so`
- Library SHA-256: bb6054b02fb133d8a432800cda84c75f76faca24554e8c70b3136054b3d69fd5
- Tool: LLVM objdump, ARM ELF32 disassembly
- Symbol: `JNI_OnLoad`, address `0x8af0`, size 56 bytes

## Recovered behavior
The complete symbol-bounded function contains 14 ARM instructions:
1. Saves registers and prepares a local output slot.
2. Constructs version constant `0x00010006` (JNI 1.6).
3. Reads the JavaVM function table and loads the function pointer at offset `0x18`, corresponding to `GetEnv` in the JNI Invocation API.
4. Calls that function with the requested JNI version and output environment pointer.
5. Returns the requested version constant.

## Interpretation
- The library requests JNI 1.6 and obtains a JNIEnv through JavaVM.GetEnv.
- No RegisterNatives call occurs inside the 56-byte JNI_OnLoad symbol body.
- Together with the 9 conventional `Java_cn_autohack_hondahack_d_*` exports in libnative.so, this supports name-based JNI entry resolution for those exported methods.
- This does not rule out registration or lookup behavior elsewhere in the library, nor does it recover the native methods' complete business semantics.

## Artifacts
- `jni_onload_disassembly.txt`: exact symbol-bounded ARM disassembly and input hashes.
- `stage61_jni_onload.sh`: reproducible read-only extraction/disassembly script.

## Limits and safety
- Static disassembly only; no APK or native library was loaded or executed.
- No C/C++ source, full call graph, or argument-level semantic mapping was recovered.
- No system modification, vehicle command, Xposed injection, licensing bypass, or embedded script execution was performed.
