# Stage 76 — APK identity and artifact verification

## CI enhancement
- After Gradle compilation, CI runs Android Build Tools `aapt dump badging` against the generated APK.
- Asserts the application ID is `cn.autohack.hondahack.rebuild` (side-by-side install safety).
- Asserts min SDK is 17.
- Writes a SHA-256 checksum and APK badging metadata beside the build output.
- Uploads the APK, badging report and checksum in one Actions artifact.

## Acceptance value
A successful CI run now proves more than file existence: the APK was produced by Gradle and its package identity/minSdk metadata match the project configuration. It still does not prove runtime compatibility on the Honda head unit.
