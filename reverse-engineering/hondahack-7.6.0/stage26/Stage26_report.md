# Stage 26 — Android resource and UI structure inventory

## Scope
Static parsing of the original APK ZIP and Android XML chunks. No APK code was executed or modified.

## Results
- Resource files under `res/`: **975**
- XML files inspected: **337**
- Parsed start elements: **2,441**
- Unique XML element names: **127**
- Layout variants: **115**
- Likely app-specific layout files (filename heuristic): **33**
- Binary XML parse issues: **0**

## Outputs
- `resource_file_inventory.tsv`: resource paths, sizes, extensions, qualifiers
- `binary_xml_inventory.tsv`: XML file hashes and element counts
- `binary_xml_elements.tsv`: parsed element names and attribute values
- `xml_tag_frequency.tsv`: element frequency
- `xml_parse_issues.tsv`: parser diagnostics

## Limitations
Both Android binary XML and plain XML are parsed structurally. This stage does not decode `resources.arsc` into a complete symbolic resource table and does not fully resolve `@0x...` references. XML parsing is not a reconstruction of original source formatting. Filename-based app-layout classification is heuristic. No Java/Smali source was generated and no APK was built.
