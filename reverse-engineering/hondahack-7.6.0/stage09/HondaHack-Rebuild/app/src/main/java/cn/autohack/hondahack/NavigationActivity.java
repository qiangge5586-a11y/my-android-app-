package cn.autohack.hondahack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public final class NavigationActivity extends Activity {
    private AudioManager audio;
    private AudioManager.OnAudioFocusChangeListener focusListener;
    private boolean focusHeld;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        focusListener = change -> {
            if (change == AudioManager.AUDIOFOCUS_LOSS
                    || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                focusHeld = false;
            }
        };
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(1);
        int p = dp(18);
        r.setPadding(p, p, p, p);

        TextView t = new TextView(this);
        t.setText("导航 / Navigation");
        t.setTextSize(22);
        r.addView(t);
        TextView h = new TextView(this);
        h.setText("输入地点名称或地址，交由设备上已安装的地图应用处理。");
        h.setPadding(0, dp(10), 0, dp(10));
        r.addView(h);
        final EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint("例如：最近的加油站");
        r.addView(e);
        Button go = new Button(this);
        go.setText("在地图中搜索");
        go.setOnClickListener(v -> {
            String q = e.getText().toString().trim();
            if (q.isEmpty()) {
                Toast.makeText(this, "请先输入目的地", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(q)));
            try {
                startActivity(Intent.createChooser(i, "选择地图应用"));
            } catch (RuntimeException x) {
                Toast.makeText(this, "没有可处理地图请求的应用", Toast.LENGTH_LONG).show();
            }
        });
        r.addView(go);

        TextView audioTitle = new TextView(this);
        audioTitle.setText("\n导航音频焦点（手动测试）");
        audioTitle.setTextSize(18);
        r.addView(audioTitle);
        TextView audioNote = new TextView(this);
        audioNote.setText("请求短暂音频焦点，通知支持该机制的音乐应用在模拟导航播报时降低音量。实际效果取决于音乐应用和车机音频策略。");
        r.addView(audioNote);
        Button request = new Button(this);
        request.setText("请求临时音频焦点");
        request.setOnClickListener(v -> requestFocus());
        r.addView(request);
        Button release = new Button(this);
        release.setText("释放音频焦点");
        release.setOnClickListener(v -> releaseFocus());
        r.addView(release);

        TextView s = new TextView(this);
        s.setText("\n只调用 Android 标准地图 Intent 和公开 AudioManager API；不读取车辆定位或 CAN 数据，也不直接改写系统音量。");
        r.addView(s);
        setContentView(r);
    }

    private void requestFocus() {
        if (audio == null) {
            Toast.makeText(this, "当前设备不提供 AudioManager", Toast.LENGTH_SHORT).show();
            return;
        }
        int result = audio.requestAudioFocus(focusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        focusHeld = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        Toast.makeText(this, focusHeld
                ? "已请求临时音频焦点；其他应用是否降低音量由其自身决定"
                : "设备未授予音频焦点", Toast.LENGTH_LONG).show();
    }

    private void releaseFocus() {
        if (audio != null && focusHeld) {
            audio.abandonAudioFocus(focusListener);
            focusHeld = false;
        }
        Toast.makeText(this, "已释放临时音频焦点", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onDestroy() {
        releaseFocus();
        super.onDestroy();
    }

    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + .5f);
    }
}
