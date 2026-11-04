package cn.autohack.hondahack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Displays and exports only app-owned screen navigation events. */
public final class LogActivity extends Activity {
    private TextView content;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("本地操作日志");
        title.setTextSize(22);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText("仅记录本应用打开过的页面，最多保留 100 条。不会读取 Android 全局日志、位置、输入内容或车辆数据。");
        note.setPadding(0, dp(8), 0, dp(8));
        root.addView(note);

        content = new TextView(this);
        content.setTextIsSelectable(true);
        root.addView(content);

        Button refresh = new Button(this);
        refresh.setText("刷新");
        refresh.setOnClickListener(v -> refresh());
        root.addView(refresh);

        Button share = new Button(this);
        share.setText("分享本地日志");
        share.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "HondaHack Rebuild local operation log");
            i.putExtra(Intent.EXTRA_TEXT, LocalEventLog.exportText(this));
            startActivity(Intent.createChooser(i, "分享本地日志"));
        });
        root.addView(share);

        Button clear = new Button(this);
        clear.setText("清除本地日志");
        clear.setOnClickListener(v -> {
            LocalEventLog.clear(this);
            refresh();
            Toast.makeText(this, "本地日志已清除", Toast.LENGTH_SHORT).show();
        });
        root.addView(clear);
        setContentView(root);
        refresh();
    }

    private void refresh() {
        content.setText(LocalEventLog.exportText(this));
    }

    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + .5f);
    }
}
