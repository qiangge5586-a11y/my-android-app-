package cn.autohack.hondahack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.*;

public final class MediaActivity extends Activity {
    private static final int REQUEST_VIDEO = 6401;
    private static final String PREF_STARTUP_VIDEO = "startup_video_uri";
    private SharedPreferences prefs;
    private Uri selectedUri;
    private TextView selection;
    private Button savePreset;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(1);
        int p = dp(18);
        r.setPadding(p, p, p, p);

        TextView t = new TextView(this);
        t.setText("本地视频 / 启动视频预设");
        t.setTextSize(22);
        r.addView(t);
        TextView h = new TextView(this);
        h.setText("选择一个视频，可立即预览，也可保存为本应用的启动视频预设。预设只在本应用中手动打开，不会在设备开机时自动播放。");
        h.setPadding(0, dp(10), 0, dp(12));
        r.addView(h);

        Button pick = new Button(this);
        pick.setText("选择视频");
        pick.setOnClickListener(v -> chooseVideo());
        r.addView(pick);

        selection = new TextView(this);
        selection.setText("尚未选择视频");
        selection.setPadding(0, dp(8), 0, dp(8));
        r.addView(selection);

        Button preview = new Button(this);
        preview.setText("预览所选视频");
        preview.setOnClickListener(v -> {
            if (selectedUri == null) Toast.makeText(this, "请先选择视频", Toast.LENGTH_SHORT).show();
            else openVideo(selectedUri);
        });
        r.addView(preview);

        savePreset = new Button(this);
        savePreset.setText("保存为本应用启动视频");
        savePreset.setOnClickListener(v -> savePreset());
        r.addView(savePreset);

        Button openSaved = new Button(this);
        openSaved.setText("打开已保存的启动视频");
        openSaved.setOnClickListener(v -> {
            String value = prefs.getString(PREF_STARTUP_VIDEO, "");
            if (value.isEmpty()) Toast.makeText(this, "尚未保存启动视频", Toast.LENGTH_SHORT).show();
            else openVideo(Uri.parse(value));
        });
        r.addView(openSaved);

        TextView n = new TextView(this);
        n.setText("\n不会扫描整个存储空间、复制或上传所选文件，也不会注册开机广播或自动启动播放。");
        r.addView(n);
        setContentView(r);
    }

    private void chooseVideo() {
        Intent i;
        if (Build.VERSION.SDK_INT >= 19) {
            i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
        }
        i.setType("video/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= 19) {
            i.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }
        try {
            startActivityForResult(i, REQUEST_VIDEO);
        } catch (RuntimeException ex) {
            Toast.makeText(this, "没有可用的文件选择器", Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != REQUEST_VIDEO || result != RESULT_OK || data == null) return;
        selectedUri = data.getData();
        if (selectedUri == null) return;
        selection.setText("已选择：" + selectedUri.getLastPathSegment());
        if (Build.VERSION.SDK_INT < 19) {
            savePreset.setEnabled(false);
            Toast.makeText(this, "Android 4.2/4.3 不支持持久化文档授权；仍可立即预览视频", Toast.LENGTH_LONG).show();
        } else {
            savePreset.setEnabled(true);
        }
    }

    private void savePreset() {
        if (selectedUri == null) return;
        if (Build.VERSION.SDK_INT < 19) {
            Toast.makeText(this, "当前 Android 版本不支持保存持久文件授权", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(selectedUri, flags);
            prefs.edit().putString(PREF_STARTUP_VIDEO, selectedUri.toString()).apply();
            Toast.makeText(this, "已保存为本应用启动视频预设", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "文件提供方未授予持久读取权限，请重新选择文件", Toast.LENGTH_LONG).show();
        }
    }

    private void openVideo(Uri uri) {
        Intent v = new Intent(Intent.ACTION_VIEW);
        v.setDataAndType(uri, "video/*");
        v.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(v, "选择视频播放器"));
        } catch (RuntimeException x) {
            Toast.makeText(this, "没有可播放该视频的应用", Toast.LENGTH_LONG).show();
        }
    }

    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + .5f);
    }
}
