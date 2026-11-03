package cn.autohack.hondahack;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

/** Read-only device information screen. */
public final class DeviceInfoActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        root.setPadding(pad, pad, pad, pad);

        add(root, "设备信息", 23);
        add(root, "制造商：" + Build.MANUFACTURER, 16);
        add(root, "型号：" + Build.MODEL, 16);
        add(root, "设备代号：" + Build.DEVICE, 16);
        add(root, "产品：" + Build.PRODUCT, 16);
        add(root, "Android：" + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")", 16);
        add(root, "Build ID：" + Build.ID, 16);
        add(root, "硬件平台：" + Build.HARDWARE, 16);
        add(root, "主板：" + Build.BOARD, 16);
        add(root, "CPU ABI：" + Build.CPU_ABI, 16);

        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long total = stat.getBlockCount() * (long) stat.getBlockSize();
            long free = stat.getAvailableBlocks() * (long) stat.getBlockSize();
            add(root, String.format(Locale.getDefault(), "内部存储：总计 %.2f GB，可用 %.2f GB",
                    total / 1073741824.0, free / 1073741824.0), 16);
        } catch (RuntimeException ignored) {
            add(root, "内部存储：暂时无法读取", 16);
        }
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
