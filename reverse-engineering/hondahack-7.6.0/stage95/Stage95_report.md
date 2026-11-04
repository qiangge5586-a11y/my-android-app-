# Stage 95 — App-local display preference

## Evidence baseline

The original SettingsActivity contains a force_portrait preference and other app settings. This stage adds a safe local display preference that is useful on a head unit without writing global system settings.

## Implemented

- Added `keep_screen_on` to the Settings screen.
- MyApplication registers Activity lifecycle callbacks and applies `FLAG_KEEP_SCREEN_ON` to this app's visible Activities only.
- The flag is cleared when an Activity pauses and reapplied on resume according to the local preference.
- Added static CI checks and documentation.

## Not implemented

- No global screen timeout changes, rotation lock writes, status-bar modifications, or system settings writes.
- No root or privileged APIs.

## Acceptance

CI static checks and debug build must pass. On a supported Android head unit, toggle the preference, navigate between app screens, background the app, and confirm the display flag is cleared when the app pauses.
