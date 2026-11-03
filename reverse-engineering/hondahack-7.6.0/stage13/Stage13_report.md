# HondaHack 7.6.0 — Stage 13: DEX operand and cross-reference recovery

DEX SHA-256: 3c3a0ac9deac6fd33bf9a76dd338085896a3010c9f4d9dfc7cb54eafc3595319

Recovered identifier tables:
- Strings: 12,302
- Types: 2,555
- Prototypes: 3,490
- Fields: 13,775
- Methods: 15,752

Code recovery:
- Methods with code_item: 11,104
- Methods fully walked: 11,104
- Instruction rows: 196,173
- Per-class semantic instruction files: 1,540
- Branch/payload target operands: 26,881
- Decoded references: 46,287 method, 38,166 field, 4,545 string, 9,345 type
- Operand decode issues: 0

Outputs include per-class instruction listings with register operands and resolved string/type/field/method references, plus complete identifier index tables and quality summaries. Original code units are retained for audit.

Limitations: this is a custom DEX operand decoder, not a validated baksmali/JADX export. Invoke-custom/polymorphic call-site details, packed/sparse switch case mapping, debug metadata and exception-handler reconstruction still require mature disassembler validation. It is not Java source.

Safety: static analysis only. No APK, shell script, or native library execution; no vehicle state writes, license bypass, root/Xposed injection, or factory reset behavior.
