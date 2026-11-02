#!/usr/bin/env bash
# HondaHack 7.6.0 reverse-engineering workstation bootstrap
# Supported target: Debian/Ubuntu Linux. This script installs analysis tools only.
# It does NOT execute, install, patch, or modify the target APK/device.
set -Eeuo pipefail

if [[ "${EUID}" -ne 0 ]]; then
  echo "Run with sudo: sudo bash scripts/setup-re-toolchain-debian.sh" >&2
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update

# Core utilities and Java/Python runtimes.
apt-get install -y --no-install-recommends \
  ca-certificates curl wget git unzip zip xz-utils file \
  openjdk-17-jdk-headless python3 python3-venv python3-pip pipx \
  binutils graphviz

# Debian/Ubuntu package names vary by release. Install available packages,
# and report missing optional tools instead of failing the whole bootstrap.
optional=(apktool jadx smali aapt apksigner zipalign radare2)
available=()
missing=()
for package in "${optional[@]}"; do
  if apt-cache show "$package" >/dev/null 2>&1; then
    available+=("$package")
  else
    missing+=("$package")
  fi
done
if (("${#available[@]}")); then
  apt-get install -y --no-install-recommends "${available[@]}"
fi

# Isolated Python environment for Androguard; avoids modifying system Python.
install_dir="${HONDAHACK_TOOLS_DIR:-/opt/hondahack-re-tools}"
python3 -m venv "$install_dir/venv"
"$install_dir/venv/bin/python" -m pip install --upgrade pip
"$install_dir/venv/bin/python" -m pip install 'androguard>=4.1,<5'

mkdir -p "$install_dir"
cat > "$install_dir/activate.sh" <<'ACTIVATE'
#!/usr/bin/env bash
source /opt/hondahack-re-tools/venv/bin/activate
ACTIVATE
chmod 755 "$install_dir/activate.sh"

echo
echo "=== Runtime versions ==="
java -version 2>&1 | head -n 1 || true
python3 --version
"$install_dir/venv/bin/python" -c 'import androguard; print("Androguard:", getattr(androguard, "__version__", "installed"))'
echo
echo "=== Tool availability ==="
for tool in jadx apktool smali aapt apksigner zipalign radare2 readelf objdump dot; do
  if command -v "$tool" >/dev/null 2>&1; then
    printf 'OK   %-12s %s\n' "$tool" "$(command -v "$tool")"
  else
    printf 'MISS %-12s\n' "$tool"
  fi
done
if (("${#missing[@]}")); then
  echo
  echo "Not available as distro packages on this release: ${missing[*]}"
  echo "Install these from their official upstream release pages after checking checksums."
fi

echo
echo "Bootstrap finished. This does not imply every optional tool is installed."
echo "Androguard venv: $install_dir/venv"
