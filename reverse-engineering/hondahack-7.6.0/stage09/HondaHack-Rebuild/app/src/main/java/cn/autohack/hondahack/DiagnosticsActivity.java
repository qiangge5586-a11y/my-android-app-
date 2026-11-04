package cn.autohack.hondahack;
import android.app.Activity;import android.content.Intent;import android.content.pm.PackageInfo;import android.content.pm.PackageManager;import android.os.Build;import android.os.Bundle;import android.os.Environment;import android.os.StatFs;import android.view.View;import android.widget.*;
import java.util.Locale;
/** Builds a user-reviewed diagnostic summary from non-sensitive local device metadata. */
public final class DiagnosticsActivity extends Activity {
 private String summary;
 @Override protected void onCreate(Bundle b){super.onCreate(b);LinearLayout r=new LinearLayout(this);r.setOrientation(1);int p=dp(18);r.setPadding(p,p,p,p);
 TextView t=new TextView(this);t.setText("诊断信息");t.setTextSize(22);r.addView(t);
 summary=buildSummary();TextView body=new TextView(this);body.setText(summary);body.setTextIsSelectable(true);body.setPadding(0,dp(12),0,dp(12));r.addView(body);
 Button share=new Button(this);share.setText("分享诊断摘要");share.setOnClickListener(new View.OnClickListener(){public void onClick(View v){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"HondaHack Rebuild diagnostic summary");i.putExtra(Intent.EXTRA_TEXT,summary);startActivity(Intent.createChooser(i,"分享诊断摘要"));}});r.addView(share);
 TextView note=new TextView(this);note.setText("\n仅包含设备型号、Android 版本、应用版本和存储概况；不会自动上传，也不会收集联系人、位置、车辆数据或文件内容。");r.addView(note);setContentView(r);}
 private String buildSummary(){String version="unknown";try{PackageInfo p=getPackageManager().getPackageInfo(getPackageName(),0);version=p.versionName;}catch(PackageManager.NameNotFoundException ignored){}
 String storage="unavailable";try{StatFs s=new StatFs(Environment.getDataDirectory().getPath());long total=s.getBlockCount()*(long)s.getBlockSize(),free=s.getAvailableBlocks()*(long)s.getBlockSize();storage=String.format(Locale.getDefault(),"total %.2f GB, available %.2f GB",total/1073741824.0,free/1073741824.0);}catch(RuntimeException ignored){}
 return "HondaHack Rebuild diagnostic summary\nApp version: "+version+"\nManufacturer: "+Build.MANUFACTURER+"\nModel: "+Build.MODEL+"\nAndroid: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")\nBuild ID: "+Build.ID+"\nStorage: "+storage;}
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}