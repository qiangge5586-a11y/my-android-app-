#!/usr/bin/env python3
from __future__ import annotations
import json
import re
import sys
from pathlib import Path

if len(sys.argv) != 2:
    raise SystemExit("usage: patch_diplay_hotspot.py <apktool-smali-root>")
ROOT = Path(sys.argv[1]).resolve()
CFG = ROOT / "smali_classes3/com/shilapi/xcertplay/network/HostapdConfig.smali"
MGR = ROOT / "smali_classes3/com/shilapi/xcertplay/network/HostapdHotspotManager.smali"
BASE = "/data/local/tmp/diplay-root-hotspot"
for p in (CFG, MGR):
    if not p.is_file():
        raise SystemExit(f"missing target smali: {p}")


def smali_string(value: str) -> str:
    return json.dumps(value, ensure_ascii=True).replace("'", "\\'")


def replace_method_const(path: Path, method: str, old_value: str, new_value: str) -> None:
    text = path.read_text(encoding="utf-8")
    start = text.find(f".method public final {method}()Ljava/lang/String;")
    if start < 0:
        raise SystemExit(f"{path.name}: method {method} not found")
    end = text.find(".end method", start)
    if end < 0:
        raise SystemExit(f"{path.name}: unterminated method {method}")
    chunk = text[start:end]
    old = smali_string(old_value)
    new = smali_string(new_value)
    count = chunk.count(old)
    if count != 1:
        raise SystemExit(f"{path.name}:{method}: expected one target literal, got {count}")
    chunk = chunk.replace(old, new, 1)
    path.write_text(text[:start] + chunk + text[end:], encoding="utf-8")



def replace_method_literal(path: Path, signature: str, old_line: str, new_line: str, label: str) -> None:
    source = path.read_text(encoding="utf-8")
    start = source.find(signature)
    if start < 0:
        raise SystemExit(f"{path.name}: method {label} not found")
    end = source.find(".end method", start)
    if end < 0:
        raise SystemExit(f"{path.name}: unterminated method {label}")
    chunk = source[start:end]
    count = chunk.count(old_line)
    if count != 1:
        raise SystemExit(f"{path.name}:{label}: expected one target line, got {count}")
    chunk = chunk.replace(old_line, new_line, 1)
    path.write_text(source[:start] + chunk + source[end:], encoding="utf-8")

def replace_binary_path(path: Path, binary: str, candidates: list[str]) -> None:
    """Replace a fixed executable path inside Smali string constants with a POSIX shell probe."""
    source = path.read_text(encoding="utf-8")
    lines = source.splitlines(keepends=True)
    hits = 0
    probe = '$(for b in ' + " ".join(candidates) + '; do [ -x "$b" ] && { echo "$b"; break; }; done)'
    for i, line in enumerate(lines):
        if "const-string" not in line:
            continue
        try:
            prefix, raw = line.split(", ", 1)
            value = json.loads(raw.strip().replace("\\\\'", "'"))
        except Exception:
            continue
        if f"/system/bin/{binary}" not in value:
            continue
        value = value.replace(f"/system/bin/{binary}", probe)
        lines[i] = prefix + ", " + smali_string(value) + "\\n"
        hits += 1
    if hits < 1:
        raise SystemExit(f"{path.name}: no fixed /system/bin/{binary} executable literal found")
    path.write_text("".join(lines), encoding="utf-8")
    print(f"{path.name}: enabled {binary} multi-path discovery in {hits} string(s)")


