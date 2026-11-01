#!/system/bin/sh
# Read-only diagnostics for the rooted 2019 Civic head unit.
# This script does not start/stop Wi-Fi, hostapd, dnsmasq, or change files.
set +e
have() { command -v "$1" >/dev/null 2>&1; }
tail_lines() {
  n="$1"
  awk -v n="$n" '{ a[NR % n]=$0 } END { first=NR>n?NR-n+1:1; for(i=first;i<=NR;i++) print a[i%n] }'
}
first_lines() { n="$1"; awk -v n="$n" 'NR<=n { print }'; }
resolve_link() {
  if have readlink; then readlink "$1" 2>/dev/null
  else echo "readlink unavailable: $1"
  fi
}
section() { echo; echo "===== $1 ====="; }
section "ROOT AND ANDROID"
id
uname -a
for p in ro.product.manufacturer ro.product.model ro.board.platform ro.hardware ro.build.version.release ro.build.version.sdk; do
  printf '%s=' "$p"; getprop "$p"
done
printf 'SELinux='; if have getenforce; then getenforce 2>/dev/null; else echo unavailable; fi

section "WLAN INTERFACE"
if have ip; then ip link show wlan0 2>&1; ip addr show wlan0 2>&1; else echo "ip utility unavailable"; fi
if have ifconfig; then ifconfig wlan0 2>&1; else echo "ifconfig unavailable"; fi
printf 'driver='; resolve_link /sys/class/net/wlan0/device/driver
printf 'device='; resolve_link /sys/class/net/wlan0/device

section "WIRELESS TOOLS"
for b in iw iwconfig hostapd dnsmasq ip ifconfig svc; do
  command -v "$b" 2>/dev/null
done
for b in /vendor/bin/hostapd /system/bin/hostapd /system_ext/bin/hostapd /odm/bin/hostapd /vendor/bin/hw/hostapd; do
  if [ -x "$b" ]; then
    echo "--- $b ---"
    "$b" -v 2>&1
    echo "HELP (full; check for -P support):"
    "$b" -h 2>&1
  fi
done
for b in /vendor/bin/dnsmasq /system/bin/dnsmasq /system_ext/bin/dnsmasq /odm/bin/dnsmasq; do
  if [ -x "$b" ]; then
    echo "--- $b ---"
    ls -l "$b"
  fi
done

section "WIRELESS CAPABILITIES"
if command -v iw >/dev/null 2>&1; then
  iw dev 2>&1
  iw list 2>&1 | awk 'BEGIN { active=0 } { low=tolower($0); if (low ~ /supported interface modes:/) active=1; if (active) print; if (active && low ~ /^[[:space:]]*band[[:space:]]/) exit }'
else
  echo "iw not installed"
fi

section "KERNEL DRIVER AND MODULE HINTS"
if have dmesg; then dmesg 2>/dev/null | awk '{ low=tolower($0); if (low ~ /(wlan|wifi|cfg80211|mac80211|hostapd|firmware)/) print }' | tail_lines 100; else echo "dmesg unavailable"; fi
if [ -d /sys/class/net/wlan0 ]; then ls -l /sys/class/net/wlan0/ 2>/dev/null | first_lines 40; else echo "wlan0 sysfs directory missing"; fi
ls -l /vendor/lib/modules /vendor_dlkm/lib/modules /odm/lib/modules 2>/dev/null || true

section "CURRENT SERVICE STATE"
if have svc; then svc wifi status 2>&1; else echo "svc unavailable"; fi
if have ps; then ps -A 2>/dev/null | awk '{ low=tolower($0); if (low ~ /(hostapd|dnsmasq|wpa_supplicant|wifi)/) print }'; else echo "ps unavailable"; fi


section "DIPLAY RUNTIME FILES AND HOSTAPD LOGS"
for f in /data/local/tmp/diplay-root-hotspot/hostapd.log /data/local/tmp/diplay-root-hotspot/dnsmasq.log /data/local/tmp/diplay-root-hotspot/hostapd.pid /data/local/tmp/diplay-root-hotspot/hostapd.owner /data/local/tmp/hapd.log /data/local/tmp/hostapd.log /data/local/tmp/dns.log; do
  if [ -e "$f" ]; then
    ls -l "$f" 2>&1
    case "$f" in
      *.log) echo "--- last lines: $f ---"; tail_lines 160 < "$f" 2>&1 ;;
      *) echo "--- value: $f ---"; cat "$f" 2>&1 ;;
    esac
  else
    echo "MISSING $f"
  fi
done

section "HOSTAPD CONFIG (SECRETS REDACTED)"
for f in /data/misc/wifi/hostapd.conf /data/local/tmp/diplay-root-hotspot/hostapd.stage.conf; do
  if [ -r "$f" ]; then
    ls -l "$f"
    awk '{ key=$0; sub(/^[[:space:]]*/, "", key); split(key, a, "="); k=tolower(a[1]); if (k=="ssid" || k=="wpa_passphrase" || k=="wpa_psk" || k=="sae_password") sub(/=.*/, "=<REDACTED>"); print }' "$f"
  else
    echo "MISSING OR UNREADABLE $f"
  fi
done

section "KERNEL WIFI ERROR TAIL"
if have dmesg; then dmesg 2>/dev/null | awk '{ low=tolower($0); if (low ~ /(nl80211|cfg80211|mac80211|wlan0|hostapd|firmware|ap-sta|failed|error)/) print }' | tail_lines 160; else echo "dmesg unavailable"; fi

echo
echo "===== END (read-only; no settings changed) ====="
