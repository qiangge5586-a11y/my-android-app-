# HondaHack 7.6.0 — Stage 23: API and nearby string-context correlation

- Semantic method sections scanned: 11,104.
- API call sites with nearby string literals: 708.
- Unique contextual evidence rows: 892.
- SharedPreferences key candidates: 120.

Category hits: SharedPreferences 247; PackageManager 45; file I/O 73; Intent/broadcast 284; URI/URL 59.

Outputs: api_string_context.tsv, preference_key_candidates.tsv, stage23_summary.json, and build_stage23.py (included in the stage archive).

Limitations: nearby const-string instructions are contextual clues only; they are not proof that the string is passed as the API argument. Register aliasing, control flow and object construction are not fully tracked. Preference candidates require manual validation.

Static analysis only. No APK, script, native code or vehicle interface was executed.
