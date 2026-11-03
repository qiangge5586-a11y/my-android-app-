# HondaHack 7.6.0 — Stage 24: limited string-to-API argument resolution

- Semantic instruction rows scanned: 196,173.
- API call sites with resolved string argument registers: 233.
- Methods with at least one resolved value: 82.
- Distinct category/string pairs: 170.
- Category counts: SharedPreferences writes 38; SharedPreferences reads 98; IntentFilter actions 96; Intent.setAction 1.

Outputs: resolved_preference_and_action_strings.tsv, resolved_key_frequency.tsv, stage24_summary.json, and build_stage24.py (included in the stage archive).

Limitations: conservative intra-method linear propagation of const-string values through simple move aliases. It does not perform control-flow joins, interprocedural analysis, heap tracking, or full register semantics. Unresolved calls are not evidence that a value is absent. Validate results against the instruction listing.

Static analysis only. No APK, script, native code or vehicle interface was executed.
