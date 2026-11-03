# Stage 80 — Build summary and checksum visibility

- CI now writes the verified APK package ID, minSdk and SHA-256 to the GitHub Actions job summary.
- The checksum is also printed to the job log and included in the downloadable artifact.
- This makes the artifact independently verifiable without unpacking the CI job environment.
