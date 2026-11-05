package com.honda.carmonitor;
import android.app.*; import android.os.*; import android.view.*; import android.widget.*; import android.graphics.*; import android.util.*; import java.io.*; import java.util.*;
public class MainActivity extends Activity {
 LinearLayout list; int fg=Color.WHITE; int muted=Color.rgb(170,185,200);
 public void onCreate(Bundle b){super.onCreate(b); getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
 LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(22,16,22,16); root.setBackgroundColor(Color.rgb(13,20,31));
 TextView title=new TextView(this); title.setText("HONDA CAR MONITOR  |  本田车机监控"); title.setTextColor(fg); title.setTextSize(23); title.setTypeface(null,1); root.addView(title);
 TextView intro=new TextView(this); intro.setText("第一阶段：系统版本与 ROOT 权限检测（本机只读）"); intro.setTextColor(muted); intro.setTextSize(14); root.addView(intro);
 Button refresh=new Button(this); refresh.setText("重新检测"); root.addView(refresh);
 ScrollView sc=new ScrollView(this); list=new LinearLayout(this); list.setOrientation(1); sc.addView(list); root.addView(sc,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
 refresh.setOnClickListener(v->detect()); detect(); }
 void section(String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.rgb(60,185,255));t.setTextSize(18);t.setTypeface(null,1);t.setPadding(0,18,0,6);list.addView(t);}
 void item(String k,String v){LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(12,8,12,8);box.setBackgroundColor(Color.rgb(27,38,53));TextView a=new TextView(this);a.setText(k);a.setTextColor(muted);a.setTextSize(13);TextView z=new TextView(this);z.setText(v);z.setTextColor(fg);z.setTextSize(17);box.addView(a);box.addView(z);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=6;list.addView(box,p);}
 String s(String x){return x==null||x.length()==0?"未知":x;}
 String rootCheck(){boolean clue=new File("/system/bin/su").exists()||new File("/system/xbin/su").exists()||new File("/sbin/su").exists()||(Build.TAGS!=null&&Build.TAGS.contains("test-keys"));
 try{Process p=Runtime.getRuntime().exec(new String[]{"su","-c","id"}); final StringBuilder out=new StringBuilder(); BufferedReader r=new BufferedReader(new InputStreamReader(p.getInputStream())); Thread t=new Thread(()->{try{String l;while((l=r.readLine())!=null)synchronized(out){out.append(l);}}catch(Exception e){}});t.setDaemon(true);t.start();t.join(3000);if(t.isAlive()){p.destroy();return "未能确认：Root 请求超时"+(clue?"（发现 Root 线索）":"");}p.waitFor();String v=out.toString();if(v.contains("uid=0"))return "已获取 Root 权限（su 验证 uid=0）";if(v.length()>0)return "未获取 Root 权限（su 返回："+v+"）";return "未能确认 Root"+(clue?"（发现 Root 相关线索）":"");}catch(Exception e){return clue?"发现 Root 相关文件/标记，但未验证到授权权限":"未检测到可用 su，Root 未确认";}}
 void detect(){list.removeAllViews();section("ROOT 权限状态");String rs=rootCheck();item("检测结果",rs);item("检测方式","执行 su -c id 验证 UID。若出现 Root 授权弹窗，请允许；拒绝或超时不会误报为已 Root。");
 section("Android 系统版本");item("Android 版本",s(Build.VERSION.RELEASE));item("API Level",Build.VERSION.SDK_INT+"");item("安全补丁",Build.VERSION.SDK_INT>=23?s(Build.VERSION.SECURITY_PATCH):"系统版本不提供");item("系统构建版本",s(Build.DISPLAY));item("Build 指纹",s(Build.FINGERPRINT));item("Build 标签",s(Build.TAGS));
 section("车机硬件信息");item("设备型号",s(Build.MODEL));item("品牌 / 制造商",s(Build.BRAND)+" / "+s(Build.MANUFACTURER));item("设备代号",s(Build.DEVICE));item("主板",s(Build.BOARD));item("硬件平台",s(Build.HARDWARE));item("产品",s(Build.PRODUCT));
 DisplayMetrics d=new DisplayMetrics();getWindowManager().getDefaultDisplay().getRealMetrics(d);section("屏幕信息");item("分辨率",d.widthPixels+" × "+d.heightPixels);item("密度",d.densityDpi+" dpi");
 ActivityManager.MemoryInfo m=new ActivityManager.MemoryInfo();((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(m);section("内存信息");item("RAM 总量",gb(m.totalMem));item("RAM 可用",gb(m.availMem));
 try{StatFs f=new StatFs(Environment.getDataDirectory().getPath());long total=(long)f.getBlockCount()*f.getBlockSize(),free=(long)f.getAvailableBlocks()*f.getBlockSize();section("存储信息");item("内部存储总量",gb(total));item("可用空间",gb(free));}catch(Exception e){item("存储信息","读取失败");}
 section("说明");item("本阶段范围","只检测车机 Android 系统和硬件，不需要 OBD2。不会读取车辆 ECU 数据、不会联网上传、不会修改系统。");}
 String gb(long n){return String.format(Locale.US,"%.2f GB",n/1073741824.0);}
}