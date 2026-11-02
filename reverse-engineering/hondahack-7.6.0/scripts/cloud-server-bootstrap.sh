#!/usr/bin/env bash
# Provision the HondaHack analysis workspace on a fresh Ubuntu 24.04 cloud VM.
# Run as root after configuring a GitHub SSH deploy key for this repository.
# This script prepares a workstation; it does not execute the APK or extracted code.
set -Eeuo pipefail

if [[ "${EUID}" -ne 0 ]]; then
  echo "Run as root: sudo bash cloud-server-bootstrap.sh" >&2
  exit 1
fi

if [[ -r /etc/os-release ]]; then
  . /etc/os-release
else
  echo "Cannot identify operating system." >&2
  exit 1
fi
if [[ "${ID:-}" != "ubuntu" || "${VERSION_ID:-}" != "24.04" ]]; then
  echo "This bootstrap is tested by design for Ubuntu 24.04; found ${PRETTY_NAME:-unknown}." >&2
  exit 1
fi

REPO_SSH_URL="${REPO_SSH_URL:-git@github.com:qiangge5586-a11y/my-android-app-.git}"
WORKSPACE="/opt/hondahack-workspace"
REPO_DIR="$WORKSPACE/my-android-app-"

apt-get update
apt-get install -y --no-install-recommends git openssh-client ca-certificates

install -d -m 0755 "$WORKSPACE"
if [[ ! -d "$REPO_DIR/.git" ]]; then
  git clone "$REPO_SSH_URL" "$REPO_DIR"
else
  git -C "$REPO_DIR" pull --ff-only
fi

SETUP="$REPO_DIR/reverse-engineering/hondahack-7.6.0/scripts/setup-re-toolchain-debian.sh"
if [[ ! -f "$SETUP" ]]; then
  echo "Expected toolchain script not found: $SETUP" >&2
  exit 1
fi

bash "$SETUP"

install -d -m 0750 "$WORKSPACE"/{samples,artifacts,logs}
cat > "$WORKSPACE/host-inventory.txt" <<EOF
Provisioned at: $(date -u +%FT%TZ)
OS: $PRETTY_NAME
Kernel: $(uname -r)
Architecture: $(uname -m)
CPU: $(nproc) vCPU
Memory:
$(free -h)
Disk:
$(df -h "$WORKSPACE")
Repository: $REPO_SSH_URL
EOF
chmod 0640 "$WORKSPACE/host-inventory.txt"

echo
echo "Workspace prepared at: $WORKSPACE"
echo "Host inventory: $WORKSPACE/host-inventory.txt"
echo "Next: verify tool versions, then upload the original APK through a secure channel."
echo "Do not execute extracted scripts, APK components, or native libraries."
