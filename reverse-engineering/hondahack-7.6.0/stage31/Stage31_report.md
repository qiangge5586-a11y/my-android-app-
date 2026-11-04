# Stage 31 — Layout element and view-ID inventory

## Results
- Layout files with parsed elements: **115**
- Layout element rows: **780**
- Unique element tags: **53**
- ID attribute occurrences: **640**
- Resolved ID references: **602**
- Unresolved ID references: **38**

## Outputs
- `layout_element_inventory.tsv`: per-element layout path, tag, ID and selected attributes
- `layout_element_frequency.tsv`: element-type frequency
- `layout_complexity.tsv`: per-layout element counts
- `layout_id_references.tsv`: resource-ID/name usage counts

## Limitations
The binary XML parser records attributes structurally; style inheritance, data binding, include expansion and runtime-generated views are not reconstructed. No APK was built.
