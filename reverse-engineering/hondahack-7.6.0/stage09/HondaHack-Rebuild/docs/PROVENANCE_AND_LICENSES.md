# Provenance, reverse-engineering scope and third-party notices

## Original input
The analysis input was supplied by the project owner as HondaHack 7.6.0 APK. The reverse-engineering reports record the input APK and native-library SHA-256 values so findings can be tied to a specific binary.

The original APK is not copied into this repository and is not redistributed by this reconstruction project.

## Rebuild source
The Java source under `stage09/HondaHack-Rebuild` is newly authored reconstruction code. It does not contain recovered proprietary Java/Kotlin/C/C++ source. The project uses Android platform APIs and has no third-party runtime library dependency at this stage.

## Public research reference
The public repository `cysin/honda_hacking_analysis` was identified as a relevant Honda head-unit research reference. It contains research artifacts and historical samples. This project records it as a research lead; it does not vendor its source or binary artifacts. Review that repository's GPL-3.0 license and notices before copying or adapting any of its code in a future change.

## Third-party components in the original APK
Static inspection identified an embedded LAME MP3 library and an Xposed-related native library in the original APK. Neither native library is packaged in this reconstruction project. No LAME or Xposed code is copied into the rebuilt APK.

## Functional and legal boundaries
- Do not redistribute the original APK or private assets as part of this project.
- Do not copy third-party source without complying with its license and preserving required notices.
- Do not reproduce private backend credentials, account services, authorization bypasses, or protected server APIs.
- Do not implement vehicle commands, privileged system modifications, destructive resets, or hidden persistence without documented authorization and a safe, testable interface.
