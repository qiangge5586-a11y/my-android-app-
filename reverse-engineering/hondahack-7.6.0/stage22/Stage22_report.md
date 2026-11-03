# HondaHack 7.6.0 — Stage 22: string constant and configuration evidence

- Unique string constants used by instructions: 2,903.
- const-string instruction uses: 4,500.
- Methods with string uses: 1,322.
- Category hit rows: 4,632 (categories may overlap).

Category counts: other 3,569; native/system keywords 307; log/debug 132; preference/config candidates 539; filesystem paths 32; intent/action candidates 36; Android permission strings 4; URL strings 13.

Outputs: string_constant_inventory.tsv, string_use_sites.tsv, string_category_hits.tsv, class_string_density.tsv, stage22_summary.json, and build_stage22.py (included in the stage archive).

Limitations: string presence at a const-string site does not prove the value reaches a network request, file operation, preference store, or broadcast. Categories are heuristic and can overlap.

Static analysis only. No APK, script, native library or vehicle interface was executed.
