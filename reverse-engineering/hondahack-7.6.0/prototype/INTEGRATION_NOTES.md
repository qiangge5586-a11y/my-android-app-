# EnhancementActivity prototype integration notes

## Source

- Java class: `com.autohack.enhance.EnhancementActivity`
- Intended original package: `cn.autohack.hondahack`
- Minimum runtime: Android 4.2 / API 17
- UI: framework ScrollView / LinearLayout / AlertDialog / Button
- No third-party dependencies

## Manifest entry (fragment)

The final merged manifest must register the fully qualified Activity name because its Java package is separate from the original application package:

```xml
<activity
    android:name="com.autohack.enhance.EnhancementActivity"
    android:exported="false"
    android:label="增强功能中心"
    android:screenOrientation="unspecified" />
```

The read-only network status page requires the existing application to retain:

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Do not add broad storage permissions for the file picker. It uses the system content picker and receives only the URI the user selects.

## Entry point

The original app must add an explicit internal Intent to the enhancement page from a new menu item/button:

```java
Intent intent = new Intent(this,
    com.autohack.enhance.EnhancementActivity.class);
startActivity(intent);
```

This snippet is an integration instruction only. The exact insertion point in the obfuscated original MainActivity is not yet proven and must be identified from DEX call/layout evidence before patching.

## Build and test checklist

- Compile against Android SDK 28 or newer while keeping minSdk 17.
- Set Java source/target compatibility to 1.7 for the prototype.
- Confirm the compiler output contains no method references unavailable on API 17 without a runtime guard.
- Test device info, network status with/without connectivity, storage reporting, launching an app, opening Wi-Fi settings, and file selection.
- Confirm Back returns to the previous HondaHack screen.
- Verify the original application signing identity and signature-protected integrations before attempting installation.

## Current validation status

Source is committed to the feature branch. No Gradle project has yet been created for this prototype, no APK has been assembled, and no device/runtime test has been performed.
