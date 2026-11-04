# Stage 30 — Manifest component to DEX class mapping

## Results
- Manifest components checked (excluding application): **27**
- Component classes present in DEX: **27**
- Component classes missing from DEX: **0**
- Lifecycle method rows found: **47**
- Components with declared lifecycle methods: **27**

## Outputs
- `manifest_to_dex_component_map.tsv`: component/class presence and lifecycle counts
- `component_lifecycle_methods.tsv`: declared lifecycle methods and signatures

## Limitations
Name-based class descriptor matching only; inherited lifecycle implementations and dynamic component registration are not resolved. Presence does not prove runtime reachability. No APK was built.