def replace_const_fragment(path: Path, old: str, new: str, label: str) -> None:
    lines = path.read_text(encoding="utf-8").splitlines(keepends=True)
    hits = []
    for i, line in enumerate(lines):
        if "const-string" not in line or old not in line:
            continue
        prefix, raw = line.split(", ", 1)
        raw = raw.strip()
        try:
            value = json.loads(raw.replace("\\'", "'"))
        except Exception as exc:
            raise SystemExit(f"{path.name}: cannot decode smali literal for {label}: {exc}")
        if old in value:
            hits.append((i, prefix, value))
    if len(hits) != 1:
        raise SystemExit(f"{path.name}: {label}: expected one const-string fragment, got {len(hits)}")
    i, prefix, value = hits[0]
    value = value.replace(old, new, 1)
    lines[i] = prefix + ", " + smali_string(value) + "\n"
    path.write_text("".join(lines), encoding="utf-8")


# Namespaced runtime paths avoid collisions with unrelated system services.
# Keep the known-working shared vendor hostapd.conf path, but protect it.
PATHS = {
    "/data/local/tmp/dnsmasq.pid": f"{BASE}/dnsmasq.pid",
    "/data/local/tmp/dnsmasq.leases": f"{BASE}/dnsmasq.leases",
    "/data/local/tmp/dns.log": f"{BASE}/dnsmasq.log",
    "/data/local/tmp/hapd.log": f"{BASE}/hostapd.log",
    "/data/local/tmp/hostapd_full.conf": f"{BASE}/hostapd.stage.conf",
}
for path in (CFG, MGR):
    text = path.read_text(encoding="utf-8")
    for old, new in PATHS.items():
        text = text.replace(old, new)
    path.write_text(text, encoding="utf-8")

# Resolve firmware-specific binary locations without assuming /system/bin.
replace_binary_path(CFG, "hostapd", [
    "/system/bin/hostapd", "/system/xbin/hostapd", "/vendor/bin/hostapd",
    "/vendor/bin/hw/hostapd", "/system_ext/bin/hostapd", "/odm/bin/hostapd", "/bin/hostapd",
])
replace_binary_path(CFG, "dnsmasq", [
    "/system/bin/dnsmasq", "/system/xbin/dnsmasq", "/vendor/bin/dnsmasq",
    "/vendor/bin/hw/dnsmasq", "/system_ext/bin/dnsmasq", "/odm/bin/dnsmasq", "/bin/dnsmasq",
])

# Readiness must only count the hostapd process registered by this DiPlay session.
replace_method_literal(MGR, ".method private final hostapdProcessCount()I", "    const-string/jumbo v1, \"ps\"", "    const-string/jumbo v1, \"d=/data/local/tmp/diplay-root-hotspot; owner=\\\"$d/hostapd.owner\\\"; [ -r \\\"$owner\\\" ] || exit 0; read p st osid < \\\"$owner\\\"; case \\\"$p:$st\\\" in *[!0-9:]*|'':*) exit 0;; esac; [ \\\"$p\\\" = \\\"$(cat \\\"$d/hostapd.pid\\\" 2>/dev/null)\\\" ] || exit 0; [ -n \\\"$osid\\\" ] && [ \\\"$osid\\\" = \\\"$(cat \\\"$d/session.id\\\" 2>/dev/null)\\\" ] || exit 0; [ -r \\\"/proc/$p/stat\\\" ] || exit 0; now=$(awk '{print $22}' \\\"/proc/$p/stat\\\" 2>/dev/null); comm=$(cat \\\"/proc/$p/comm\\\" 2>/dev/null); exe=$(readlink \\\"/proc/$p/exe\\\" 2>/dev/null); exe=${exe##*/}; [ \\\"$now\\\" = \\\"$st\\\" ] && [ \\\"$comm\\\" = hostapd ] && [ \\\"$exe\\\" = hostapd ] && echo hostapd\"", "owned hostapd readiness probe")
probe_start = MGR.read_text(encoding="utf-8").find(".method private final hostapdProcessCount()I")
probe_end = MGR.read_text(encoding="utf-8").find(".end method", probe_start)
probe = MGR.read_text(encoding="utf-8")[probe_start:probe_end]
if '"ps"' in probe:
    raise SystemExit("global hostapd process scan remains in readiness probe")


