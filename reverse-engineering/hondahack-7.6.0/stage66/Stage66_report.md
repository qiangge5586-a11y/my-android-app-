# Stage 66 — Gradle Java compatibility correction

The Stage 65 CI run reached Gradle project evaluation and failed because the app module used the nonexistent Gradle enum constant `JavaVersion.VERSION_8`. Gradle 8.9 expects `JavaVersion.VERSION_1_8` for Java 8 source compatibility.

Changes:
- Corrected both sourceCompatibility and targetCompatibility to VERSION_1_8.
- Extended the static regression script to assert this configuration.
- The next CI run must complete the actual compile and APK artifact upload before this stage is considered verified.

The prior CI SDK bootstrap failure is already fixed. No runtime APK is claimed from the failed compile attempt.
