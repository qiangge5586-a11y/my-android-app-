# Stage 28 — DEX to Android resource cross-reference

## Results
- Resource IDs indexed from Stage 27: **3,131**
- Direct DEX resource constant sites: **472**
- Methods with direct constant references: **168**
- Classes with direct constant references: **94**
- Explicit app R-field reference sites: **0**

## Outputs
- `dex_resource_constant_hits.tsv`: literal resource-ID constants mapped to resource names
- `dex_r_field_references.tsv`: symbolic app R$ field references
- `resource_usage_by_type.tsv` and `resource_usage_by_class.tsv`: usage rollups

## Limitations
Only literal const* instructions are matched. Dynamic resource lookup, generated IDs, reflection and indirect propagation are not covered. This is a static cross-reference, not proof that a UI element is reachable at runtime. No APK was built.
