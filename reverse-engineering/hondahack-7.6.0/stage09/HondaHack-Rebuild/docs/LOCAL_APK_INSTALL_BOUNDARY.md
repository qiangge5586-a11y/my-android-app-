# Local APK installation boundary

The rebuild exposes a local APK picker and hands the selected content URI to Android's package installer with `ACTION_VIEW` and MIME type `application/vnd.android.package-archive`.

## User control

- The user selects the APK file.
- The Android package installer displays the package and requests confirmation.
- The rebuild does not use PackageInstaller session APIs to install silently, request device-owner privileges, grant permissions, or modify system whitelists.
- No APK is bundled or automatically downloaded by this screen.

## Compatibility

- API 19+ uses `ACTION_OPEN_DOCUMENT`.
- API 17–18 uses `ACTION_GET_CONTENT` as a picker fallback.
- The selected content URI is passed with a read grant.
- Installation still depends on the device's package installer, unknown-source policy and firmware restrictions.

The original 2019 Civic factory CarPlay display should not be assumed to host Android APKs. Verify the actual head-unit OS and installer before testing.
