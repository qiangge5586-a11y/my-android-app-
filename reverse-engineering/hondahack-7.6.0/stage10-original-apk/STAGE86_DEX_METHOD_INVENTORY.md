# Stage 86 — DEX class-data and method-body inventory

## Verified counts
Input APK SHA-256: e52aed97a77bc4be1e4d248bb9526edafaae428ea20b8926f7763e532cc55fbe

A second-pass parser read DEX class_data_item structures, skipped encoded static/instance field lists, and resolved direct/virtual method index deltas against method_ids.

- DEX class definitions: 1,770
- DEX method ID table: 15,752
- Methods with code bodies across all classes: 11,104
- Methods declared by cn.autohack.hondahack classes: 1,151
- Those app-namespace methods with code bodies: 1,131
- App-namespace methods without code bodies (abstract/native declarations): 20

This confirms the historical 11,104 method-body count from the original APK. It is not a claim that all methods have already been decompiled into Java source.

## High-priority class method counts

| Class | Method bodies |
|---|---:|
| cn.autohack.hondahack.ua | 56 |
| cn.autohack.hondahack.b | 43 |
| cn.autohack.hondahack.MyService | 29 |
| cn.autohack.hondahack.MyApplication | 20 |
| cn.autohack.hondahack.ub | 17 |
| cn.autohack.hondahack.W | 15 |
| cn.autohack.hondahack.E | 14 |
| cn.autohack.hondahack.FixActivity | 14 |
| cn.autohack.hondahack.SettingsActivity | 14 |
| cn.autohack.hondahack.fc | 14 |
| cn.autohack.hondahack.na | 13 |
| cn.autohack.hondahack.SdcardActivity | 12 |
| cn.autohack.hondahack.RemapKeysActivity | 11 |
| cn.autohack.hondahack.Wb | 11 |
| cn.autohack.hondahack.OptimizeActivity | 10 |

Obfuscated class names are preserved exactly. They must not be renamed by guesswork. The class with 56 methods and the 43-method Xposed entry class should be decompiled early because they may contain core logic.

## Key class signatures recovered from class_data

### MainActivity
- constructor
- onCreate(Bundle)
- onCreateOptionsMenu(Menu)
- onHelp(View)
- onTitleChanged(CharSequence, int)

### MyService
- onBind(Intent)
- onDestroy()
- onLocationChanged(Location)
- onProviderDisabled(String)
- onProviderEnabled(String)
- onStartCommand(Intent, int, int)
- onStatusChanged(String, int, Bundle)
- onTaskRemoved(Intent)
- onUnbind(Intent)
- additional obfuscated static/instance helpers

The location callbacks are strong evidence that the service implements Android location-listener behavior. Exact use (navigation, positioning, geofencing or another purpose) requires code-body inspection.

### MyBroadcastReceiver
- constructor
- onReceive(Context, Intent)

### MyApplication
- class initializer
- constructor
- onCreate()
- onServiceConnected(ComponentName, IBinder)
- onServiceDisconnected(ComponentName)
- onTerminate()
- multiple app-specific static and instance helpers

### Native bridge class cn.autohack.hondahack.d
- JNI declarations: a(Context), b(Context,String), c(Context,String,double,double), d(Context), e(Context,int), f(Context,int), g(Context), q(Context,String), v(Context)
- These are native declarations with no DEX code body. They map to exported JNI symbols in libnative.so and require native disassembly before their purpose can be stated.

### Xposed
- cn.autohack.hondahack.Xposed has a constructor and getVersion()
- assets/xposed_init names cn.autohack.hondahack.b as the module entry class. Do not confuse this metadata entry with the separate class named Xposed.

## Analysis priorities
1. Decompile cn.autohack.hondahack.b and inspect its superclass/interfaces and hook registration methods.
2. Decompile ua, ub, MyApplication and MyService.
3. Map Activity method bodies to layout/resource IDs and UI strings.
4. Resolve all native bridge declarations to JNI exports and parameter/return semantics.
5. Build call graphs from decoded invoke instructions and annotate confidence.

## Limitations
Method names and signatures are metadata; they do not reveal full method semantics. DEX-to-Java decompilation is still required. All recovered names, signatures and offsets must remain linked to the original APK hash above.
