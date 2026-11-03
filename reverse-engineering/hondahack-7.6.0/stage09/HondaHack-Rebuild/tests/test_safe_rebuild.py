#!/usr/bin/env python3
"""Static safety and completeness checks for the HondaHack safe rebuild."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app"
ANDROID = "{http://schemas.android.com/apk/res/android}"

def fail(message):
    print("FAIL:", message)
    sys.exit(1)

manifest_path = APP / "src/main/AndroidManifest.xml"
try:
    manifest = ET.parse(manifest_path).getroot()
except Exception as exc:
    fail(f"manifest is not valid XML: {exc}")

application = manifest.find("application")
if application is None:
    fail("manifest has no application element")
activities = application.findall("activity")
launcher = []
for activity in activities:
    exported = activity.attrib.get(ANDROID + "exported")
    if exported == "true":
        for filt in activity.findall("intent-filter"):
            actions = [x.attrib.get(ANDROID + "name") for x in filt.findall("action")]
            cats = [x.attrib.get(ANDROID + "name") for x in filt.findall("category")]
            if "android.intent.action.MAIN" in actions and "android.intent.category.LAUNCHER" in cats:
                launcher.append(activity.attrib.get(ANDROID + "name"))
if len(launcher) != 1:
    fail("expected exactly one exported launcher activity")

permissions = [node.attrib.get(ANDROID + "name") for node in manifest.findall("uses-permission")]
if permissions:
    fail("safe rebuild must not request Android permissions: " + ", ".join(permissions))

java_root = APP / "src/main/java/cn/autohack/hondahack"
required = ["MainActivity.java", "DeviceInfoActivity.java", "AppCatalogActivity.java",
            "SettingsActivity.java", "MyApplication.java", "MyService.java",
            "MyBroadcastReceiver.java"]
for filename in required:
    if not (java_root / filename).is_file():
        fail("missing Java source: " + filename)

sources = "\n".join(p.read_text(encoding="utf-8") for p in java_root.glob("*.java"))
for forbidden in ["Runtime.getRuntime().exec", "new ProcessBuilder", "su -c",
                  "Settings.Global.put", "Settings.Secure.put"]:
    if forbidden in sources:
        fail("forbidden privileged execution pattern found: " + forbidden)

for marker in ["getLaunchIntentForPackage", "Build.VERSION.RELEASE",
               "show_device_summary", "show_app_count"]:
    if marker not in sources:
        fail("expected safe feature not found: " + marker)

print("PASS: manifest XML, launcher export, no declared permissions, required screens,")
print("      safe local preferences, public app-launch API, and privileged-execution guard.")
