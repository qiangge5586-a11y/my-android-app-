# Stage 73 — Side-by-side installation safety

- Changed the Android application ID from the original-looking package to `cn.autohack.hondahack.rebuild`.
- Kept the Java namespace `cn.autohack.hondahack` so source package paths remain stable.
- This prevents the reconstruction from implicitly replacing the original app or its private data during testing.
- Updated project documentation and static CI assertion.
- CI must rebuild the changed package identity; device installation remains unverified until the APK is installed on the target head unit.
