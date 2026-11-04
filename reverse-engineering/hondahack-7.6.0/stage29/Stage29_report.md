# Stage 29 — Android manifest component and permission inventory

## Results
- Manifest elements: **86**
- Application/component records: **28** (1 application, 23 activities, 2 services, 1 receiver, 1 provider)
- Declared permission entries: **22** (22 unique)
- Action elements: **22** (22 unique)
- Intent-filter elements: **8**
- Explicit exported=true: **0**; explicit false: **1**; unspecified: **27**

## Outputs
Component records, permissions, action names and intent-filter line locations are in separate TSV files. Label resource references are correlated with the Stage 27 resource symbol index when possible.

## Limitations
The flattened XML inventory does not preserve full parent-child hierarchy. Attribute values are decoded as present in binary XML; Android-version-specific defaults for exported/permission behavior are not inferred. This is not a security verdict. No APK was built.
