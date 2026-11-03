package cn.autohack.hondahack;
import android.app.Activity;import android.content.Intent;import android.net.Uri;import android.os.Bundle;import android.view.View;import android.widget.*;
public final class NavigationActivity extends Activity {
 @Override protected void onCreate(Bundle b){super.onCreate(b);LinearLayout r=new LinearLayout(this);r.setOrientation(1);int p=dp(18);r.setPadding(p,p,p,p);
 TextView t=new TextView(this);t.setText("导航 / Navigation");t.setTextSize(22);r.addView(t);
 TextView h=new TextView(this);h.setText("输入地点名称或地址，交由设备上已安装的地图应用处理。");h.setPadding(0,dp(10),0,dp(10));r.addView(h);
 final EditText e=new EditText(this);e.setSingleLine(true);e.setHint("例如：最近的加油站");r.addView(e);
 Button go=new Button(this);go.setText("在地图中搜索");go.setOnClickListener(new View.OnClickListener(){public void onClick(View v){String q=e.getText().toString().trim();if(q.isEmpty()){Toast.makeText(NavigationActivity.this,"请先输入目的地",0).show();return;}Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q="+Uri.encode(q)));try{startActivity(Intent.createChooser(i,"选择地图应用"));}catch(RuntimeException x){Toast.makeText(NavigationActivity.this,"没有可处理地图请求的应用",1).show();}}});r.addView(go);
 TextView s=new TextView(this);s.setText("\n只调用 Android 标准地图 Intent，不读取车辆定位或 CAN 数据。");r.addView(s);setContentView(r);}
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}