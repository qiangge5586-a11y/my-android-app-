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
    "$b" -h 2>&1 | head -n 35
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

echo
echo "===== END (read-only; no settings changed) ====="
