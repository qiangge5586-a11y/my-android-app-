#!/usr/bin/env bash
# Read-only, symbol-bounded JNI_OnLoad disassembly for HondaHack's ARM library.
set -euo pipefail
APK="${1:?usage: stage61_jni_onload.sh path/to/HondaHack.apk [output-dir]}"
OUT="${2:-stage61-output}"
for tool in unzip readelf llvm-objdump sha256sum awk; do
  command -v "$tool" >/dev/null || { echo "missing required tool: $tool" >&2; exit 2; }
done
mkdir -p "$OUT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
unzip -p "$APK" lib/armeabi-v7a/libnative.so > "$TMP/libnative.so"
read -r addr_hex size_hex < <(readelf -Ws "$TMP/libnative.so" | awk '$4=="FUNC" && $8=="JNI_OnLoad" {print $2, $3; exit}')
[ -n "${addr_hex:-}" ] && [ -n "${size_hex:-}" ] || { echo 'JNI_OnLoad not found' >&2; exit 3; }
addr=$((16#$addr_hex)); size=$((size_hex)); end=$((addr+size))
{
  echo "APK_SHA256=$(sha256sum "$APK" | awk '{print $1}')"
  echo "LIB_SHA256=$(sha256sum "$TMP/libnative.so" | awk '{print $1}')"
  echo "JNI_OnLoad_ADDRESS=0x$addr_hex"
  echo "JNI_OnLoad_SIZE=$size"
  echo "JNI_OnLoad_END=0x$(printf '%x' "$end")"
  echo
  llvm-objdump -d --start-address="$addr" --stop-address="$end" "$TMP/libnative.so"
} > "$OUT/jni_onload_disassembly.txt"
echo "Wrote $OUT/jni_onload_disassembly.txt"
