package com.autohack.enhance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Standalone prototype for HondaHack 7.7.0's enhancement page.
 * Uses framework APIs available on the Android 4.2 / API 17 baseline.
 *
 * Integration into the original APK still requires a DEX-aware merge,
 * manifest registration and an explicit entry point in MainActivity.
 */
public class EnhancementActivity extends Activity {
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16), dp(12), dp(16), dp(20));
        page.setBackgroundColor(0xfff2f3f5);

        TextView heading = new TextView(this);
        heading.setText("增强功能中心");
        heading.setTextSize(24);
        heading.setTextColor(0xff20242a);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setPadding(dp(4), dp(8), dp(4), dp(16));
        page.addView(heading, new LinearLayout.LayoutParams(-1, -2));

        addSection(page, "设备信息", "查看车机 Android 版本、型号、ABI 和系统构建信息",
                new View.OnClickListener() {
                    public void onClick(View v) { showDeviceInfo(); }
                });

        addSection(page, "网络诊断", "只读检查当前网络连接和 Wi-Fi 状态",
                new View.OnClickListener() {
                    public void onClick(View v) { showNetworkInfo(); }
                });

        addSection(page, "存储信息", "查看共享存储空间的总量和可用空间",
                new View.OnClickListener() {
                    public void onClick(View v) { showStorageInfo(); }
                });

        addSection(page, "应用快捷入口", "浏览系统中可启动的应用，不修改或卸载应用",
                new View.OnClickListener() {
                    public void onClick(View v) { showLaunchableApps(); }
                });

        addSection(page, "Wi-Fi 设置", "打开 Android 系统网络设置页面",
                new View.OnClickListener() {
                    public void onClick(View v) { openSettings(Settings.ACTION_WIFI_SETTINGS); }
                });

        addSection(page, "文件选择", "通过系统文件选择器选择用户可访问的文件",
                new View.OnClickListener() {
                    public void onClick(View v) { openFilePicker(); }
                });

        TextView note = new TextView(this);
        note.setText("安全说明：本页面不执行 Root 命令、不修改系统分区、不控制车辆设备，也不会上传诊断数据。");
        note.setTextSize(12);
        note.setTextColor(0xff626a73);
        note.setPadding(dp(6), dp(14), dp(6), dp(8));
        page.addView(note, new LinearLayout.LayoutParams(-1, -2));

        scroll.addView(page);
        setContentView(scroll);
    }

    private void addSection(LinearLayout parent, String title, String description,
                            View.OnClickListener listener) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(10), dp(14), dp(10));
        card.setBackgroundColor(0xffffffff);

        TextView name = new TextView(this);
        name.setText(title);
        name.setTextSize(18);
        name.setTextColor(0xff20242a);
        card.addView(name, new LinearLayout.LayoutParams(-1, -2));

        TextView detail = new TextView(this);
        detail.setText(description);
        detail.setTextSize(13);
        detail.setTextColor(0xff68717c);
        detail.setPadding(0, dp(4), 0, dp(8));
        card.addView(detail, new LinearLayout.LayoutParams(-1, -2));

        Button action = new Button(this);
        action.setText("打开");
        action.setOnClickListener(listener);
        card.addView(action, new LinearLayout.LayoutParams(-2, -2));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(10);
        parent.addView(card, params);
    }

    private void showDeviceInfo() {
        StringBuilder text = new StringBuilder();
        text.append("制造商：").append(Build.MANUFACTURER).append('\n');
        text.append("型号：").append(Build.MODEL).append('\n');
        text.append("Android：").append(Build.VERSION.RELEASE).append('\n');
        text.append("API：").append(Build.VERSION.SDK_INT).append('\n');
        text.append("产品：").append(Build.PRODUCT).append('\n');
        text.append("设备：").append(Build.DEVICE).append('\n');
        text.append("ABI：").append(Build.CPU_ABI);
        if (Build.VERSION.SDK_INT >= 21) {
            text.append('\n').append("支持 ABI：");
            String[] abis = Build.SUPPORTED_ABIS;
            for (int i = 0; i < abis.length; i++) {
                if (i > 0) text.append(", ");
                text.append(abis[i]);
            }
        }
        showMessage("设备信息", text.toString());
    }

    private void showNetworkInfo() {
        String text = "网络状态未知";
        try {
            ConnectivityManager manager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo active = manager == null ? null : manager.getActiveNetworkInfo();
            if (active == null) {
                text = "当前没有活动网络连接";
            } else {
                text = "网络类型：" + active.getTypeName()
                        + "\n连接状态：" + active.getState()
                        + "\n是否已连接：" + active.isConnected();
            }
        } catch (Exception e) {
            text = "读取网络状态失败：" + e.getClass().getSimpleName();
        }
        showMessage("网络诊断", text);
    }

    @SuppressWarnings("deprecation")
    private void showStorageInfo() {
        File root = Environment.getExternalStorageDirectory();
        String text;
        try {
            StatFs stat = new StatFs(root.getAbsolutePath());
            long blockSize = stat.getBlockSize();
            long total = (long) stat.getBlockCount() * blockSize;
            long available = (long) stat.getAvailableBlocks() * blockSize;
            text = "存储路径：" + root.getAbsolutePath()
                    + "\n总空间：" + formatBytes(total)
                    + "\n可用空间：" + formatBytes(available);
        } catch (Exception e) {
            text = "读取存储信息失败：" + e.getClass().getSimpleName();
        }
        showMessage("存储信息", text);
    }

    private String formatBytes(long bytes) {
        return String.format(java.util.Locale.US, "%.2f GB", bytes / 1073741824.0);
    }

    private void showLaunchableApps() {
        final PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> apps = pm.queryIntentActivities(query, 0);
        if (apps == null || apps.size() == 0) {
            showMessage("应用快捷入口", "没有找到可启动的应用");
            return;
        }

        final String[] labels = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) {
            CharSequence label = apps.get(i).loadLabel(pm);
            labels[i] = label == null ? apps.get(i).activityInfo.packageName : label.toString();
        }

        new AlertDialog.Builder(this)
                .setTitle("选择要启动的应用")
                .setItems(labels, (dialog, which) -> {
                    ResolveInfo info = apps.get(which);
                    Intent launch = pm.getLaunchIntentForPackage(info.activityInfo.packageName);
                    if (launch != null) {
                        try {
                            startActivity(launch);
                        } catch (Exception e) {
                            showMessage("启动失败", e.getClass().getSimpleName());
                        }
                    }
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void openSettings(String action) {
        try {
            startActivity(new Intent(action));
        } catch (Exception e) {
            showMessage("无法打开设置", e.getClass().getSimpleName());
        }
    }

    private void openFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 17);
        } catch (Exception e) {
            showMessage("文件选择器不可用", e.getClass().getSimpleName());
        }
    }

    private void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }
}
