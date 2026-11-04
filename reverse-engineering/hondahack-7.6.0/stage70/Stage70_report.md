# Stage 70 — Android 4.2+ compatibility review

- Updated TextToSpeech calls to the legacy-compatible three-argument API and uses a locale constructor for Simplified Chinese.
- Added Android API-level branching for the Storage Access Framework: ACTION_OPEN_DOCUMENT on API 19+, ACTION_GET_CONTENT fallback on API 17–18.
- Added CI assertions for both code paths.
- The project minSdk remains 17. Device-specific chooser/player/TTS availability still needs real hardware verification.
