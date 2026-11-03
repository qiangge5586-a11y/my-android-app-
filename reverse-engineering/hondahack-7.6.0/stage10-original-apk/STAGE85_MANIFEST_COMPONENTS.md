# Stage 85 — Binary AndroidManifest.xml reconstruction

## Input
Original APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe

## Package metadata
- Package: cn.autohack.hondahack
- Version name: 7.6.0
- Version code: 1
- Compile SDK: 28 (Android 9)
- minSdkVersion: 17
- targetSdkVersion: 17
- Application class: np.manager.signature.PmsHookApplication
- appComponentFactory: androidx.core.app.CoreComponentFactory
- Launcher activity: cn.autohack.hondahack.MainActivity

## Manifest component inventory
- Activities: 23
- Services: 2
- Receivers: 1
- Providers: 1
- Uses-permission declarations: 22

### Activities
1. cn.autohack.hondahack.MainActivity
2. cn.autohack.hondahack.RemapKeysActivity
3. cn.autohack.hondahack.SdcardActivity
4. cn.autohack.hondahack.SettingsActivity
5. cn.autohack.hondahack.AppsActivity
6. cn.autohack.hondahack.StartupActivity
7. cn.autohack.hondahack.AutorunManagerActivity
8. cn.autohack.hondahack.NavigatorActivity
9. cn.autohack.hondahack.OptimizeActivity
10. cn.autohack.hondahack.FixActivity
11. cn.autohack.hondahack.AboutActivity
12. cn.autohack.hondahack.AppsInstallLocalActivity
13. cn.autohack.hondahack.EmergencyActivity
14. cn.autohack.hondahack.AppsInstallOnlineActivity
15. cn.autohack.hondahack.TestActivity
16. cn.autohack.hondahack.StartupVideoActivity
17. cn.autohack.hondahack.SelectLocalVideoActivity
18. cn.autohack.hondahack.SelectOnlineVideoActivity
19. cn.autohack.hondahack.CleanInternalSdActivity
20. cn.autohack.hondahack.LogActivity
21. cn.autohack.hondahack.AdvancedActivity
22. cn.autohack.hondahack.MeterActivity
23. cn.autohack.hondahack.VoiceAssistantActivity

### Services
- cn.autohack.hondahack.MyService
- cn.autohack.voiceassistant.VoiceAssistantService

### Receiver
- cn.autohack.hondahack.MyBroadcastReceiver

Intent actions declared by the receiver include BOOT_COMPLETED, ACTION_SHUTDOWN, QUICKBOOT_POWEROFF, REBOOT, package replaced/removed, media mounted/unmounted/removed/eject/bad removal, power connected, USER_PRESENT, connectivity changes, Wi-Fi state/supplicant changes, two Mitsubishi HMI service actions, Clarion WARP_BOOT and the app's UPDATE_ORIENTATION_LOCK action. These are registration declarations only; runtime effects must be confirmed from the receiver's method body.

### Provider
- com.balsikandar.crashreporter.CrashReporterInitProvider
- authorities: cn.autohack.hondahack.CrashReporterInitProvider
- enabled: true
- exported: false

## Permission declarations (22)
1. android.permission.ACCESS_SUPERUSER
2. android.permission.ACCESS_COARSE_LOCATION
3. android.permission.ACCESS_FINE_LOCATION
4. android.permission.BLUETOOTH_ADMIN
5. android.permission.CHANGE_WIFI_STATE
6. android.permission.REORDER_TASKS
7. android.permission.WRITE_EXTERNAL_STORAGE
8. android.permission.SYSTEM_ALERT_WINDOW
9. android.permission.RECEIVE_BOOT_COMPLETED
10. android.permission.ACCESS_WIFI_STATE
11. android.permission.ACCESS_NETWORK_STATE
12. android.permission.INTERNET
13. android.permission.BLUETOOTH
14. android.permission.GET_TASKS
15. android.permission.RESTART_PACKAGES
16. android.permission.KILL_BACKGROUND_PROCESSES
17. android.permission.READ_LOGS
18. android.permission.MOUNT_UNMOUNT_FILESYSTEMS
19. android.permission.MODIFY_AUDIO_SETTINGS
20. android.permission.CLEAR_APP_CACHE
21. com.android.launcher.permission.INSTALL_SHORTCUT
22. android.permission.RECORD_AUDIO

Permission strings do not establish that a permission is granted or that the app successfully uses the associated operation. Several are privileged, signature, deprecated or vendor/system-specific on modern Android. Runtime code and target device privilege context must be traced separately.

## Xposed metadata
- assets/xposed_init contains: cn.autohack.hondahack.b
- assets/VERSION contains: 54

This proves that an Xposed entry class is declared in the package metadata; it does not by itself prove which methods are hooked or that the module is active.

## Important application wrapper
The application class is declared as np.manager.signature.PmsHookApplication rather than a class in the app's own namespace. This needs a dedicated trace through DEX inheritance, initialization and referenced library code; do not infer its purpose from its name alone.

## Next analysis
- Trace Application.attachBaseContext/onCreate and all static initialization.
- Trace MainActivity lifecycle and navigation graph.
- Trace MyService start/stop, foreground/background behavior and privileged operations.
- Trace MyBroadcastReceiver action dispatch and startup/shutdown behavior.
- Trace Xposed entry class cn.autohack.hondahack.b and all hook registrations.
- Resolve string resource IDs into Chinese UI labels and map each Activity to its layout.
