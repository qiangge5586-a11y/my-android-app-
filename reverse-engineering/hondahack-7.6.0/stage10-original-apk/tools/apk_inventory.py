#!/usr/bin/env python3
"""Read-only inventory for the original HondaHack APK. Python stdlib only."""
from __future__ import annotations
import argparse, hashlib, json, os, struct, zipfile
from pathlib import Path

def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()

def u32(data: bytes, off: int) -> int:
    return struct.unpack_from("<I", data, off)[0]

def uleb(data: bytes, pos: int):
    value = shift = 0
    while True:
        b = data[pos]
        pos += 1
        value |= (b & 0x7f) << shift
        if b < 0x80:
            return value, pos
        shift += 7

def dex_inventory(data: bytes):
    if data[:4] != b"dex\n":
        raise ValueError("classes.dex has an unexpected header")
    string_count, string_off = u32(data, 56), u32(data, 60)
    type_count, type_off = u32(data, 64), u32(data, 68)
    method_count = u32(data, 88)
    class_count, class_off = u32(data, 96), u32(data, 100)
    strings = []
    for i in range(string_count):
        off = u32(data, string_off + 4 * i)
        _, p = uleb(data, off)
        end = data.index(b"\0", p)
        strings.append(data[p:end].decode("utf-8", "replace"))
    types = [strings[u32(data, type_off + 4 * i)] for i in range(type_count)]
    classes = [types[u32(data, class_off + 32 * i)] for i in range(class_count)]
    return {
        "version": data[4:7].decode("ascii", "replace"),
        "file_size": u32(data, 32),
        "sha256": sha256(data),
        "string_ids": string_count,
        "type_ids": type_count,
        "method_ids": method_count,
        "class_defs": class_count,
        "classes": classes,
        "app_classes": [x for x in classes if x.startswith("Lcn/autohack/hondahack/")],
    }

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("apk", type=Path)
    ap.add_argument("--output", type=Path, default=Path("apk_inventory.json"))
    args = ap.parse_args()
    raw = args.apk.read_bytes()
    with zipfile.ZipFile(args.apk) as z:
        infos = z.infolist()
        names = [x.filename for x in infos]
        dex = dex_inventory(z.read("classes.dex"))
        native = {}
        for info in infos:
            if info.filename.endswith(".so"):
                blob = z.read(info.filename)
                native[info.filename] = {"size": len(blob), "sha256": sha256(blob)}
        result = {
            "file": args.apk.name,
            "size": len(raw),
            "sha256": sha256(raw),
            "zip_entries": len(infos),
            "uncompressed_bytes": sum(x.file_size for x in infos),
            "dex": {k: v for k, v in dex.items() if k not in ("classes", "app_classes")},
            "class_descriptors": len(dex["classes"]),
            "app_namespace_classes": len(dex["app_classes"]),
            "app_classes": dex["app_classes"],
            "resource_entries": sum(n.startswith("res/") for n in names),
            "assets": [n for n in names if n.startswith("assets/")],
            "native_abis": sorted({n.split("/")[1] for n in names if n.endswith(".so")}),
            "native_libraries": native,
        }
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({k: v for k, v in result.items() if k != "app_classes"}, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    main()
