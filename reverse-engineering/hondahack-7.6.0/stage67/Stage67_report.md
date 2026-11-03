# Stage 67 — Navigation and local video

- Added a destination search screen using the standard Android geo URI and an installed maps app.
- Added a local-video picker using Storage Access Framework ACTION_OPEN_DOCUMENT; the selected URI is handed to an installed player.
- Added both screens to the dashboard and manifest.
- No location, microphone, or broad storage permission is requested; no background tracking, scanning, copying, or upload occurs.
- CI static checks now require these features and check the manifest has no declared permissions.
- Device testing is still required for map/player availability and OEM-specific chooser behavior.
