# Stage 63 — libnative.so JNI method boundary recovery

## Verified input
- APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe
- Library: lib/armeabi-v7a/libnative.so
- Library SHA-256: bb6054b02fb133d8a432800cda84c75f76faca24554e8c70b3136054b3d69fd5
- ELF: 32-bit ARM, little-endian
- Inspection: readelf -Ws and symbol-bounded llvm-objdump -d; no execution

## Nine exported JNI method bodies
| Symbol | Address | Size (bytes) |
|---|---:|---:|
| Java_cn_autohack_hondahack_d_a | 0x8f60 | 2756 |
| Java_cn_autohack_hondahack_d_b | 0x9a24 | 308 |
| Java_cn_autohack_hondahack_d_c | 0x9b58 | 452 |
| Java_cn_autohack_hondahack_d_d | 0x9d1c | 232 |
| Java_cn_autohack_hondahack_d_e | 0x9e04 | 324 |
| Java_cn_autohack_hondahack_d_f | 0x9f48 | 304 |
| Java_cn_autohack_hondahack_d_g | 0xa078 | 224 |
| Java_cn_autohack_hondahack_d_q | 0xa158 | 348 |
| Java_cn_autohack_hondahack_d_v | 0xa2b4 | 136 |
| Total | | 5084 |

## Findings
- Nine conventional JNI exports are present in libnative.so; all have bounded ELF function symbols with non-zero sizes.
- The largest export, d_a, occupies 2,756 bytes, about 54.2% of the nine functions' combined symbol size. It should be prioritized for detailed control-flow and string-reference analysis.
- The other eight exports range from 136 to 452 bytes.
- Stage 59's DEX-side inventory reports 14 native declarations and 14 conventional JNI export matches across the APK's native libraries. This stage isolates the nine d_* exports in libnative.so; it does not imply the remaining five declarations are absent.
- Symbol names are obfuscated. Names and byte sizes alone do not establish business meaning or safe-to-recreate behavior.

## Reproducible artifact
stage63_native_jni_audit.sh extracts only the named library, records its SHA-256, enumerates the nine symbol ranges, and emits symbol-bounded ARM disassembly. It requires unzip, readelf, llvm-objdump, sha256sum, and standard shell tools.

## Limits
This is static function-boundary recovery, not recovered C/C++ source. No full argument/dataflow interpretation is claimed. No native library or APK code was executed. Xposed injection, authentication bypass, vehicle-control commands, factory reset, and system partition writes remain out of scope.
