# HondaHack 7.6.0 — Stage 14: branch and switch validation

DEX SHA-256: 3c3a0ac9deac6fd33bf9a76dd338085896a3010c9f4d9dfc7cb54eafc3595319

- DEX call_site_ids: 0 (this APK does not use invoke-custom call-site entries).
- Branch instructions inspected: 26,881.
- Branch targets within their method code ranges: 26,881.
- Invalid branch targets: 0.
- Packed/sparse switch case targets recovered: 857.
- Validation issues: 0.
- Instructions scanned: 196,173.

Evidence files: call_sites.tsv, branch_targets.tsv, switch_cases.tsv, validation_issues.tsv.

Static analysis only. The custom parser output remains subject to mature-tool verification and is not Java source or a complete Smali project.
