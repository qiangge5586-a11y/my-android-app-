# HondaHack 7.6.0 — Stage 19: DEX debug metadata

DEX SHA-256: `3c3a0ac9deac6fd33bf9a76dd338085896a3010c9f4d9dfc7cb54eafc3595319`

- Methods with debug_info_item: 4.
- Debug streams parsed: 4.
- Source line events: 45.
- Local-variable lifecycle events: 35.
- Debug streams terminated: 4.
- Parser/validation issues: 0.

Outputs: method_debug_summary.tsv, source_line_events.tsv, local_variable_events.tsv, debug_parse_issues.tsv, stage19_summary.json, and build_stage19.py (included in the stage archive).

Debug metadata is sparse. Line events map code-unit addresses to line values; they do not reconstruct source text. Local names/types are only those explicitly present in debug metadata. Static analysis only; no code was executed.