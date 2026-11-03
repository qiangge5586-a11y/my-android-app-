package cn.autohack.hondahack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Rebuilt launcher shell. Original fragment implementation remains obfuscated. */
public final class MainActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title = new TextView(this);
        title.setText("HondaHack — safe reconstruction");
        title.setTextSize(22);
        root.addView(title);
        TextView notice = new TextView(this);
        notice.setText("Recovered UI structure only. Vehicle controls, privileged operations and Xposed hooks are disabled.");
        notice.setPadding(0, 24, 0, 24);
        root.addView(notice);
        Button settings = new Button(this);
        settings.setText("Settings / 设置");
        settings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        root.addView(settings);
        setContentView(root);
    }
}
