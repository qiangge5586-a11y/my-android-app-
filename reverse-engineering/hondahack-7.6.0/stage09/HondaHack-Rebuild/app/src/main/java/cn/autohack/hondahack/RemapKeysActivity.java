package cn.autohack.hondahack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceManager;

/**
 * Safe foreground-only key mapping preview.
 * Android/OEM firmware may consume steering-wheel keys before they reach this Activity.
 */
public final class RemapKeysActivity extends Activity {
    private static final String[] LABELS = {
            "打开导航页面", "打开本地视频页面", "打开语音助手", "打开应用管理"
    };
    private static final String[] ACTIONS = {
            "navigation", "media", "voice", "apps"
    };

    private SharedPreferences prefs;
    private TextView status;
    private Spinner actionPicker;
    private boolean capturing;
    private int pendingKey = KeyEvent.KEYCODE_UNKNOWN;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("按键映射");
        title.setTextSize(23);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText("安全预览：只记录本页面处于前台时，Android 实际发送给本应用的按键事件。方向盘按键可能被原厂车机或系统服务拦截，本功能不能保证接收。");
        note.setPadding(0, dp(8), 0, dp(14));
        root.addView(note);

        status = new TextView(this);
        status.setText("尚未捕获按键");
        status.setTextIsSelectable(true);
        root.addView(status);

        Button capture = new Button(this);
        capture.setText("开始捕获下一个按键");
        capture.setOnClickListener(v -> {
            capturing = true;
            pendingKey = KeyEvent.KEYCODE_UNKNOWN;
            status.setText("请按下一个按键…");
            root.setFocusableInTouchMode(true);
            root.requestFocus();
        });
        root.addView(capture);

        actionPicker = new Spinner(this);
        actionPicker.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, LABELS));
        root.addView(actionPicker);

        Button save = new Button(this);
        save.setText("保存本地映射");
        save.setOnClickListener(v -> saveMapping());
        root.addView(save);

        Button clear = new Button(this);
        clear.setText("清除当前按键映射");
        clear.setOnClickListener(v -> clearMapping());
        root.addView(clear);

        Button test = new Button(this);
        test.setText("测试当前映射");
        test.setOnClickListener(v -> {
            if (pendingKey == KeyEvent.KEYCODE_UNKNOWN) {
                Toast.makeText(this, "请先捕获一个按键", Toast.LENGTH_SHORT).show();
            } else {
                dispatch(ACTIONS[actionPicker.getSelectedItemPosition()]);
            }
        });
        root.addView(test);

        TextView saved = new TextView(this);
        saved.setText("\n映射保存在本应用 SharedPreferences 中，不会修改方向盘固件、系统按键表或车机分区。");
        root.addView(saved);
        setContentView(root);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (capturing) {
            capturing = false;
            pendingKey = keyCode;
            status.setText("已捕获：" + KeyEvent.keyCodeToString(keyCode)
                    + "（" + keyCode + "）");
            return true;
        }
        String action = prefs.getString("keymap_" + keyCode, "");
        if ("navigation".equals(action) || "media".equals(action)
                || "voice".equals(action) || "apps".equals(action)) {
            dispatch(action);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void saveMapping() {
        if (pendingKey == KeyEvent.KEYCODE_UNKNOWN) {
            Toast.makeText(this, "请先捕获一个按键", Toast.LENGTH_SHORT).show();
            return;
        }
        int index = actionPicker.getSelectedItemPosition();
        prefs.edit().putString("keymap_" + pendingKey, ACTIONS[index]).apply();
        Toast.makeText(this, "已保存到本应用", Toast.LENGTH_SHORT).show();
    }

    private void clearMapping() {
        if (pendingKey == KeyEvent.KEYCODE_UNKNOWN) {
            Toast.makeText(this, "请先捕获一个按键", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit().remove("keymap_" + pendingKey).apply();
        Toast.makeText(this, "已清除本地映射", Toast.LENGTH_SHORT).show();
    }

    private void dispatch(String action) {
        Class<?> target;
        if ("navigation".equals(action)) target = NavigationActivity.class;
        else if ("media".equals(action)) target = MediaActivity.class;
        else if ("voice".equals(action)) target = VoiceAssistantActivity.class;
        else if ("apps".equals(action)) target = AppCatalogActivity.class;
        else return;
        startActivity(new Intent(this, target));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
