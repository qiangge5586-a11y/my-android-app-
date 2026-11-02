package com.diplay.hotspotcheck;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView output;
    private Button runButton, copyButton, saveButton, shareButton;
    private String latestReport = "";

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

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        copyButton = new Button(this); copyButton.setText("复制报告");
        saveButton = new Button(this); saveButton.setText("保存 TXT");
        shareButton = new Button(this); shareButton.setText("分享报告");
        actions.addView(copyButton, new LinearLayout.LayoutParams(0, -2, 1));
        actions.addView(saveButton, new LinearLayout.LayoutParams(0, -2, 1));
        actions.addView(shareButton, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(actions);

        ScrollView scroll = new ScrollView(this);
        output = new TextView(this);
        output.setTextSize(13);
        output.setTypeface(Typeface.MONOSPACE);
        output.setPadding(4, 14, 4, 20);
        scroll.addView(output);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        setReportActionsEnabled(false);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                runButton.setEnabled(false);
                setReportActionsEnabled(false);
                output.setText("正在进行只读检测…\n");
                new Thread(new Runnable() {
                    @Override public void run() {
                        final String report = performCheck();
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                latestReport = report;
                                output.setText(report);
                                runButton.setEnabled(true);
                                setReportActionsEnabled(true);
                            }
                        });
                    }
                }).start();
            }
        });
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("DiPlay热点检测报告", latestReport));
                Toast.makeText(MainActivity.this, "报告已复制，返回聊天窗口粘贴发送即可", Toast.LENGTH_LONG).show();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { saveReport(); }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "DiPlay车机热点兼容性检测报告");
                intent.putExtra(Intent.EXTRA_TEXT, latestReport);
                startActivity(Intent.createChooser(intent, "分享检测报告"));
            }
        });
        output.setText("等待检测。\n\n提示：AP 能力需要驱动明确报告支持，单纯能连接 Wi-Fi 不代表能开热点。");
    }

    private void setReportActionsEnabled(boolean enabled) {
        copyButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        shareButton.setEnabled(enabled);
    }

    private void saveReport() {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) throw new Exception("无法访问应用专属下载目录");
            if (!dir.exists() && !dir.mkdirs()) throw new Exception("无法创建下载目录");
            File file = new File(dir, "DiPlay_Hotspot_Report.txt");
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write(latestReport);
            writer.close();
            Toast.makeText(this, "报告已保存：" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败：" + e.getMessage() + "，请使用复制或分享功能", Toast.LENGTH_LONG).show();
        }
    }

    private String performCheck() {
        StringBuilder r = new StringBuilder();
        r.append("DiPlay 独立热点兼容性报告\n");
        r.append("Android: ").append(Build.VERSION.RELEASE).append(" / API ").append(Build.VERSION.SDK_INT).append("\n");
        r.append("型号: ").append(Build.MODEL).append("\n板级: ").append(Build.BOARD).append("\n硬件: ").append(Build.HARDWARE).append("\n固件: ").append(Build.DISPLAY).append("\n");
        r.append("内核: ").append(readFile("/proc/version")).append("\n\n");
        r.append("\n===== Android 4.2 legacy SoftAP API preflight =====\n");
        r.append(LegacyHotspotProbe.inspect(this)).append("\n");
        String shell = "echo __ROOT__; id; " +
            "echo __NET__; ls /sys/class/net; " +
            "echo __WLAN0_PRESENT__; if [ -d /sys/class/net/wlan0 ]; then echo WLAN0_PRESENT=yes; else echo WLAN0_PRESENT=no; fi; " +
            "echo __WLAN__; ls -ld /sys/class/net/wlan0 /sys/class/net/wlan0/phy80211 /sys/class/net/wlan0/wireless 2>&1; " +
            "echo __DRIVER_LINK__; ls -l /sys/class/net/wlan0/device/driver /sys/class/net/wlan0/device/driver/module 2>&1; " +
            "echo __WIRELESS__; cat /proc/net/wireless 2>&1; " +
            "echo __WIFI_PROPERTIES__; getprop wifi.interface; getprop wifi.driver.status; getprop wlan.driver.status; getprop wifi.driver; " +
            "echo __SELINUX__; if command -v getenforce >/dev/null 2>&1; then getenforce; else cat /sys/fs/selinux/enforce 2>&1; fi; " +
            "echo __PHY_DRIVER__; readlink -f /sys/class/net/wlan0/phy80211 2>&1; readlink -f /sys/class/net/wlan0/device/driver 2>&1; " +
            "echo __RFKILL__; for f in /sys/class/rfkill/rfkill*/type /sys/class/rfkill/rfkill*/state; do if [ -r \"$f\" ]; then echo $f=$(cat \"$f\" 2>/dev/null); fi; done; " +
            "echo __BINS__; for x in hostapd dnsmasq iw; do for d in /system/bin /system/xbin /vendor/bin /vendor/xbin /sbin /bin; do if [ -x $d/$x ]; then echo $x=$d/$x; break; fi; done; done; " +
            "echo __HOSTAPD_VERSION__; H=; for d in /system/bin /system/xbin /vendor/bin /vendor/xbin /sbin /bin; do if [ -x $d/hostapd ]; then H=$d/hostapd; break; fi; done; if [ -n \"$H\" ]; then $H -v 2>&1; fi; " +
            "echo __IW__; IW=; for d in /system/bin /system/xbin /vendor/bin /vendor/xbin /sbin /bin; do if [ -x $d/iw ]; then IW=$d/iw; break; fi; done; if [ -n \"$IW\" ]; then echo IW_PATH=$IW; $IW dev 2>&1; $IW list 2>&1; $IW dev wlan0 info 2>&1; $IW dev wlan0 station dump 2>&1; else echo IW_NOT_FOUND; fi; " +
            "echo __WIFI_STATE__; settings get global wifi_on 2>&1; getprop init.svc.wpa_supplicant; getprop init.svc.hostapd; getprop init.svc.dnsmasq; " +
            "echo __DAEMON_PROCESS_TABLE__; ps 2>&1 | grep -E '[h]ostapd|[d]nsmasq' 2>&1; echo __DAEMON_PROCESS_TABLE_A__; ps -A 2>&1 | grep -E '[h]ostapd|[d]nsmasq' 2>&1; " +
            "echo __HOTSPOT_FILES__; for f in /data/misc/wifi/hostapd.conf /data/misc/wifi/hostapd.pid /data/misc/wifi/dnsmasq.pid /data/misc/wifi/dnsmasq.leases /data/misc/wifi/softap.conf /data/misc/wifi/wpa_supplicant.conf; do if [ -e \"$f\" ]; then ls -ld \"$f\"; else echo MISSING:$f; fi; done; " +
            "echo __PID_FILES__; for f in /data/misc/wifi/hostapd.pid /data/misc/wifi/dnsmasq.pid; do if [ -r \"$f\" ]; then echo $f=$(cat \"$f\" 2>/dev/null); fi; done; " +
            "echo __MODULES__; cat /proc/modules 2>&1; " +
            "echo __WIFI_KERNEL_MODULES__; for f in /sys/module/wlcore /sys/module/wl18xx /sys/module/cfg80211 /sys/module/mac80211; do if [ -d \"$f\" ]; then echo MODULE_PRESENT:$f; fi; done; " +
            "echo __WIFI_CONFIG_LABELS__; ls -ldZ /data/misc/wifi /data/misc/wifi/hostapd.conf /system/bin/hostapd /system/bin/dnsmasq 2>&1";
        String result = runSu(shell);
        r.append(result);
        r.append("\n\n===== 进阶只读观察 =====\n");
        r.append("注意：ps、iw station dump 和配置文件检查均为只读；不输出配置文件内容或 Wi-Fi 密码。\n");
        r.append("若系统没有 iw/ps -A，相关错误信息属于工具兼容性线索，不代表硬件不支持 AP。\n");
        r.append("\n\n===== 初步判断 =====\n");
        boolean root = result.contains("uid=0");
        boolean ap = result.matches("(?s).*\\*\\s+AP(?:\\s|\\r|\\n).*");
        boolean iwList = result.contains("Supported interface modes");
        boolean interfaceCombos = result.toLowerCase(java.util.Locale.US).contains("valid interface combinations");
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
        final StringBuilder out = new StringBuilder();
        Process p = null;
        Thread reader = null;
        try {
            p = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
            final Process process = p;
            reader = new Thread(new Runnable() {
                @Override public void run() {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = br.readLine()) != null) {
                            synchronized (out) { out.append(line).append('\n'); }
                        }
                    } catch (Exception ignored) {
                    } finally {
                        if (br != null) try { br.close(); } catch (Exception ignored) {}
                    }
                }
            }, "hotspot-report-reader");
            reader.setDaemon(true);
            reader.start();

            // Do not use readLine() on the UI worker as a timeout: readLine
            // blocks until data/EOF and can otherwise hang forever on old su.
            long deadline = System.currentTimeMillis() + 20000;
            Integer exitCode = null;
            while (System.currentTimeMillis() < deadline) {
                try {
                    exitCode = p.exitValue();
                    break;
                } catch (IllegalThreadStateException stillRunning) {
                    try { Thread.sleep(50); } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw interrupted;
                    }
                }
            }
            if (exitCode == null) {
                p.destroy();
                try { p.getInputStream().close(); } catch (Exception ignored) {}
                out.append("[su timeout after 20s]\n");
            } else {
                reader.join(1000);
                out.append("[su exit=").append(exitCode).append("]\n");
            }
        } catch (Exception e) {
            out.append("Root 检测失败：").append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append('\n');
        } finally {
            if (p != null) p.destroy();
            if (reader != null) {
                try { reader.join(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
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