# Stage 65 — Static safety and feature regression gate

## Changes
- Added a Python standard-library verification script for manifest XML, launcher activity export, declared permissions, required screens, safe preferences, and public app-launch API usage.
- Added a privileged-execution pattern guard for process spawning, su, and protected Settings writes.
- Integrated the check into GitHub Actions before Android compilation.
- Corrected the app-catalog count preference so it actually controls the count label.
- Changed the app theme to Holo Light to retain compatibility with the project's minSdk 17.

## Checks
The script requires Python 3.12 and has no third-party dependencies. It exits non-zero on a failed assertion. The CI workflow then builds the debug APK and checks that the APK output exists before uploading it.

## Verification status
The script is committed; the new GitHub Actions run must complete to establish the current branch's pass/fail state. Static checks do not replace device testing.
