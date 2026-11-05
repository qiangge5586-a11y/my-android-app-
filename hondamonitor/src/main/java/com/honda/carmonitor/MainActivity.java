package com.honda.carmonitor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.Manifest;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout list;
    private final int fg = Color.WHITE;
    private final int muted = Color.rgb(170, 185, 200);
    private TextView cpuLive;
    private long previousIdle = -1;
    private long previousTotal = -1;
    private static final int REQUEST_WRITE_STORAGE = 7302;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 16, 22, 16);
        root.setBackgroundColor(Color.rgb(13, 20, 31));

        TextView title = new TextView(this);
        title.setText("HONDA CAR MONITOR  |  本田车机监控");
        title.setTextColor(fg);
        title.setTextSize(23);
        title.setTypeface(null, 1);
        root.addView(title);

        TextView intro = new TextView(this);
        intro.setText("车机系统检测 · CPU 监控 · 兼容性报告（本机只读）");
        intro.setTextColor(muted);
        intro.setTextSize(14);
        root.addView(intro);

        Button refresh = new Button(this);
        refresh.setText("重新检测全部项目");
        root.addView(refresh);
        LinearLayout exportBar = new LinearLayout(this);
        exportBar.setOrientation(LinearLayout.HORIZONTAL);
        Button saveReport = new Button(this);
        saveReport.setText("导出 TXT 报告");
        Button shareReport = new Button(this);
        shareReport.setText("分享检测报告");
        Button viewReports = new Button(this);
        viewReports.setText("查看已保存报告");
        Button usbReport = new Button(this);
        usbReport.setText("保存报告到 U 盘");
        exportBar.addView(saveReport, new LinearLayout.LayoutParams(0, -2, 1));
        exportBar.addView(shareReport, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(exportBar);
        root.addView(viewReports);
        root.addView(usbReport);
        saveReport.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { saveReportFile(); }
        });
        shareReport.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { shareReport(); }
        });
        viewReports.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { showSavedReports(); }
        });
        usbReport.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { saveReportToUsb(); }
        });

        ScrollView scroller = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroller.addView(list);
        root.addView(scroller, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { detectAll(); }
        });
        detectAll();
    }

    private void section(String value) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextColor(Color.rgb(60, 185, 255));
        label.setTextSize(18);
        label.setTypeface(null, 1);
        label.setPadding(0, 18, 0, 6);
        list.addView(label);
    }

    private void item(String key, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(12, 8, 12, 8);
        card.setBackgroundColor(Color.rgb(27, 38, 53));
        TextView k = new TextView(this);
        k.setText(key);
        k.setTextColor(muted);
        k.setTextSize(13);
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(fg);
        v.setTextSize(16);
        card.addView(k);
        card.addView(v);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = 6;
        list.addView(card, params);
    }

    private String safe(String value) {
        return value == null || value.length() == 0 ? "未知" : value;
    }

    private String rootCheck() {
        boolean clue = new File("/system/bin/su").exists()
                || new File("/system/xbin/su").exists()
                || new File("/sbin/su").exists()
                || (Build.TAGS != null && Build.TAGS.contains("test-keys"));
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
            final StringBuilder output = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Thread readerThread = new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            synchronized (output) { output.append(line); }
                        }
                    } catch (Exception ignored) { }
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();
            readerThread.join(2500);
            if (readerThread.isAlive()) {
                process.destroy();
                return "未能确认：Root 请求超时" + (clue ? "（发现 Root 线索）" : "");
            }
            process.waitFor();
            String result = output.toString();
            if (result.contains("uid=0")) return "已获取 Root（su 验证 uid=0）";
            if (result.length() > 0) return "未获取 Root（su 返回：" + result + "）";
            return "未能确认 Root" + (clue ? "（发现 Root 相关线索）" : "");
        } catch (Exception error) {
            return clue ? "发现 Root 相关文件/标记，但未验证到授权权限" : "未检测到可用 su，Root 未确认";
        }
    }

    private String readFirstLine(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String value = reader.readLine();
            reader.close();
            return value == null ? "" : value.trim();
        } catch (Exception ignored) { return ""; }
    }

    private String cpuModel() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/cpuinfo"));
            String line;
            while ((line = reader.readLine()) != null) {
                String lower = line.toLowerCase(Locale.US);
                if (lower.startsWith("model name") || lower.startsWith("hardware")
                        || lower.startsWith("processor") || lower.startsWith("cpu model")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2 && parts[1].trim().length() > 0) return parts[1].trim();
                }
            }
        } catch (Exception ignored) {
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignored) { }
        }
        return safe(Build.HARDWARE);
    }

    private int cpuCores() {
        int count = 0;
        File cpu = new File("/sys/devices/system/cpu/");
        File[] files = cpu.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().matches("cpu[0-9]+")) count++;
            }
        }
        return count > 0 ? count : Runtime.getRuntime().availableProcessors();
    }

    private String cpuFrequency() {
        String max = readFirstLine("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
        String current = readFirstLine("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
        if (max.length() == 0) max = readFirstLine("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
        return "当前 " + (current.length() == 0 ? "不可读取" : mhz(current))
                + " / 最高 " + (max.length() == 0 ? "不可读取" : mhz(max));
    }

    private String mhz(String khz) {
        try { return String.format(Locale.US, "%.0f MHz", Long.parseLong(khz) / 1000.0); }
        catch (Exception ignored) { return khz; }
    }

    private long[] cpuTimes() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
            String line = reader.readLine();
            reader.close();
            if (line == null || !line.startsWith("cpu ")) return null;
            String[] values = line.trim().split("\\s+");
            long total = 0;
            for (int i = 1; i < values.length; i++) total += Long.parseLong(values[i]);
            long idle = Long.parseLong(values[4]);
            if (values.length > 5) idle += Long.parseLong(values[5]);
            return new long[]{total, idle};
        } catch (Exception ignored) { return null; }
    }

    private void sampleCpuUsage(final TextView target) {
        target.setText("正在采样 CPU 负载…");
        new Thread(new Runnable() {
            @Override public void run() {
                final long[] first = cpuTimes();
                try { Thread.sleep(700); } catch (InterruptedException ignored) { }
                final long[] second = cpuTimes();
                final String result;
                if (first == null || second == null || second[0] <= first[0]) {
                    result = "当前系统不允许读取 /proc/stat";
                } else {
                    long total = second[0] - first[0];
                    long idle = second[1] - first[1];
                    result = String.format(Locale.US, "%.1f%%（约 0.7 秒采样）",
                            Math.max(0, Math.min(100, (total - idle) * 100.0 / total)));
                }
                runOnUiThread(new Runnable() {
                    @Override public void run() { target.setText(result); }
                });
            }
        }, "cpu-sampler").start();
    }

    private String abiList() {
        if (Build.VERSION.SDK_INT >= 21 && Build.SUPPORTED_ABIS != null) {
            StringBuilder result = new StringBuilder();
            for (String abi : Build.SUPPORTED_ABIS) {
                if (result.length() > 0) result.append(", ");
                result.append(abi);
            }
            return result.toString();
        }
        return safe(Build.CPU_ABI) + (Build.CPU_ABI2 == null ? "" : ", " + Build.CPU_ABI2);
    }

    private String gb(long bytes) {
        return String.format(Locale.US, "%.2f GB", bytes / 1073741824.0);
    }


    private String collectText(View view) {
        if (view instanceof TextView) {
            CharSequence value = ((TextView) view).getText();
            return value == null ? "" : value.toString();
        }
        StringBuilder result = new StringBuilder();
        if (view instanceof LinearLayout) {
            LinearLayout group = (LinearLayout) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                String child = collectText(group.getChildAt(i));
                if (child.length() > 0) {
                    if (result.length() > 0) result.append("\n");
                    result.append(child);
                }
            }
        }
        return result.toString();
    }

    private String createReport() {
        StringBuilder report = new StringBuilder();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        report.append("HONDA CAR MONITOR - 本田车机检测报告\n");
        report.append("报告生成时间：").append(time).append("\n");
        report.append("应用版本：0.7.0\n========================================\n");
        report.append(collectText(list));
        report.append("\n========================================\n");
        report.append("说明：本报告来自 Android 本机只读检测，结果受系统权限和厂商接口限制。");
        report.append("\n不包含发动机转速、车速、水温、故障码等 ECU 数据；读取这些数据需要兼容 OBD2 适配器或车厂授权接口。");
        report.append("\n报告由用户主动导出或分享，不会由本 APP 自动联网上传。\n");
        return report.toString();
    }

    private void saveReportFile() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
            return;
        }
        writeReportToDownloads();
    }

    private File saveReportPrivately(String fileName) throws Exception {
        File folder = new File(getFilesDir(), "reports");
        if (!folder.exists() && !folder.mkdirs()) throw new Exception("无法创建应用内报告目录");
        File reportFile = new File(folder, fileName);
        FileOutputStream stream = new FileOutputStream(reportFile);
        try {
            stream.write(createReport().getBytes("UTF-8"));
            stream.flush();
        } finally {
            stream.close();
        }
        return reportFile;
    }

    private void saveReportToUsb() {
        if (Build.VERSION.SDK_INT < 19) {
            Toast.makeText(this, "当前 Android 版本不支持自动识别 U 盘", Toast.LENGTH_LONG).show();
            return;
        }
        File[] appDirs = getExternalFilesDirs(null);
        final ArrayList<File> usbDirs = new ArrayList<File>();
        if (appDirs != null) {
            for (File dir : appDirs) {
                if (dir == null) continue;
                boolean removable = false;
                if (Build.VERSION.SDK_INT >= 21) {
                    try { removable = Environment.isExternalStorageRemovable(dir); }
                    catch (Exception ignored) { }
                }
                if (removable && dir.exists()) usbDirs.add(dir);
            }
        }
        if (usbDirs.size() == 0) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("未检测到 U 盘")
                    .setMessage("请先插入 U 盘并等待车机识别，再点击保存。部分车机不向普通 APP 开放 U 盘存储权限。")
                    .setPositiveButton("知道了", null).show();
            return;
        }
        if (usbDirs.size() == 1) {
            writeReportToUsb(usbDirs.get(0));
            return;
        }
        String[] choices = new String[usbDirs.size()];
        for (int i = 0; i < usbDirs.size(); i++) choices[i] = "可移动存储 " + (i + 1);
        new android.app.AlertDialog.Builder(this).setTitle("选择 U 盘")
                .setItems(choices, new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface dialog, int which) {
                        writeReportToUsb(usbDirs.get(which));
                    }
                }).setNegativeButton("取消", null).show();
    }

    private void writeReportToUsb(File appExternalDir) {
        String fileName = "HondaCarMonitor-检测报告-"
                + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + ".txt";
        OutputStream output = null;
        try {
            File folder = new File(appExternalDir, "HondaCarMonitor/Reports");
            if (!folder.exists() && !folder.mkdirs()) throw new Exception("无法在 U 盘创建报告目录");
            File report = new File(folder, fileName);
            output = new FileOutputStream(report);
            output.write(createReport().getBytes("UTF-8"));
            output.flush();
            Toast.makeText(this, "报告已保存到 U 盘 Android/data/com.honda.carmonitor/files/HondaCarMonitor/Reports", Toast.LENGTH_LONG).show();
            item("U 盘导出状态", "成功：" + report.getAbsolutePath());
        } catch (Exception error) {
            Toast.makeText(this, "写入 U 盘失败：" + safe(error.getMessage()), Toast.LENGTH_LONG).show();
            item("U 盘导出状态", "失败：" + safe(error.getMessage()));
        } finally {
            try { if (output != null) output.close(); } catch (Exception ignored) { }
        }
    }

    private void showSavedReports() {
        File folder = new File(getFilesDir(), "reports");
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("检测报告")
                    .setMessage("目前还没有保存的报告。请先点击“导出 TXT 报告”。")
                    .setPositiveButton("知道了", null).show();
            return;
        }
        java.util.Arrays.sort(files, new java.util.Comparator<File>() {
            @Override public int compare(File a, File b) {
                return b.lastModified() > a.lastModified() ? 1 : (b.lastModified() < a.lastModified() ? -1 : 0);
            }
        });
        final File[] reports = files;
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) names[i] = files[i].getName();
        new android.app.AlertDialog.Builder(this)
                .setTitle("已保存报告（点击查看）")
                .setItems(names, new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface dialog, int which) {
                        showReportContent(reports[which]);
                    }
                })
                .setNegativeButton("关闭", null).show();
    }

    private void showReportContent(File file) {
        try {
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) content.append(line).append("\\n");
            reader.close();
            TextView text = new TextView(this);
            text.setText(content.toString());
            text.setTextColor(fg);
            text.setTextSize(15);
            text.setPadding(20, 16, 20, 16);
            ScrollView scroll = new ScrollView(this);
            scroll.addView(text);
            new android.app.AlertDialog.Builder(this)
                    .setTitle(file.getName()).setView(scroll)
                    .setPositiveButton("关闭", null).show();
        } catch (Exception error) {
            Toast.makeText(this, "读取报告失败：" + safe(error.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void writeReportToDownloads() {
        String fileName = "HondaCarMonitor-检测报告-"
                + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + ".txt";
        OutputStream output = null;
        File privateReport = null;
        try {
            privateReport = saveReportPrivately(fileName);
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/HondaCarMonitor");
                Uri uri = getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new Exception("无法在下载目录创建文件");
                output = getContentResolver().openOutputStream(uri);
            } else {
                File downloads = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                if (!downloads.exists() && !downloads.mkdirs())
                    throw new Exception("无法创建 Download 目录");
                File folder = new File(downloads, "HondaCarMonitor");
                if (!folder.exists() && !folder.mkdirs())
                    throw new Exception("无法创建 HondaCarMonitor 目录");
                File reportFile = new File(folder, fileName);
                output = new FileOutputStream(reportFile);
            }
            if (output == null) throw new Exception("无法打开报告文件");
            output.write(createReport().getBytes("UTF-8"));
            output.flush();
            Toast.makeText(this, "报告已保存，可在 APP 内点击“查看已保存报告”查看", Toast.LENGTH_LONG).show();
            item("报告导出状态", "成功：APP 内已保存；公共目录 Download/HondaCarMonitor/" + fileName);
        } catch (Exception error) {
            if (privateReport != null) {
                Toast.makeText(this, "报告已保存在 APP 内；公共下载目录不可用，可点击“查看已保存报告”查看", Toast.LENGTH_LONG).show();
                item("报告导出状态", "APP 内保存成功；公共目录导出失败：" + safe(error.getMessage()));
            } else {
                Toast.makeText(this, "报告保存失败：" + safe(error.getMessage()), Toast.LENGTH_LONG).show();
                item("报告导出状态", "保存失败：" + safe(error.getMessage()));
            }
        } finally {
            try { if (output != null) output.close(); } catch (Exception ignored) { }
        }
    }

    private void shareReport() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "本田车机检测报告");
        intent.putExtra(Intent.EXTRA_TEXT, createReport());
        try {
            startActivity(Intent.createChooser(intent, "分享检测报告"));
        } catch (Exception error) {
            Toast.makeText(this, "车机没有可用的分享应用；请使用“导出 TXT 报告”", Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                       int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeReportToDownloads();
            } else {
                Toast.makeText(this, "未获得存储权限，无法写入公共下载目录", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void detectAll() {
        list.removeAllViews();

        section("一、ROOT 权限状态");
        item("检测结果", rootCheck());
        item("检测说明", "通过 su -c id 验证 UID。若车机出现 Root 授权弹窗，请按实际需要处理；超时或拒绝不等于已 Root。");

        section("二、Android 系统版本");
        item("Android 版本", safe(Build.VERSION.RELEASE));
        item("API Level", String.valueOf(Build.VERSION.SDK_INT));
        item("安全补丁", Build.VERSION.SDK_INT >= 23 ? safe(Build.VERSION.SECURITY_PATCH) : "系统版本不提供");
        item("系统构建", safe(Build.DISPLAY));
        item("Build 指纹", safe(Build.FINGERPRINT));
        item("Build 标签", safe(Build.TAGS));

        section("三、CPU 信息与实时负载");
        item("CPU 型号 / 硬件", cpuModel());
        item("CPU 核心数", String.valueOf(cpuCores()) + " 核");
        item("CPU ABI 架构", abiList());
        item("CPU 频率", cpuFrequency());
        cpuLive = new TextView(this);
        cpuLive.setTextColor(fg);
        cpuLive.setTextSize(17);
        cpuLive.setPadding(12, 10, 12, 10);
        cpuLive.setBackgroundColor(Color.rgb(27, 38, 53));
        list.addView(cpuLive);
        sampleCpuUsage(cpuLive);

        section("四、车机硬件信息");
        item("设备型号", safe(Build.MODEL));
        item("品牌 / 制造商", safe(Build.BRAND) + " / " + safe(Build.MANUFACTURER));
        item("设备代号", safe(Build.DEVICE));
        item("主板", safe(Build.BOARD));
        item("硬件平台", safe(Build.HARDWARE));
        item("产品代号", safe(Build.PRODUCT));

        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        display.getRealMetrics(metrics);
        section("五、屏幕与内存");
        item("屏幕分辨率", metrics.widthPixels + " × " + metrics.heightPixels);
        item("屏幕密度", metrics.densityDpi + " dpi");
        ActivityManager.MemoryInfo memory = new ActivityManager.MemoryInfo();
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(memory);
        item("RAM 总量 / 可用", gb(memory.totalMem) + " / " + gb(memory.availMem));

        long totalStorage = 0;
        long freeStorage = 0;
        try {
            StatFs stats = new StatFs(Environment.getDataDirectory().getPath());
            totalStorage = (long) stats.getBlockCount() * stats.getBlockSize();
            freeStorage = (long) stats.getAvailableBlocks() * stats.getBlockSize();
        } catch (Exception ignored) { }
        item("内部存储总量 / 可用", gb(totalStorage) + " / " + gb(freeStorage));

        section("六、硬件与系统兼容性报告");
        List<String> checks = new ArrayList<String>();
        int passed = 0;
        int warnings = 0;
        if (Build.VERSION.SDK_INT >= 17) { checks.add("通过：Android API 17+，满足本 APP 最低系统要求"); passed++; }
        else { checks.add("不通过：Android 版本低于 API 17"); warnings++; }
        if (Build.VERSION.SDK_INT >= 21 && Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0) {
            checks.add("通过：系统可报告 CPU ABI（" + abiList() + "）"); passed++;
        } else {
            checks.add("提示：旧版 Android，使用传统 CPU ABI 字段"); passed++;
        }
        if (metrics.widthPixels >= metrics.heightPixels) { checks.add("通过：当前屏幕为横向布局"); passed++; }
        else { checks.add("注意：屏幕当前为竖向，车机横屏显示效果需实机确认"); warnings++; }
        if (memory.totalMem >= 1024L * 1024L * 1024L) { checks.add("通过：RAM 至少 1 GB"); passed++; }
        else { checks.add("注意：RAM 少于 1 GB，运行大型应用可能受限"); warnings++; }
        if (freeStorage >= 512L * 1024L * 1024L) { checks.add("通过：内部可用空间至少 512 MB"); passed++; }
        else { checks.add("注意：内部可用空间不足 512 MB 或无法读取"); warnings++; }
        PackageManager pm = getPackageManager();
        checks.add((pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) ? "通过" : "提示")
                + "：触摸屏 " + (pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) ? "可用" : "未报告"));
        checks.add((pm.hasSystemFeature(PackageManager.FEATURE_WIFI) ? "通过" : "提示")
                + "：Wi-Fi " + (pm.hasSystemFeature(PackageManager.FEATURE_WIFI) ? "可用" : "未报告"));
        for (int i = 0; i < checks.size(); i++) item("检测项 " + (i + 1), checks.get(i));
        String verdict = warnings == 0 ? "基础兼容性良好（仍需实机安装验证）"
                : "发现 " + warnings + " 项注意事项（不代表 APP 一定无法安装）";
        item("兼容性结论", verdict);
        item("通过项", passed + " 项；注意项 " + warnings + " 项");

        section("七、检测范围说明");
        item("本 APP 能读取", "Android 系统版本、CPU、内存、存储、屏幕、设备标识和部分系统能力。");
        item("本 APP 暂不能读取", "发动机转速、车速、水温、故障码等 ECU 数据；这些需要兼容的 OBD2 适配器或车厂授权车辆接口。");
        item("隐私与安全", "本地只读检测，不联网、不上传、不修改系统分区，不执行车辆控制操作。");
    }
}
