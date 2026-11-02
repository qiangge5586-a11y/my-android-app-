# HondaHack 7.6.0 — Cloud Analysis Host

## Stage objective

Prepare a persistent, isolated Linux workstation for static reverse engineering. This stage does **not** provision a cloud account/VM, access a user's cloud provider, execute the APK, or claim that the analysis toolchain has been verified on a live host.

## Recommended VM

- Ubuntu Server 24.04 LTS, x86_64
- 4 vCPU minimum; 8 vCPU preferred for Ghidra
- 16 GB RAM minimum; 32 GB preferred
- 100 GB SSD minimum; increase storage before retaining full decompilation exports
- Restrict SSH ingress to the operator's IP using the cloud provider firewall
- Use SSH keys; disable password login in the provider's image/console configuration
- Do not expose analysis services or Jupyter ports to the public Internet

The APK under review is an ARMv7 sample. An x86_64 host is suitable for static analysis; do not run the sample as a normal application.

## Before provisioning

1. Create an Ubuntu 24.04 VM in the cloud provider console.
2. Attach a dedicated SSH key and restrict inbound SSH to a trusted source IP.
3. Add a **read-only deploy key** for `qiangge5586-a11y/my-android-app-` to the VM's SSH agent or `~/.ssh`. Never paste a private key into chat or commit it.
4. Connect over SSH and verify the host identity and disk capacity.

## Bootstrap

From a trusted copy of this repository, or after obtaining the deploy key on the VM:

```bash
sudo install -d -m 0755 /opt/hondahack-workspace
sudo REPO_SSH_URL='git@github.com:qiangge5586-a11y/my-android-app-.git' \
  bash reverse-engineering/hondahack-7.6.0/scripts/cloud-server-bootstrap.sh
```

If the script is not yet present locally, clone the repository first using the configured SSH deploy key, then run the script from its repository path.

The bootstrap calls the existing Debian/Ubuntu toolchain installer and records a host inventory. Optional packages vary by Ubuntu repository. A missing tool must remain explicitly marked missing; do not infer successful installation from the script's exit status alone.

## Verify and record

After bootstrap, capture:

```bash
cat /opt/hondahack-workspace/host-inventory.txt
java -version
python3 --version
command -v jadx || true
command -v apktool || true
command -v aapt || true
command -v apksigner || true
command -v ghidra || true
```

Record exact tool versions and checksums in the Stage 03 evidence ledger. JADX, Apktool, and Ghidra may require separately verified upstream releases if not available in the distribution repositories. Do not install arbitrary binaries from unverified mirrors.

## Sample handling

- Verify the original APK SHA-256 against the Stage 03 runbook before analysis.
- Keep the original sample read-only; analyze a working copy.
- Store samples and generated artifacts under access-restricted directories.
- Do not commit the APK, extracted personal data, tokens, keys, auth codes, or device identifiers.
- Never execute `factory_reset.sh`, USB recovery scripts, APK components, or native libraries on a real head unit.
- Preserve logs, tool versions, hashes, and class/method evidence.

## Stage gate

Cloud-host preparation is complete only after a real VM has been provisioned and the operator supplies a redacted tool-version report and host inventory. Stage 03 DEX analysis remains blocked until the original APK is available on that host and JADX/Apktool exports have actually been produced and reviewed.

## Current limitation

This repository automation cannot create or pay for a cloud VM without an authorized cloud-provider connection. The operator must provision the VM and install the repository deploy key. Never share cloud passwords, private SSH keys, or access tokens in this project issue or chat.
