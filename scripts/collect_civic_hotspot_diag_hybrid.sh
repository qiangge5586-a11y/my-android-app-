#!/system/bin/sh
# Read-only diagnostics for old Android/toolbox head units.
# Never starts/stops Wi-Fi, hostapd, or dnsmasq and never changes system files.
set +e

section() { echo; echo "===== $1 ====="; }
have() { command -v "$1" >/dev/null 2>&1; }
run_if() {
  tool="$1"; shift
  if have "$tool"; then "$tool" "$@" 2>&1
  else echo "$tool: unavailable"
  fi
}
last_lines() {
  # POSIX awk ring buffer; avoids depending on tail.
  count="$1"
  awk -v n="$count" '{ line[NR % n] = $0 } END {
    first = NR > n ? NR - n + 1 : 1
    for (i = first; i <= NR; i++) print line[i % n]
  }'
}
section "ROOT AND ANDROID"
run_if id
run_if uname -a
if have getprop; then
  for p in ro.product.manufacturer ro.product.model ro.board.platform ro.hardware ro.build.version.release ro.build.version.sdk; do
    printf '%s=' "$p"; getprop "$p"
  done
else echo "getprop: unavailable"; fi
printf 'SELinux='
if have getenforce; then getenforce 2>&1; else echo "getenforce unavailable"; fi

section "WLAN INTERFACE"
if have ip; then
  ip link show wlan0 2>&1
  ip addr show wlan0 2>&1
else echo "ip: unavailable"; fi
run_if ifconfig wlan0
printf 'driver symlink: '
if have readlink; then readlink /sys/class/net/wlan0/device/driver 2>/dev/null
else ls -ld /sys/class/net/wlan0/device/driver 2>/dev/null || echo unavailable; fi
printf 'device symlink: '
if have readlink; then readlink /sys/class/net/wlan0/device 2>/dev/null
else ls -ld /sys/class/net/wlan0/device 2>/dev/null || echo unavailable; fi

section "WIRELESS TOOLS AND BINARY DISCOVERY"
for b in iw iwconfig hostapd dnsmasq ip ifconfig svc; do
  if have "$b"; then command -v "$b"; else echo "$b: unavailable in PATH"; fi
done
for d in /system/bin /system/xbin /vendor/bin /vendor/bin/hw /system_ext/bin /odm/bin /bin; do
  for n in hostapd dnsmasq; do
    f="$d/$n"
    if [ -x "$f" ]; then
      echo "--- $f ---"
      if [ "$n" = hostapd ]; then "$f" -v 2>&1; "$f" -h 2>&1 | last_lines 60
      else ls -l "$f" 2>&1; fi
    fi
  done
done

section "WIRELESS CAPABILITIES"
if have iw; then
  iw dev 2>&1
  iw list 2>&1 | awk '
    /Supported interface modes:/ { show=1 }
    show { print }
    show && /Band [0-9]+:/ { exit }
  '
else echo "iw not installed; AP capability cannot be confirmed from iw"; fi

section "KERNEL DRIVER AND MODULE HINTS"
if have dmesg; then
  dmesg 2>/dev/null | awk '
    BEGIN { IGNORECASE=1 }
    /wlan|wifi|cfg80211|mac80211|hostapd|firmware|nl80211/ { line[NR % 120]=$0; n++ }
    END {
      # Print the most recent matching lines retained in source order.
      first = n > 120 ? n - 119 : 1
      for (i=first; i<=n; i++) if (line[i % 120] != "") print line[i % 120]
    }
  '
else echo "dmesg unavailable"; fi
ls -ld /sys/class/net/wlan0 2>/dev/null
ls -ld /vendor/lib/modules /vendor_dlkm/lib/modules /odm/lib/modules 2>/dev/null

section "CURRENT SERVICE STATE"
if have svc; then svc wifi status 2>&1; else echo "svc unavailable"; fi
if have ps; then
  ps 2>&1 | awk '
    BEGIN { IGNORECASE=1 }
    /hostapd|dnsmasq|wpa_supplicant|wifi/ { print }
  '
else echo "ps unavailable"; fi

section "DIPLAY RUNTIME FILES AND LOGS"
for f in /data/local/tmp/diplay-root-hotspot/hostapd.log /data/local/tmp/diplay-root-hotspot/dnsmasq.log /data/local/tmp/diplay-root-hotspot/hostapd.pid /data/local/tmp/diplay-root-hotspot/hostapd.owner /data/local/tmp/diplay-root-hotspot/dnsmasq.pid /data/local/tmp/diplay-root-hotspot/dnsmasq.owner /data/local/tmp/hapd.log /data/local/tmp/hapd-fg.log /data/local/tmp/hostapd.log /data/local/tmp/dns.log; do
  if [ -e "$f" ]; then
    ls -l "$f" 2>&1
    case "$f" in
      *.log) echo "--- recent content: $f ---"; last_lines 160 < "$f" ;;
      *) echo "--- value: $f ---"; cat "$f" 2>&1 ;;
    esac
  else echo "MISSING $f"; fi
done

section "HOSTAPD CONFIG (SECRETS REDACTED)"
for f in /data/misc/wifi/hostapd.conf /data/local/tmp/diplay-root-hotspot/hostapd.stage.conf; do
  if [ -r "$f" ]; then
    ls -l "$f"
    awk '{
      key=$0
      sub(/^[[:space:]]*/, "", key)
      split(key, a, "=")
      k=tolower(a[1])
      if (k=="ssid" || k=="wpa_passphrase" || k=="wpa_psk" || k=="sae_password") sub(/=.*/, "=<REDACTED>")
      print
    }' "$f"
  else echo "MISSING OR UNREADABLE $f"; fi
done

section "KERNEL WIFI ERROR EVIDENCE"
if have dmesg; then
  dmesg 2>/dev/null | awk '
    BEGIN { IGNORECASE=1 }
    /nl80211|cfg80211|mac80211|wlan0|hostapd|firmware|AP-STA|failed|error/ { print }
  ' | last_lines 160
else echo "dmesg unavailable"; fi

echo
echo "===== END (read-only; no settings changed) ====="
