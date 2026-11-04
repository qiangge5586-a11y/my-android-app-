# Stage 99 — Head-unit compatibility diagnostics

## Implemented

- Expanded DeviceInfoActivity with screen resolution and density.
- Added read-only APK package-installer availability check.
- Added a minimum SDK comparison against minSdk 17.
- Retained Android build, device, board, hardware, ABI and storage metadata.
- Documented that MCU firmware and proprietary OEM integration cannot be inferred from public Android Build properties.

## Acceptance

CI static checks and debug build must pass. On the actual target, save the displayed Android version/API, board, CPU ABI, screen resolution/density and installer availability. Check MCU version separately in the head-unit vendor settings if available.

## Compatibility gate

If the unit is the original Honda factory CarPlay display, it should not be treated as an Android APK host. If it is an aftermarket Android unit, use the reported OS/API and board information for subsequent device testing.
