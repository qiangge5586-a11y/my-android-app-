package cn.autohack.hondahack;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

/** Read-only device and head-unit compatibility information. */
public final class DeviceInfoActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        root.setPadding(pad, pad, pad, pad);

        add(root, "设备信息 / 兼容性检查", 23);
        add(root, "制造商：" + Build.MANUFACTURER, 16);
        add(root, "型号：" + Build.MODEL, 16);
        add(root, "设备代号：" + Build.DEVICE, 16);
        add(root, "产品：" + Build.PRODUCT, 16);
        add(root, "Android：" + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")", 16);
        add(root, "Build ID：" + Build.ID, 16);
        add(root, "硬件平台：" + Build.HARDWARE, 16);
        add(root, "主板：" + Build.BOARD, 16);
        add(root, "CPU ABI：" + Build.CPU_ABI, 16);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        add(root, "屏幕分辨率：" + metrics.widthPixels + " × " + metrics.heightPixels, 16);
        add(root, "屏幕密度：" + metrics.densityDpi + " dpi (density " +
                String.format(Locale.getDefault(), "%.2f", metrics.density) + ")", 16);

        Intent installer = new Intent(Intent.ACTION_VIEW);
        installer.setType("application/vnd.android.package-archive");
        boolean hasInstaller = getPackageManager().resolveActivity(installer,
                PackageManager.MATCH_DEFAULT_ONLY) != null;
        add(root, "APK 安装器：" + (hasInstaller ? "检测到可处理 APK 的应用" : "未检测到可处理 APK 的应用"), 16);
        add(root, "最低 SDK 检查：" + (Build.VERSION.SDK_INT >= 17
                ? "满足本重建版 minSdk 17" : "低于本重建版 minSdk 17"), 16);

        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long total = stat.getBlockCount() * (long) stat.getBlockSize();
            long free = stat.getAvailableBlocks() * (long) stat.getBlockSize();
            add(root, String.format(Locale.getDefault(), "内部存储：总计 %.2f GB，可用 %.2f GB",
                    total / 1073741824.0, free / 1073741824.0), 16);
        } catch (RuntimeException ignored) {
            add(root, "内部存储：暂时无法读取", 16);
        }

        TextView note = new TextView(this);
        note.setText("\n说明：此页只读取 Android 公共 Build/Display/Storage 信息。部分车机的 MCU 版本需要在厂商设置页查看；屏幕触控或 CarPlay 功能本身不能证明设备支持安装 APK。");
        note.setPadding(0, dp(12), 0, 0);
        root.addView(note);
        setContentView(root);
    }

    private void add(LinearLayout root, String text, int size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setPadding(0, dp(7), 0, dp(7));
        root.addView(view);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
