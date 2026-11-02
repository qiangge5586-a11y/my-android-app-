# Stage 03 — Operator Runbook (Debian/Ubuntu)

This runbook is for preparing a disposable analysis workstation and collecting static DEX outputs. It does not execute the APK.

## 1. Prepare the workstation

From the repository root:

```bash
sudo bash reverse-engineering/hondahack-7.6.0/scripts/setup-re-toolchain-debian.sh
```

The bootstrap script installs packages available from the distribution repositories and creates an isolated Python virtual environment for Androguard. Some Android tools may not be packaged for the installed distribution; check the script output rather than assuming they are present.

## 2. Verify tool availability

```bash
java -version
jadx --version
apktool --version
smali --version
aapt version
apksigner version
```

A missing optional command must be recorded as missing. Do not substitute an unrelated binary with the same command name.

## 3. Verify the sample identity

Set the path to the original APK supplied for this investigation:

```bash
APK="/path/to/本田黑科技7.6.0_可联网_去注销_任意.apk"
sha256sum "$APK"
stat -c '%n %s bytes' "$APK"
```

Expected SHA-256:

```
e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe
```

Stop if the hash differs. Do not combine outputs from different APK builds.

## 4. Create a clean output directory

```bash
OUT="$PWD/reverse-engineering/hondahack-7.6.0/artifacts/stage03"
mkdir -p "$OUT"/{jadx,smali,logs,inventory}
```

Keep generated outputs outside the source APK and never overwrite the original sample.

## 5. Static DEX export

Only after the tool versions and sample hash have been recorded:

```bash
jadx --show-bad-code --deobf -d "$OUT/jadx" "$APK" 2>&1 | tee "$OUT/logs/jadx.log"
```

If Apktool and Smali are available:

```bash
apktool d -f -o "$OUT/smali" "$APK" 2>&1 | tee "$OUT/logs/apktool.log"
```

Do not run any extracted shell script, APK component, or native library.

## 6. Evidence to preserve

Commit or archive:
- APK hash and size record (do not commit the APK itself).
- Tool versions and operating system details.
- JADX and Apktool logs.
- Decompiled source and Smali exports, subject to repository size and licensing constraints.
- Class/method inventory and call graph.
- A findings ledger linking each claim to class, method, and source location.

Do not commit authentication tokens, private keys, device identifiers, user data, or any authorization secrets discovered in the sample. Redact secrets while preserving the fact and location of the finding.

## 7. Completion gate

Stage 03 is not complete until the exports and evidence ledger exist and have been reviewed. A successful tool installation alone is not completion.

Potentially destructive factory-reset and USB recovery paths remain out of scope for execution. Their callers must be identified statically before any separately approved isolated testing.
