#!/system/bin/sh
# Read-only diagnostics for the rooted head unit.
# This script does not start/stop Wi-Fi, hostapd, dnsmasq, or change settings.
set +e

section() { echo; echo "===== $1 ====="; }
have() { command -v "$1" >/dev/null 2>&1; }
run_if() {
  c="$1"; shift
  if have "$c"; then "$c" "$@"; else echo "$c: not installed"; fi
}
last_lines() {
  n="$1"
  awk -v n="$n" '{ ring[NR % n]=$0 } END { first=NR>n?NR-n+1:1; for(i=first;i<=NR;i++) print ring[i%n] }'
}
filter_wifi() {
  awk '
    BEGIN { IGNORECASE=1 }
    /wlan|wifi|cfg80211|mac80211|hostapd|firmware|nl80211|ap-sta|failed|error/ { print }
  '
}

section "ROOT AND ANDROID"
id
uname -a
for p in ro.product.manufacturer ro.product.model ro.board.platform ro.hardware ro.build.version.release ro.build.version.sdk; do
  printf '%s=' "$p"; run_if getprop "$p"
done
printf 'SELinux='
if have getenforce; then getenforce 2>/dev/null; else echo "getenforce unavailable"; fi

section "WLAN INTERFACE"
if have ip; then
  ip link show wlan0 2>&1
  ip addr show wlan0 2>&1
else
  echo "ip: not installed"
fi
run_if ifconfig wlan0
printf 'driver='
if have readlink; then readlink /sys/class/net/wlan0/device/driver 2>/dev/null; else echo "readlink unavailable"; fi
printf 'device='
if have readlink; then readlink /sys/class/net/wlan0/device 2>/dev/null; else echo "readlink unavailable"; fi

section "WIRELESS TOOLS"
for b in iw iwconfig hostapd dnsmasq ip ifconfig svc; do
  if have "$b"; then command -v "$b"; else echo "$b: not installed"; fi
done
for b in /vendor/bin/hostapd /system/bin/hostapd /system/xbin/hostapd /vendor/bin/hw/hostapd /system_ext/bin/hostapd /odm/bin/hostapd /bin/hostapd; do
  if [ -x "$b" ]; then
    echo "--- $b ---"
    "$b" -v 2>&1
    echo "HELP (check for -P support):"
    "$b" -h 2>&1
  fi
done
for b in /vendor/bin/dnsmasq /system/bin/dnsmasq /system/xbin/dnsmasq /vendor/bin/hw/dnsmasq /system_ext/bin/dnsmasq /odm/bin/dnsmasq /bin/dnsmasq; do
  if [ -x "$b" ]; then echo "--- $b ---"; ls -l "$b"; fi
done

section "WIRELESS CAPABILITIES"
if have iw; then
  iw dev 2>&1
  iw list 2>&1 | awk '/Supported interface modes:/{show=1} show{print} show && /Band [0-9]+:/{exit}'
else
  echo "iw not installed"
fi

section "KERNEL DRIVER AND MODULE HINTS"
if have dmesg; then dmesg 2>/dev/null | filter_wifi | last_lines 100; else echo "dmesg unavailable"; fi
if [ -d /sys/class/net/wlan0 ]; then ls -l /sys/class/net/wlan0/ 2>/dev/null; else echo "/sys/class/net/wlan0 missing"; fi
for d in /vendor/lib/modules /vendor_dlkm/lib/modules /odm/lib/modules; do
  if [ -d "$d" ]; then ls -l "$d" 2>/dev/null; fi
done

section "CURRENT SERVICE STATE"
if have svc; then svc wifi status 2>&1; else echo "svc unavailable"; fi
if have ps; then
  ps 2>/dev/null | awk '
    BEGIN { IGNORECASE=1 }
    /hostapd|dnsmasq|wpa_supplicant|wifi/ && !/awk/ { print }
  '
else echo "ps unavailable"; fi

section "DIPLAY RUNTIME FILES AND HOSTAPD LOGS"
for f in /data/local/tmp/diplay-root-hotspot/hostapd.log /data/local/tmp/diplay-root-hotspot/dnsmasq.log /data/local/tmp/diplay-root-hotspot/hostapd.pid /data/local/tmp/diplay-root-hotspot/hostapd.owner /data/local/tmp/diplay-root-hotspot/dnsmasq.pid /data/local/tmp/diplay-root-hotspot/dnsmasq.owner /data/local/tmp/hapd.log /data/local/tmp/hostapd.log /data/local/tmp/dns.log; do
  if [ -e "$f" ]; then
    ls -l "$f" 2>&1
    case "$f" in
      *.log) echo "--- last 160 lines: $f ---"; last_lines 160 < "$f" ;;
      *) echo "--- value: $f ---"; cat "$f" 2>&1 ;;
    esac
  else echo "MISSING $f"; fi
done

section "HOSTAPD CONFIG (SECRETS REDACTED)"
for f in /data/misc/wifi/hostapd.conf /data/local/tmp/diplay-root-hotspot/hostapd.stage.conf; do
  if [ -r "$f" ]; then
    ls -l "$f"
    awk '{
      key=$0; sub(/^[[:space:]]*/, "", key); split(key, a, "="); k=tolower(a[1]);
      if (k=="ssid" || k=="wpa_passphrase" || k=="wpa_psk" || k=="sae_password") sub(/=.*/, "=<REDACTED>");
      print
    }' "$f"
  else echo "MISSING OR UNREADABLE $f"; fi
done

section "KERNEL WIFI ERROR TAIL"
if have dmesg; then dmesg 2>/dev/null | filter_wifi | last_lines 160; else echo "dmesg unavailable"; fi

echo
echo "===== END (read-only; no settings changed) ====="
