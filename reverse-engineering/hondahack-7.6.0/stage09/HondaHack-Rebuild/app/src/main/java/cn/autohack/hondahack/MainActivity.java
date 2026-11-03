package cn.autohack.hondahack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Safe local dashboard. It deliberately exposes no privileged vehicle controls. */
public final class MainActivity extends Activity {
    private LinearLayout content;
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(24), dp(20), dp(24));
        scroll.addView(content);
        setContentView(scroll);
        renderDashboard();
    }

    @Override protected void onResume() {
        super.onResume();
        if (content != null) renderDashboard();
    }

    private void renderDashboard() {
        content.removeAllViews();
        TextView title = new TextView(this);
        title.setText("HondaHack");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("7.6.0 功能重建 · 安全模式");
        subtitle.setTextSize(14);
        subtitle.setPadding(0, dp(4), 0, dp(16));
        content.addView(subtitle);

        if (prefs.getBoolean("show_device_summary", true)) {
            TextView device = new TextView(this);
            device.setText("设备：" + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
                    + "\nAndroid " + android.os.Build.VERSION.RELEASE);
            device.setTextSize(16);
            device.setPadding(dp(14), dp(14), dp(14), dp(14));
            device.setBackgroundColor(0xffeeeeee);
            content.addView(device);
        }

        addAction("设备信息", "查看 Android 版本、硬件标识和存储概况", DeviceInfoActivity.class);
        addAction("应用管理", "查看并启动设备上可启动的应用", AppCatalogActivity.class);
        addAction("设置", "管理本应用的本地显示选项", SettingsActivity.class);

        TextView notice = new TextView(this);
        notice.setText("\n安全说明：当前版本只提供本地信息和应用启动功能。原 APK 中的 Root、Xposed、车辆控制、系统分区写入及授权绕过功能未移植。");
        notice.setTextSize(12);
        notice.setPadding(0, dp(18), 0, 0);
        content.addView(notice);
    }

    private void addAction(String label, String description, final Class<?> target) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundColor(0xfff2f2f2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(10);
        content.addView(card, params);

        TextView heading = new TextView(this);
        heading.setText(label);
        heading.setTextSize(18);
        card.addView(heading);
        TextView detail = new TextView(this);
        detail.setText(description);
        detail.setTextSize(13);
        card.addView(detail);
        Button open = new Button(this);
        open.setText("打开");
        open.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, target));
            }
        });
        card.addView(open);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
