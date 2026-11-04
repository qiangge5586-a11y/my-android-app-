# HondaHack 7.6.0 — Stage 20: method control-flow graph recovery

- Methods with semantic instruction streams: 11,104.
- Instructions indexed: 195,434.
- Basic blocks: 50,288.
- Normal/switch/exception CFG edges: 57,960.
- Conditional/switch decision points: 18,836.
- Methods with multiple blocks: 4,982.
- Branch-target stream validation issues: 0.

Outputs: method_cfg_metrics.tsv, basic_blocks.tsv, cfg_edges.tsv, cfg_validation_issues.tsv, stage20_summary.json, and build_stage20.py (included in the downloadable stage archive).

Limitations: this is a CFG approximation built from Stage 13 semantic instruction listings plus Stage 14 switch cases and Stage 15 exception metadata. Payload pseudo-instructions are excluded. Edge completeness depends on decoded operands and signature matching; complexity metric is a lower-bound decision count plus one, not a formal whole-program proof.

Static analysis only. No APK, scripts, native libraries or vehicle interfaces were executed.
