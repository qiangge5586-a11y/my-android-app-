# Stage 34 — Probable Activity-to-layout links

## Results
- `setContentView(int)` invocation sites: **7**
- Sites with a nearby resource constant mapped to a layout: **5**
- Unresolved sites: **2**

## Outputs
- `probable_activity_layout_links.tsv`: candidate method-to-layout links
- `unresolved_setcontentview_sites.tsv`: sites without a confident nearby mapping

## Limitations
The nearest preceding resource constant is a heuristic, not register-aware dataflow. Constants may be unrelated; inherited framework implementations and dynamic layout selection are not resolved. Treat these as candidates only. No APK was built.
