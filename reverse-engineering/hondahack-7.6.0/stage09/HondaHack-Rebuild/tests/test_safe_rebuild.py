#!/usr/bin/env python3
from pathlib import Path
import sys, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]; APP=ROOT/"app"; A="{http://schemas.android.com/apk/res/android}"
def fail(s): print("FAIL:",s); sys.exit(1)
try: m=ET.parse(APP/"src/main/AndroidManifest.xml").getroot()
except Exception as e: fail("manifest XML: "+str(e))
a=m.find("application")
if a is None: fail("missing application")
launch=[]
for x in a.findall("activity"):
 if x.attrib.get(A+"exported")=="true":
  for f in x.findall("intent-filter"):
   if "android.intent.action.MAIN" in [z.attrib.get(A+"name") for z in f.findall("action")] and "android.intent.category.LAUNCHER" in [z.attrib.get(A+"name") for z in f.findall("category")]: launch.append(x)
if len(launch)!=1: fail("expected one launcher activity")
perms=[x.attrib.get(A+"name") for x in m.findall("uses-permission")]
if perms: fail("unexpected permissions: "+str(perms))
src=APP/"src/main/java/cn/autohack/hondahack"
required=["MainActivity.java","RemapKeysActivity.java","DeviceInfoActivity.java","AppCatalogActivity.java","NavigationActivity.java","MediaActivity.java","VoiceAssistantActivity.java","DiagnosticsActivity.java","SettingsActivity.java","MyApplication.java","MyService.java","MyBroadcastReceiver.java"]
for f in required:
 if not (src/f).is_file(): fail("missing "+f)
code="\n".join(p.read_text(encoding="utf-8") for p in src.glob("*.java"))
for x in ["Runtime.getRuntime().exec","new ProcessBuilder","su -c","Settings.Global.put","Settings.Secure.put","RECORD_AUDIO"]:
 if x in code: fail("unsafe code pattern: "+x)
for x in ["getLaunchIntentForPackage","ACTION_APPLICATION_DETAILS_SETTINGS","TextWatcher","onKeyDown","keymap_","PreferenceManager.getDefaultSharedPreferences(this)","Build.VERSION.RELEASE","show_device_summary","show_app_count","ACTION_OPEN_DOCUMENT","ACTION_GET_CONTENT","geo:0,0?q=","ACTION_SEND","TextToSpeech","new Locale(\"zh\",\"CN\")"]:
 if x not in code: fail("missing feature or compatibility path: "+x)
g=(APP/"build.gradle").read_text(encoding="utf-8")
if "JavaVersion.VERSION_1_8" not in g: fail("Java compatibility constant incorrect")
if "applicationId 'cn.autohack.hondahack.rebuild'" not in g: fail("rebuild must use distinct application ID")
print("PASS: manifest, permissions, modules, app catalog search/details, shared prefs, public intents and safe APIs")
