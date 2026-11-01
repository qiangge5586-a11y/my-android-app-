package com.diplay.hotspotcheck;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView output;
    private Button runButton;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 18, 18, 18);
        TextView title = new TextView(this);
        title.setText("车机底层 / 热点兼容性检测");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);
        TextView intro = new TextView(this);
        intro.setText("只读取系统信息，不会开启/关闭 Wi-Fi，不会重启网卡，不会启动热点进程。点击检测后如有 Root 授权请求，请允许。");
        intro.setTextSize(14);
        intro.setPadding(0, 10, 0, 14);
        root.addView(intro);
        runButton = new Button(this);
        runButton.setText("开始检测");
        root.addView(runButton);
        ScrollView scroll = new ScrollView(this);
        output = new TextView(this);
        output.setTextSize(13);
        output.setTypeface(Typeface.MONOSPACE);
        output.setPadding(4, 14, 4, 20);
        scroll.addView(output);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                runButton.setEnabled(false);
                output.setText("正在进行只读检测…\n");
                new Thread(new Runnable() {
                    @Override public void run() {
                        final String report = performCheck();
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                output.setText(report);
                                runButton.setEnabled(true);
                            }
                        });
                    }
                }).start();
            }
        });
        output.setText("等待检测。\n\n提示：AP 能力需要驱动明确报告支持，单纯能连接 Wi-Fi 不代表能开热点。");
    }

    private String performCheck() {
        StringBuilder r = new StringBuilder();
        r.append("DiPlay 独立热点兼容性报告\n");
        r.append("Android: ").append(Build.VERSION.RELEASE).append(" / API ").append(Build.VERSION.SDK_INT).append("\n");
        r.append("型号: ").append(Build.MODEL).append("\n板级: ").append(Build.BOARD).append("\n硬件: ").append(Build.HARDWARE).append("\n固件: ").append(Build.DISPLAY).append("\n");
        r.append("内核: ").append(readFile("/proc/version")).append("\n\n");
        String shell = "echo __ROOT__; id; " +
            "echo __NET__; ls /sys/class/net; " +
            "echo __WLAN0_PRESENT__; if [ -d /sys/class/net/wlan0 ]; then echo WLAN0_PRESENT=yes; else echo WLAN0_PRESENT=no; fi; " +
            "echo __WLAN__; ls -ld /sys/class/net/wlan0 /sys/class/net/wlan0/phy80211 /sys/class/net/wlan0/wireless 2>&1; " +
            "echo __WIRELESS__; cat /proc/net/wireless 2>&1; " +
            "echo __BINS__; for x in hostapd dnsmasq iw; do for d in /system/bin /system/xbin /vendor/bin /sbin /bin; do if [ -x $d/$x ]; then echo $x=$d/$x; break; fi; done; done; " +
            "echo __IW__; IW=; for d in /system/bin /system/xbin /vendor/bin /vendor/xbin /sbin /bin; do if [ -x $d/iw ]; then IW=$d/iw; break; fi; done; if [ -n \"$IW\" ]; then echo IW_PATH=$IW; $IW list 2>&1; else echo IW_NOT_FOUND; fi; " +
            "echo __MODULES__; cat /proc/modules 2>&1";
        String result = runSu(shell);
        r.append(result);
        r.append("\n\n===== 初步判断 =====\n");
        String low = result.toLowerCase(Locale.US);
        boolean root = result.contains("uid=0");
        boolean ap = result.matches("(?s).*\\*\\s+AP(?:\\s|\\r|\\n).*");
        boolean iwList = result.contains("Supported interface modes");
        boolean wlan = result.contains("WLAN0_PRESENT=yes");
        boolean hostapd = result.contains("hostapd=/");
        boolean dnsmasq = result.contains("dnsmasq=/");
        if (!root) r.append("❌ Root 未确认：su 未返回 uid=0。\n");
        else r.append("✓ Root 可用。\n");
        if (ap) r.append("✓ 驱动报告支持标准 AP 模式（强证据，仍需真实启动验证）。\n");
        else if (iwList) r.append("⚠ iw 返回能力列表，但未发现 AP 模式；当前驱动可能不支持标准 AP。\n");
        else r.append("？无法读取驱动 AP 能力；可能缺少 iw 或厂商使用私有接口。\n");
        r.append(wlan ? "✓ 检测到 wlan0（不代表可切换 AP）。\n" : "⚠ 未明确检测到 wlan0。\n");
        r.append(hostapd ? "✓ 找到 hostapd。\n" : "⚠ 未找到常见路径下的 hostapd。\n");
        r.append(dnsmasq ? "✓ 找到 dnsmasq。\n" : "⚠ 未找到常见路径下的 dnsmasq。\n");
        if (Build.VERSION.SDK_INT <= 17) r.append("⚠ Android 4.2/API17：需使用旧系统兼容的 hostapd/工具链，不能直接套用新 Android 命令。\n");
        r.append("\n结论：").append(ap && root && wlan && hostapd && dnsmasq ? "具备进一步实机验证条件" : "需要驱动/工具适配或补充检测，暂不能确认可开热点").append("\n");
        r.append("\n安全说明：本工具没有执行 svc wifi、ifconfig、ip link、kill 或启动 hostapd/dnsmasq。\n");
        return r.toString();
    }

    private String runSu(String command) {
        StringBuilder out = new StringBuilder();
        Process p = null;
        try {
            p = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            long until = System.currentTimeMillis() + 20000;
            while (System.currentTimeMillis() < until && (line = br.readLine()) != null) out.append(line).append('\n');
            out.append("[su exit=").append(p.waitFor()).append("]\n");
        } catch (Exception e) {
            out.append("Root 检测失败：").append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append('\n');
        } finally {
            if (p != null) p.destroy();
        }
        return out.toString();
    }

    private String readFile(String path) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(path)));
            String s = br.readLine(); br.close(); return s == null ? "unknown" : s;
        } catch (Exception e) { return "unknown"; }
    }
}
