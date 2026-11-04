# Local operation log privacy boundary

The rebuild records only app screen names when Activities are resumed. It keeps at most 100 entries in the app's private SharedPreferences.

- No Android `READ_LOGS` permission.
- No logcat/system log collection.
- No location, microphone, destination text, APK path, video URI, account data or vehicle telemetry.
- No automatic network upload.
- The user can review, clear, or explicitly share the local log.

This replaces the original privileged log-viewing concept with an app-owned, bounded diagnostic history.
