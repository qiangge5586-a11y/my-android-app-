# Stage 71 — Native library string reconnaissance (corrected)

## Input hashes
- APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe
- libnative.so: bb6054b02fb133d8a432800cda84c75f76faca24554e8c70b3136054b3d69fd5
- libxposed.so: c241f274694c37cf5db0f5a3eb9baa9f7beca8ccc4ab2f911a91cb9661e1e9bb
- libmp3lame.so: bb7c1a46782f04b68bbbfa0db3b58db00b8c0dabaff081e33957982f92c6cd72

## Meaningful printable strings
| Library | File offset | String | Conservative interpretation |
|---|---:|---|---|
| libnative.so | 0x1d18 | COM_TENCENT_QQMUSICCAR_ACTION | Broadcast action identifier naming a QQ Music car integration; receiver behavior is not established by the string alone. |
| libnative.so | 0x18129 | cn/autohack/hondahack/e | Java class descriptor embedded in the native binary; purpose is not established. |
| libmp3lame.so | 0x1e86 | get_lame_short_version | LAME MP3 encoder library symbol. |
| libmp3lame.so | 0x2a38 | lame_encode_buffer | LAME MP3 encoding API symbol. |

## URL and credential keyword scan — corrected observation
- The scan does find an HTTPS URL embedded in compiler build metadata in both libnative.so and libxposed.so: an Android LLVM toolchain source URL (`https://android.googlesource.com/toolchain/llvm-project`).
- This is compiler provenance metadata, not evidence of an application backend endpoint.
- The filtered printable strings did not reveal an obvious app-specific HTTP(S) API endpoint or obvious token/password/secret label.
- This does not prove the APK has no network endpoints or credentials: strings may be encoded, constructed, stored in DEX/resources, or supplied at runtime.

## Reproducible artifact
`stage71_native_string_scan.sh` extracts the three exact APK libraries, checks their SHA-256 values, and records matching printable strings with file offsets.

## Limits and safety
Static strings do not prove runtime call paths. No native library was loaded or executed. No credentials were recovered or published. Xposed hooks, authentication bypass, vehicle commands, privileged writes and destructive operations remain out of scope.
