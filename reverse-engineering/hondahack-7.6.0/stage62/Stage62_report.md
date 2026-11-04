# Stage 62 — Reproducible Android build gate

## Goal
Add a dedicated GitHub Actions build for the safe Android reconstruction scaffold without changing the existing DiPlay build workflow.

## Build configuration
- Android Gradle Plugin: 8.7.3
- Gradle: 8.9
- JDK: 17
- compileSdk: 35
- minSdk: 17
- targetSdk: 28
- Output: debug APK artifact only

## Changes
- Added `.github/workflows/hondahack-safe-rebuild.yml`.
- Workflow triggers on PR changes to the Stage 09 reconstruction project and supports manual dispatch.
- Installs Android platform 35 and build-tools 35.0.0.
- Runs `assembleDebug` for the isolated HondaHack-Rebuild project.
- Uploads `app-debug.apk` as a GitHub Actions artifact.

## Verification status
- Workflow source committed to the existing Stage 61 PR branch.
- The workflow is configured to run on the PR update. A successful APK build is not claimed until the GitHub Actions run completes.
- No local APK was built in this environment because Android SDK/Gradle are not installed.

## Scope and safety
This builds only the safe reconstruction scaffold. It does not import or execute the original APK, Root/Xposed code, vehicle commands, factory-reset scripts, partition writes, licensing bypass, or privileged repair operations.
