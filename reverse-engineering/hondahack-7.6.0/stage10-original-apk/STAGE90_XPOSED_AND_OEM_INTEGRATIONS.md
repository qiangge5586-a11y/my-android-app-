# Stage 90 — Confirmed Xposed entry point and automotive integration vocabulary

## Input identity
Original APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe

## Xposed entry point is confirmed

- assets/xposed_init contains exactly: `cn.autohack.hondahack.b`
- DEX class `cn.autohack.hondahack.b` directly implements `de.robv.android.xposed.IXposedHookLoadPackage`
- It declares `handleLoadPackage(XC_LoadPackage.LoadPackageParam)`
- The class has 43 methods with code bodies.

This is conclusive metadata/interface evidence that class b is the Xposed load-package hook module entry. The separate class `cn.autohack.hondahack.Xposed` only exposes a getVersion() method and is not the xposed_init entry class.

The app also embeds Xposed API classes such as IXposedHookLoadPackage, XC_MethodHook, XC_MethodReplacement, XSharedPreferences, XposedBridge and XposedHelpers.

## Native bridge class c

DEX class `cn.autohack.hondahack.c` has a native declaration:
- `static native b(Context, Object): void`

It maps to `Java_cn_autohack_hondahack_c_b` in libxposed.so at ELF address 0x85a8, size 1,056 bytes. The bridge's exact operation remains unresolved; disassembly shows repeated JNI object/method access and exception cleanup, but the full target of each JNI call still needs decoding.

## Automotive system integration vocabulary found in DEX strings

The DEX string table includes numerous vendor service interfaces and app package identifiers:

### Mitsubishi Electric / OEM HMI service
- `com.mitsubishielectric.ada.app.dalauncher`
- `com.mitsubishielectric.ada.appservice.avapservice.AvApService`
- `com.mitsubishielectric.ada.appservice.camera.CameraAPService`
- `com.mitsubishielectric.ada.appservice.powermanager.ACTION_HMI_SERVICE_START`
- `com.mitsubishielectric.ada.appservice.powermanager.ACTION_HMI_SERVICE_RESTART`
- `com.mitsubishielectric.ada.appservice.vehicleinfomanager.IVehicleInfoManagerApService`
- Vehicle information listener names for gear, ignition, battery/VSP pulse and FCAN.
- Video manager service interfaces.

### Clarion
- `com.clarion.displayaudio.disclaimer`
- `com.clarion.displayaudio.disclaimer.activity.DisclaimerActivity`
- `com.clarion.intent.action.WARP_BOOT`

### Navigation / media package identifiers
- `com.autonavi.amapauto`
- `com.baidu.carlifevehicle`
- `com.baidu.BaiduMap.auto`
- `com.baidu.naviauto`
- `com.tencent.qqmusiccar`
- `com.tencent.wecarnavi`
- `cn.kuwo.kwmusiccar`
- `com.waze`
- `com.google.android.apps.maps`
- `com.here.app.maps`
- `com.sygic.aura`
- `com.nng.igo.primong.igoworld`

These identifiers indicate compatibility logic or integration references for multiple aftermarket/OEM infotainment environments. They do not establish that all services are present on the user's device or that every integration is active.

## Vehicle data and UI action vocabulary

The DEX string table contains identifiers for:
- engine coolant temperature;
- average fuel economy and units;
- distance to empty;
- trip A distance;
- vehicle voltage;
- GPS status and bearing;
- meter visibility/update/test modes;
- rear-camera toggle;
- navigation start/stop/update for AMap and Waze;
- voice assistant state, simulation and playback;
- source selection and audio focus.

These strongly indicate instrument-cluster / vehicle-information overlays and HMI integrations. The precise data source, transport and hardware requirements remain to be reconstructed from the hooked target methods and service bindings.

## Next stage
1. Read the complete handleLoadPackage body and all methods it invokes.
2. Identify package filters and target class/method names used by the hook.
3. Map every XC_MethodHook registration to before/after behavior.
4. Correlate target package names with vendor services and broadcast action constants.
5. Analyze the native c.b bridge alongside its Java caller.
6. Separate generic navigation/media integration from vendor-specific vehicle-bus data integration.
