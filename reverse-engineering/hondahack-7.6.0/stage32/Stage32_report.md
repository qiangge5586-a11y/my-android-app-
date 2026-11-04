# Stage 32 — Android resource value decoding

## Results
- Resource value/configuration rows: **6,563**
- Global value string pool entries: **3,428**
- Value kinds: 4,336 strings; 1,387 complex entries; 457 booleans; 234 dimensions; 66 ARGB8 colors; 25 references; 23 decimal integers; 14 fractions; 12 floats; 9 RGB8 colors.
- Parser boundary diagnostics: **7**, all on complex entries whose map payload extends beyond the local type chunk boundary.

## Outputs
- `resource_values.tsv`: scalar values and complex-entry markers per configuration
- `resource_value_kind_counts.tsv`: decoded value-kind totals

## Limitations
Simple scalar values are decoded where recognized; dimensions/fractions/colors are retained as raw packed data, and complex bag/map contents are not expanded. Dynamic overlays and full reference chains are not resolved. No APK was built.