kill_script = r'''set -f; d=/data/local/tmp/diplay-root-hotspot; f=/data/misc/wifi/hostapd.conf; sid=$(cat "$d/session.id" 2>/dev/null); stop_owned(){ owner="$1"; name="$2"; pidfile="$3"; [ -r "$owner" ] || return 0; read pid start owner_sid < "$owner"; case "$pid" in ''|*[!0-9]*) return 0;; esac; case "$start" in ''|*[!0-9]*) return 0;; esac; [ -n "$sid" ] && [ "$owner_sid" = "$sid" ] || return 0; [ "$(cat "$pidfile" 2>/dev/null)" = "$pid" ] || { rm -f "$owner" "$pidfile"; return 0; }; proc="/proc/$pid"; if [ ! -r "$proc/stat" ]; then rm -f "$owner" "$pidfile"; return 0; fi; now=$(awk '{print $22}' "$proc/stat" 2>/dev/null); comm=$(cat "$proc/comm" 2>/dev/null); exe=$(readlink "$proc/exe" 2>/dev/null); exe=${exe##*/}; if [ "$now" != "$start" ] || [ "$comm" != "$name" ] || [ "$exe" != "$name" ]; then rm -f "$owner" "$pidfile"; return 0; fi; kill -TERM "$pid" 2>/dev/null; n=0; while [ "$n" -lt 10 ]; do [ -r "$proc/stat" ] || break; now=$(awk '{print $22}' "$proc/stat" 2>/dev/null); comm=$(cat "$proc/comm" 2>/dev/null); exe=$(readlink "$proc/exe" 2>/dev/null); exe=${exe##*/}; [ "$now" = "$start" ] && [ "$comm" = "$name" ] && [ "$exe" = "$name" ] || break; sleep 0.2; n=$((n+1)); done; if [ -r "$proc/stat" ]; then now=$(awk '{print $22}' "$proc/stat" 2>/dev/null); comm=$(cat "$proc/comm" 2>/dev/null); exe=$(readlink "$proc/exe" 2>/dev/null); exe=${exe##*/}; if [ "$now" = "$start" ] && [ "$comm" = "$name" ] && [ "$exe" = "$name" ]; then kill -KILL "$pid" 2>/dev/null; fi; fi; rm -f "$owner" "$pidfile"; }; if [ -n "$sid" ]; then stop_owned "$d/hostapd.owner" hostapd "$d/hostapd.pid"; stop_owned "$d/dnsmasq.owner" dnsmasq "$d/dnsmasq.pid"; if [ -r "$d/hostapd.written.sha256" ] && [ -r "$d/hostapd.backup.state" ]; then expected=$(cat "$d/hostapd.written.sha256"); actual=$(sha256sum "$f" 2>/dev/null | awk '{print $1}'); state=$(cat "$d/hostapd.backup.state"); if [ -n "$expected" ] && [ "$actual" = "$expected" ]; then case "$state" in existed) [ -r "$d/hostapd.conf.backup" ] && cp -p "$d/hostapd.conf.backup" "$f" || exit 1;; missing) rm -f "$f";; *) exit 1;; esac; rm -f "$d/hostapd.written.sha256" "$d/hostapd.backup.state" "$d/hostapd.conf.backup"; else echo "DiPlay: shared hostapd.conf changed externally; preserving it and the backup" >&2; fi; fi; rm -f "$d/session.id"; rmdir "$d/lock" 2>/dev/null || true; else if [ -d "$d/lock" ] && [ ! -e "$d/session.id" ] && [ ! -e "$d/hostapd.backup.state" ] && [ ! -e "$d/hostapd.owner" ] && [ ! -e "$d/dnsmasq.owner" ]; then rmdir "$d/lock" 2>/dev/null || true; fi; fi; sleep 1'''
old_kill = 'set -f; for name in hostapd dnsmasq; do ps | while read line; do set -- $line; hit=0; for f in "$@"; do [ "${f##*/}" = "$name" ] && hit=1; done; [ $hit = 1 ] || continue; case "$2" in \'\'|*[!0-9]*) continue;; esac; kill -9 $2 2>/dev/null; done; done; sleep 1; rm -f /data/local/tmp/diplay-root-hotspot/dnsmasq.leases /data/local/tmp/diplay-root-hotspot/dnsmasq.pid'
replace_method_const(CFG, "killStaleScript", old_kill, kill_script)

