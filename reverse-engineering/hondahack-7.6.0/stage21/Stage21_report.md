# HondaHack 7.6.0 — Stage 21: Android/API invocation inventory

- Methods scanned: 11,104.
- Invoke instructions scanned: 46,287.
- Unique invoked method references: 11,763.
- Unique target classes: 2,139.
- Caller-class to API-class edges: 10,439.
- Name-based audit triage rows: 451.

Triage category hit counts: reflection 209; file write/delete 56; broadcast APIs 59; network 86; Binder IPC 29; location 4; process execution 6; audio capture 2.

Outputs: invoked_method_refs.tsv, invoked_api_classes.tsv, caller_to_api_method.tsv, api_risk_triage.tsv, class_api_dependency_edges.tsv, stage21_summary.json, and build_stage21.py (included in the stage archive).

Limitations: method references show static call sites, not runtime reachability or successful execution. Risk categories are conservative name-pattern matches for manual review, not proof of malicious or destructive behavior. Reflection, JNI, dynamic dispatch and obfuscation can hide behavior.

Static analysis only. No APK, script, native code or vehicle interface was executed.
