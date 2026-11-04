package cn.autohack.hondahack;
import android.app.Activity;import android.content.Intent;import android.content.SharedPreferences;import android.content.pm.PackageManager;import android.content.pm.ResolveInfo;import android.net.Uri;import android.os.Bundle;import android.preference.PreferenceManager;import android.provider.Settings;import android.text.Editable;import android.text.TextWatcher;import android.view.View;import android.widget.*;import java.text.Collator;import java.util.*;
/** Lists launcher-visible apps, supports local filtering, and opens Android's app-info screen. */
public final class AppCatalogActivity extends Activity {
 private LinearLayout list;private List<ResolveInfo> apps;private PackageManager pm;private TextView count;
 @Override protected void onCreate(Bundle b){super.onCreate(b);pm=getPackageManager();ScrollView scroll=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(1);int p=dp(16);list.setPadding(p,p,p,p);scroll.addView(list);setContentView(scroll);loadApps();}
 private void loadApps(){list.removeAllViews();TextView title=new TextView(this);title.setText("应用管理 / 可启动应用");title.setTextSize(22);list.addView(title);
 EditText search=new EditText(this);search.setSingleLine(true);search.setHint("搜索应用名称或包名");list.addView(search);
 Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);apps=pm.queryIntentActivities(q,0);Collections.sort(apps,(a,b)->Collator.getInstance(Locale.getDefault()).compare(a.loadLabel(pm).toString(),b.loadLabel(pm).toString()));
 SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);count=new TextView(this);count.setPadding(0,dp(8),0,dp(12));if(prefs.getBoolean("show_app_count",true))list.addView(count);
 final LinearLayout rows=new LinearLayout(this);rows.setOrientation(1);list.addView(rows);renderRows(rows,"");
 search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int c){renderRows(rows,s.toString());}public void afterTextChanged(Editable e){}});
 }
 private void renderRows(LinearLayout rows,String query){rows.removeAllViews();String needle=query.toLowerCase(Locale.ROOT);int visible=0;
 for(final ResolveInfo info:apps){final String label=info.loadLabel(pm).toString();final String pkg=info.activityInfo.packageName;if(!label.toLowerCase(Locale.ROOT).contains(needle)&&!pkg.toLowerCase(Locale.ROOT).contains(needle))continue;visible++;
 TextView row=new TextView(this);row.setText(label+"\n"+pkg+"\n长按查看系统应用信息");row.setTextSize(15);row.setPadding(dp(10),dp(12),dp(10),dp(12));row.setBackgroundColor(0xfff1f1f1);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(6);rows.addView(row,lp);
 row.setOnClickListener(new View.OnClickListener(){public void onClick(View v){Intent launch=pm.getLaunchIntentForPackage(pkg);if(launch!=null){try{startActivity(launch);}catch(RuntimeException e){Toast.makeText(AppCatalogActivity.this,"无法启动该应用",Toast.LENGTH_SHORT).show();}}}});
 row.setOnLongClickListener(new View.OnLongClickListener(){public boolean onLongClick(View v){Intent i=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",pkg,null));try{startActivity(i);}catch(RuntimeException e){Toast.makeText(AppCatalogActivity.this,"无法打开系统应用信息",Toast.LENGTH_SHORT).show();}return true;}});
 }
 count.setText("显示 "+visible+" / "+apps.size()+" 个可启动入口");
 }
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}