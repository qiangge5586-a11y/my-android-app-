package cn.autohack.hondahack;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Stores a bounded, app-only event history. No system logs or personal input are collected. */
public final class LocalEventLog {
    private static final String KEY = "local_event_log";
    private static final int MAX_ENTRIES = 100;

    private LocalEventLog() { }

    public static synchronized void append(Context context, String event) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        JSONArray old = readArray(prefs.getString(KEY, "[]"));
        JSONArray next = new JSONArray();
        int start = Math.max(0, old.length() - MAX_ENTRIES + 1);
        for (int i = start; i < old.length(); i++) next.put(old.opt(i));
        JSONObject item = new JSONObject();
        try {
            item.put("time", System.currentTimeMillis());
            item.put("event", event);
            next.put(item);
            prefs.edit().putString(KEY, next.toString()).apply();
        } catch (JSONException ignored) {
            // If serialization fails, leave the previous local history untouched.
        }
    }

    public static synchronized String exportText(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        JSONArray entries = readArray(prefs.getString(KEY, "[]"));
        StringBuilder out = new StringBuilder("HondaHack Rebuild local operation log\n");
        for (int i = 0; i < entries.length(); i++) {
            JSONObject item = entries.optJSONObject(i);
            if (item == null) continue;
            out.append(item.optLong("time")).append("  ")
                    .append(item.optString("event")).append('\n');
        }
        return out.toString();
    }

    public static synchronized void clear(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(KEY).apply();
    }

    private static JSONArray readArray(String raw) {
        try { return new JSONArray(raw); }
        catch (JSONException ignored) { return new JSONArray(); }
    }
}