old_wpa = 'set -f; ps | while read line; do set -- $line; hit=0; for f in "$@"; do [ "${f##*/}" = "wpa_supplicant" ] && hit=1; done; [ $hit = 1 ] || continue; case "$2" in \'\'|*[!0-9]*) continue;; esac; kill -9 $2 2>/dev/null; done'
replace_method_const(CFG, "killWpaSupplicantScript", old_wpa, ":")

old_cleanup = f"rm -f /data/misc/wifi/hostapd.conf {BASE}/hostapd.stage.conf {BASE}/hostapd.log {BASE}/dnsmasq.log {BASE}/dnsmasq.leases {BASE}/dnsmasq.pid"
new_cleanup = f"rm -f {BASE}/hostapd.stage.conf {BASE}/hostapd.log {BASE}/dnsmasq.log {BASE}/dnsmasq.leases {BASE}/dnsmasq.pid {BASE}/hostapd.pid {BASE}/hostapd.owner {BASE}/dnsmasq.owner; rmdir {BASE} 2>/dev/null || true"
replace_method_const(CFG, "removeGeneratedFilesScript", old_cleanup, new_cleanup)

old_prefix = "mkdir -p /data/misc/wifi; cp "
new_prefix = f"mkdir -p {BASE} /data/misc/wifi && [ ! -e {BASE}/hostapd.backup.state ] || {{ echo 'unresolved previous DiPlay hostapd backup' >&2; exit 1; }}; mkdir {BASE}/lock || exit 1; sid=$(date +%s).$$; printf '%s\\n' \"$sid\" > {BASE}/session.id; if [ -e /data/misc/wifi/hostapd.conf ]; then cp -p /data/misc/wifi/hostapd.conf {BASE}/hostapd.conf.backup && echo existed > {BASE}/hostapd.backup.state; else echo missing > {BASE}/hostapd.backup.state; fi && cp "
replace_const_fragment(MGR, old_prefix, new_prefix, "shared config backup prefix")

old_suffix = f" {BASE}/hostapd.stage.conf && cp {BASE}/hostapd.stage.conf /data/misc/wifi/hostapd.conf"
new_suffix = f''' {BASE}/hostapd.stage.conf && cp {BASE}/hostapd.stage.conf /data/misc/wifi/hostapd.conf && sha256sum /data/misc/wifi/hostapd.conf | awk '{{print $1}}' > {BASE}/hostapd.written.sha256 || {{ state=$(cat {BASE}/hostapd.backup.state 2>/dev/null); if [ "$state" = existed ]; then cp -p {BASE}/hostapd.conf.backup /data/misc/wifi/hostapd.conf; else rm -f /data/misc/wifi/hostapd.conf; fi; rm -f {BASE}/hostapd.backup.state {BASE}/hostapd.conf.backup {BASE}/hostapd.written.sha256 {BASE}/session.id; rmdir {BASE}/lock 2>/dev/null || true; exit 1; }}'''
replace_const_fragment(MGR, old_suffix, new_suffix, "shared config hash suffix")

