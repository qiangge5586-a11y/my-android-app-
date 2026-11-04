#!/usr/bin/env bash
# Stage 60: read-only ELF/JNI inventory for HondaHack APK.
set -euo pipefail
APK="${1:?usage: stage60_audit.sh path/to/HondaHack.apk [output-dir]}"
OUT="${2:-stage60-output}"
for tool in unzip readelf sha256sum awk; do
  command -v "$tool" >/dev/null || { echo "missing required tool: $tool" >&2; exit 2; }
done
mkdir -p "$OUT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
printf 'library\tbytes\telf_class\tendianness\tmachine\tjni_export_count\tjni_exports\tneeded_libraries\tsha256\n' > "$OUT/native_elf_inventory.tsv"
while IFS= read -r path; do
  name="$(basename "$path")"
  file="$TMP/$name"
  unzip -p "$APK" "$path" > "$file"
  bytes="$(wc -c < "$file" | tr -d ' ')"
  sha="$(sha256sum "$file" | awk '{print $1}')"
  hdr="$(readelf -h "$file")"
  cls="$(printf '%s\n' "$hdr" | awk -F: '/Class:/{gsub(/^[ \t]+/,"",$2);print $2}')"
  data="$(printf '%s\n' "$hdr" | awk -F: '/Data:/{gsub(/^[ \t]+/,"",$2);print $2}')"
  machine="$(printf '%s\n' "$hdr" | awk -F: '/Machine:/{gsub(/^[ \t]+/,"",$2);print $2}')"
  exports="$(readelf -Ws "$file" | awk '$4=="FUNC" && $7!="UND" && $8 ~ /^Java_/ {print $8}' | paste -sd ';' -)"
  count=0; if [ -n "$exports" ]; then count="$(printf '%s' "$exports" | awk -F';' '{print NF}')"; fi
  needed="$(readelf -d "$file" | sed -n 's/.*Shared library: \[\(.*\)\].*/\1/p' | paste -sd ';' -)"
  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$name" "$bytes" "$cls" "$data" "$machine" "$count" "$exports" "$needed" "$sha" >> "$OUT/native_elf_inventory.tsv"
done < <(unzip -Z1 "$APK" | grep '^lib/[^/]*/[^/]*\.so$' | sort)
echo "Wrote $OUT/native_elf_inventory.tsv"
