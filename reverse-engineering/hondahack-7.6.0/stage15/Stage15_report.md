# HondaHack 7.6.0 — Stage 15: exception handlers and class initialization

DEX SHA-256: 3c3a0ac9deac6fd33bf9a76dd338085896a3010c9f4d9dfc7cb54eafc3595319

- Class definitions: 1,770.
- Class-data method declarations: 11,787.
- Constructors and class initializers: 1,793.
- Code items with register dimensions: 11,104.
- Decoded try/handler rows: 3,268.
- Method-handle map entries: 0.
- Parser issues: 0.

Evidence: exception_handlers.tsv, class_initializers.tsv, class_methods.tsv, method_handles.tsv, register_usage_summary.tsv, validation_issues.tsv, stage15_summary.json.

Limitations: register output is structural register-count metadata, not reaching-definitions analysis. Exception records are decoded from DEX try_item and encoded_catch_handler structures and still warrant verification with a mature DEX tool. This is not recovered Java source or a buildable APK.

Static analysis only. No APK, scripts, or native libraries were executed. No Shizuku, root, Xposed injection, vehicle/HMI writes, authorization bypass, or destructive system operations are implemented.
