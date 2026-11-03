package cn.autohack.hondahack;
import android.app.Activity;import android.content.Intent;import android.net.Uri;import android.os.Bundle;import android.view.View;import android.widget.*;
public final class MediaActivity extends Activity {
 private static final int REQUEST_VIDEO=6401;
 @Override protected void onCreate(Bundle b){super.onCreate(b);LinearLayout r=new LinearLayout(this);r.setOrientation(1);int p=dp(18);r.setPadding(p,p,p,p);
 TextView t=new TextView(this);t.setText("本地视频");t.setTextSize(22);r.addView(t);
 TextView h=new TextView(this);h.setText("使用系统文件选择器选择视频，再交给设备上已安装的播放器打开。");h.setPadding(0,dp(10),0,dp(12));r.addView(h);
 Button pick=new Button(this);pick.setText("选择视频");pick.setOnClickListener(new View.OnClickListener(){public void onClick(View v){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("video/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,REQUEST_VIDEO);}});r.addView(pick);
 TextView n=new TextView(this);n.setText("\n不会扫描整个存储空间，也不会复制或上传所选文件。");r.addView(n);setContentView(r);}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=REQUEST_VIDEO||result!=RESULT_OK||data==null)return;Uri u=data.getData();if(u==null)return;Intent v=new Intent(Intent.ACTION_VIEW);v.setDataAndType(u,"video/*");v.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{startActivity(Intent.createChooser(v,"选择视频播放器"));}catch(RuntimeException x){Toast.makeText(this,"没有可播放该视频的应用",1).show();}}
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}