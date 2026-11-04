# Stage 96 — Manual local startup-video preset

## Evidence baseline

The original 7.6.0 report identifies StartupVideoActivity, SelectLocalVideoActivity and SelectOnlineVideoActivity, along with startup video playback actions. This stage reconstructs the local media selection and preset workflow only.

## Implemented

- MediaActivity now supports selecting a local video through the Android document picker.
- User can preview the selected video in an installed player.
- Android 4.4/API 19+ can persist a read-only content URI and save it as an in-app startup-video preset.
- User can manually open the saved preset later.
- Android 4.2/4.3 keeps the immediate-preview fallback and clearly reports that persistent document authorization is unavailable.
- Static CI checks cover persisted URI permission and the preference key.

## Not implemented

- No BOOT_COMPLETED receiver, background playback, boot animation replacement, or automatic video launch.
- No online video backend, private service, file scanning, copying, or uploading.

## Acceptance

CI static checks and debug build must pass. On a supported device, select a local video, save the preset, leave the app, return and open the saved preset. Confirm that cancelling the picker or player does not change any file.

## Next stage

Stage 97 should add a safe local action log and user-controlled export, replacing the original privileged full-system log viewer with app-owned diagnostics only.
