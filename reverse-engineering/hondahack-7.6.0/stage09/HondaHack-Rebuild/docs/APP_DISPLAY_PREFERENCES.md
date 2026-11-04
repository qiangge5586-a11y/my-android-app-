# App-local display preference

Stage 95 adds a `keep_screen_on` preference. When enabled, the rebuild applies Android's `FLAG_KEEP_SCREEN_ON` only to its own visible Activities. When an Activity pauses, the flag is cleared; the next resumed Activity reapplies the setting.

This does not alter global display timeout settings, system settings, vehicle firmware, or other applications. The preference is stored in the app's private SharedPreferences and can be disabled from the app's Settings page.
