# Device smoke-test plan

## Test device and setup
Record:
- Head-unit manufacturer/model and Android version/API
- Screen resolution/density
- Build fingerprint (if available)
- Installed map app, video player and TTS engine
- APK version and SHA-256

Install only the debug APK produced by the project's GitHub Actions artifact. Keep the original HondaHack APK unchanged and available for rollback.

## Functional checks

- [ ] App installs and launches on the target head unit.
- [ ] Dashboard renders without clipping at the device's display density.
- [ ] Device Info shows expected model, Android release/API and storage summary.
- [ ] App Catalog lists launcher-visible applications.
- [ ] Selecting an app opens it; returning to HondaHack restores the dashboard.
- [ ] Navigation accepts a destination and opens the system map chooser.
- [ ] Canceling the map chooser returns safely to HondaHack.
- [ ] Local Video opens the system document picker.
- [ ] Canceling video selection returns safely.
- [ ] Selecting a supported video opens the installed player.
- [ ] Diagnostics summary is displayed and can be shared only after tapping Share.
- [ ] Local assistant routes each supported typed command correctly.
- [ ] TTS speaks when a Chinese voice is installed; absence of a voice engine does not crash the app.
- [ ] Both local display preferences persist after force-stop/relaunch.

## Privacy and safety checks

- [ ] Android App Info shows no runtime permissions requested by this build.
- [ ] No microphone recording indicator appears.
- [ ] No location permission or background location activity.
- [ ] No automatic network traffic is required for core screens.
- [ ] No root prompt, Xposed hook, shell command, system setting write, vehicle command, or reboot occurs.
- [ ] No system partition, whitelist, factory-reset, or boot configuration is changed.

## Compatibility matrix

Test at least:
- [ ] Android 4.2–4.4 (API 17–19) if the head unit uses this generation.
- [ ] Android 5–9 (API 21–28).
- [ ] Android 10–15 (API 29–35).

The app's minSdk is 17, but the map chooser, document picker fallback and TTS engine must be verified on the actual device.

## Result

Record each test as PASS / FAIL / NOT TESTED, include screenshots/logcat only if they contain no secrets, and create a GitHub issue for failures. Do not enable any privileged vehicle feature as part of this smoke test.