old_launch = f" -B /data/misc/wifi/hostapd.conf >{BASE}/hostapd.log 2>&1 & echo $!; sleep 1"
new_launch = f''' /data/misc/wifi/hostapd.conf >{BASE}/hostapd.log 2>&1 & p=$!; sleep 1; if [ ! -r /proc/$p/stat ]; then wait "$p"; rc=$?; echo "hostapd exited before readiness (status=$rc)" >&2; awk '{{ line[NR % 80]=$0 }} END {{ first=NR>80?NR-79:1; for(i=first;i<=NR;i++) print line[i%80] }}' {BASE}/hostapd.log 2>&1; exit 1; fi; st=$(awk '{{print $22}}' /proc/$p/stat 2>/dev/null); comm=$(cat /proc/$p/comm 2>/dev/null); exe=$(readlink /proc/$p/exe 2>/dev/null); exe=${{exe##*/}}; if [ -z "$st" ] || [ "$comm" != hostapd ] || [ "$exe" != hostapd ]; then echo "hostapd process identity check failed" >&2; awk '{{ line[NR % 80]=$0 }} END {{ first=NR>80?NR-79:1; for(i=first;i<=NR;i++) print line[i%80] }}' {BASE}/hostapd.log 2>&1; exit 1; fi; sid=$(cat {BASE}/session.id 2>/dev/null); [ -n "$sid" ] || {{ kill -TERM "$p" 2>/dev/null; exit 1; }}; printf '%s\\n' "$p" > {BASE}/hostapd.pid && printf '%s %s %s\\n' "$p" "$st" "$sid" > {BASE}/hostapd.owner || {{ kill -TERM "$p" 2>/dev/null; rm -f {BASE}/hostapd.pid {BASE}/hostapd.owner; exit 1; }}'''
replace_const_fragment(MGR, old_launch, new_launch, "hostapd PID ownership registration")

text = CFG.read_text(encoding="utf-8")
pat = re.compile(r'(const-string v1, )(" --pid-file=' + re.escape(BASE) + r'/dnsmasq\.pid[^\n]*?2>&1 &")')
match = pat.search(text)
if not match:
    raise SystemExit("HostapdConfig: dnsmasq launch command not found after path namespacing")
old_lit = json.loads(match.group(2))
new_lit = old_lit + f''' sleep 1; p=$(cat {BASE}/dnsmasq.pid 2>/dev/null); case "$p" in ''|*[!0-9]*) exit 1;; esac; st=$(awk '{{print $22}}' /proc/$p/stat 2>/dev/null); comm=$(cat /proc/$p/comm 2>/dev/null); exe=$(readlink /proc/$p/exe 2>/dev/null); exe=${{exe##*/}}; [ -n "$st" ] && [ "$comm" = dnsmasq ] && [ "$exe" = dnsmasq ] || exit 1; sid=$(cat {BASE}/session.id 2>/dev/null); [ -n "$sid" ] || exit 1; printf '%s %s %s\\n' "$p" "$st" "$sid" > {BASE}/dnsmasq.owner'''
text = text[:match.start(2)] + smali_string(new_lit) + text[match.end(2):]
CFG.write_text(text, encoding="utf-8")

# Capture the framework Wi-Fi state before marking the system as touched.
# Work inside startHotspot only; register allocation can differ between builds.
manager_text = MGR.read_text(encoding="utf-8")
method_start = manager_text.find(".method private final startHotspot(")
if method_start < 0:
    raise SystemExit("startHotspot method not found")
method_end = manager_text.find(".end method", method_start)
if method_end < 0:
    raise SystemExit("startHotspot method is unterminated")
method = manager_text[method_start:method_end]
# Locate the three Smali instructions by method-local line content, not regex escaping.
method_lines = method.splitlines(keepends=True)
invoke_hits = [i for i, line in enumerate(method_lines) if "->readFrameworkWifiOn()Z" in line and "invoke-direct" in line]
if len(invoke_hits) != 1:
    raise SystemExit(f"expected one readFrameworkWifiOn invoke in startHotspot, got {len(invoke_hits)}")
invoke_i = invoke_hits[0]
invoke_line = method_lines[invoke_i]
try:
    receiver = invoke_line.split("{", 1)[1].split("}", 1)[0].strip()
