# Stage 93 — Navigation hand-off and public audio-focus test

## Evidence baseline

The original 7.6.0 report records navigation start/stop/update actions and a MyService workflow that requests temporary audio focus during navigation prompts, with configurable media-volume reduction. This confirms a feature to reconstruct, but not a public integration contract for all map apps or OEM head units.

## Implemented

- Kept destination hand-off through the standard Android `geo:` intent.
- Added an explicit user-triggered transient audio-focus request using `AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`.
- Added explicit release and automatic focus abandonment when the Activity is destroyed.
- Added static CI checks for the public API calls.
- Documented that actual ducking depends on the receiving media app and OEM audio policy.

## Not implemented

- No direct volume writes or control of other applications.
- No automatic observation of external navigation state.
- No OEM private audio service binding or vehicle broadcast interception.
- No background service that holds audio focus continuously.

## Acceptance

CI static checks and debug APK build must pass. Physical audio testing must be done while parked at low volume and must verify that focus is released when leaving the page.

## Next stage

Stage 94 should improve the app catalog and local install workflow using Android's public package/document intents, without installing apps silently or modifying system whitelists.
