# HondaHack 7.6.0 — Stage 25: preference and intent-action catalog

- Distinct preference keys with resolved argument evidence: 62.
- Preference read sites: 98.
- Preference write sites: 38.
- Keys observed with both reads and writes: 29.
- Distinct IntentFilter/Intent action strings: 79.
- Action registration/set sites: 97.

Outputs: preference_catalog.tsv, intent_action_catalog.tsv, stage25_summary.json, and build_stage25.py (included in the stage archive).

Limitations: catalog is derived from Stage 24's limited intra-method string propagation. It can miss keys constructed dynamically or passed across methods and does not prove UI exposure or runtime behavior.

Static analysis only; no APK or vehicle interface was executed.
