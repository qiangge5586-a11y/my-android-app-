# Meter preview — data provenance and safety

The current MeterActivity is a visual prototype only. Its displayed values (72 km/h, 86°C, 13.8V, 6.8 L/100km and 420 km) are fixed illustrative constants and are explicitly labeled demo data.

It does not read vehicle CAN/OBD, Mitsubishi vehicle-info AIDL, OEM broadcasts, files under /data/local/tmp/shared_files, or any vendor service. It must never be presented as live vehicle information.

## Path to real read-only data

A later integration requires a documented, user-authorized, read-only data source on the exact head unit. First identify whether the OEM HMI exposes a public API or whether an aftermarket Android unit has an OBD adapter. Validate signal units, update rate, stale-data behavior and failure handling before displaying any live value. No write commands, control actions, or safety-critical alerts should be added based on guessed signal mappings.

## Acceptance checks

- Screen title says 仪表盘预览.
- The warning says 演示数据 and 未连接车辆.
- Fixed values are visible and selectable text is not needed for operation.
- No runtime permissions or new service bindings are introduced.
- Static CI checks and Android debug build pass.
