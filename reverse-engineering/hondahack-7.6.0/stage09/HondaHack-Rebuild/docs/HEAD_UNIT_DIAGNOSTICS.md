# Head-unit compatibility diagnostics

DeviceInfoActivity now reports Android release/API level, build/device/board/hardware identifiers, CPU ABI, display pixel dimensions and density, internal storage summary, and whether Android resolves an APK package-installer intent.

These are read-only public Android values. The app cannot reliably read proprietary MCU firmware strings, CAN capabilities, factory CarPlay protocol details or vendor AIDL availability. Those must be inspected in the head unit's own settings or documented service interfaces.

The rebuild requires API 17 or later. An API level meeting minSdk does not by itself prove that the unit can install third-party APKs or expose steering-wheel and vehicle-data interfaces.