except IndexError:
    raise SystemExit("could not parse readFrameworkWifiOn receiver register")
move_hits = [i for i in range(invoke_i + 1, min(invoke_i + 12, len(method_lines)))
             if method_lines[i].strip().startswith("move-result ")]
if len(move_hits) != 1:
    raise SystemExit(f"expected one move-result after Wi-Fi state read, got {len(move_hits)}")
move_i = move_hits[0]
result_reg = method_lines[move_i].strip().split(None, 1)[1]
if result_reg not in ("v" + str(n) for n in range(100)) and not result_reg.startswith("p"):
    raise SystemExit("unexpected move-result register")
save_hits = [i for i in range(move_i + 1, min(move_i + 12, len(method_lines)))
             if "->wifiWasOn:Z" in method_lines[i] and "iput-boolean" in method_lines[i]]
if len(save_hits) != 1:
    raise SystemExit(f"expected one wifiWasOn store after state read, got {len(save_hits)}")
save_i = save_hits[0]
save_parts = method_lines[save_i].strip().split()
if len(save_parts) < 4 or save_parts[1].rstrip(",") != result_reg or save_parts[2].rstrip(",") != receiver:
    raise SystemExit(f"Wi-Fi state read/store register relationship did not match: receiver={receiver!r}, result={result_reg!r}, store={method_lines[save_i].strip()!r}")
read_block = method_lines[invoke_i:save_i + 1]
del method_lines[invoke_i:save_i + 1]
touched_hits = [i for i, line in enumerate(method_lines)
                if "->touchedSystem:Z" in line and "iput-boolean" in line]
if len(touched_hits) != 1:
    raise SystemExit(f"expected one touchedSystem activation in startHotspot, got {len(touched_hits)}")
method_lines[touched_hits[0]:touched_hits[0]] = read_block
method = "".join(method_lines)
# Validate the final method itself, then persist the modified Smali file.
final_lines = method.splitlines()
final_read = [i for i, line in enumerate(final_lines)
              if "invoke-direct" in line and "->readFrameworkWifiOn()Z" in line]
final_touch = [i for i, line in enumerate(final_lines)
               if "iput-boolean" in line and "->touchedSystem:Z" in line]
if len(final_read) != 1 or len(final_touch) != 1:
    raise SystemExit(f"final Wi-Fi ordering anchors invalid: read={final_read}, touched={final_touch}")
if final_read[0] >= final_touch[0]:
    raise SystemExit(f"Wi-Fi state capture still occurs after touchedSystem: read_line={final_read[0]}, touched_line={final_touch[0]}")
manager_text = manager_text[:method_start] + method + manager_text[method_end:]
MGR.write_text(manager_text, encoding="utf-8")


for p in (CFG, MGR):
    t = p.read_text(encoding="utf-8")
    for bad in ("kill -9 $2", "ps | while read line", "killall hostapd", "pkill hostapd"):
        if bad in t:
            raise SystemExit(f"unsafe broad process kill remains in {p.name}: {bad}")
if "rm -f /data/misc/wifi/hostapd.conf" in CFG.read_text(encoding="utf-8"):
    raise SystemExit("shared hostapd.conf is still unconditionally deleted")
for required in ("hw_mode=g", "interface=wlan0", "192.168.42.1", "192.168.42.10,192.168.42.50", "wpa=2", "rsn_pairwise=CCMP"):
    if required not in CFG.read_text(encoding="utf-8"):
        raise SystemExit(f"compatibility invariant missing: {required}")
print("Smali patch applied successfully")
print("- namespaced PID/log/lease/staged config paths")
print("- per-run session token and PID + proc start-time ownership")
print("- bounded TERM then identity-checked KILL for owned hostapd/dnsmasq only")
print("- global wpa_supplicant kill disabled")
print("- shared hostapd.conf backup and hash-guarded restore")
print("- original 2.4 GHz / IP / DHCP / WPA2 settings preserved")
