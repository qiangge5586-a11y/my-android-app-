# Stage 94 — User-confirmed local APK installation hand-off

## Evidence baseline

The original 7.6.0 report identifies local and online app-install Activities. It also documents system whitelist and privileged installation mechanisms. This stage reconstructs only the user-facing local file-selection path.

## Implemented

- Added `ApkInstallActivity` and a dashboard entry.
- API 19+ uses the Storage Access Framework document picker; API 17–18 uses the content picker fallback.
- Passes the user-selected APK content URI to the Android package installer.
- Requires the system installer to display its normal confirmation UI.
- Added static CI assertions for the package archive MIME type and picker fallback.
- Documented firmware and installer dependencies.

## Not implemented

- No online store or private autohack.cn backend.
- No silent installation, root package-manager calls, whitelist changes, privileged permission grants, or background APK installation.
- No installation assumption for the original Honda factory CarPlay display.

## Acceptance

CI static checks and debug build must pass. Physical testing requires a compatible Android head unit and a harmless test APK; confirm that the system installer appears and that cancelling it leaves the device unchanged.

## Next stage

Stage 95 should add a safe startup-media preference and a user-selected local video hand-off, without boot receivers or automatic playback at device startup.
