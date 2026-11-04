package cn.autohack.hondahack;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Preferences for the reconstructed app only; these do not modify vehicle settings. */
public final class SettingsActivity extends Activity {
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("设置");
        title.setTextSize(23);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText("以下选项只影响本应用的显示，不会更改车机或系统配置。");
        note.setPadding(0, dp(8), 0, dp(12));
        root.addView(note);

        addPreference(root, "show_device_summary", "在首页显示设备摘要", true);
        addPreference(root, "show_app_count", "在应用管理页显示应用数量", true);
        addPreference(root, "keep_screen_on", "本应用使用期间保持屏幕常亮", false);
    }

    private void addPreference(LinearLayout root, final String key, String label, boolean fallback) {
        CheckBox box = new CheckBox(this);
        box.setText(label);
        box.setChecked(prefs.getBoolean(key, fallback));
        box.setOnCheckedChangeListener((button, checked) -> {
            prefs.edit().putBoolean(key, checked).apply();
            if ("keep_screen_on".equals(key)) {
                if (checked) getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
        root.addView(box, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
