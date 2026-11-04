package cn.autohack.hondahack;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** User-selected APK handoff to the Android package installer. No silent install. */
public final class ApkInstallActivity extends Activity {
    private static final int REQUEST_APK = 6402;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("本地 APK 安装");
        title.setTextSize(23);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText("选择一个 APK 文件后，交给 Android 系统安装器。安装器会显示应用信息并要求你确认；本应用不会静默安装、授予权限或修改系统白名单。");
        note.setPadding(0, dp(10), 0, dp(12));
        root.addView(note);

        Button choose = new Button(this);
        choose.setText("选择 APK 文件");
        choose.setOnClickListener(v -> chooseApk());
        root.addView(choose);

        TextView caveat = new TextView(this);
        caveat.setText("\n如果设备没有 Android 软件包安装器，或系统策略禁止安装，操作将无法继续。原厂 CarPlay 显示屏不等于 Android APK 安装环境。");
        root.addView(caveat);
        setContentView(root);
    }

    private void chooseApk() {
        Intent picker;
        if (Build.VERSION.SDK_INT >= 19) {
            picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            picker.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            picker = new Intent(Intent.ACTION_GET_CONTENT);
            picker.addCategory(Intent.CATEGORY_OPENABLE);
        }
        picker.setType("application/vnd.android.package-archive");
        picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(picker, REQUEST_APK);
        } catch (RuntimeException e) {
            Toast.makeText(this, "没有可用的文件选择器", Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != REQUEST_APK || result != RESULT_OK || data == null) return;
        Uri apk = data.getData();
        if (apk == null) return;
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(apk, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(install);
        } catch (RuntimeException e) {
            Toast.makeText(this, "系统没有可用的 APK 安装器，或安装被系统策略阻止", Toast.LENGTH_LONG).show();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
