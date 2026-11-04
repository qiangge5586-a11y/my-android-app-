# Stage 81 — CI summary formatting correction and APK hash record

The Stage 80 run successfully built and uploaded the APK, but shell backticks in the summary echo lines were interpreted as command substitution. This did not fail the build or artifact upload, but the summary text was malformed.

Changes:
- Removed shell-special backticks from the GitHub Actions summary text.
- Retained package ID and minSdk verification.
- Recorded the successful run's APK SHA-256 in the handoff document.

Verified artifact from run 41692507933:
- APK SHA-256: ebd9bbf6d35ea1ae5c838aebc7a0a5d1c75b2bb39f11c9fe5fe58ef628749c69
- Package ID: cn.autohack.hondahack.rebuild
- minSdk: 17
- Artifact ZIP: 19,235 bytes
