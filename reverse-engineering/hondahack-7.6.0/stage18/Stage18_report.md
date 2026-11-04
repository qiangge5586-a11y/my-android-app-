# HondaHack 7.6.0 — Stage 18: ARM32 native JNI disassembly

- ELF shared libraries disassembled: 3.
- JNI/JNI_OnLoad symbol blocks: 15.
- Instruction rows in those symbol blocks: 3,162.
- Direct call sites in JNI export blocks: 336.
- Indirect call sites in JNI export blocks: 67.
- Branch instructions in JNI export blocks: 597.

Outputs: per-library ARM32 assembly listings (*.arm32.asm), jni_disassembly_index.tsv, library_disassembly_summary.tsv, stage18_summary.json, and build_stage18.py (included in the downloadable stage archive).

Limitations: disassembly is instruction-level evidence, not decompiled C/C++ source. Indirect JNI table calls and stripped/internal helper functions may prevent reliable semantic reconstruction. Function names do not establish exact runtime behavior.

Safety: static disassembly only. No ELF code, APK, embedded script or vehicle interaction was executed. No root/Shizuku/Xposed injection, vehicle/HMI writes, authorization bypass, or destructive system operations are implemented.
