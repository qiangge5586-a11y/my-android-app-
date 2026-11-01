#!/system/bin/sh
# Read-only diagnostics for the rooted 2019 Civic head unit.
# This script does not start/stop Wi-Fi, hostapd, dnsmasq, or change files.
set +e
section() { echo; echo "===== $1 ====="; }
section "ROOT AND ANDROID"
id
uname -a
for p in ro.product.manufacturer ro.product.model ro.board.platform ro.hardware ro.build.version.release ro.build.version.sdk; do
  printf '%s=' "$p"; getprop "$p"
done
printf 'SELinux='; getenforce 2>/dev/null

section "WLAN INTERFACE"
ip link show wlan0 2>&1
ip addr show wlan0 2>&1
ifconfig wlan0 2>&1
printf 'driver='
readlink -f /sys/class/net/wlan0/device/driver 2>/dev/null
printf 'device='
readlink -f /sys/class/net/wlan0/device 2>/dev/null

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
  iw list 2>&1 | sed -n '/Supported interface modes:/,/Band /p'
else
  echo "iw not installed"
fi

section "KERNEL DRIVER AND MODULE HINTS"
dmesg 2>/dev/null | grep -iE 'wlan|wifi|cfg80211|mac80211|hostapd|firmware' | tail -n 100
ls -l /sys/class/net/wlan0/ 2>/dev/null | head -n 40
ls -l /vendor/lib/modules /vendor_dlkm/lib/modules /odm/lib/modules 2>/dev/null

section "CURRENT SERVICE STATE"
svc wifi status 2>&1
ps -A 2>/dev/null | grep -E 'hostapd|dnsmasq|wpa_supplicant|wifi' | grep -v grep


section "DIPLAY RUNTIME FILES AND HOSTAPD LOGS"
for f in /data/local/tmp/diplay-root-hotspot/hostapd.log /data/local/tmp/diplay-root-hotspot/dnsmasq.log /data/local/tmp/diplay-root-hotspot/hostapd.pid /data/local/tmp/diplay-root-hotspot/hostapd.owner /data/local/tmp/hapd.log /data/local/tmp/hostapd.log /data/local/tmp/dns.log; do
  if [ -e "$f" ]; then
    ls -l "$f" 2>&1
    case "$f" in
      *.log) echo "--- tail: $f ---"; tail -n 160 "$f" 2>&1 ;;
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
dmesg 2>/dev/null | grep -iE 'nl80211|cfg80211|mac80211|wlan0|hostapd|firmware|AP-STA|failed|error' | tail -n 160

echo
echo "===== END (read-only; no settings changed) ====="
