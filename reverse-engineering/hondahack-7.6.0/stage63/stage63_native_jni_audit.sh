#!/usr/bin/env bash
# Static, symbol-bounded JNI export disassembly. Does not execute the APK/library.
set -euo pipefail
APK="${1:?usage: stage63_native_jni_audit.sh path/to/HondaHack.apk [output-dir]}"
OUT="${2:-stage63-output}"
for tool in unzip readelf llvm-objdump sha256sum awk; do
  command -v "$tool" >/dev/null || { echo "missing required tool: $tool" >&2; exit 2; }
done
mkdir -p "$OUT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
LIB="$TMP/libnative.so"
unzip -p "$APK" lib/armeabi-v7a/libnative.so > "$LIB"
LIB_SHA="$(sha256sum "$LIB" | awk '{print $1}')"
EXPECTED="bb6054b02fb133d8a432800cda84c75f76faca24554e8c70b3136054b3d69fd5"
if [ "$LIB_SHA" != "$EXPECTED" ]; then echo "Unexpected libnative.so hash: $LIB_SHA" >&2; exit 3; fi
printf 'symbol\taddress_hex\tsize_bytes\tlibrary\tsha256\n' > "$OUT/native_jni_symbols.tsv"
: > "$OUT/native_jni_disassembly.txt"
while read -r addr_hex size_dec symbol; do
  [ -n "${symbol:-}" ] || continue
  addr=$((16#$addr_hex)); end=$((addr+size_dec))
  printf '%s\t0x%s\t%s\tlibnative.so\t%s\n' "$symbol" "$addr_hex" "$size_dec" "$LIB_SHA" >> "$OUT/native_jni_symbols.tsv"
  { echo; echo "===== $symbol address=0x$addr_hex size=$size_dec ====="; llvm-objdump -d --start-address="$addr" --stop-address="$end" "$LIB"; } >> "$OUT/native_jni_disassembly.txt"
done < <(readelf -Ws "$LIB" | awk '$4=="FUNC" && $7!="UND" && $8 ~ /^Java_cn_autohack_hondahack_d_/ {print $2, $3, $8}' | sort -k1,1)
count="$(($(wc -l < "$OUT/native_jni_symbols.tsv")-1))"
[ "$count" -eq 9 ] || { echo "Expected 9 JNI exports, found $count" >&2; exit 4; }
echo "Audited $count JNI exports. Output: $OUT"
