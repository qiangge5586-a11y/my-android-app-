# Stage 27 — resources.arsc symbolic resource index

## Results
- Package: `cn.autohack.hondahack` (ID `127`)
- Type-name strings: **19**
- Key-name strings: **3,099**
- Type-spec chunks: **18**
- Configuration-specific type chunks: **162**
- Resource symbol rows: **6,570**
- Unique resource IDs: **3,131**
- Parser issues: **0**

## Outputs
`resource_symbol_index.tsv` maps numeric IDs to package/type/entry names and configuration fingerprints. `resource_string_pools.tsv` contains type and key string pools. Type-spec and configuration tables are included.

## Limitations
This is a partial structural decoder for the single package in this APK. It does not decode every scalar value, complex bag/map, overlay, dynamic package mapping, or all reference chains. No APK was modified or built.
