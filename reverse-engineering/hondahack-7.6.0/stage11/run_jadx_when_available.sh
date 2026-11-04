#!/usr/bin/env bash
set -euo pipefail
# Install JADX from https://github.com/skylot/jadx/releases
ROOT="$(cd "$(dirname "$0")" && pwd)"
JADX_BIN="${JADX_BIN:-jadx}"
command -v "$JADX_BIN" >/dev/null 2>&1 || { echo "JADX not found; install it and set JADX_BIN" >&2; exit 2; }
mkdir -p "$ROOT/decompiled_java"
"$JADX_BIN" --show-bad-code -d "$ROOT/decompiled_java" "$ROOT/../../../../inventory/classes.dex"
echo "Review decompilation errors and compare with Stage 05 method inventory."
