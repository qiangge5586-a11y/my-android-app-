#!/usr/bin/env bash
# Read-only printable-string scan for the APK's three known native libraries.
set -euo pipefail
APK="${1:?usage: stage71_native_string_scan.sh path/to/HondaHack.apk [output-dir]}"
OUT="${2:-stage71-output}"
for tool in unzip strings sha256sum grep; do command -v "$tool" >/dev/null || { echo "missing $tool" >&2; exit 2; }; done
mkdir -p "$OUT"; TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
check_lib() {
  local name="$1" expected="$2" path="lib/armeabi-v7a/$1" file="$TMP/$1"
  unzip -p "$APK" "$path" > "$file"
  local actual; actual="$(sha256sum "$file" | awk '{print $1}')"
  [ "$actual" = "$expected" ] || { echo "hash mismatch for $name: $actual" >&2; exit 3; }
  strings -a -t x "$file" > "$OUT/$name.strings.txt"
  grep -Ei 'https?://|COM_[A-Z0-9_]+|cn/autohack|token|secret|password|auth|serial|vehicle|gps|location|xposed|lame_encode_buffer|get_lame_short_version' "$OUT/$name.strings.txt" > "$OUT/$name.hits.txt" || true
}
check_lib libnative.so bb6054b02fb133d8a432800cda84c75f76faca24554e8c70b3136054b3d69fd5
check_lib libxposed.so c241f274694c37cf5db0f5a3eb9baa9f7beca8ccc4ab2f911a91cb9661e1e9bb
check_lib libmp3lame.so bb7c1a46782f04b68bbbfa0db3b58db00b8c0dabaff081e33957982f92c6cd72
echo "String scan complete. Results: $OUT"
