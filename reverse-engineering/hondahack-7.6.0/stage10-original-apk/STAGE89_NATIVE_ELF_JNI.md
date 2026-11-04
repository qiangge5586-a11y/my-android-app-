# Stage 89 — ARM32 ELF headers, JNI exports and initial disassembly

## Input identity
Original APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe

All three packaged shared objects are ELF32, little-endian, ARM. They are stored under lib/armeabi-v7a only.

## libnative.so exports

| Symbol | ELF virtual address | Size |
|---|---:|---:|
| JNI_OnLoad | 0x8af0 | 56 |
| Java_cn_autohack_hondahack_d_a | 0x8f60 | 2,756 |
| Java_cn_autohack_hondahack_d_b | 0x9a24 | 308 |
| Java_cn_autohack_hondahack_d_c | 0x9b58 | 452 |
| Java_cn_autohack_hondahack_d_d | 0x9d1c | 232 |
| Java_cn_autohack_hondahack_d_e | 0x9e04 | 324 |
| Java_cn_autohack_hondahack_d_f | 0x9f48 | 304 |
| Java_cn_autohack_hondahack_d_g | 0xa078 | 224 |
| Java_cn_autohack_hondahack_d_q | 0xa158 | 348 |
| Java_cn_autohack_hondahack_d_v | 0xa2b4 | 136 |

These names correspond to native declarations in cn.autohack.hondahack.d. DEX signatures recovered in Stage 86:
- a(Context)
- b(Context, String)
- c(Context, String, double, double)
- d(Context)
- e(Context, int)
- f(Context, int)
- g(Context)
- q(Context, String)
- v(Context)

All are native declarations without DEX code bodies. JNI function semantics are not assigned yet.

## libxposed.so exports

| Symbol | ELF virtual address | Size |
|---|---:|---:|
| Java_cn_autohack_hondahack_c_b | 0x85a8 | 1,056 |

The symbol is a JNI bridge declared by app class cn.autohack.hondahack.c. Its role must be traced through the Java caller and disassembly. The library name and symbol alone do not establish that it is a full Xposed framework implementation.

## libmp3lame.so JNI exports

| Symbol | ELF virtual address | Size |
|---|---:|---:|
| Java_cn_autohack_voiceassistant_SimpleLame_init | 0x1c695 | 100 |
| Java_cn_autohack_voiceassistant_SimpleLame_encode | 0x1c6f9 | 164 |
| Java_cn_autohack_voiceassistant_SimpleLame_close | 0x1c7f5 | 24 |
| Java_cn_autohack_voiceassistant_SimpleLame_flush | 0x1c79d | 88 |

This establishes a Java-to-LAME native audio encoding bridge for the voice-assistant package. The exact recording and encoding pipeline must be traced in the Java classes.

## JNI_OnLoad initial disassembly

libnative.so JNI_OnLoad at 0x8af0:
- obtains the JavaVM function table from the first argument;
- loads the function pointer at vtable offset 0x18 (JNI GetEnv slot);
- passes JNI version constant 0x00010006;
- returns that version constant.

This is consistent with requesting JNI 1.6. It does not show RegisterNatives in this function.

## First native method disassembly observations

libnative.so Java_cn_autohack_hondahack_d_a begins at 0x8f60 and is 2,756 bytes. Its prologue obtains the third JNI argument (the Context object for a static native method), calls JNI function-table entries, and uses vector instructions. The function body is too long and indirect to infer its purpose from the prologue; full JNI call decoding and referenced string/data analysis are required.

## Reproducible tools used

- `readelf -h` to verify ELF class, byte order and ARM machine.
- `readelf -Ws` to enumerate dynamic exported symbols and sizes.
- `llvm-objdump -d --triple=armv7-none-linux-gnueabihf` to inspect ARM code.

## Next native-analysis stages
1. Decode JNI function-table call offsets to identify GetObjectClass, GetMethodID, Call*Method, GetStringUTFChars, GetArrayLength and related JNI operations.
2. Map each function's literal strings, referenced data and control flow.
3. Compare native declarations and parameters against Java call sites.
4. Disassemble libxposed.so and determine the exact purpose of its exported JNI bridge.
5. Trace SimpleLame Java methods and determine whether audio is recorded, encoded and stored locally or transmitted.
